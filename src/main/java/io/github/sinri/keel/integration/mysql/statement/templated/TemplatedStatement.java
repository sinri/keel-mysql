package io.github.sinri.keel.integration.mysql.statement.templated;


import io.github.sinri.keel.core.utils.FileUtils;
import org.jspecify.annotations.NullMarked;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;


/**
 * 模板语句接口，用于构建基于模板的SQL语句
 * <p>
 * 此接口提供从文件加载SQL模板、获取模板内容、
 * 获取参数映射，以及通过将模板中的占位符
 * 替换为相应值来构建最终SQL字符串的方法。
 * </p>
 *
 * @see TemplatedReadStatement
 * @see TemplatedModifyStatement
 * @since 5.0.0
 */
@NullMarked
public interface TemplatedStatement {
    /**
     * 从文件加载模板生成读取语句
     *
     * @param templatePath 模板文件路径
     * @return 模板读取语句实例
     */
    static TemplatedReadStatement loadTemplateToRead(String templatePath) {
        try {
            byte[] bytes = FileUtils.readFileAsByteArray(templatePath, true);
            String sqlTemplate = new String(bytes);
            return new TemplatedReadStatement(sqlTemplate);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 从文件加载模板生成修改语句
     *
     * @param templatePath 模板文件路径
     * @return 模板修改语句实例
     */
    static TemplatedModifyStatement loadTemplateToModify(String templatePath) {
        try {
            byte[] bytes = FileUtils.readFileAsByteArray(templatePath, true);
            String sqlTemplate = new String(bytes);
            return new TemplatedModifyStatement(sqlTemplate);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取SQL模板字符串
     *
     * @return SQL模板字符串
     */
    String getSqlTemplate();

    /**
     * 获取参数映射
     *
     * @return 参数映射实例
     */
    TemplateArgumentMapping getArguments();

    /**
     * 构建最终的SQL字符串
     *
     * @return 构建后的SQL字符串
     */
    default String build() {
        AtomicReference<String> sqlRef = new AtomicReference<>(getSqlTemplate());

        getArguments().forEach((argumentName, argumentValue) -> {
            String s = sqlRef.get()
                             .replaceAll("\\{" + argumentName + "}", Matcher.quoteReplacement(argumentValue.toString()));
            sqlRef.set(s);
        });

        return sqlRef.get();
    }
}

