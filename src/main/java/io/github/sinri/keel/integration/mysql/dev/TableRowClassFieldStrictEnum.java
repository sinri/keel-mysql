package io.github.sinri.keel.integration.mysql.dev;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * 表行类字段严格枚举类，用于处理字段的严格枚举定义
 *
 * @since 5.0.0
 */
public class TableRowClassFieldStrictEnum {
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
    public TableRowClassFieldStrictEnum(@NotNull String fieldName, @Nullable String enumPackage, @NotNull String enumClass) {
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
