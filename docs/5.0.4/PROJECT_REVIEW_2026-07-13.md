# keel-mysql 项目审查问题清单

审查日期：2026-07-13
审查分支：`dev-5.0.4`（`cf40668`）
审查范围：公共 API 兼容性、连接与事务生命周期、5.0.4 Tuple/批处理执行路径、虚拟线程入口、构建与测试。
审查方法：静态代码审查、Git 历史对比、依赖源码核对、`./gradlew clean test javadoc`。

## 审查摘要

本轮确认 5 个待决问题：高 2 个、中 2 个、低 1 个。上一轮
`docs/5.0.1/DESIGN_REVIEW_ISSUES.md` 中已关闭或明确延期的问题不重复计入。

完整质量检查通过：`BUILD SUCCESSFUL`，但当前测试没有覆盖连接回调同步抛错、旧 API
兼容行为、批处理结果链和虚拟线程调用约束，因此未能发现下述问题。

| 编号 | 严重性 | 问题 | 状态 | 决策/结果 |
| --- | --- | --- | --- | --- |
| 1 | 高 | 5.0.4 在保留旧 API 签名的同时改为运行时抛错 | 不处理 | 维护者决定维持当前 5.0.4 行为 |
| 2 | 高 | `withConnection` 的同步异常路径会泄漏连接和活跃计数 | 已建 Issue | [#18](https://github.com/sinri/keel-mysql/issues/18) |
| 3 | 中 | 批处理结果只统计第一段 `RowSet` | 已建 Issue | [#19](https://github.com/sinri/keel-mysql/issues/19) |
| 4 | 中 | 虚拟线程入口只检查运行时能力，不检查当前线程 | 已快速修复 | 平台线程在连接前立即失败；已增加回归测试 |
| 5 | 低 | 新增执行路径缺少行为测试 | 已建 Issue | [#20](https://github.com/sinri/keel-mysql/issues/20) |

## 1. 5.0.4 在保留旧 API 签名的同时改为运行时抛错

严重性：高
状态：不处理

维护者决策：维持当前 5.0.4 行为，不恢复旧 API 的兼容实现，也不建立 GitHub Issue。

### 证据

- `AnyStatement#setToPrepareStatement(boolean)` 与 `isToPrepareStatement()` 仍可正常编译，
  但实现无条件抛出 `UnsupportedOperationException`：
  `src/main/java/io/github/sinri/keel/integration/mysql/statement/AnyStatement.java:74-97`。
- `RawStatement(String, boolean)` 仍为 `public`，但构造时无条件抛错：
  `src/main/java/io/github/sinri/keel/integration/mysql/statement/RawStatement.java:37-43`。
- `RunnableStatementFactory#rawForDirectQuery(String)` 仍为 `public default` 方法，但调用时无条件抛错：
  `src/main/java/io/github/sinri/keel/integration/mysql/connection/RunnableStatementFactory.java:51-56`。
- 在前一版本中，上述 API 都具有可用行为；Git 历史显示行为变更由提交 `864a89b` 引入。
- 项目版本从 5.0.3 演进到 5.0.4，按补丁版本的通常预期不应让现有调用在不重新编译或重新编译后直接失败。

### 影响

现有消费者即使没有编译错误，也会在升级到 5.0.4 后于运行时失败。尤其是
`rawForDirectQuery(sql).execute()`，此前是公开文档推荐的普通查询协议入口。将“废弃”实现为
“保留签名但必定抛错”无法提供迁移窗口，也比直接删除更难在编译期发现。

### 建议

在 5.x 内保留旧行为并标记废弃：

- 让旧的语句级开关继续保存协议选择状态；
- `rawForDirectQuery` 返回一个默认走 query 路径的兼容对象；
- `RawStatement(String, boolean)` 继续按布尔值初始化兼容状态；
- 新 API 可作为推荐入口，并在下一个主版本删除旧状态与签名。

若不希望恢复内部状态，也可使用兼容适配层实现等价行为；具体方案不限定于上述方式。

## 2. `withConnection` 的同步异常路径会泄漏连接和活跃计数

严重性：高
状态：已建立 GitHub Issue

处理结果：已建立 [GitHub Issue #18：修复 withConnection 同步异常路径的连接与计数泄漏](https://github.com/sinri/keel-mysql/issues/18)。

### 证据

`NamedMySQLDataSource#withConnection` 在成功取得连接后先递增计数，再直接调用
`function.apply(sqlConnectionWrapper)`，只有成功返回 `Future` 后才注册关闭回调：
`src/main/java/io/github/sinri/keel/integration/mysql/datasource/NamedMySQLDataSource.java:324-339`。

如果业务回调在返回 `Future` 之前同步抛出异常（例如参数校验、构建 SQL、空指针），控制流不会到达
`.andThen(...)`：

- 底层 `SqlConnection` 不会关闭/归还；
- `borrowedConnectionCounter` 不会递减；
- 异常也不会被现有 `.recover(...)` 包装，因为该 Future 链尚未被构造出来。

同类窗口还存在于 `fetchMySQLConnection()` 的 `sqlConnectionWrapper.apply(sqlConnection)`：包装器同步抛错时，
已经从池中取得的连接不会关闭（同文件 `197-224` 行）。虚拟线程入口在包装器抛错时也有相同风险
（`438-453` 行）。

### 影响

少量同步异常即可逐步耗尽连接池，并让活跃连接指标永久偏高；之后正常请求可能因获取连接超时而失败。
事务入口复用 `withConnection`，因此也受影响。

### 建议

将“取得连接之后的所有用户代码”纳入统一资源边界：

- 用 `Future` 的惰性/捕获异常入口调用包装器和业务函数，或显式 `try/catch` 转换为 failed Future；
- 用 `eventually` 等待关闭完成，并在关闭完成回调中确保计数只递减一次；
- 包装器构造失败时立即关闭刚取得的连接；
- 增加同步抛错、返回 failed Future、关闭失败三类测试。

## 3. 批处理结果只统计第一段 `RowSet`

严重性：中
状态：已建立 GitHub Issue

处理结果：已建立 [GitHub Issue #19：修正批处理结果链的影响行数统计](https://github.com/sinri/keel-mysql/issues/19)。

### 证据

`RunnableStatementForWrite#execute(Keel, List<Tuple>)` 将 Vert.x `executeBatch` 返回的首个
`RowSet<Row>` 直接包装为 `StatementExecuteResult`：
`src/main/java/io/github/sinri/keel/integration/mysql/connection/target/RunnableStatementForWrite.java:39-56`。

`StatementExecuteResult#getTotalAffectedRows()` 和 `getTotalFetchedRows()` 分别只读取当前
`rowSet.rowCount()` 与 `rowSet.size()`：
`src/main/java/io/github/sinri/keel/integration/mysql/result/StatementExecuteResult.java:32-43`。

Vert.x 5.1.3 的 `SqlResult#next()` 明确用于访问 batch 的后续结果；其内部结果构建器将每次执行结果链接到
`next`，返回首段作为链头。因此多 Tuple 批处理时，当前“Total”方法及 SQL 审计日志只反映第一组参数，
不会累计后续结果。

### 影响

批处理本身仍可能成功，但调用方得到的影响行数和审计数据偏小；依赖影响行数做完整性判断或指标统计时会
产生错误结论。后续批次的 `LAST_INSERTED_ID` 等属性也没有明确暴露方式。

### 建议

明确 `StatementExecuteResult` 对结果链的语义，并至少提供遍历/聚合能力。批处理入口可返回专用结果类型，
或让 total 方法遍历 `RowSet#next()` 累加，同时保留访问每段结果的 API。需要用两组以上 Tuple 的测试锁定行为。

## 4. 虚拟线程入口只检查运行时能力，不检查当前线程

严重性：中
状态：已快速修复

处理结果：`NamedMySQLDataSource` 改为通过兼容 Java 17 编译目标的反射逻辑调用当前线程的
`Thread#isVirtual()`；运行时不支持虚拟线程或当前线程为平台线程时，在访问连接池前抛出
`UnsupportedOperationException`。新增 `NamedMySQLDataSourceTest`，验证平台线程快速失败且连接池大小与
活跃计数均保持为 0。`./gradlew clean test javadoc` 验证通过。

### 证据

`fetchConnectionInVirtualThread()` 声明“仅可在虚拟线程中调用”，Javadoc 还声明当前线程不是虚拟线程时
抛出 `UnsupportedOperationException`，但实际只调用
`ReflectionUtils.isVirtualThreadsAvailable()`：
`src/main/java/io/github/sinri/keel/integration/mysql/datasource/NamedMySQLDataSource.java:422-453`。

当前 `keel-core:5.0.3` 的该方法只在类初始化时检查 `Thread.isVirtual` 方法是否存在，即仅表示运行中的 JDK
支持虚拟线程；它不读取 `Thread.currentThread().isVirtual()`。在 Java 21+ 上，平台线程和 Vert.x 事件循环线程
都会通过这项检查，然后执行阻塞式 `Future.await()`。

### 影响

API 无法兑现自己的线程安全约束。误从事件循环调用时可能阻塞事件循环、增加延迟甚至造成停滞；从普通
平台线程调用也不会得到文档承诺的快速失败。

### 建议

直接检查当前线程是否为虚拟线程（需兼顾项目 Java 17 编译目标，可通过集中封装的反射兼容实现），不满足时
立即抛出清晰异常；同时增加平台线程与虚拟线程两类测试。

## 5. 新增执行路径缺少行为测试

严重性：低
状态：已建立 GitHub Issue

处理结果：已建立 [GitHub Issue #20：补充 5.0.4 Tuple 与批处理执行路径测试](https://github.com/sinri/keel-mysql/issues/20)。

### 证据

当前 11 个 `*Test` 类主要覆盖配置、SQL 构建、Quoter、结果行与版本解析。5.0.4 新增的以下路径没有测试：

- `RunnableStatement#executeThroughPrepare(Tuple)`；
- 顺序复用预编译语句的 `executeThroughPrepare(Keel, List<Tuple>)`；
- `RunnableStatementForWrite#execute(Keel, List<Tuple>)` 批处理；
- `instantQuery(..., Tuple)` 与参数化流式查询；
- 旧 API 的兼容/废弃行为。

本轮 `./gradlew clean test javadoc` 全部通过，但无法证明这些新路径的协议选择、资源关闭、结果链聚合和异常
传播行为正确。

### 影响

执行层属于数据库库的核心路径，回归可能直接表现为运行时异常、资源泄漏或错误统计；当前问题 1—3 均未被
测试发现。

### 建议

优先补充无需真实数据库的 mock/contract 测试，再增加可选的 MySQL 集成测试。测试至少覆盖成功、异步失败、
同步抛错、空 Tuple、批量多结果、资源关闭与旧 API 迁移行为。

## 已核对但未新报的问题

- `instantQueryForStream` 的 cursor、connection、pool 依次使用 `eventually` 关闭，失败路径资源边界完整。
- `WriteIntoStatement#divide(int)` 已对非正数增加参数校验。
- 连接初始化 handler 按 Vert.x 约定在异步初始化完成后调用 `close()` 将连接释放入池。
- 上一轮已延期的 `dev` 包拆分和整体测试覆盖建设仍有效，本轮不重复计数。

## 决策记录

按问题逐项与维护者确认后更新本节及摘要表。可选决策：不处理、建立 GitHub Issue、快速修复。

- 2026-07-13：问题 1 决定不处理；维持旧 API 调用时抛出 `UnsupportedOperationException` 的现状。
- 2026-07-13：问题 2 建立 GitHub Issue [#18](https://github.com/sinri/keel-mysql/issues/18)，留待后续修复。
- 2026-07-13：问题 3 建立 GitHub Issue [#19](https://github.com/sinri/keel-mysql/issues/19)，留待后续修复。
- 2026-07-13：问题 4 已快速修复；增加当前线程虚拟线程检查及平台线程回归测试，完整质量检查通过，尚未提交。
- 2026-07-13：问题 5 建立 GitHub Issue [#20](https://github.com/sinri/keel-mysql/issues/20)，用于后续完善执行层测试。
