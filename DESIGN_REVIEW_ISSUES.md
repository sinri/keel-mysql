# keel-mysql 设计 / 实现审查问题清单

审查范围：数据源与连接、语句执行、配置、模块边界、构建与文档一致性。  
生成日期：2025-03-25。严重性：高 / 中 / 低（主观分级，便于排期）。

---

## 高

1. ~~**`NamedMySQLDataSource` 连接计数语义与 Vert.x 池不一致**~~
   **已修复。** 移除了只增不减的 `initializedConnectionCounter`，改为委托 Vert.x 5 `Pool.size()` 获取池大小近似值；`getCurrentIdleConnectionCount()` 改为 `Math.max(0, pool.size() - borrowed)`；原 `getCurrentInitializedConnectionCount()` 标记 `@Deprecated` 并转发到 `pool.size()`。
   涉及：`NamedMySQLDataSource.java`。

2. ~~**`lateFullVersion` 存在并发竞态**~~
   **确认为非问题。** 理论上 `initializeConnection` 中 `isInitialized()` 与 `set()` 之间隔了异步的 `SELECT VERSION()` 查询，存在竞态窗口；但实际影响仅为多执行一次版本查询和一个被 `compose` 链静默消化的 `IllegalStateException`，不会导致数据错误、连接泄漏或功能异常（`onComplete` 无论成功失败均会关闭连接）。同一数据源的所有连接查到的版本号相同，因此无论哪个先 `set` 结果都正确。`LateObject.ensure()` 不可用于此场景：其接受同步 `Supplier`，无法适配异步的 `Future`；且其自身的双重检查锁实现（外层判空在 `synchronized` 之外、`value` 无 `volatile`）也存在缺陷。
   涉及：`NamedMySQLDataSource.initializeConnection`。

3. ~~**`fetchConnectionInVirtualThread` 易泄漏连接且易误用**~~
   **已修复。** 补充了完整的 JavaDoc 说明阻塞风险和关闭责任；借出时纳入 `borrowedConnectionCounter` 计数并设置 MySQL 版本信息；新增 `returnConnectionFromVirtualThread()` 配套归还方法，负责关闭连接并递减计数。
   涉及：`NamedMySQLDataSource.fetchConnectionInVirtualThread`、`NamedMySQLDataSource.returnConnectionFromVirtualThread`。

4. ~~**`KeelMySQLDataSourceProvider.waitForLoading` 行为粗糙**~~
   **已修复。** 连接成功时取消定时器；`withConnection` 的失败通过 `.onFailure(e -> promise.tryFail(e))` 立即传播，不再等满超时；超时改用 `TimeoutException` 替代裸字符串，便于上层类型区分。
   涉及：`KeelMySQLDataSourceProvider.waitForLoading`。

5. ~~**`NamedMySQLConnection` 事务相关默认方法在无事务时易 NPE**~~
   **已修复。** Vert.x 5 中 `SqlConnection.transaction()` 在无活跃事务时返回 `null`（已通过源码确认）。`commitTransaction()` 和 `rollbackTransaction()` 原先直接在 `transaction()` 返回值上调用方法，未做 null 检查。现已添加防御性检查：`transaction()` 为 null 时返回 `Future.failedFuture(new IllegalStateException(...))`。`isForTransaction()` 和 `getTransaction()` 本身已正确处理 null。注：高层 API `NamedMySQLDataSource.executeInTransaction()` / `withTransaction()` 未使用这些接口方法，它们直接持有 `begin()` 返回的 `Transaction` 引用操作，不受此问题影响。
   涉及：`NamedMySQLConnection.java`。

---

## 中

6. ~~**预处理语句路径未绑定参数**~~
   **确认为设计预期，已补充文档。** 所有内置查询构建器通过 `Quoter` 将值内联到 SQL 字符串中，从不生成 `?` 占位符，库中无 `Tuple` 使用。`toPrepareStatement` 标志控制的是 MySQL COM_STMT_PREPARE 协议（服务端语句缓存与执行计划复用），而非 JDBC 风格的参数绑定。对不含 `?` 的完整 SQL 调用 `preparedQuery(sql).execute()` 是合法的。`RawStatement` 若传入含 `?` 的 SQL 会因参数数量不匹配而直接报错（fail-fast），不会静默产生错误结果。已在 `RawStatement`、`RunnableStatementFactory.rawForPreparedQuery/rawForDirectQuery`、`AnyStatement.setToPrepareStatement/isToPrepareStatement` 的 JavaDoc 中明确说明语义与限制。
   涉及：`RawStatement.java`、`RunnableStatementFactory.java`、`AnyStatement.java`。

7. ~~**模板 SQL：`buildSql` 字符串替换与编码**~~
   **已修复。** (a) `loadTemplateTo*` 中 `new String(bytes)` 改为 `new String(bytes, StandardCharsets.UTF_8)`，显式指定 UTF-8 编码。(b) `buildSql()` 中 `String.replaceAll`（正则替换）改为 `String.replace`（字面量替换），消除 `argumentName` 含正则元字符时匹配错误或 `PatternSyntaxException` 的风险，同时移除了不再需要的 `Matcher.quoteReplacement` 和 `java.util.regex.Matcher` 导入。
   涉及：`TemplatedStatement.java`。

8. ~~**查询构建器将标识符与字面量直接拼接**~~
   **确认为 DSL 设计预期，已补充 JavaDoc 安全说明。** 标识符直接拼接是 SQL DSL 构建器的标准做法（与 jOOQ、QueryDSL 等一致），字面量值的注入防护由 `Quoter` 负责。已在 `AbstractStatement`（覆盖所有语句子类）、`MySQLCondition`（覆盖所有条件实现类）、`RawCondition`（风险最高的原始表达式类）的 JavaDoc 中添加安全说明，明确标识符/表达式参数仅接受可信输入。
   涉及：`AbstractStatement.java`、`MySQLCondition.java`、`RawCondition.java`。

9. ~~**`KeelMySQLConfiguration.generatePropertiesForConfig` 包含明文密码**~~
   **已修复。** 原方法标记为 `@Deprecated(since = "5.0.1", forRemoval = true)`，JavaDoc 中说明了明文密码泄露风险。新增 `generateSamplePropertiesForConfig(String dataSourceName)` 静态方法，生成包含占位符值的样本配置字符串，不含任何真实连接信息，可安全用于文档、日志或版本控制。
   涉及：`KeelMySQLConfiguration.java`。

10. **`checkMySQLVersion` 静默吞异常**  
    解析 `VERSION()` 失败时返回 `null` 且注释掉日志，排障困难。  
    涉及：`NamedMySQLDataSource.checkMySQLVersion`。

11. **`instantQuery` / 流式查询的资源与失败路径**  
    `instantQuery` 用 `andThen` 关闭 `sqlClient`，需确认 Vert.x 版本下是否在查询失败时仍关闭（通常 `onComplete` 类行为会执行）。`instantQueryForStream` 链路长，依赖 `eventually` 多处兜底，建议集成测试覆盖失败与中途取消场景。  
    涉及：`KeelMySQLConfiguration.java`。

12. **主模块导出 `dev` 包**  
    `module-info` 将代码生成等开发工具类与运行时 API 同模块导出，增加暴露面与依赖混淆；长期更适合拆分为 `*-dev` 或 `test` 可见。  
    涉及：`module-info.java`。

---

## 低

13. **`AbstractStatement.SQL_COMPONENT_SEPARATOR` 为可变 `static` 非 `final`**  
    被任一语句类修改会影响全局 SQL 拼接格式，线程安全与可预测性差。更稳妥为 `private static final` 或实例字段。  
    涉及：`AbstractStatement.java`。

14. **`RawStatement` 构造参数拼写错误**  
    `prepareStatment` → 应为 `prepareStatement`（破坏性 API 变更，可标记 deprecated 再改）。  
    涉及：`RawStatement.java`。

15. **`RunnableStatementForRead.executeForRowList` 的 JavaDoc 不准确**  
    文档称「查询不到时返回 null」，实现返回 `getRowList()`，空结果多为空列表而非 `null`。  
    涉及：`RunnableStatementForRead.java`。

16. **`StatementAuditorHolder` 注释笔误**  
    「重新加载SQL审计问题记录器」应为「日志记录器」一类表述。  
    涉及：`StatementAuditorHolder.java`。

17. **构建强依赖内部 Nexus**  
    `build.gradle.kts` 中 `repositories` 将 Internal Nexus 置首且需凭据，公开克隆仓库者在无凭据时可能无法解析依赖（取决于 `gradle.properties` 是否齐全）。需在 README 说明匿名只读或使用 Maven Central 的可行配置。  
    涉及：`build.gradle.kts`。

18. **测试覆盖面**  
    当前测试能通过，但以样例 / 开发类为主；缺少对连接池失败、`waitForLoading` 超时、`executeInConnection` 失败路径、事务 rollback 分支等的单元 / 集成测试。  
    涉及：`src/test`。

---

## 已确认的非问题（简记）

- `executeInConnection` / `withConnection` 使用 `andThen` 关闭连接：Vert.x 中 `andThen` 对应完成回调，成功与失败均可触达关闭处理（仍建议用单元测试锁定行为）。  
- GPL-v3.0 许可证：若作为库被专有软件链接，存在合规约束，属产品策略而非实现缺陷。

---

## 建议的后续动作（可选）

1. ~~将池统计改为文档化「仅供诊断的近似值」或改为暴露 Vert.x Pool 指标（若 API 提供）。~~ **已完成**：改用 `Pool.size()`，注释中标注为近似值。  
2. ~~用 `synchronized` / `AtomicReference` / 一次性 `Future` 固定 `lateFullVersion` 的写入。~~ **无需修复**：竞态实际无害，且 `LateObject.ensure()` 不适用于异步场景。  
3. ~~为 `fetchConnectionInVirtualThread` 提供 `try-with-resources` 式封装或明确废弃。~~ **已完成**：新增 `returnConnectionFromVirtualThread()` 配套归还方法，JavaDoc 明确关闭责任。  
4. ~~重构 `waitForLoading`：`compose` 链式处理 `withConnection` 的 Future，超时用 `Vertx.timer()` + `cancel` 或 `Future.timeout`。~~ **已完成**：连接失败立即传播，成功时取消定时器，超时改用 `TimeoutException`。  
5. ~~为含占位符 SQL 增加 `execute(Tuple)` 或专用 API。~~ **暂不实施**：当前库设计为值内联模式，`toPrepareStatement` 用于协议层优化而非参数绑定；已在相关 API 的 JavaDoc 中明确说明语义与限制。如未来需要真正的参数化查询能力，可在 `RawStatement` 中扩展 `Tuple` 支持。
