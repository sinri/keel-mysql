package io.github.sinri.keel.integration.mysql.dev;

import io.github.sinri.keel.core.utils.StringUtils;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 表行类字段类，定义了表字段的信息和生成规则
 *
 * @since 5.0.0
 */
@NullMarked
class TableRowClassField {
    private static final Pattern patternForLooseEnum;
    private static final Pattern patternForStrictEnum;
    private static final Pattern patternForAnyEnvelope;

    static {
        patternForLooseEnum = Pattern.compile("Enum\\{([A-Za-z0-9_, ]+)}");
        patternForStrictEnum = Pattern.compile("Enum<([A-Za-z0-9_.]+)>");
        patternForAnyEnvelope = Pattern.compile("Envelope<([A-Za-z0-9_.]+)>");
    }

    private final String field;
    private final String type;
    private final @Nullable String comment;
    private final @Nullable String strictEnumPackage;
    private final @Nullable String envelopePackage;
    private final boolean nullable;
    private final String tableExpression;
    private String returnType;
    private String readMethod;
    private @Nullable TableRowClassFieldLooseEnum looseEnum;
    private @Nullable TableRowClassFieldStrictEnum strictEnum;
    private @Nullable TableRowClassFieldAnyEnvelope envelope;
    private boolean fieldDeprecated = false;
    private String actualComment;

    public TableRowClassField(
            String tableExpression,
            String field,
            String type,
            boolean nullable,
            @Nullable String comment,
            @Nullable String strictEnumPackage,
            @Nullable String envelopePackage
    ) {
        this.tableExpression = tableExpression;
        this.field = field;
        this.type = type;
        this.nullable = nullable;
        this.comment = comment;
        this.strictEnumPackage = strictEnumPackage;
        this.envelopePackage = envelopePackage;

        parseType();
        parseComment();
    }

    protected void parseType() {
        returnType = "Object";
        readMethod = "readValue";

        if (type.contains("bigint")) {
            returnType = "Long";
            readMethod = "readLong";
        } else if (type.contains("int")) {
            // tinyint smallint mediumint
            returnType = "Integer";
            readMethod = "readInteger";
        } else if (type.contains("float")) {
            returnType = "Float";
            readMethod = "readFloat";
        } else if (type.contains("double")) {
            returnType = "Double";
            readMethod = "readDouble";
        } else if (type.contains("decimal")) {
            returnType = "Number";
            readMethod = "readNumber";
        } else if (type.contains("datetime") || type.contains("timestamp")) {
            returnType = "String";
            readMethod = "readDateTime";
        } else if (type.contains("date")) {
            returnType = "String";
            readMethod = "readDate";
        } else if (type.contains("time")) {
            returnType = "String";
            readMethod = "readTime";
        } else if (type.contains("text") || type.contains("char")) {
            // mediumtext, varchar, etc.
            returnType = "String";
            readMethod = "readString";
        }
    }

    protected void parseComment() {
        if (type.contains("char") && comment != null) {
            // supportLooseEnum
            Matcher matcherForLoose = patternForLooseEnum.matcher(comment);
            if (matcherForLoose.find()) {
                String enumValuesString = matcherForLoose.group(1);
                String[] enumValueArray = enumValuesString.split("[, ]+");
                if (enumValueArray.length > 0) {
                    looseEnum = new TableRowClassFieldLooseEnum(field, List.of(enumValueArray));
                }
            }
            // supportStrictEnum
            Matcher matcherForStrict = patternForStrictEnum.matcher(comment);
            if (matcherForStrict.find()) {
                String enumClassPathTail = matcherForStrict.group(1);
                strictEnum = new TableRowClassFieldStrictEnum(field, strictEnumPackage, enumClassPathTail);
            }

            // Any Envelope
            Matcher matcherForAnyEnvelope = patternForAnyEnvelope.matcher(comment);
            if (matcherForAnyEnvelope.find()) {
                String any = matcherForAnyEnvelope.group(1);
                envelope = new TableRowClassFieldAnyEnvelope(any, Objects.requireNonNull(this.envelopePackage));
            }
        }

        if (comment != null) {
            String[] split = comment.split("@[Dd]eprecated", 2);
            if (split.length > 1) {
                // this table is deprecated
                this.fieldDeprecated = true;
                actualComment = StringUtils.escapeForHttpEntity(split[1]);
            } else {
                actualComment = StringUtils.escapeForHttpEntity(comment);
            }
        } else {
            actualComment = "";
        }
    }

    public String build() {
        String getter = "get" + StringUtils.fromUnderScoreCaseToCamelCase(field, false);

        StringBuilder code = new StringBuilder();
        if (looseEnum != null) {
            code.append(looseEnum.build()).append("\n");
            code.append("\t/**\n");
            code.append("\t * Field {@code ").append(tableExpression).append(".").append(field).append("}.\n");
            code.append("\t * ").append(actualComment).append("\n");
            code.append("\t * <p>\n");
            code.append("\t * Loose Enum of Field `").append(field).append("` of type `").append(type).append("`.\n");
            code.append("\t */\n");
            if (fieldDeprecated) {
                code.append("\t@Deprecated\n");
            }
            code.append("\tpublic ")
                .append(nullable ? "@Nullable " : " ").append(looseEnum.looseEnumName())
                .append(" ").append(getter).append("() {\n");
            code.append("\t\t// enumExpression is nullable\n");
            code.append("\t\tString enumExpression=").append(readMethod).append("(\"").append(field)
                .append("\");\n");
            if (nullable) {
                code.append("\t\tif (enumExpression==null) return null;\n");
            } else {
                code.append("\t\tObjects.requireNonNull(enumExpression,\"The Enum Field `").append(field)
                    .append("` should not be null!\");\n");
            }
            code.append("\t\treturn ").append(looseEnum.looseEnumName()).append(".valueOf(enumExpression);\n")
                .append("\t}\n");
        } else if (strictEnum != null) {
            code.append("\t/**\n");
            code.append("\t * Field {@code ").append(tableExpression).append(".").append(field).append("}.\n");
            code.append("\t * ").append(actualComment).append("\n");
            code.append("\t * <p>\n");
            code.append("\t * Strict Enum of Field `").append(field).append("` of type `").append(type).append("`.\n");
            code.append("\t */\n");
            if (fieldDeprecated) {
                code.append("\t@Deprecated\n");
            }
            code.append("\tpublic ")
                .append(nullable ? "@Nullable " : " ")
                .append(strictEnum.fullEnumRef()).append(" ").append(getter).append("() {\n");
            code.append("\t\t// enumExpression is nullable\n");
            code.append("\t\tString enumExpression=").append(readMethod).append("(\"").append(field)
                .append("\");\n");
            if (nullable) {
                code.append("\t\tif (enumExpression==null) return null;\n");
            } else {
                code.append("\t\tObjects.requireNonNull(enumExpression,\"The Enum Field `").append(field)
                    .append("` should not be null!\");\n");
            }
            code.append("\t\treturn ").append(strictEnum.fullEnumRef()).append(".valueOf(enumExpression);\n");
            code.append("\t}\n");
        } else {
            code.append("\t/**\n");
            code.append("\t * Field {@code ").append(tableExpression).append(".").append(field).append("}.\n");
            if (comment != null) {
                code.append("\t * ").append(actualComment).append("\n");
                code.append("\t * <p>\n");
            }
            code.append("\t * Field `").append(field).append("` of type `").append(type).append("`.\n");
            code.append("\t */\n");
            if (fieldDeprecated) {
                code.append("\t@Deprecated\n");
            }
            code.append("\tpublic ")
                .append(nullable ? "@Nullable " : " ")
                .append(returnType).append(" ").append(getter).append("() {\n");
            code.append("\t\treturn ")
                .append(nullable ? "" : "Objects.requireNonNull(")
                .append(readMethod).append("(\"").append(field).append("\")")
                .append(nullable ? "" : ")").append(";\n");
            code.append("\t}\n");
        }

        if (envelope != null) {
            code.append("\t/**\n");
            code.append("\t * EXTRACTED VALUE of {@code ").append(tableExpression).append(".").append(field)
                .append("}.\n");
            if (comment != null) {
                code.append("\t * ").append(actualComment).append("\n");
                code.append("\t * <p>\n");
            }
            code.append("\t */\n");
            code.append("\tpublic @Nullable String ").append(getter).append("Extracted() {\n");
            code.append("\t\treturn ")
                .append(envelope.buildCallClassMethodCode(readMethod + "(\"" + field + "\")")).append("\n");
            code.append("\t}\n");
        }

        return code.toString();
    }

    @Override
    public String toString() {
        return build();
    }
}
