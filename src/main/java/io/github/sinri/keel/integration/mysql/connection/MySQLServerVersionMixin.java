package io.github.sinri.keel.integration.mysql.connection;

import org.jspecify.annotations.Nullable;

interface MySQLServerVersionMixin {
    /**
     * 获取MySQL版本信息
     *
     * @return MySQL版本，可能为null
     */
    @Nullable String getMysqlVersion();

    void setMysqlVersion(@Nullable String mysqlVersion);

    /**
     * 判断是否为MySQL 5.6.x版本
     *
     * @return 是否为MySQL 5.6.x
     */
    default boolean isMySQLVersion5dot6() {
        var mysqlVersion = getMysqlVersion();
        return mysqlVersion != null
                && mysqlVersion.startsWith("5.6.");
    }

    /**
     * 判断是否为MySQL 5.7.x版本
     *
     * @return 是否为MySQL 5.7.x
     */
    default boolean isMySQLVersion5dot7() {
        var mysqlVersion = getMysqlVersion();
        return mysqlVersion != null
                && mysqlVersion.startsWith("5.7.");
    }

    /**
     * 判断是否为MySQL 8.0.x版本
     *
     * @return 是否为MySQL 8.0.x
     */
    default boolean isMySQLVersion8dot0() {
        var mysqlVersion = getMysqlVersion();
        return mysqlVersion != null
                && mysqlVersion.startsWith("8.0.");
    }

    /**
     * 判断是否为MySQL 8.2.x版本
     *
     * @return 是否为MySQL 8.2.x
     */
    default boolean isMySQLVersion8dot2() {
        var mysqlVersion = getMysqlVersion();
        return mysqlVersion != null
                && mysqlVersion.startsWith("8.2.");
    }
}
