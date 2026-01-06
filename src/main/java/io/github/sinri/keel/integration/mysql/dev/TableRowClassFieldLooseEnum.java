package io.github.sinri.keel.integration.mysql.dev;


import io.github.sinri.keel.core.utils.StringUtils;
import org.jspecify.annotations.NullMarked;

import java.util.Collection;


/**
 * 表行类字段松散枚举类，用于处理字段的松散枚举定义
 *
 * @since 5.0.0
 */
@NullMarked
class TableRowClassFieldLooseEnum {
    private final String fieldName;
    private final Collection<String> enumElements;
    private final String enumName;

    /**
     * 构造表行类字段松散枚举
     *
     * @param fieldName    字段名
     * @param enumElements 枚举元素集合
     */
    public TableRowClassFieldLooseEnum(String fieldName, Collection<String> enumElements) {
        this.fieldName = fieldName;
        this.enumElements = enumElements;
        this.enumName = StringUtils.fromUnderScoreCaseToCamelCase(fieldName, false) + "Enum";
    }

    /**
     * 获取松散枚举名称
     *
     * @return 松散枚举名称
     */
    public String looseEnumName() {
        return this.enumName;
    }

    /**
     * 构建枚举源代码
     *
     * @return 枚举源代码
     */
    public String build() {
        StringBuilder code = new StringBuilder();
        code
                .append("\t/**\n")
                .append("\t * Enum for Field `").append(fieldName).append("` \n")
                .append("\t */\n")
                .append("\tpublic enum ").append(looseEnumName()).append(" {\n");
        enumElements.forEach(enumValue -> code.append("\t\t").append(enumValue).append(",\n"));
        code.append("\t}\n");

        return code.toString();
    }

    @Override
    public String toString() {
        return build();
    }
}
