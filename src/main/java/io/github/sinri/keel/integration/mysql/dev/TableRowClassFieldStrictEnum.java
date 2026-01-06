package io.github.sinri.keel.integration.mysql.dev;

import io.github.sinri.keel.core.utils.value.ValueEnveloping;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * 表行类字段严格枚举类，用于处理字段的严格枚举定义
 *
 * @see ValueEnveloping
 * @since 5.0.0
 */
@NullMarked
class TableRowClassFieldStrictEnum {
    private final String fieldName;
    private final String enumPackage;
    private final String enumClass;
    private final String enumClassRef;

    /**
     * 构造表行类字段严格枚举
     *
     * @param fieldName   字段名
     * @param enumPackage 枚举包名
     * @param enumClass   枚举类名
     */
    public TableRowClassFieldStrictEnum(String fieldName, @Nullable String enumPackage, String enumClass) {
        this.fieldName = fieldName;
        this.enumPackage = Objects.requireNonNullElse(enumPackage, "");
        this.enumClass = enumClass;

        enumClassRef = this.enumPackage + "." + enumClass;
        try {
            Class<?> enumClassToCheck = Class.forName(enumClassRef);
            if (!enumClassToCheck.isEnum()) {
                throw new RuntimeException("Defined Enum Class not enum in Strict Mode: " + enumClassRef);
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Defined Enum Class not found in Strict Mode: " + enumClassRef, e);
        }
    }

    public String fullEnumRef() {
        return this.enumClassRef;
    }
}
