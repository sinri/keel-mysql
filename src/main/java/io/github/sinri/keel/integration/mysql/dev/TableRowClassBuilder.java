package io.github.sinri.keel.integration.mysql.dev;

import io.github.sinri.keel.core.utils.StringUtils;
import io.github.sinri.keel.integration.mysql.result.row.AbstractTableRow;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.NullMarked;

import java.util.Date;


/**
 * 表行类构建器，用于根据数据库表结构生成对应的Java表行类
 *
 * @since 5.0.0
 */
@NullMarked
class TableRowClassBuilder {


    private final TableRowClassBuildOptions options;
    private boolean tableDeprecated = false;

    /**
     * 构造表行类构建器
     *
     * @param options 构建选项
     */
    public TableRowClassBuilder(TableRowClassBuildOptions options) {
        this.options = options;
    }

    /**
     * 解析表格注释
     *
     * @return 解析后的表格注释
     */
    protected String parsedTableComment() {
        var tableComment = options.getTableComment();
        if (tableComment == null || tableComment.isBlank()) {
            return "Table comment is empty.";
        } else {
            String[] split = tableComment.split("@[Dd]eprecated", 2);
            if (split.length > 1) {
                // this table is deprecated
                this.tableDeprecated = true;
                return StringUtils.escapeForHttpEntity(split[1]);
            } else {
                return StringUtils.escapeForHttpEntity(tableComment);
            }
        }
    }

    /**
     * 构建表行类源代码
     *
     * @return 生成的Java类源代码
     */
    public String build() {
        var table = options.getTable();
        var schema = options.getSchema();
        var className = getClassName();
        var vcsFriendly = options.isVcsFriendly();
        StringBuilder code = new StringBuilder();
        code.append("package ").append(options.getPackageName()).append(";").append("\n")
            .append("import ").append(AbstractTableRow.class.getName()).append(";\n")
            .append("import ").append(JsonObject.class.getName()).append(";\n")
            .append("\n")
            .append("import org.jspecify.annotations.NullMarked;\n")
            .append("import org.jspecify.annotations.Nullable;\n")
            .append("import java.util.Objects;\n")
            .append("\n")
            .append("/**\n")
            .append(" * ").append(parsedTableComment()).append("\n")
            .append(" * (´^ω^`)\n");
        if (schema != null && !schema.isBlank()) {
            code.append(" * SCHEMA: ").append(schema).append("\n");
        }
        code
                .append(" * TABLE: ").append(table).append("\n")
                .append(" * (*￣∇￣*)\n")
                .append(" * NOTICE BY KEEL:\n")
                .append(" * \tTo avoid being rewritten, do not modify this file manually.\n");
        if (!vcsFriendly) {
            code.append(" * \tIt was auto-generated on ").append(new Date()).append(".\n");
        }
        code.append(" * @see ").append(TableRowClassSourceCodeGenerator.class.getName()).append("\n")
            .append(" */\n");
        if (tableDeprecated) {
            code.append("@Deprecated\n");
        }
        code.append("@NullMarked\n");
        code.append("public class ").append(className).append(" extends AbstractTableRow {").append("\n");

        if (schema != null && !schema.isBlank()) {
            if (this.options.isProvideConstSchema()) {
                code.append("\tpublic static final String SCHEMA = \"").append(schema).append("\";\n");
            }
            if (this.options.isProvideConstSchemaAndTable()) {
                code.append("\tpublic static final String SCHEMA_AND_TABLE = \"").append(schema).append(".")
                    .append(table).append("\";\n");
            }
        }
        if (this.options.isProvideConstTable()) {
            code.append("\tpublic static final String TABLE = \"").append(table).append("\";\n");
        }

        code
                .append("\n")
                .append("\t").append("public ").append(className).append("(JsonObject tableRow) {\n")
                .append("\t\tsuper(tableRow);\n")
                .append("\t}\n")
                .append("\n")
                .append("\t@Override\n")
                .append("\tpublic String sourceTableName() {\n")
                .append("\t\treturn ").append(this.options.isProvideConstTable() ? "TABLE" : "\"" + table + "\"")
                .append(";\n")
                .append("\t}\n")
                .append("\n");
        if (schema != null) {
            code.append("\tpublic String sourceSchemaName(){\n")
                .append("\t\treturn ").append(this.options.isProvideConstSchema() ? "SCHEMA" : "\"" + schema + "\"")
                .append(";\n")
                .append("\t}\n");
        }

        options.getFields().forEach(field -> code.append(field).append("\n"));

        code.append("\n}\n");

        var ddl = options.getDdl();
        if (ddl != null) {
            if (vcsFriendly) {
                // AUTO_INCREMENT=2395644
                var cleanedDDL = ddl.replaceAll("\\s+AUTO_INCREMENT=\\d+\\s+", " AUTO_INCREMENT=_ ");
                code.append("\n/*\n").append(cleanedDDL).append("\n */\n");
            } else {
                code.append("\n/*\n").append(ddl).append("\n */\n");
            }
        }

        return code.toString();
    }

    @Override
    public String toString() {
        return build();
    }

    public String getClassName() {
        return StringUtils.fromUnderScoreCaseToCamelCase(options.getTable(), false) + "TableRow";
    }
}
