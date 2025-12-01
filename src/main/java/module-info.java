module io.github.sinri.keel.integration.mysql {
    requires io.github.sinri.keel.base;
    requires io.github.sinri.keel.core;
    requires io.github.sinri.keel.logger.api;
    requires io.vertx.core;
    requires io.vertx.sql.client;
    requires io.vertx.sql.client.mysql;
    requires org.jetbrains.annotations;

    // 默认导出所有包
    exports io.github.sinri.keel.integration.mysql;
    exports io.github.sinri.keel.integration.mysql.action;
    exports io.github.sinri.keel.integration.mysql.condition;
    exports io.github.sinri.keel.integration.mysql.dev;
    exports io.github.sinri.keel.integration.mysql.exception;
    // 注意：父包 result 无直接类型，导出其子包
    exports io.github.sinri.keel.integration.mysql.result.matrix;
    exports io.github.sinri.keel.integration.mysql.result.row;
    exports io.github.sinri.keel.integration.mysql.result.stream;
    exports io.github.sinri.keel.integration.mysql.statement;
    exports io.github.sinri.keel.integration.mysql.statement.component;
    exports io.github.sinri.keel.integration.mysql.statement.impl;
    // 注意：父包 impl.ddl 无直接类型，导出其子包
    exports io.github.sinri.keel.integration.mysql.statement.impl.ddl.table;
    exports io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.alter;
    exports io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.alter.column;
    exports io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.alter.index;
    exports io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.component;
    exports io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.create;
    exports io.github.sinri.keel.integration.mysql.statement.impl.ddl.view;
    exports io.github.sinri.keel.integration.mysql.statement.mixin;
    exports io.github.sinri.keel.integration.mysql.statement.templated;

    opens io.github.sinri.keel.integration.mysql.dev;
}