package io.github.sinri.keel.integration.mysql.dev;


import org.jetbrains.annotations.NotNull;

/**
 * 表行类字段任意包装器类，用于处理字段的解密包装器
 *
 * @since 5.0.0
 */
public class TableRowClassFieldAnyEnvelope {
    private final String envelopePackage;
    private final String envelopeName;

    /**
     * 构造表行类字段任意包装器
     *
     * @param envelopeName    包装器名称
     * @param envelopePackage 包装器包名
     */
    public TableRowClassFieldAnyEnvelope(@NotNull String envelopeName, @NotNull String envelopePackage) {
        this.envelopePackage = envelopePackage;
        this.envelopeName = envelopeName;
    }

    /**
     * 构建调用类方法代码
     * @param parameter 参数
     * @return 生成的代码
     */
    public String buildCallClassMethodCode(@NotNull String parameter) {
        return "new " + envelopePackage + "." + envelopeName + "().decrypt(" + parameter + ");";
    }
}
