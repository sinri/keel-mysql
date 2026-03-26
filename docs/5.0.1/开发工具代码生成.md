# 开发工具与代码生成（`dev`）

包：`io.github.sinri.keel.integration.mysql.dev`。

该包提供**从数据库 schema 生成 Java 源码**（表行类型等）的工具接口，典型在 **IDE 即席运行**或一次性任务中使用，而非应用运行时依赖路径。

## JPMS 说明

模块 **`module-info.java`** 对 **`io.github.sinri.keel.integration.mysql.dev`** 既 **`exports`** 又 **`opens`
**：可正常编译引用，同时允许运行时代码（如反射）访问该包内类型。若你的应用在**模块路径**下做深度反射，需确认是否还要 *
*`opens ... to your.module`**。

一般业务应用**不必**在运行时依赖 dev 包能力；生成任务可使用**同模块测试源码**或**独立 main**。

## `MySQLSchemaTableClassFileGenerator`

接口默认方法依赖：

- **`Keel getKeel()`**：由实现类提供（异步写文件等）。
- **`table.package.path`**：根配置属性，生成类文件落盘的**根目录**绝对路径；未配置会抛 **`RuntimeException`**。
- **`getTablePackage()`**：生成类的**基础包名**（无末尾点）。
- 可选 **`getStrictEnumPackage()`**、**`getEnvelopePackage()`** 等，用于枚举、封装类型的分包。

另有 **`getUnitLogger()`** 默认 **`StdoutLogger`**。

完整流程（扫描表、写 Java 文件）见接口上其它默认方法与 **`TableRowClassSourceCodeGenerator`**、*
*`TableRowClassBuildStandard`** 的 Javadoc。

## 与数据源的关系

生成过程需要连库读取元数据：通常通过 **`KeelMySQLDataSourceProvider`** 加载数据源，在 **`withConnection`
** 内执行信息_schema 或自定义查询；具体以 **`MySQLSchemaTableClassFileGenerator`** 实现类为准（可参考仓库内 **`src/test`
** 下的生成测试）。

## 使用建议

- 将 **`table.package.path`** 指向源码树中**允许覆盖**的包目录，或生成后人工审阅再提交。
- 版本升级后重新生成，避免与手工改动冲突（可用 VCS 区分生成区与手写区）。
