package io.github.sinri.keel.integration.mysql.connection;

import java.util.Objects;

import org.jspecify.annotations.Nullable;

interface MySQLServerVersionMixin {
    /**
     * 获取MySQL版本信息。
     * <p>
     * 版本信息来源于数据源首次连接时执行的 {@code SELECT VERSION()}。
     * 若版本查询失败或连接尚未完成初始化，返回 {@code null}。
     * </p>
     *
     * @return MySQL版本字符串，若不可用则返回 {@code null}
     */
    @Nullable String getMysqlVersion();

    void setMysqlVersion(@Nullable String mysqlVersion);

    /**
     * 判断是否为MySQL 5.6.x版本。
     *
     * @return 是否为MySQL 5.6.x
     * @throws NullPointerException 当版本信息不可用时
     */
    default boolean isMySQLVersion5dot6() {
        return Objects.requireNonNull(getMysqlVersion(), "MySQL version is not available")
                .startsWith("5.6.");
    }

    /**
     * 判断是否为MySQL 5.7.x版本。
     *
     * @return 是否为MySQL 5.7.x
     * @throws NullPointerException 当版本信息不可用时
     */
    default boolean isMySQLVersion5dot7() {
        return Objects.requireNonNull(getMysqlVersion(), "MySQL version is not available")
                .startsWith("5.7.");
    }

    /**
     * 判断是否为MySQL 8.0.x版本。
     *
     * @return 是否为MySQL 8.0.x
     * @throws NullPointerException 当版本信息不可用时
     */
    default boolean isMySQLVersion8dot0() {
        return Objects.requireNonNull(getMysqlVersion(), "MySQL version is not available")
                .startsWith("8.0.");
    }

    /**
     * 判断是否为MySQL 8.2.x版本。
     *
     * @return 是否为MySQL 8.2.x
     * @throws NullPointerException 当版本信息不可用时
     */
    default boolean isMySQLVersion8dot2() {
        return Objects.requireNonNull(getMysqlVersion(), "MySQL version is not available")
                .startsWith("8.2.");
    }
}
