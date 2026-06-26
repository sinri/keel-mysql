package io.github.sinri.keel.integration.mysql.datasource;

import io.github.sinri.keel.base.annotations.TechnicalPreview;
import io.github.sinri.keel.core.utils.ReflectionUtils;
import io.github.sinri.keel.core.utils.value.ValueBox;
import io.github.sinri.keel.integration.mysql.KeelMySQLConfiguration;
import io.github.sinri.keel.integration.mysql.Quoter;
import io.github.sinri.keel.integration.mysql.action.single.NamedActionInterface;
import io.github.sinri.keel.integration.mysql.connection.NamedMySQLConnection;
import io.github.sinri.keel.integration.mysql.exception.KeelMySQLConnectionException;
import io.github.sinri.keel.integration.mysql.exception.KeelMySQLException;
import io.github.sinri.keel.integration.mysql.result.matrix.ResultMatrix;
import io.github.sinri.keel.logger.api.LateObject;
import io.vertx.core.Closeable;
import io.vertx.core.Completable;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.mysqlclient.MySQLBuilder;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.TransactionRollbackException;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;


/**
 * 命名MySQL数据源类，将数据源与命名MySQL连接配对
 *
 * @param <C> 连接类型
 * @since 5.0.0
 */
@NullMarked
public class NamedMySQLDataSource<C extends NamedMySQLConnection> implements Closeable {

    private final Pool pool;
    private final KeelMySQLConfiguration configuration;

    /**
     * 记录当前正在使用的连接数
     */
    private final AtomicInteger borrowedConnectionCounter = new AtomicInteger(0);
    private final Function<SqlConnection, C> sqlConnectionWrapper;
    private final LateObject<String> lateFullVersion = new LateObject<>();
    private final LateObject<Quoter.EscapeContext> lateStringLiteralEscapeContext = new LateObject<>();
    /**
     * 一次性的 MySQL 会话信息初始化 Future 缓存。访问受 {@code synchronized(this)} 保护：
     * 冷启动并发建连时仅首个连接真正发起会话信息查询，其余连接复用同一 Future。
     * 查询失败或返回 {@code null} 时清除缓存以允许后续连接重试。
     *
     * @see #initializeServerSessionInfoOnce(SqlConnection)
     */
    private @Nullable Future<@Nullable ServerSessionInfo> serverSessionInfoInitFuture;

    /**
     * 检查 MySQL 会话信息。
     *
     * @param sqlConnection SQL连接
     * @return 包含会话信息的Future
     */
    private static Future<@Nullable ServerSessionInfo> checkMySQLServerSessionInfo(SqlConnection sqlConnection) {
        return sqlConnection.query("""
                                   SELECT
                                       VERSION() as v,
                                       @@session.sql_mode as sql_mode,
                                       @@session.character_set_connection as character_set_connection;
                                   """)
                            .execute()
                            .compose(rows -> {
                                return Future.succeededFuture(ResultMatrix.createSimple(rows));
                            })
                            .compose(resultMatrix -> {
                                try {
                                    var row = resultMatrix.getFirstRow();
                                    String versionExp = row.readString("v");
                                    String sqlMode = row.readString("sql_mode");
                                    String characterSetConnection = row.readString("character_set_connection");
                                    return Future.succeededFuture(new ServerSessionInfo(
                                            versionExp,
                                            sqlMode,
                                            characterSetConnection
                                    ));
                                } catch (Throwable e) {
                                    // Keel.getLogger().exception(e);
                                    return Future.succeededFuture(null);
                                }
                            });
    }

    /**
     * 构造命名MySQL数据源
     *
     * @param vertx                Vertx实例
     * @param configuration        MySQL配置
     * @param sqlConnectionWrapper SQL连接包装器
     */
    public NamedMySQLDataSource(
            Vertx vertx,
            KeelMySQLConfiguration configuration,
            Function<SqlConnection, C> sqlConnectionWrapper
    ) {
        this(vertx, configuration, sqlConnection -> Future.succeededFuture(), sqlConnectionWrapper);
    }

    /**
     * 构造命名MySQL数据源，支持自定义连接设置函数
     *
     * @param vertx                   Vertx实例
     * @param configuration           MySQL配置
     * @param connectionSetUpFunction 连接设置函数
     * @param sqlConnectionWrapper    SQL连接包装器
     */
    public NamedMySQLDataSource(
            Vertx vertx,
            KeelMySQLConfiguration configuration,
            @Nullable Function<SqlConnection, Future<Void>> connectionSetUpFunction,
            Function<SqlConnection, C> sqlConnectionWrapper
    ) {
        this.configuration = configuration;
        this.sqlConnectionWrapper = sqlConnectionWrapper;
        this.pool = MySQLBuilder.pool()
                                .with(configuration.getPoolOptions())
                                .connectingTo(configuration.getConnectOptions())
                                .using(vertx)
                                .withConnectHandler(sqlConnection -> initializeConnection(sqlConnection, connectionSetUpFunction))
                                .build();
    }

    /**
     * 返回一次性的 MySQL 会话信息初始化 Future。
     * <p>
     * 并发建连时仅首个连接真正发起会话信息查询，其余连接复用同一 Future，
     * 既消除竞态又避免重复查询。{@code checkMySQLServerSessionInfo} 是异步的，{@code synchronized}
     * 块内仅做内存读写与启动异步操作，不阻塞事件循环。
     * <p>
     * 查询失败或返回 {@code null}（解析失败）时清除缓存以保留"下次连接重试"语义；
     * 成功时将版本与字符串转义上下文写入缓存，作为同步读取来源。
     *
     * @param sqlConnection 触发查询所用的连接
     * @return 会话信息初始化 Future（结果可能为 {@code null}）
     */
    private synchronized Future<@Nullable ServerSessionInfo> initializeServerSessionInfoOnce(SqlConnection sqlConnection) {
        if (serverSessionInfoInitFuture != null) {
            return serverSessionInfoInitFuture;
        }
        serverSessionInfoInitFuture = checkMySQLServerSessionInfo(sqlConnection).andThen(ar -> {
            ServerSessionInfo result = ar.result();
            if (ar.succeeded() && result != null) {
                if (result.version() != null) {
                    lateFullVersion.set(result.version());
                }
                lateStringLiteralEscapeContext.set(result.toEscapeContext());
            } else {
                synchronized (this) {
                    serverSessionInfoInitFuture = null;
                }
            }
        });
        return serverSessionInfoInitFuture;
    }

    /**
     * 对新建立的连接进行初始化，然后将其释放到池中
     *
     * @param sqlConnection           SQL连接
     * @param connectionSetUpFunction 连接设置函数
     */
    private void initializeConnection(
            SqlConnection sqlConnection,
            @Nullable Function<SqlConnection, Future<Void>> connectionSetUpFunction
    ) {
        Future.succeededFuture()
              .compose(v -> {
                  if (connectionSetUpFunction != null) {
                      return connectionSetUpFunction.apply(sqlConnection);
                  } else {
                      return Future.succeededFuture();
                  }
              })
              .compose(v -> initializeServerSessionInfoOnce(sqlConnection))
              .compose(ignored -> Future.succeededFuture())
              .onComplete(ar -> {
                  sqlConnection.close();
              });
    }

    /**
     * 从池中获取一个 {@link SqlConnection} 并包装为 {@code C}。
     * <p>
     * 该方法仅负责获取连接，不管理借出计数；借出计数由调用方（如
     * {@link #withConnection} / {@link #executeInConnection}）负责维护。
     *
     * @return 包装后的连接 Future
     */
    private Future<C> fetchMySQLConnection() {
        return Future.succeededFuture()
                     .compose(v -> pool.getConnection())
                     .compose(
                             sqlConnection -> {
                                 C c = this.sqlConnectionWrapper.apply(sqlConnection);

                                 // add mysql version to c;
                                 if (this.lateFullVersion.isInitialized()) {
                                     c.setMysqlVersion(lateFullVersion.get());
                                 }
                                 if (this.lateStringLiteralEscapeContext.isInitialized()) {
                                     Quoter.EscapeContext escapeContext = lateStringLiteralEscapeContext.get();
                                     c.setMysqlSqlMode(escapeContext.sqlMode());
                                     c.setMysqlCharacterSetConnection(escapeContext.characterSet());
                                 }

                                 return Future.succeededFuture(c);
                             },
                             throwable -> Future.failedFuture(
                                     new KeelMySQLConnectionException(
                                             "MySQLDataSource Failed to get SqlConnection From Pool " +
                                                     "`" + this.getConfiguration().getDataSourceName() + "` " +
                                                     "(active: " + borrowedConnectionCounter.get()
                                                     + ", pool size: " + pool.size() + "): " +
                                                     throwable,
                                             throwable))
                     );
    }

    /**
     * 获取MySQL配置
     *
     * @return MySQL配置对象
     */

    public KeelMySQLConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * 获取当前池大小的近似值，委托 Vert.x {@link Pool#size()}。
     * <p>
     * 该值反映池中当前存活的连接数（含空闲与借出），由 Vert.x 内部维护，属于近似值。
     *
     * @return 池中当前连接数（近似值）
     * @since 5.0.1
     */
    public int getCurrentPoolSize() {
        return pool.size();
    }

    /**
     * 获取池中当前未使用的连接数的近似值。
     * <p>
     * 计算方式为 {@code pool.size() - borrowedCount}，并以 0 为下限保护，
     * 避免并发场景下出现瞬态负值。
     *
     * @return 空闲连接数（近似值，≥ 0）
     * @since 5.0.0
     */
    public int getCurrentIdleConnectionCount() {
        return Math.max(0, pool.size() - borrowedConnectionCounter.get());
    }

    /**
     * 获取池中初始化的连接总数。
     *
     * @return 当前池大小（近似值）
     * @deprecated 该方法原基于只增不减的累计计数器，语义不正确。请改用 {@link #getCurrentPoolSize()}。
     */
    @Deprecated
    public int getCurrentInitializedConnectionCount() {
        return pool.size();
    }

    /**
     * 获取当前正在使用的连接数，即从池中借出且尚未归还的连接。
     *
     * @return 活跃连接数
     * @since 5.0.0
     */
    public int getCurrentActiveConnectionCount() {
        return borrowedConnectionCounter.get();
    }

    /**
     * 获取MySQL完整版本信息（如 {@code "8.0.35"}）。
     * <p>
     * 版本信息在首次连接初始化时通过 {@code SELECT VERSION()} 异步获取。
     * 若查询或解析失败，版本信息将保持为 {@code null}，但不影响连接池及查询等核心功能。
     *
     * @return MySQL版本字符串，若尚未获取到则返回 {@code null}
     */
    public @Nullable String getFullVersion() {
        return lateFullVersion.get();
    }

    /**
     * 从池中借出一个连接，执行给定的异步操作，操作完成后自动归还连接。
     * <p>
     * 连接的借出与归还由本方法自动管理，无需调用方手动关闭。
     *
     * @param function 在连接上执行的异步操作
     * @param <T>      操作结果类型
     * @return 包含操作结果的 {@link ValueBox} Future
     * @since 5.0.0
     */
    public <T> Future<ValueBox<T>> executeInConnection(Function<C, Future<@Nullable T>> function) {
        return withConnection(function)
                .compose(t -> {
                    ValueBox<T> valueBox = new ValueBox<>();
                    valueBox.setValue(t);
                    return Future.succeededFuture(valueBox);
                });
    }

    /**
     * 从池中借出一个连接，执行给定的异步操作，操作完成后自动归还连接。
     * <p>
     * 连接的借出与归还由本方法自动管理，无需调用方手动关闭。
     *
     * @param function 在连接上执行的异步操作
     * @param <T>      操作结果类型
     * @return 操作结果 Future，结果可能为 null
     * @since 5.0.0
     */
    public <T> Future<@Nullable T> withConnection(Function<C, Future<@Nullable T>> function) {
        return Future.succeededFuture().compose(
                v -> fetchMySQLConnection().compose(sqlConnectionWrapper -> {
                    borrowedConnectionCounter.incrementAndGet();
                    return function.apply(sqlConnectionWrapper).andThen(tAsyncResult -> {
                        Future.succeededFuture()
                              .compose(vv -> sqlConnectionWrapper.getSqlConnection().close())
                              .andThen(ar -> {
                                  borrowedConnectionCounter.decrementAndGet();
                              });
                    }).recover(throwable -> Future.failedFuture(new KeelMySQLException(
                            "MySQLDataSource Failed Within SqlConnection: " + throwable,
                            throwable))
                    ).compose(Future::succeededFuture);
                })
        );
    }

    /**
     * 在事务中执行给定的异步操作。
     * <p>
     * 自动管理 {@code BEGIN} / {@code COMMIT} / {@code ROLLBACK} 生命周期：
     * 操作成功时提交事务，抛出异常时回滚事务。连接在事务结束后自动归还。
     *
     * @param function 在事务连接上执行的异步操作
     * @param <T>      操作结果类型
     * @return 包含操作结果的 {@link ValueBox} Future
     * @since 5.0.0
     */
    public <T> Future<ValueBox<T>> executeInTransaction(Function<C, Future<@Nullable T>> function) {
        return withTransaction(function)
                .compose(t -> {
                    ValueBox<T> valueBox = new ValueBox<>();
                    valueBox.setValue(t);
                    return Future.succeededFuture(valueBox);
                });
    }

    /**
     * 在事务中执行给定的异步操作。
     * <p>
     * 自动管理 {@code BEGIN} / {@code COMMIT} / {@code ROLLBACK} 生命周期：
     * 操作成功时提交事务，抛出异常时回滚事务。连接在事务结束后自动归还。
     *
     * @param function 在事务连接上执行的异步操作
     * @param <T>      操作结果类型
     * @return 操作结果 Future，结果可能为 null
     * @since 5.0.0
     */
    public <T> Future<@Nullable T> withTransaction(Function<C, Future<@Nullable T>> function) {
        return withConnection(c -> {
            return Future.succeededFuture()
                         .compose(v -> c.getSqlConnection().begin())
                         .compose(transaction -> {
                                     return function.apply(c).compose(t -> transaction
                                             .commit().compose(committed -> Future.succeededFuture(t))
                                     ).compose(Future::succeededFuture, err -> {
                                         if (err instanceof TransactionRollbackException) {
                                             // already rollback
                                             String error = "MySQLDataSource ROLLBACK Done Manually.";
                                             return Future.failedFuture(new KeelMySQLException(error, err));
                                         } else {
                                             String error = "MySQLDataSource ROLLBACK Finished. Core Reason: "
                                                     + err.getMessage();
                                             // rollback failure would be thrown directly to downstream.
                                             return transaction.rollback()
                                                               .compose(rollbackDone -> Future
                                                                       .failedFuture(new KeelMySQLException(error, err)));
                                         }
                                     });
                                 },
                                 beginFailure -> Future.failedFuture(new KeelMySQLConnectionException(
                                         "MySQLDataSource Failed to get SqlConnection for transaction From Pool: "
                                                 + beginFailure,
                                         beginFailure))
                         );
        });
    }

    /**
     * 关闭数据源
     *
     * @return 关闭操作Future
     */
    public Future<Void> close() {
        return this.pool.close();
    }

    /**
     * 关闭数据源并处理结果
     *
     * @param completion 异步结果处理器
     */
    @Override
    public void close(Completable<Void> completion) {
        this.pool.close().onComplete(completion);
    }

    /**
     * 在虚拟线程中以阻塞方式从池中获取一个连接。
     * <p>
     * 该方法通过 {@code Future.await()} 阻塞当前虚拟线程直到连接就绪，
     * 因此<strong>仅可在虚拟线程中调用</strong>，在事件循环线程调用会导致阻塞。
     * <p>
     * 调用方必须在使用完毕后通过 {@link #returnConnectionFromVirtualThread(NamedMySQLConnection)}
     * 归还连接，以确保连接释放和活跃计数准确。直接调用
     * {@link NamedMySQLConnection#asyncClose()} 或 {@link NamedMySQLConnection#close()}
     * 可以释放连接，但不会递减活跃计数。
     *
     * @return 命名MySQL连接
     * @throws UnsupportedOperationException 如果当前不在虚拟线程中
     * @see #returnConnectionFromVirtualThread(NamedMySQLConnection)
     * @since 5.0.0
     */
    public C fetchConnectionInVirtualThread() {
        if (!ReflectionUtils.isVirtualThreadsAvailable()) {
            throw new UnsupportedOperationException("Not in Virtual Thread!");
        }
        var sqlConnection = getPool().getConnection().await();
        borrowedConnectionCounter.incrementAndGet();
        C c = getSqlConnectionWrapper().apply(sqlConnection);
        if (this.lateFullVersion.isInitialized()) {
            c.setMysqlVersion(lateFullVersion.get());
        }
        if (this.lateStringLiteralEscapeContext.isInitialized()) {
            Quoter.EscapeContext escapeContext = lateStringLiteralEscapeContext.get();
            c.setMysqlSqlMode(escapeContext.sqlMode());
            c.setMysqlCharacterSetConnection(escapeContext.characterSet());
        }
        return c;
    }

    /**
     * 获取底层 Vert.x 连接池实例。
     *
     * @return 连接池
     */
    Pool getPool() {
        return pool;
    }

    /**
     * 获取 SQL 连接包装器函数。
     *
     * @return 将 {@link SqlConnection} 转换为 {@code C} 的函数
     */
    Function<SqlConnection, C> getSqlConnectionWrapper() {
        return sqlConnectionWrapper;
    }

    private record ServerSessionInfo(
            @Nullable String version,
            @Nullable String sqlMode,
            @Nullable String characterSetConnection
    ) {
        Quoter.EscapeContext toEscapeContext() {
            return new Quoter.EscapeContext(characterSetConnection, sqlMode);
        }
    }

    /**
     * 归还通过 {@link #fetchConnectionInVirtualThread()} 获取的连接。
     * <p>
     * 关闭底层 {@link SqlConnection} 以释放回池，并递减活跃连接计数。
     *
     * @param connection 需要归还的连接
     * @see #fetchConnectionInVirtualThread()
     * @since 5.0.1
     */
    public void returnConnectionFromVirtualThread(C connection) {
        connection.getSqlConnection().close()
                  .andThen(ar -> borrowedConnectionCounter.decrementAndGet());
    }


    /**
     * 在连接或事务中构建并执行一个命名动作
     * <p>
     * 当 {@code inTransaction} 为 {@code true} 时，整个动作在事务中执行（自动 BEGIN/COMMIT，异常时 ROLLBACK）；
     * 为 {@code false} 时仅借用连接执行，不开启事务。
     * <p>
     * 典型用法：
     * <pre>{@code
     * dataSource.withAction(true,
     *         conn -> new MyAction(conn),
     *         action -> action.doSomething(param))
     * }</pre>
     *
     * @param inTransaction  是否在事务中执行
     * @param actionBuilder  从命名连接构建动作实例的工厂函数
     * @param actionFunction 对构建好的动作执行具体操作并返回异步结果的函数
     * @param <A>            动作类型，须实现 {@link NamedActionInterface}
     * @param <T>            操作结果的类型
     * @return 包含操作结果的 {@link ValueBox} 的异步 Future
     * @since 5.0.3
     */
    @TechnicalPreview(since = "5.0.3")
    protected <A extends NamedActionInterface<C>, T> Future<ValueBox<T>> withAction(
            boolean inTransaction,
            Function<C, A> actionBuilder,
            Function<A, Future<@Nullable T>> actionFunction
    ) {
        if (inTransaction) {
            return this.executeInTransaction(c -> {
                A a = actionBuilder.apply(c);
                return actionFunction.apply(a);
            });
        } else {
            return this.executeInConnection(c -> {
                A a = actionBuilder.apply(c);
                return actionFunction.apply(a);
            });
        }
    }

    @TechnicalPreview(since = "5.0.3")
    public <A extends NamedActionInterface<C>, T> Future<ValueBox<T>> actInConnection(
            Function<C, A> actionBuilder,
            Function<A, Future<@Nullable T>> actionFunction
    ) {
        return withAction(false, actionBuilder, actionFunction);
    }

    @TechnicalPreview(since = "5.0.3")
    public <A extends NamedActionInterface<C>, T> Future<ValueBox<T>> actInTransaction(
            Function<C, A> actionBuilder,
            Function<A, Future<@Nullable T>> actionFunction
    ) {
        return withAction(true, actionBuilder, actionFunction);
    }
}
