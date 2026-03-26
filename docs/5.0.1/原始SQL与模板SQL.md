# 原始 SQL 与模板 SQL（`RawStatement` / `templated`）

## `RawStatement`

类：`io.github.sinri.keel.integration.mysql.statement.RawStatement`。

构造函数 **`RawStatement(String sql, boolean prepareStatement)`**：

- **`prepareStatement == true`**：通过 MySQL **COM_STMT_PREPARE**（Vert.x **`preparedQuery`**）执行，可能带来服务端缓存等收益。
- **`false`**：普通 **COM_QUERY**（**`query`**）。

**重要限制**：当前实现**不支持** `?` **占位符绑定**。SQL 必须是**完整可执行字符串**；若含 `?` 且未绑定，易出现参数数量错误。

工厂方法（在 **`RunnableStatementFactory`** 上）：

- **`rawForPreparedQuery(sql)`**：等价于 `new RawStatement(sql, true).attachToConnection(...)`。
- **`rawForDirectQuery(sql)`**：`prepareStatement = false`。

执行路径与其它语句相同：**`execute()`**、或先 attach 再按目标类型调用（只读场景可转为 **`RunnableStatementForRead`** 需自行
`new` attach，通常直接用 **`execute()`** 拿 **`StatementExecuteResult`**）。

**安全**：原始 SQL 由调用方负全责，必须防注入（可信拼接或严格校验）。

## `TemplatedStatement` 族

接口：**`io.github.sinri.keel.integration.mysql.statement.templated.TemplatedStatement`**。

### 从文件加载

- **`TemplatedStatement.loadTemplateToRead(path)`** → **`TemplatedReadStatement`**
- **`TemplatedStatement.loadTemplateToModify(path)`** → **`TemplatedModifyStatement`**

内部使用 Keel Core 的 **`FileUtils.readFileAsByteArray(path, true)`**：路径需为**进程可读**的绝对或相对路径；失败抛 *
*`RuntimeException`**（包装 **`IOException`**）。

### 占位符规则

**`buildSql()`** 对模板做简单字符串替换：将 **`{argumentName}`** 替换为 **`TemplateArgumentMapping`** 中对应值的 *
*`toString()`**。

- **不是** JDBC `?` 预编译参数，而是**文本替换**。
- 调用方必须保证替换后的片段在 SQL 中合法；**若参数来自用户输入，仍要转义或引用**（可结合 **`Quoter`**）。

### 工厂方法

在连接上：

- **`templatedRead(path, handler)`**：handler 内对 **`TemplateArgumentMapping`** 填参，返回 **`RunnableStatementForRead`
  **。
- **`templatedModify(path, handler)`**：返回 **`RunnableStatementForModify`**。

### `TemplateArgument` / `TemplateArgumentMapping`

用于按名注册参数；具体 API 以源码为准（`put` / `forEach` 等与 **`buildSql`** 联动）。

## 选型建议

| 需求                | 方案                                                |
|-------------------|---------------------------------------------------|
| 动态 WHERE、类型安全     | DSL + **`ConditionsComponent`**                   |
| 长 SQL 放资源文件、少量占位符 | **`templatedRead` / `templatedModify`**           |
| ORM 未覆盖的复杂语句、指定协议 | **`rawForDirectQuery` / `rawForPreparedQuery`**   |
| 真正参数化防注入          | 需 Vert.x 带参数的 API 或上层封装；本库 Raw/模板路径以**字符串**为主，要谨慎 |
