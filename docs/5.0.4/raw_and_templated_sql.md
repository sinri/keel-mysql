# 原始 SQL 与模板 SQL（`RawStatement` / `templated`）

## `RawStatement`

类：`io.github.sinri.keel.integration.mysql.statement.RawStatement`。

5.0.4 起，`RawStatement` 只表示一段原始 SQL，不再保存“是否走预编译协议”的状态：

- 使用 `new RawStatement(String sql)` 创建语句。
- 旧构造器 `RawStatement(String sql, boolean prepareStatement)` 已废弃，调用会抛出 `UnsupportedOperationException`。
- 执行协议和参数绑定在 `RunnableStatement` 执行层选择。

工厂方法：

- `rawForPreparedQuery(sql)`：创建原始 SQL 并附着当前连接。方法名为兼容旧命名保留，实际执行方式由返回的 `RunnableStatement` 决定。
- `rawForDirectQuery(sql)`：已废弃，调用会抛出 `UnsupportedOperationException`。请改用 `rawForPreparedQuery(sql).executeThroughQuery()`。

## 执行与参数绑定

`RawStatement` 可以承载包含 `?` 占位符的 SQL，但只有预编译执行方法支持参数绑定：

```java
RunnableStatement statement = connection.rawForPreparedQuery(
        "SELECT * FROM user WHERE id = ? AND status = ?"
);

statement.executeThroughPrepare(Tuple.of(123, "ACTIVE"));
```

常用执行方法：

- `execute()`：默认走无参数预编译路径，适用于不含 `?` 的 SQL。
- `executeThroughPrepare(Tuple)`：走预编译路径，并将 `Tuple` 绑定到 `?` 占位符。
- `executeThroughQuery()`：走普通查询协议，SQL 必须是完整可执行字符串，不支持 `Tuple` 参数绑定。

## `TemplatedStatement` 族

接口：`io.github.sinri.keel.integration.mysql.statement.templated.TemplatedStatement`。

模板语句仍然通过 `{argumentName}` 做文本替换，`TemplateArgumentMapping` 的值会进入最终 SQL 字符串：

- `{name}` 不是 Vert.x / JDBC 风格的 `?` 参数。
- `buildSql()` 不解析 SQL 上下文，只做文本替换。
- 用户输入若走模板替换，应使用 `TemplateArgument.forString(value)` 或 `TemplateArgumentMapping.bindString(name, value)` 生成已转义字符串字面量。

如需服务端参数绑定，应让模板最终生成包含 `?` 的 SQL，再在执行层传入 `Tuple`：

```java
RunnableStatement statement = connection.templatedRead(path, arguments -> {
    // 模板中保留 ?，这里仅替换可信 SQL 片段，例如表名白名单。
});

statement.executeThroughPrepare(Tuple.of("Alice"));
```

## 选型建议

| 需求 | 方案 |
|---|---|
| 类型安全构造查询 | DSL + `ConditionsComponent` |
| 长 SQL 放资源文件、少量可信片段替换 | `templatedRead` / `templatedModify` |
| 手写 SQL 并绑定用户输入 | `RawStatement` 或模板 SQL + `executeThroughPrepare(Tuple)` |
| 强制普通查询协议 | `executeThroughQuery()` |
