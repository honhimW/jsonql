package org.hibernate.dialect;

import org.hibernate.*;
import org.hibernate.boot.model.TypeContributions;
import org.hibernate.boot.model.relational.AuxiliaryDatabaseObject;
import org.hibernate.boot.model.relational.Sequence;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.dialect.identity.IdentityColumnSupport;
import org.hibernate.dialect.lock.LockingStrategy;
import org.hibernate.dialect.pagination.LimitHandler;
import org.hibernate.dialect.unique.UniqueDelegate;
import org.hibernate.engine.jdbc.env.spi.IdentifierHelper;
import org.hibernate.engine.jdbc.env.spi.IdentifierHelperBuilder;
import org.hibernate.engine.jdbc.env.spi.NameQualifierSupport;
import org.hibernate.engine.jdbc.env.spi.SchemaNameResolver;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.exception.spi.SQLExceptionConversionDelegate;
import org.hibernate.exception.spi.SQLExceptionConverter;
import org.hibernate.exception.spi.ViolatedConstraintNameExtracter;
import org.hibernate.hql.spi.id.MultiTableBulkIdStrategy;
import org.hibernate.loader.BatchLoadSizingStrategy;
import org.hibernate.mapping.Constraint;
import org.hibernate.mapping.ForeignKey;
import org.hibernate.mapping.Index;
import org.hibernate.mapping.Table;
import org.hibernate.persister.entity.Lockable;
import org.hibernate.procedure.spi.CallableStatementSupport;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.sql.CaseFragment;
import org.hibernate.sql.JoinFragment;
import org.hibernate.tool.schema.extract.spi.SequenceInformationExtractor;
import org.hibernate.tool.schema.spi.Exporter;
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor;

import java.sql.CallableStatement;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @author hon_him
 * @since 2024-06-28
 */

public class DialectDelegateWrapper extends Dialect {

    private final Dialect dialect;

    public DialectDelegateWrapper(Dialect dialect) {
        this.dialect = dialect;
    }

    @Override
    public boolean equivalentTypes(int typeCode1, int typeCode2) {
        return dialect.equivalentTypes(typeCode1, typeCode2);
    }

    @Deprecated
    public static Dialect getDialect() throws HibernateException {
        return Dialect.getDialect();
    }

    @Deprecated
    public static Dialect getDialect(Properties props) throws HibernateException {
        return Dialect.getDialect(props);
    }

    @Override
    public String toString() {
        return dialect.toString();
    }

    @Override
    public void contributeTypes(TypeContributions typeContributions, ServiceRegistry serviceRegistry) {
        dialect.contributeTypes(typeContributions, serviceRegistry);
    }

    @Override
    public String getTypeName(int code) throws HibernateException {
        return dialect.getTypeName(code);
    }

    @Override
    public String getTypeName(int code, long length, int precision, int scale) throws HibernateException {
        return dialect.getTypeName(code, length, precision, scale);
    }

    @Override
    public String getCastTypeName(int code) {
        return dialect.getCastTypeName(code);
    }

    @Override
    public String cast(String value, int jdbcTypeCode, int length, int precision, int scale) {
        return dialect.cast(value, jdbcTypeCode, length, precision, scale);
    }

    @Override
    public String cast(String value, int jdbcTypeCode, int length) {
        return dialect.cast(value, jdbcTypeCode, length);
    }

    @Override
    public String cast(String value, int jdbcTypeCode, int precision, int scale) {
        return dialect.cast(value, jdbcTypeCode, precision, scale);
    }

    @Override
    public void registerColumnType(int code, long capacity, String name) {
        dialect.registerColumnType(code, capacity, name);
    }

    @Override
    public void registerColumnType(int code, String name) {
        dialect.registerColumnType(code, name);
    }

    @Override
    public SqlTypeDescriptor remapSqlTypeDescriptor(SqlTypeDescriptor sqlTypeDescriptor) {
        return dialect.remapSqlTypeDescriptor(sqlTypeDescriptor);
    }

    @Override
    public SqlTypeDescriptor getSqlTypeDescriptorOverride(int sqlCode) {
        return dialect.getSqlTypeDescriptorOverride(sqlCode);
    }

    @Override
    public LobMergeStrategy getLobMergeStrategy() {
        return dialect.getLobMergeStrategy();
    }

    @Override
    public String getHibernateTypeName(int code) throws HibernateException {
        return dialect.getHibernateTypeName(code);
    }

    @Override
    public boolean isTypeNameRegistered(String typeName) {
        return dialect.isTypeNameRegistered(typeName);
    }

    @Override
    public String getHibernateTypeName(int code, int length, int precision, int scale) throws HibernateException {
        return dialect.getHibernateTypeName(code, length, precision, scale);
    }

    @Override
    public void registerHibernateType(int code, long capacity, String name) {
        dialect.registerHibernateType(code, capacity, name);
    }

    @Override
    public void registerHibernateType(int code, String name) {
        dialect.registerHibernateType(code, name);
    }

    @Override
    public void registerFunction(String name, SQLFunction function) {
        dialect.registerFunction(name, function);
    }

    @Override
    @Deprecated
    public Class getNativeIdentifierGeneratorClass() {
        return dialect.getNativeIdentifierGeneratorClass();
    }

    @Override
    public String getNativeIdentifierGeneratorStrategy() {
        return dialect.getNativeIdentifierGeneratorStrategy();
    }

    @Override
    public IdentityColumnSupport getIdentityColumnSupport() {
        return dialect.getIdentityColumnSupport();
    }

    @Override
    public boolean supportsSequences() {
        return dialect.supportsSequences();
    }

    @Override
    public boolean supportsPooledSequences() {
        return dialect.supportsPooledSequences();
    }

    @Override
    public String getSequenceNextValString(String sequenceName) throws MappingException {
        return dialect.getSequenceNextValString(sequenceName);
    }

    @Override
    public String getSelectSequenceNextValString(String sequenceName) throws MappingException {
        return dialect.getSelectSequenceNextValString(sequenceName);
    }

    @Override
    @Deprecated
    public String[] getCreateSequenceStrings(String sequenceName) throws MappingException {
        return dialect.getCreateSequenceStrings(sequenceName);
    }

    @Override
    public String[] getCreateSequenceStrings(String sequenceName, int initialValue, int incrementSize) throws MappingException {
        return dialect.getCreateSequenceStrings(sequenceName, initialValue, incrementSize);
    }

    @Override
    public String getCreateSequenceString(String sequenceName) throws MappingException {
        return dialect.getCreateSequenceString(sequenceName);
    }

    @Override
    public String getCreateSequenceString(String sequenceName, int initialValue, int incrementSize) throws MappingException {
        return dialect.getCreateSequenceString(sequenceName, initialValue, incrementSize);
    }

    @Override
    public String[] getDropSequenceStrings(String sequenceName) throws MappingException {
        return dialect.getDropSequenceStrings(sequenceName);
    }

    @Override
    public String getDropSequenceString(String sequenceName) throws MappingException {
        return dialect.getDropSequenceString(sequenceName);
    }

    @Override
    public String getQuerySequencesString() {
        return dialect.getQuerySequencesString();
    }

    @Override
    public SequenceInformationExtractor getSequenceInformationExtractor() {
        return dialect.getSequenceInformationExtractor();
    }

    @Override
    public String getSelectGUIDString() {
        return dialect.getSelectGUIDString();
    }

    @Override
    public LimitHandler getLimitHandler() {
        return dialect.getLimitHandler();
    }

    @Override
    @Deprecated
    public boolean supportsLimit() {
        return dialect.supportsLimit();
    }

    @Override
    @Deprecated
    public boolean supportsLimitOffset() {
        return dialect.supportsLimitOffset();
    }

    @Override
    @Deprecated
    public boolean supportsVariableLimit() {
        return dialect.supportsVariableLimit();
    }

    @Override
    @Deprecated
    public boolean bindLimitParametersInReverseOrder() {
        return dialect.bindLimitParametersInReverseOrder();
    }

    @Override
    @Deprecated
    public boolean bindLimitParametersFirst() {
        return dialect.bindLimitParametersFirst();
    }

    @Override
    @Deprecated
    public boolean useMaxForLimit() {
        return dialect.useMaxForLimit();
    }

    @Override
    @Deprecated
    public boolean forceLimitUsage() {
        return dialect.forceLimitUsage();
    }

    @Override
    @Deprecated
    public String getLimitString(String query, int offset, int limit) {
        return dialect.getLimitString(query, offset, limit);
    }

    @Override
    @Deprecated
    public String getLimitString(String query, boolean hasOffset) {
        return dialect.getLimitString(query, hasOffset);
    }

    @Override
    @Deprecated
    public int convertToFirstRowValue(int zeroBasedFirstResult) {
        return dialect.convertToFirstRowValue(zeroBasedFirstResult);
    }

    @Override
    public boolean supportsLockTimeouts() {
        return dialect.supportsLockTimeouts();
    }

    @Override
    public boolean isLockTimeoutParameterized() {
        return dialect.isLockTimeoutParameterized();
    }

    @Override
    public LockingStrategy getLockingStrategy(Lockable lockable, LockMode lockMode) {
        return dialect.getLockingStrategy(lockable, lockMode);
    }

    @Override
    public String getForUpdateString(LockOptions lockOptions) {
        return dialect.getForUpdateString(lockOptions);
    }

    @Override
    public String getForUpdateString(LockMode lockMode) {
        return dialect.getForUpdateString(lockMode);
    }

    @Override
    public String getForUpdateString() {
        return dialect.getForUpdateString();
    }

    @Override
    public String getWriteLockString(int timeout) {
        return dialect.getWriteLockString(timeout);
    }

    @Override
    public String getWriteLockString(String aliases, int timeout) {
        return dialect.getWriteLockString(aliases, timeout);
    }

    @Override
    public String getReadLockString(int timeout) {
        return dialect.getReadLockString(timeout);
    }

    @Override
    public String getReadLockString(String aliases, int timeout) {
        return dialect.getReadLockString(aliases, timeout);
    }

    @Override
    public boolean forUpdateOfColumns() {
        return dialect.forUpdateOfColumns();
    }

    @Override
    public boolean supportsOuterJoinForUpdate() {
        return dialect.supportsOuterJoinForUpdate();
    }

    @Override
    public String getForUpdateString(String aliases) {
        return dialect.getForUpdateString(aliases);
    }

    @Override
    public String getForUpdateString(String aliases, LockOptions lockOptions) {
        return dialect.getForUpdateString(aliases, lockOptions);
    }

    @Override
    public String getForUpdateNowaitString() {
        return dialect.getForUpdateNowaitString();
    }

    @Override
    public String getForUpdateSkipLockedString() {
        return dialect.getForUpdateSkipLockedString();
    }

    @Override
    public String getForUpdateNowaitString(String aliases) {
        return dialect.getForUpdateNowaitString(aliases);
    }

    @Override
    public String getForUpdateSkipLockedString(String aliases) {
        return dialect.getForUpdateSkipLockedString(aliases);
    }

    @Override
    @Deprecated
    public String appendLockHint(LockMode mode, String tableName) {
        return dialect.appendLockHint(mode, tableName);
    }

    @Override
    public String appendLockHint(LockOptions lockOptions, String tableName) {
        return dialect.appendLockHint(lockOptions, tableName);
    }

    @Override
    public String applyLocksToSql(String sql, LockOptions aliasedLockOptions, Map<String, String[]> keyColumnNames) {
        return dialect.applyLocksToSql(sql, aliasedLockOptions, keyColumnNames);
    }

    @Override
    public String getCreateTableString() {
        return dialect.getCreateTableString();
    }

    @Override
    public String getAlterTableString(String tableName) {
        return dialect.getAlterTableString(tableName);
    }

    @Override
    public String getCreateMultisetTableString() {
        return dialect.getCreateMultisetTableString();
    }

    @Override
    public MultiTableBulkIdStrategy getDefaultMultiTableBulkIdStrategy() {
        return dialect.getDefaultMultiTableBulkIdStrategy();
    }

    @Override
    public int registerResultSetOutParameter(CallableStatement statement, int position) throws SQLException {
        return dialect.registerResultSetOutParameter(statement, position);
    }

    @Override
    public int registerResultSetOutParameter(CallableStatement statement, String name) throws SQLException {
        return dialect.registerResultSetOutParameter(statement, name);
    }

    @Override
    public ResultSet getResultSet(CallableStatement statement) throws SQLException {
        return dialect.getResultSet(statement);
    }

    @Override
    public ResultSet getResultSet(CallableStatement statement, int position) throws SQLException {
        return dialect.getResultSet(statement, position);
    }

    @Override
    public ResultSet getResultSet(CallableStatement statement, String name) throws SQLException {
        return dialect.getResultSet(statement, name);
    }

    @Override
    public boolean supportsCurrentTimestampSelection() {
        return dialect.supportsCurrentTimestampSelection();
    }

    @Override
    public boolean isCurrentTimestampSelectStringCallable() {
        return dialect.isCurrentTimestampSelectStringCallable();
    }

    @Override
    public String getCurrentTimestampSelectString() {
        return dialect.getCurrentTimestampSelectString();
    }

    @Override
    public String getCurrentTimestampSQLFunctionName() {
        return dialect.getCurrentTimestampSQLFunctionName();
    }

    @Override
    @Deprecated
    public SQLExceptionConverter buildSQLExceptionConverter() {
        return dialect.buildSQLExceptionConverter();
    }

    @Override
    public SQLExceptionConversionDelegate buildSQLExceptionConversionDelegate() {
        return dialect.buildSQLExceptionConversionDelegate();
    }

    @Override
    public ViolatedConstraintNameExtracter getViolatedConstraintNameExtracter() {
        return dialect.getViolatedConstraintNameExtracter();
    }

    @Override
    public String getSelectClauseNullString(int sqlType) {
        return dialect.getSelectClauseNullString(sqlType);
    }

    @Override
    public boolean supportsUnionAll() {
        return dialect.supportsUnionAll();
    }

    @Override
    public JoinFragment createOuterJoinFragment() {
        return dialect.createOuterJoinFragment();
    }

    @Override
    public CaseFragment createCaseFragment() {
        return dialect.createCaseFragment();
    }

    @Override
    public String getNoColumnsInsertString() {
        return dialect.getNoColumnsInsertString();
    }

    @Override
    public boolean supportsNoColumnsInsert() {
        return dialect.supportsNoColumnsInsert();
    }

    @Override
    public String getLowercaseFunction() {
        return dialect.getLowercaseFunction();
    }

    @Override
    public String getCaseInsensitiveLike() {
        return dialect.getCaseInsensitiveLike();
    }

    @Override
    public boolean supportsCaseInsensitiveLike() {
        return dialect.supportsCaseInsensitiveLike();
    }

    @Override
    public String transformSelectString(String select) {
        return dialect.transformSelectString(select);
    }

    @Override
    public int getMaxAliasLength() {
        return dialect.getMaxAliasLength();
    }

    @Override
    public String toBooleanValueString(boolean bool) {
        return dialect.toBooleanValueString(bool);
    }

    @Override
    public void registerKeyword(String word) {
        dialect.registerKeyword(word);
    }

    @Override
    @Deprecated
    public Set<String> getKeywords() {
        return dialect.getKeywords();
    }

    @Override
    public IdentifierHelper buildIdentifierHelper(IdentifierHelperBuilder builder, DatabaseMetaData dbMetaData) throws SQLException {
        return dialect.buildIdentifierHelper(builder, dbMetaData);
    }

    @Override
    public char openQuote() {
        return dialect.openQuote();
    }

    @Override
    public char closeQuote() {
        return dialect.closeQuote();
    }

    @Override
    public Exporter<Table> getTableExporter() {
        return dialect.getTableExporter();
    }

    @Override
    public Exporter<Sequence> getSequenceExporter() {
        return dialect.getSequenceExporter();
    }

    @Override
    public Exporter<Index> getIndexExporter() {
        return dialect.getIndexExporter();
    }

    @Override
    public Exporter<ForeignKey> getForeignKeyExporter() {
        return dialect.getForeignKeyExporter();
    }

    @Override
    public Exporter<Constraint> getUniqueKeyExporter() {
        return dialect.getUniqueKeyExporter();
    }

    @Override
    public Exporter<AuxiliaryDatabaseObject> getAuxiliaryDatabaseObjectExporter() {
        return dialect.getAuxiliaryDatabaseObjectExporter();
    }

    @Override
    public boolean canCreateCatalog() {
        return dialect.canCreateCatalog();
    }

    @Override
    public String[] getCreateCatalogCommand(String catalogName) {
        return dialect.getCreateCatalogCommand(catalogName);
    }

    @Override
    public String[] getDropCatalogCommand(String catalogName) {
        return dialect.getDropCatalogCommand(catalogName);
    }

    @Override
    public boolean canCreateSchema() {
        return dialect.canCreateSchema();
    }

    @Override
    public String[] getCreateSchemaCommand(String schemaName) {
        return dialect.getCreateSchemaCommand(schemaName);
    }

    @Override
    public String[] getDropSchemaCommand(String schemaName) {
        return dialect.getDropSchemaCommand(schemaName);
    }

    @Override
    public String getCurrentSchemaCommand() {
        return dialect.getCurrentSchemaCommand();
    }

    @Override
    public SchemaNameResolver getSchemaNameResolver() {
        return dialect.getSchemaNameResolver();
    }

    @Override
    public boolean hasAlterTable() {
        return dialect.hasAlterTable();
    }

    @Override
    public boolean dropConstraints() {
        return dialect.dropConstraints();
    }

    @Override
    public boolean qualifyIndexName() {
        return dialect.qualifyIndexName();
    }

    @Override
    public String getAddColumnString() {
        return dialect.getAddColumnString();
    }

    @Override
    public String getAddColumnSuffixString() {
        return dialect.getAddColumnSuffixString();
    }

    @Override
    public String getDropForeignKeyString() {
        return dialect.getDropForeignKeyString();
    }

    @Override
    public String getTableTypeString() {
        return dialect.getTableTypeString();
    }

    @Override
    public String getAddForeignKeyConstraintString(String constraintName, String[] foreignKey, String referencedTable, String[] primaryKey, boolean referencesPrimaryKey) {
        return dialect.getAddForeignKeyConstraintString(constraintName, foreignKey, referencedTable, primaryKey, referencesPrimaryKey);
    }

    @Override
    public String getAddForeignKeyConstraintString(String constraintName, String foreignKeyDefinition) {
        return dialect.getAddForeignKeyConstraintString(constraintName, foreignKeyDefinition);
    }

    @Override
    public String getAddPrimaryKeyConstraintString(String constraintName) {
        return dialect.getAddPrimaryKeyConstraintString(constraintName);
    }

    @Override
    public boolean hasSelfReferentialForeignKeyBug() {
        return dialect.hasSelfReferentialForeignKeyBug();
    }

    @Override
    public String getNullColumnString() {
        return dialect.getNullColumnString();
    }

    @Override
    public boolean supportsCommentOn() {
        return dialect.supportsCommentOn();
    }

    @Override
    public String getTableComment(String comment) {
        return dialect.getTableComment(comment);
    }

    @Override
    public String getColumnComment(String comment) {
        return dialect.getColumnComment(comment);
    }

    @Override
    public boolean supportsIfExistsBeforeTableName() {
        return dialect.supportsIfExistsBeforeTableName();
    }

    @Override
    public boolean supportsIfExistsAfterTableName() {
        return dialect.supportsIfExistsAfterTableName();
    }

    @Override
    public boolean supportsIfExistsBeforeConstraintName() {
        return dialect.supportsIfExistsBeforeConstraintName();
    }

    @Override
    public boolean supportsIfExistsAfterConstraintName() {
        return dialect.supportsIfExistsAfterConstraintName();
    }

    @Override
    public boolean supportsIfExistsAfterAlterTable() {
        return dialect.supportsIfExistsAfterAlterTable();
    }

    @Override
    public String getDropTableString(String tableName) {
        return dialect.getDropTableString(tableName);
    }

    @Override
    public boolean supportsColumnCheck() {
        return dialect.supportsColumnCheck();
    }

    @Override
    public boolean supportsTableCheck() {
        return dialect.supportsTableCheck();
    }

    @Override
    public boolean supportsCascadeDelete() {
        return dialect.supportsCascadeDelete();
    }

    @Override
    public String getCascadeConstraintsString() {
        return dialect.getCascadeConstraintsString();
    }

    @Override
    public String getCrossJoinSeparator() {
        return dialect.getCrossJoinSeparator();
    }

    @Override
    public ColumnAliasExtractor getColumnAliasExtractor() {
        return dialect.getColumnAliasExtractor();
    }

    @Override
    public boolean supportsEmptyInList() {
        return dialect.supportsEmptyInList();
    }

    @Override
    public boolean areStringComparisonsCaseInsensitive() {
        return dialect.areStringComparisonsCaseInsensitive();
    }

    @Override
    public boolean supportsRowValueConstructorSyntax() {
        return dialect.supportsRowValueConstructorSyntax();
    }

    @Override
    public boolean supportsRowValueConstructorSyntaxInSet() {
        return dialect.supportsRowValueConstructorSyntaxInSet();
    }

    @Override
    public boolean supportsRowValueConstructorSyntaxInInList() {
        return dialect.supportsRowValueConstructorSyntaxInInList();
    }

    @Override
    public boolean useInputStreamToInsertBlob() {
        return dialect.useInputStreamToInsertBlob();
    }

    @Override
    public boolean supportsParametersInInsertSelect() {
        return dialect.supportsParametersInInsertSelect();
    }

    @Override
    public boolean replaceResultVariableInOrderByClauseWithPosition() {
        return dialect.replaceResultVariableInOrderByClauseWithPosition();
    }

    @Override
    public String renderOrderByElement(String expression, String collation, String order, NullPrecedence nulls) {
        return dialect.renderOrderByElement(expression, collation, order, nulls);
    }

    @Override
    public boolean requiresCastingOfParametersInSelectClause() {
        return dialect.requiresCastingOfParametersInSelectClause();
    }

    @Override
    public boolean supportsResultSetPositionQueryMethodsOnForwardOnlyCursor() {
        return dialect.supportsResultSetPositionQueryMethodsOnForwardOnlyCursor();
    }

    @Override
    public boolean supportsCircularCascadeDeleteConstraints() {
        return dialect.supportsCircularCascadeDeleteConstraints();
    }

    @Override
    public boolean supportsSubselectAsInPredicateLHS() {
        return dialect.supportsSubselectAsInPredicateLHS();
    }

    @Override
    public boolean supportsExpectedLobUsagePattern() {
        return dialect.supportsExpectedLobUsagePattern();
    }

    @Override
    public boolean supportsLobValueChangePropogation() {
        return dialect.supportsLobValueChangePropogation();
    }

    @Override
    public boolean supportsUnboundedLobLocatorMaterialization() {
        return dialect.supportsUnboundedLobLocatorMaterialization();
    }

    @Override
    public boolean supportsSubqueryOnMutatingTable() {
        return dialect.supportsSubqueryOnMutatingTable();
    }

    @Override
    public boolean supportsExistsInSelect() {
        return dialect.supportsExistsInSelect();
    }

    @Override
    public boolean doesReadCommittedCauseWritersToBlockReaders() {
        return dialect.doesReadCommittedCauseWritersToBlockReaders();
    }

    @Override
    public boolean doesRepeatableReadCauseReadersToBlockWriters() {
        return dialect.doesRepeatableReadCauseReadersToBlockWriters();
    }

    @Override
    public boolean supportsBindAsCallableArgument() {
        return dialect.supportsBindAsCallableArgument();
    }

    @Override
    public boolean supportsTupleCounts() {
        return dialect.supportsTupleCounts();
    }

    @Override
    public boolean supportsTupleDistinctCounts() {
        return dialect.supportsTupleDistinctCounts();
    }

    @Override
    public boolean requiresParensForTupleDistinctCounts() {
        return dialect.requiresParensForTupleDistinctCounts();
    }

    @Override
    public int getInExpressionCountLimit() {
        return dialect.getInExpressionCountLimit();
    }

    @Override
    public boolean forceLobAsLastValue() {
        return dialect.forceLobAsLastValue();
    }

    @Override
    @Deprecated
    public boolean useFollowOnLocking() {
        return dialect.useFollowOnLocking();
    }

    @Override
    public boolean useFollowOnLocking(QueryParameters parameters) {
        return dialect.useFollowOnLocking(parameters);
    }

    @Override
    public String getNotExpression(String expression) {
        return dialect.getNotExpression(expression);
    }

    @Override
    public UniqueDelegate getUniqueDelegate() {
        return dialect.getUniqueDelegate();
    }

    @Override
    @Deprecated
    public boolean supportsUnique() {
        return dialect.supportsUnique();
    }

    @Override
    @Deprecated
    public boolean supportsUniqueConstraintInCreateAlterTable() {
        return dialect.supportsUniqueConstraintInCreateAlterTable();
    }

    @Override
    @Deprecated
    public String getAddUniqueConstraintString(String constraintName) {
        return dialect.getAddUniqueConstraintString(constraintName);
    }

    @Override
    @Deprecated
    public boolean supportsNotNullUnique() {
        return dialect.supportsNotNullUnique();
    }

    @Override
    public String getQueryHintString(String query, List<String> hintList) {
        return dialect.getQueryHintString(query, hintList);
    }

    @Override
    public String getQueryHintString(String query, String hints) {
        return dialect.getQueryHintString(query, hints);
    }

    @Override
    public ScrollMode defaultScrollMode() {
        return dialect.defaultScrollMode();
    }

    @Override
    public boolean supportsTuplesInSubqueries() {
        return dialect.supportsTuplesInSubqueries();
    }

    @Override
    public CallableStatementSupport getCallableStatementSupport() {
        return dialect.getCallableStatementSupport();
    }

    @Override
    public NameQualifierSupport getNameQualifierSupport() {
        return dialect.getNameQualifierSupport();
    }

    @Override
    public BatchLoadSizingStrategy getDefaultBatchLoadSizingStrategy() {
        return dialect.getDefaultBatchLoadSizingStrategy();
    }

    @Override
    public boolean isJdbcLogWarningsEnabledByDefault() {
        return dialect.isJdbcLogWarningsEnabledByDefault();
    }

    @Override
    public void augmentPhysicalTableTypes(List<String> tableTypesList) {
        dialect.augmentPhysicalTableTypes(tableTypesList);
    }

    @Override
    public void augmentRecognizedTableTypes(List<String> tableTypesList) {
        dialect.augmentRecognizedTableTypes(tableTypesList);
    }

    @Override
    public boolean supportsPartitionBy() {
        return dialect.supportsPartitionBy();
    }

    @Override
    public boolean supportsNamedParameters(DatabaseMetaData databaseMetaData) throws SQLException {
        return dialect.supportsNamedParameters(databaseMetaData);
    }

    @Override
    public boolean supportsNationalizedTypes() {
        return dialect.supportsNationalizedTypes();
    }

    @Override
    public boolean supportsNonQueryWithCTE() {
        return dialect.supportsNonQueryWithCTE();
    }

    @Override
    public boolean supportsValuesList() {
        return dialect.supportsValuesList();
    }

    @Override
    public boolean supportsSkipLocked() {
        return dialect.supportsSkipLocked();
    }

    @Override
    public boolean supportsNoWait() {
        return dialect.supportsNoWait();
    }

    @Override
    public boolean isLegacyLimitHandlerBehaviorEnabled() {
        return dialect.isLegacyLimitHandlerBehaviorEnabled();
    }

    @Override
    public String inlineLiteral(String literal) {
        return dialect.inlineLiteral(literal);
    }

    @Override
    public boolean supportsJdbcConnectionLobCreation(DatabaseMetaData databaseMetaData) {
        return dialect.supportsJdbcConnectionLobCreation(databaseMetaData);
    }

    @Override
    public String escapeLiteral(String literal) {
        return dialect.escapeLiteral(literal);
    }

    @Override
    public String addSqlHintOrComment(String sql, QueryParameters parameters, boolean commentsEnabled) {
        return dialect.addSqlHintOrComment(sql, parameters, commentsEnabled);
    }

    @Override
    public String prependComment(String sql, String comment) {
        return dialect.prependComment(sql, comment);
    }

    public static String escapeComment(String comment) {
        return Dialect.escapeComment(comment);
    }

    @Override
    public boolean supportsSelectAliasInGroupByClause() {
        return dialect.supportsSelectAliasInGroupByClause();
    }

    @Override
    public String getCreateTemporaryTableColumnAnnotation(int sqlTypeCode) {
        return dialect.getCreateTemporaryTableColumnAnnotation(sqlTypeCode);
    }
}
