package io.github.sinri.keel.integration.mysql.connection;

import io.github.sinri.keel.integration.mysql.Quoter;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

interface MySQLServerVersionMixin {
    /**
     * MySQL主版本号与次版本号。
     *
     * @param major 主版本号
     * @param minor 次版本号
     */
    record MajorMinorVersion(int major, int minor) {
    }

    /**
     * 获取MySQL版本信息。
     * <p>
     * 版本信息来源于数据源首次连接时执行的 {@code SELECT VERSION()}。
     * 若版本查询失败或连接尚未完成初始化，返回 {@code null}。
     *
     * @return MySQL版本字符串，若不可用则返回 {@code null}
     */
    @Nullable String getMysqlVersion();

    void setMysqlVersion(@Nullable String mysqlVersion);

    /**
     * 获取 MySQL 会话 SQL 模式。
     * <p>
     * 该值来源于连接初始化时查询的 {@code @@session.sql_mode}。
     *
     * @return SQL 模式，若不可用则返回 {@code null}
     */
    @Nullable String getMysqlSqlMode();

    /**
     * 设置 MySQL 会话 SQL 模式。
     *
     * @param mysqlSqlMode SQL 模式
     */
    void setMysqlSqlMode(@Nullable String mysqlSqlMode);

    /**
     * 获取 MySQL 会话连接字符集。
     * <p>
     * 该值来源于连接初始化时查询的 {@code @@session.character_set_connection}。
     *
     * @return 连接字符集，若不可用则返回 {@code null}
     */
    @Nullable String getMysqlCharacterSetConnection();

    /**
     * 设置 MySQL 会话连接字符集。
     *
     * @param mysqlCharacterSetConnection 连接字符集
     */
    void setMysqlCharacterSetConnection(@Nullable String mysqlCharacterSetConnection);

    /**
     * 获取适用于当前 MySQL 会话的字符串字面量转义上下文。
     *
     * @return 转义上下文
     */
    default Quoter.EscapeContext getMysqlStringLiteralEscapeContext() {
        return new Quoter.EscapeContext(getMysqlCharacterSetConnection(), getMysqlSqlMode());
    }

    /**
     * 使用当前 MySQL 会话上下文构造字符串字面量引号处理器。
     *
     * @param value 字符串值
     * @return 引号处理器
     */
    default Quoter quoteMysqlString(@Nullable String value) {
        return new Quoter(value, getMysqlStringLiteralEscapeContext());
    }

    /**
     * 解析MySQL主版本号与次版本号。
     * <p>
     * MySQL 的 {@code VERSION()} 结果通常形如 {@code 8.0.35} 或
     * {@code 8.0.35-0ubuntu0.22.04.1}，本方法只提取前两段数字。
     *
     * @return MySQL主次版本号
     * @throws NullPointerException     当版本信息不可用时
     * @throws IllegalArgumentException 当版本字符串不包含可解析的主次版本号时
     */
    default MajorMinorVersion parseMajorMinor() {
        String version = Objects.requireNonNull(getMysqlVersion(), "MySQL version is not available").trim();
        int separator = version.indexOf('.');
        if (separator <= 0 || separator == version.length() - 1) {
            throw new IllegalArgumentException("MySQL version is not in major.minor format: " + version);
        }

        String majorPart = version.substring(0, separator);
        int minorStart = separator + 1;
        int minorEnd = minorStart;
        while (minorEnd < version.length() && Character.isDigit(version.charAt(minorEnd))) {
            minorEnd++;
        }
        if (minorEnd == minorStart) {
            throw new IllegalArgumentException("MySQL version is not in major.minor format: " + version);
        }

        try {
            return new MajorMinorVersion(
                    Integer.parseInt(majorPart),
                    Integer.parseInt(version.substring(minorStart, minorEnd))
            );
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("MySQL version is not in major.minor format: " + version, e);
        }
    }

    /**
     * 判断MySQL版本是否不低于指定主次版本。
     *
     * @param major 目标主版本号
     * @param minor 目标次版本号
     * @return 若当前版本大于或等于目标主次版本则返回 {@code true}
     * @throws NullPointerException     当版本信息不可用时
     * @throws IllegalArgumentException 当版本字符串不包含可解析的主次版本号时
     */
    default boolean isMySQLVersionAtLeast(int major, int minor) {
        MajorMinorVersion parsed = parseMajorMinor();
        return parsed.major() > major || (parsed.major() == major && parsed.minor() >= minor);
    }

    /**
     * 判断是否为MySQL 5.6.x版本。
     *
     * @return 是否为MySQL 5.6.x
     * @throws NullPointerException 当版本信息不可用时
     */
    default boolean isMySQLVersion5dot6() {
        MajorMinorVersion parsed = parseMajorMinor();
        return parsed.major() == 5 && parsed.minor() == 6;
    }

    /**
     * 判断是否为MySQL 5.7.x版本。
     *
     * @return 是否为MySQL 5.7.x
     * @throws NullPointerException 当版本信息不可用时
     */
    default boolean isMySQLVersion5dot7() {
        MajorMinorVersion parsed = parseMajorMinor();
        return parsed.major() == 5 && parsed.minor() == 7;
    }

    /**
     * 判断是否为MySQL 8.0.x版本。
     *
     * @return 是否为MySQL 8.0.x
     * @throws NullPointerException 当版本信息不可用时
     */
    default boolean isMySQLVersion8dot0() {
        MajorMinorVersion parsed = parseMajorMinor();
        return parsed.major() == 8 && parsed.minor() == 0;
    }

    /**
     * 判断是否为MySQL 8.2.x版本。
     *
     * @return 是否为MySQL 8.2.x
     * @throws NullPointerException 当版本信息不可用时
     */
    default boolean isMySQLVersion8dot2() {
        MajorMinorVersion parsed = parseMajorMinor();
        return parsed.major() == 8 && parsed.minor() == 2;
    }
}
