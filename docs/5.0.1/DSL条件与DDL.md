# DSL、条件与 DDL（`statement` / `condition`）

## 包结构概览

| 包                      | 用途                                                                                                                |
|------------------------|-------------------------------------------------------------------------------------------------------------------|
| `statement.impl`       | `SelectStatement`、`UpdateStatement`、`DeleteStatement`、`WriteIntoStatement`、`UnionStatement`、`CallStatement` 等 DML |
| `statement.impl.ddl.*` | `CREATE`/`ALTER`/`TRUNCATE` 表与视图相关语句类                                                                             |
| `statement.component`  | `ConditionsComponent`、CASE/IFNULL 等表达式组件                                                                          |
| `statement.mixin`      | 语句能力混入：`ReadStatementMixin`、`ModifyStatementMixin`、`PaginatableStatementMixin` 等                                  |
| `condition`            | `MySQLCondition` 及比较、IN、原始片段等                                                                                     |

## `RunnableStatementFactory` 上的 DSL 入口

在 **`NamedMySQLConnection`**（或 **`NamedActionInterface`**）上可直接：

- **`select(handler)`**、**`pagination(handler)`**：构建 `SelectStatement` 并 **attach**。
- **`update` / `delete` / `insert` / `replace` / `union` / `call`**。
- DDL：**`createTable`**、**`createTableLikeTable`**、**`alterTable`**、**`truncateTable`**、**`createView`**、**`alterView`
  **、**`dropView`**。

各 **handler** 接收对应语句类型，在 lambda 内链式配置列、条件、排序等，无需手动 new（也可 **`new XxxStatement()`** 再 *
*`attachToConnection`**）。

## 条件：`ConditionsComponent` 与 `MySQLCondition`

**`ConditionsComponent`**（及语句上的 **`where(Consumer<ConditionsComponent>)`**）用于拼装 `WHERE` 子句。

**安全说明**（与 **`MySQLCondition` Javadoc 一致）：

- 列名、表达式、**`RawCondition`** 等内容会**直接拼进 SQL**，**不做**标识符转义或「用户输入校验」。
- **仅允许可信来源**作为「标识符 / 表达式」传入；用户输入应通过**参数化**或严格白名单，避免 SQL 注入。

**`Quoter`**（根包 **`io.github.sinri.keel.integration.mysql.Quoter`
**）用于将字符串、数字、布尔、列表等格式化为 SQL 字面量片段；在 DSL 内部用于 **literal** 类条件。业务层若手写片段，应优先复用 *
*`Quoter`** 而非自行拼接引号。

## 分页与 LIMIT

支持分页的 SELECT 通过 **`PaginatableStatementMixin`** 提供 **`limit(limit)`**、**`limit(limit, offset)`**。

**`executeForPagination`** 在 **`RunnableStatementForReadAndPagination`** 上：会复制语句生成 **COUNT(\*)** 与带 *
*LIMIT/OFFSET** 的数据查询（见 [结果集分页与映射.md](./结果集分页与映射.md)）。

## DDL 使用建议

- DDL 通常在迁移或管理接口中执行；生产环境注意权限与锁表。
- **`alterTable`**、**`createTable`** 等类型位于细分子包（如 `alter.column`、`create`）；随版本迭代类名以源码为准。
- 执行结果一般通过 **`RunnableStatement.execute()`** 得到 **`StatementExecuteResult`**；多数 DDL 不关心行集，可关注 *
  *`getTotalAffectedRows()`** 或仅判断成功/失败。

## 与模板 SQL、原始 SQL 的分工

- **结构化、可组合**：本 DSL + **`ConditionsComponent`**。
- **长文本、运维脚本**：**`TemplatedStatement`**（见 [原始SQL与模板SQL.md](./原始SQL与模板SQL.md)）。
- **完全手写、或需指定协议**：**`RawStatement` / `rawFor*`**。
