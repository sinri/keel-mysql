package io.github.sinri.keel.integration.mysql.connection;

import io.github.sinri.keel.integration.mysql.connection.target.*;
import io.github.sinri.keel.integration.mysql.statement.RawStatement;
import io.github.sinri.keel.integration.mysql.statement.impl.*;
import io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.TruncateTableStatement;
import io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.alter.AlterTableStatement;
import io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.create.CreateTableLikeTableStatement;
import io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.create.CreateTableStatement;
import io.github.sinri.keel.integration.mysql.statement.impl.ddl.view.AlterViewStatement;
import io.github.sinri.keel.integration.mysql.statement.impl.ddl.view.CreateViewStatement;
import io.github.sinri.keel.integration.mysql.statement.impl.ddl.view.DropViewStatement;
import io.github.sinri.keel.integration.mysql.statement.templated.TemplateArgumentMapping;
import io.github.sinri.keel.integration.mysql.statement.templated.TemplatedModifyStatement;
import io.github.sinri.keel.integration.mysql.statement.templated.TemplatedReadStatement;
import io.github.sinri.keel.integration.mysql.statement.templated.TemplatedStatement;
import io.vertx.core.Handler;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface RunnableStatementFactory extends SqlConnectionHolder {

    /**
     * 创建原始 SQL 语句
     *
     * @param sql 原始 SQL 语句
     * @return 可执行的 SQL 语句对象
     */
    default RunnableStatement rawForPreparedQuery(String sql) {
        return new RawStatement(sql, true)
                .attachToConnection(getSqlConnection());
    }

    /**
     * 创建原始 SQL 语句，支持指定是否使用预处理语句
     *
     * @param sql 原始 SQL 语句
     * @return 可执行的 SQL 语句对象
     */
    default RunnableStatement rawForDirectQuery(String sql) {
        return new RawStatement(sql, false)
                .attachToConnection(getSqlConnection());
    }

    /**
     * 创建SELECT语句
     *
     * @param statementHandler SELECT语句处理器
     * @return SELECT语句对象
     */
    default RunnableStatementForReadAndPagination pagination(Handler<SelectStatement> statementHandler) {
        SelectStatement selectStatement = new SelectStatement();
        statementHandler.handle(selectStatement);
        return selectStatement.attachToConnection(getSqlConnection());
    }

    /**
     * 创建SELECT语句
     *
     * @param statementHandler SELECT语句处理器
     * @return SELECT语句对象
     */
    default RunnableStatementForReadAndPagination select(Handler<SelectStatement> statementHandler) {
        SelectStatement selectStatement = new SelectStatement();
        statementHandler.handle(selectStatement);
        return selectStatement.attachToConnection(getSqlConnection());
    }

    /**
     * 创建UNION语句
     *
     * @param unionStatementHandler UNION语句处理器
     * @return UNION 语句对象
     */
    default RunnableStatementForRead union(Handler<UnionStatement> unionStatementHandler) {
        UnionStatement unionStatement = new UnionStatement();
        unionStatementHandler.handle(unionStatement);
        return unionStatement.attachToConnection(getSqlConnection());
    }

    /**
     * 创建UPDATE语句
     *
     * @param updateStatementHandler UPDATE语句处理器
     * @return UPDATE语句对象
     */
    default RunnableStatementForModify update(Handler<UpdateStatement> updateStatementHandler) {
        UpdateStatement updateStatement = new UpdateStatement();
        updateStatementHandler.handle(updateStatement);
        return updateStatement.attachToConnection(getSqlConnection());
    }

    /**
     * 创建DELETE语句
     *
     * @param deleteStatementHandler DELETE语句处理器
     * @return DELETE语句对象
     */
    default RunnableStatementForModify delete(Handler<DeleteStatement> deleteStatementHandler) {
        DeleteStatement deleteStatement = new DeleteStatement();
        deleteStatementHandler.handle(deleteStatement);
        return deleteStatement.attachToConnection(getSqlConnection());
    }

    /**
     * 创建INSERT语句
     *
     * @param statementHandler INSERT语句处理器
     * @return INSERT语句对象
     */
    default RunnableStatementForWrite insert(Handler<WriteIntoStatement> statementHandler) {
        WriteIntoStatement writeIntoStatement = new WriteIntoStatement(WriteIntoStatement.INSERT);
        statementHandler.handle(writeIntoStatement);
        return writeIntoStatement.attachToConnection(getSqlConnection());
    }

    /**
     * 创建REPLACE语句
     *
     * @param statementHandler REPLACE语句处理器
     * @return REPLACE语句对象
     */
    default RunnableStatementForWrite replace(Handler<WriteIntoStatement> statementHandler) {
        WriteIntoStatement writeIntoStatement = new WriteIntoStatement(WriteIntoStatement.REPLACE);
        statementHandler.handle(writeIntoStatement);
        return writeIntoStatement.attachToConnection(getSqlConnection());
    }

    /**
     * 创建CALL语句
     *
     * @param statementHandler CALL语句处理器
     * @return CALL语句对象
     */
    default RunnableStatement call(Handler<CallStatement> statementHandler) {
        CallStatement callStatement = new CallStatement();
        statementHandler.handle(callStatement);
        return callStatement.attachToConnection(getSqlConnection());
    }

    /**
     * 创建TRUNCATE TABLE语句
     *
     * @param statementHandler TRUNCATE TABLE语句处理器
     * @return TRUNCATE TABLE语句对象
     */
    default RunnableStatement truncateTable(Handler<TruncateTableStatement> statementHandler) {
        TruncateTableStatement truncateTableStatement = new TruncateTableStatement();
        statementHandler.handle(truncateTableStatement);
        return truncateTableStatement.attachToConnection(getSqlConnection());
    }

    /**
     * 创建CREATE TABLE语句
     *
     * @param statementHandler CREATE TABLE语句处理器
     * @return CREATE TABLE语句对象
     */
    default RunnableStatement createTable(Handler<CreateTableStatement> statementHandler) {
        CreateTableStatement createTableStatement = new CreateTableStatement();
        statementHandler.handle(createTableStatement);
        return createTableStatement.attachToConnection(getSqlConnection());
    }

    /**
     * 创建CREATE TABLE LIKE语句
     *
     * @param statementHandler CREATE TABLE LIKE语句处理器
     * @return CREATE TABLE LIKE语句对象
     */
    default RunnableStatement createTableLikeTable(Handler<CreateTableLikeTableStatement> statementHandler) {
        CreateTableLikeTableStatement createTableStatement = new CreateTableLikeTableStatement();
        statementHandler.handle(createTableStatement);
        return createTableStatement.attachToConnection(getSqlConnection());
    }

    /**
     * 创建ALTER TABLE语句
     *
     * @param statementHandler ALTER TABLE语句处理器
     * @return ALTER TABLE语句对象
     */
    default RunnableStatement alterTable(Handler<AlterTableStatement> statementHandler) {
        AlterTableStatement alterTableStatement = new AlterTableStatement();
        statementHandler.handle(alterTableStatement);
        return alterTableStatement.attachToConnection(getSqlConnection());
    }

    /**
     * 创建CREATE VIEW语句
     *
     * @param statementHandler CREATE VIEW语句处理器
     * @return CREATE VIEW语句对象
     */
    default RunnableStatement createView(Handler<CreateViewStatement> statementHandler) {
        CreateViewStatement createViewStatement = new CreateViewStatement();
        statementHandler.handle(createViewStatement);
        return createViewStatement.attachToConnection(getSqlConnection());
    }

    /**
     * 创建ALTER VIEW语句
     *
     * @param statementHandler ALTER VIEW语句处理器
     * @return ALTER VIEW语句对象
     */
    default RunnableStatement alterView(Handler<AlterViewStatement> statementHandler) {
        AlterViewStatement alterViewStatement = new AlterViewStatement();
        statementHandler.handle(alterViewStatement);
        return alterViewStatement.attachToConnection(getSqlConnection());
    }

    /**
     * 创建DROP VIEW语句
     *
     * @param statementHandler DROP VIEW语句处理器
     * @return DROP VIEW语句对象
     */
    default RunnableStatement dropView(Handler<DropViewStatement> statementHandler) {
        DropViewStatement dropViewStatement = new DropViewStatement();
        statementHandler.handle(dropViewStatement);
        return dropViewStatement.attachToConnection(getSqlConnection());
    }

    /**
     * 创建模板化读取语句
     *
     * @param path                          模板路径
     * @param templatedReadStatementHandler 模板参数映射处理器
     * @return 模板化读取语句对象
     */
    default RunnableStatementForRead templatedRead(String path, Handler<TemplateArgumentMapping> templatedReadStatementHandler) {
        TemplatedReadStatement readStatement = TemplatedStatement.loadTemplateToRead(path);
        TemplateArgumentMapping arguments = readStatement.getArguments();
        templatedReadStatementHandler.handle(arguments);
        return readStatement.attachToConnection(getSqlConnection());
    }

    /**
     * 创建模板化修改语句
     *
     * @param path                            模板路径
     * @param templatedModifyStatementHandler 模板参数映射处理器
     * @return 模板化修改语句对象
     */
    default RunnableStatementForModify templatedModify(String path, Handler<TemplateArgumentMapping> templatedModifyStatementHandler) {
        TemplatedModifyStatement templatedModifyStatement = TemplatedStatement.loadTemplateToModify(path);
        TemplateArgumentMapping arguments = templatedModifyStatement.getArguments();
        templatedModifyStatementHandler.handle(arguments);
        return templatedModifyStatement.attachToConnection(getSqlConnection());
    }

}
