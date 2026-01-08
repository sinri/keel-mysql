package io.github.sinri.keel.integration.mysql.datasource;

import io.github.sinri.keel.core.utils.ReflectionUtils;
import io.github.sinri.keel.integration.mysql.KeelMySQLConfiguration;
import io.github.sinri.keel.integration.mysql.connection.NamedMySQLConnection;
import io.github.sinri.keel.integration.mysql.exception.KeelMySQLConnectionException;
import io.github.sinri.keel.integration.mysql.exception.KeelMySQLException;
import io.github.sinri.keel.integration.mysql.result.matrix.ResultMatrix;
import io.vertx.core.Closeable;
import io.vertx.core.Completable;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.mysqlclient.MySQLBuilder;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.TransactionRollbackException;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
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
    private final @Nullable VirtualThreadExtension<C> virtualThreadExtension;

    /**
     * 记录初始化到池中的连接数
     */

    private final AtomicInteger initializedConnectionCounter = new AtomicInteger(0);
    /**
     * 记录当前正在使用的连接数
     */

    private final AtomicInteger borrowedConnectionCounter = new AtomicInteger(0);


    private final Function<SqlConnection, C> sqlConnectionWrapper;


    private final AtomicReference<String> fullVersionRef = new AtomicReference<>(null);

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
        if (ReflectionUtils.isVirtualThreadsAvailable()) {
            this.virtualThreadExtension = new VirtualThreadExtension<>(this);
        } else {
            this.virtualThreadExtension = null;
        }
    }

    /**
     * 检查MySQL版本
     *
     * @param sqlConnection SQL连接
     * @return 包含版本信息的Future
     */

    private static Future<@Nullable String> checkMySQLVersion(SqlConnection sqlConnection) {
        return sqlConnection.preparedQuery("SELECT VERSION() as v; ")
                            .execute()
                            .compose(rows -> Future.succeededFuture(ResultMatrix.create(rows)))
                            .compose(resultMatrix -> {
                                try {
                                    JsonObject firstRow = resultMatrix.getFirstRow();
                                    String versionExp = firstRow.getString("v");
                                    return Future.succeededFuture(versionExp);
                                } catch (Throwable e) {
                                    // Keel.getLogger().exception(e);
                                    return Future.succeededFuture(null);
                                }
                            });
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
              .compose(v -> {
                  if (fullVersionRef.get() == null) {
                      return checkMySQLVersion(sqlConnection)
                              .compose(ver -> {
                                  if (ver != null) {
                                      fullVersionRef.set(ver);
                                  }
                                  return Future.succeededFuture();
                              });
                  } else {
                      return Future.succeededFuture();
                  }
              })
              .onComplete(ar -> {
                  sqlConnection.close()
                               .onSuccess(releasedConnectionToPool -> {
                                   initializedConnectionCounter.incrementAndGet();
                               });
              });
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
     * 获取池中初始化且当前未使用的连接数
     *
     * @return 空闲连接数
     */
    public int getCurrentIdleConnectionCount() {
        return getCurrentInitializedConnectionCount() - getCurrentActiveConnectionCount();
    }

    /**
     * 获取池中初始化的连接总数
     *
     * @return 初始化连接数
     */
    public int getCurrentInitializedConnectionCount() {
        return initializedConnectionCounter.get();
    }

    /**
     * 获取当前正在使用的连接数（即从池中借出的连接）
     *
     * @return 活跃连接数
     */
    public int getCurrentActiveConnectionCount() {
        return borrowedConnectionCounter.get();
    }

    /**
     * 获取MySQL完整版本信息
     *
     * @return MySQL版本信息，可能为null
     */
    public @Nullable String getFullVersionRef() {
        return fullVersionRef.get();
    }

    /**
     * 使用连接执行操作
     *
     * @param function 连接操作函数
     * @return 操作结果Future
     */

    public <T> Future<T> withConnection(Function<C, Future<T>> function) {
        return Future.succeededFuture().compose(
                v -> fetchMySQLConnection()
                        .compose(sqlConnectionWrapper -> {
                            borrowedConnectionCounter.incrementAndGet();
                            return Future.succeededFuture()
                                         .compose(vv -> function.apply(sqlConnectionWrapper))
                                         .andThen(tAsyncResult -> Future.succeededFuture()
                                                                        .compose(vv -> sqlConnectionWrapper.getSqlConnection()
                                                                                                           .close())
                                                                        .andThen(ar -> {
                                                                            borrowedConnectionCounter.decrementAndGet();
                                                                        }))
                                         .recover(throwable -> Future.failedFuture(new KeelMySQLException(
                                                 "MySQLDataSource Failed Within SqlConnection: " + throwable,
                                                 throwable)));
                        })
        );
    }

    /**
     * 在事务中使用连接执行操作
     *
     * @param function 事务操作函数
     * @return 事务结果Future
     */

    public <T> Future<T> withTransaction(Function<C, Future<T>> function) {
        return withConnection(c -> {
            return Future.succeededFuture()
                         .compose(v -> c.getSqlConnection().begin())
                         .compose(transaction -> Future.succeededFuture()
                                                       .compose(v -> {
                                                           // execute and commit
                                                           return function.apply(c)
                                                                          .compose(t -> transaction.commit()
                                                                                                   .compose(committed -> Future.succeededFuture(t)));
                                                       })
                                                       .compose(Future::succeededFuture, err -> {
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
                                                       }),
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
     * 获取MySQL连接
     *
     * @return 连接Future
     */
    private Future<C> fetchMySQLConnection() {
        return Future.succeededFuture()
                     .compose(v -> pool.getConnection())
                     .compose(
                             sqlConnection -> {
                                 C c = this.sqlConnectionWrapper.apply(sqlConnection);

                                 // since 3.1.0: add mysql version to c;
                                 c.setMysqlVersion(this.fullVersionRef.get());

                                 return Future.succeededFuture(c);
                             },
                             throwable -> Future.failedFuture(
                                     new KeelMySQLConnectionException(
                                             "MySQLDataSource Failed to get SqlConnection From Pool " +
                                                     "`" + this.getConfiguration().getDataSourceName() + "` " +
                                                     "(usage: " + borrowedConnectionCounter.get() + " of "
                                                     + initializedConnectionCounter.get() + "): " +
                                                     throwable,
                                             throwable))
                     );
    }

    Pool getPool() {
        return pool;
    }

    Function<SqlConnection, C> getSqlConnectionWrapper() {
        return sqlConnectionWrapper;
    }


    public VirtualThreadExtension<C> inVirtualThread() {
        if (virtualThreadExtension == null) {
            throw new UnsupportedOperationException("Virtual Thread Extension Not Available!");
        }
        return virtualThreadExtension;
    }
}
