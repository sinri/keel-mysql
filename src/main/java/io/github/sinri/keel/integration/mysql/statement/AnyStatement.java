package io.github.sinri.keel.integration.mysql.statement;

import io.github.sinri.keel.integration.mysql.connection.NamedMySQLConnection;
import io.github.sinri.keel.integration.mysql.result.matrix.ResultMatrix;
import io.github.sinri.keel.integration.mysql.statement.impl.*;
import io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.TruncateTableStatement;
import io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.alter.AlterTableStatement;
import io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.create.CreateTableLikeTableStatement;
import io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.create.CreateTableStatement;
import io.github.sinri.keel.integration.mysql.statement.impl.ddl.view.AlterViewStatement;
import io.github.sinri.keel.integration.mysql.statement.impl.ddl.view.CreateViewStatement;
import io.github.sinri.keel.integration.mysql.statement.impl.ddl.view.DropViewStatement;
import io.github.sinri.keel.integration.mysql.statement.mixin.ModifyStatementMixin;
import io.github.sinri.keel.integration.mysql.statement.mixin.ReadStatementMixin;
import io.github.sinri.keel.integration.mysql.statement.mixin.SelectStatementMixin;
import io.github.sinri.keel.integration.mysql.statement.mixin.WriteIntoStatementMixin;
import io.github.sinri.keel.integration.mysql.statement.templated.TemplateArgumentMapping;
import io.github.sinri.keel.integration.mysql.statement.templated.TemplatedModifyStatement;
import io.github.sinri.keel.integration.mysql.statement.templated.TemplatedReadStatement;
import io.github.sinri.keel.integration.mysql.statement.templated.TemplatedStatement;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import org.jspecify.annotations.NullMarked;


/**
 * SQL语句接口，定义了所有SQL语句的通用行为
 *
 * @since 5.0.0
 */
@NullMarked
public interface AnyStatement {

    /**
     * 创建原始SQL语句
     *
     * @param sql 原始SQL语句
     * @return SQL语句对象
     */
    static AbstractStatement raw(String sql) {
        return raw(sql, false);
    }


    /**
     * 创建原始SQL语句，支持指定是否使用预处理语句
     *
     * @param sql            原始SQL语句
     * @param withoutPrepare 是否不使用预处理语句
     * @return SQL语句对象
     */
    static AbstractStatement raw(String sql, boolean withoutPrepare) {
        return new AbstractStatement() {
            @Override
            public String toString() {
                return sql;
            }

            @Override
            public boolean isWithoutPrepare() {
                return withoutPrepare;
            }
        };
    }

    /**
     * 创建SELECT语句
     *
     * @param statementHandler SELECT语句处理器
     * @return SELECT语句对象
     */
    static SelectStatementMixin select(Handler<SelectStatement> statementHandler) {
        SelectStatement selectStatement = new SelectStatement();
        statementHandler.handle(selectStatement);
        return selectStatement;
    }

    /**
     * 创建UNION语句
     *
     * @param unionStatementHandler UNION语句处理器
     * @return UNION语句对象
     */
    static ReadStatementMixin union(Handler<UnionStatement> unionStatementHandler) {
        UnionStatement unionStatement = new UnionStatement();
        unionStatementHandler.handle(unionStatement);
        return unionStatement;
    }

    /**
     * 创建UPDATE语句
     *
     * @param updateStatementHandler UPDATE语句处理器
     * @return UPDATE语句对象
     */
    static ModifyStatementMixin update(Handler<UpdateStatement> updateStatementHandler) {
        UpdateStatement updateStatement = new UpdateStatement();
        updateStatementHandler.handle(updateStatement);
        return updateStatement;
    }

    /**
     * 创建DELETE语句
     *
     * @param deleteStatementHandler DELETE语句处理器
     * @return DELETE语句对象
     */
    static ModifyStatementMixin delete(Handler<DeleteStatement> deleteStatementHandler) {
        DeleteStatement deleteStatement = new DeleteStatement();
        deleteStatementHandler.handle(deleteStatement);
        return deleteStatement;
    }

    /**
     * 创建INSERT语句
     *
     * @param statementHandler INSERT语句处理器
     * @return INSERT语句对象
     */
    static WriteIntoStatementMixin insert(Handler<WriteIntoStatement> statementHandler) {
        WriteIntoStatement writeIntoStatement = new WriteIntoStatement(WriteIntoStatement.INSERT);
        statementHandler.handle(writeIntoStatement);
        return writeIntoStatement;
    }

    /**
     * 创建REPLACE语句
     *
     * @param statementHandler REPLACE语句处理器
     * @return REPLACE语句对象
     */
    static WriteIntoStatementMixin replace(Handler<WriteIntoStatement> statementHandler) {
        WriteIntoStatement writeIntoStatement = new WriteIntoStatement(WriteIntoStatement.REPLACE);
        statementHandler.handle(writeIntoStatement);
        return writeIntoStatement;
    }

    /**
     * 创建CALL语句
     *
     * @param statementHandler CALL语句处理器
     * @return CALL语句对象
     */
    static AbstractStatement call(Handler<CallStatement> statementHandler) {
        CallStatement callStatement = new CallStatement();
        statementHandler.handle(callStatement);
        return callStatement;
    }

    /**
     * 创建TRUNCATE TABLE语句
     *
     * @param statementHandler TRUNCATE TABLE语句处理器
     * @return TRUNCATE TABLE语句对象
     */
    static AbstractStatement truncateTable(Handler<TruncateTableStatement> statementHandler) {
        TruncateTableStatement truncateTableStatement = new TruncateTableStatement();
        statementHandler.handle(truncateTableStatement);
        return truncateTableStatement;
    }

    /**
     * 创建CREATE TABLE语句
     *
     * @param statementHandler CREATE TABLE语句处理器
     * @return CREATE TABLE语句对象
     */
    static AbstractStatement createTable(Handler<CreateTableStatement> statementHandler) {
        CreateTableStatement createTableStatement = new CreateTableStatement();
        statementHandler.handle(createTableStatement);
        return createTableStatement;
    }

    /**
     * 创建CREATE TABLE LIKE语句
     *
     * @param statementHandler CREATE TABLE LIKE语句处理器
     * @return CREATE TABLE LIKE语句对象
     */
    static AbstractStatement createTableLikeTable(Handler<CreateTableLikeTableStatement> statementHandler) {
        CreateTableLikeTableStatement createTableStatement = new CreateTableLikeTableStatement();
        statementHandler.handle(createTableStatement);
        return createTableStatement;
    }

    /**
     * 创建ALTER TABLE语句
     *
     * @param statementHandler ALTER TABLE语句处理器
     * @return ALTER TABLE语句对象
     */
    static AbstractStatement alterTable(Handler<AlterTableStatement> statementHandler) {
        AlterTableStatement alterTableStatement = new AlterTableStatement();
        statementHandler.handle(alterTableStatement);
        return alterTableStatement;
    }

    /**
     * 创建CREATE VIEW语句
     *
     * @param statementHandler CREATE VIEW语句处理器
     * @return CREATE VIEW语句对象
     */
    static AbstractStatement createView(Handler<CreateViewStatement> statementHandler) {
        CreateViewStatement createViewStatement = new CreateViewStatement();
        statementHandler.handle(createViewStatement);
        return createViewStatement;
    }

    /**
     * 创建ALTER VIEW语句
     *
     * @param statementHandler ALTER VIEW语句处理器
     * @return ALTER VIEW语句对象
     */
    static AbstractStatement alterView(Handler<AlterViewStatement> statementHandler) {
        AlterViewStatement alterViewStatement = new AlterViewStatement();
        statementHandler.handle(alterViewStatement);
        return alterViewStatement;
    }

    /**
     * 创建DROP VIEW语句
     *
     * @param statementHandler DROP VIEW语句处理器
     * @return DROP VIEW语句对象
     */
    static AbstractStatement dropView(Handler<DropViewStatement> statementHandler) {
        DropViewStatement dropViewStatement = new DropViewStatement();
        statementHandler.handle(dropViewStatement);
        return dropViewStatement;
    }

    /**
     * 创建模板化读取语句
     *
     * @param path                          模板路径
     * @param templatedReadStatementHandler 模板参数映射处理器
     * @return 模板化读取语句对象
     */
    static ReadStatementMixin templatedRead(String path, Handler<TemplateArgumentMapping> templatedReadStatementHandler) {
        TemplatedReadStatement readStatement = TemplatedStatement.loadTemplateToRead(path);
        TemplateArgumentMapping arguments = readStatement.getArguments();
        templatedReadStatementHandler.handle(arguments);
        return readStatement;
    }

    /**
     * 创建模板化修改语句
     *
     * @param path                            模板路径
     * @param templatedModifyStatementHandler 模板参数映射处理器
     * @return 模板化修改语句对象
     */
    static ModifyStatementMixin templatedModify(String path, Handler<TemplateArgumentMapping> templatedModifyStatementHandler) {
        TemplatedModifyStatement templatedModifyStatement = TemplatedStatement.loadTemplateToModify(path);
        TemplateArgumentMapping arguments = templatedModifyStatement.getArguments();
        templatedModifyStatementHandler.handle(arguments);
        return templatedModifyStatement;
    }

    /**
     * 生成SQL字符串
     *
     * @return 生成的SQL语句
     */
    String toString();

    /**
     * 在指定的MySQL连接上执行SQL语句
     *
     * @param namedSqlConnection MySQL命名连接
     * @return 执行结果的Future
     */
    Future<ResultMatrix> execute(NamedMySQLConnection namedSqlConnection);

    /**
     * 判断是否不使用预处理语句
     *
     * @return 是否不使用预处理语句
     */
    boolean isWithoutPrepare();
}
