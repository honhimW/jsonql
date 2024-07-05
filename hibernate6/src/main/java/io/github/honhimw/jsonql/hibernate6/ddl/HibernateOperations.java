package io.github.honhimw.jsonql.hibernate6.ddl;

import io.github.honhimw.jsonql.hibernate6.MetadataExtractorIntegrator;
import io.github.honhimw.jsonql.hibernate6.supports.JsonQLContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.ImplicitUniqueKeyNameSource;
import org.hibernate.boot.model.relational.QualifiedTableName;
import org.hibernate.boot.model.relational.SqlStringGenerationContext;
import org.hibernate.boot.model.relational.internal.SqlStringGenerationContextImpl;
import org.hibernate.boot.spi.MetadataBuildingContext;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Index;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.UniqueKey;
import org.hibernate.tool.schema.internal.StandardIndexExporter;
import org.hibernate.tool.schema.internal.StandardTableExporter;
import org.hibernate.tool.schema.internal.StandardUniqueKeyExporter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static java.util.Collections.singletonList;

/**
 * @author hon_him
 * @since 2023-11-20
 */

@Slf4j
public class HibernateOperations {

    private final JsonQLContext jsonQLContext;
    private final Table table;
    private final MetadataExtractorIntegrator integrator;
    private final Metadata metadata;
    private final JdbcEnvironment jdbcEnvironment;
    private final Dialect dialect;

    private final StandardTableExporter standardTableExporter;
    private final StandardUniqueKeyExporter standardUniqueKeyExporter;
    private final StandardIndexExporter standardIndexExporter;
    private final SqlStringGenerationContext generationContext;

    private HibernateOperations(JsonQLContext jsonQLContext, Table table, Dialect dialect) {
        this.jsonQLContext = jsonQLContext;
        this.table = table;
        this.integrator = jsonQLContext.getIntegrator();
        this.metadata = jsonQLContext.getIntegrator().getMetadata();
        this.jdbcEnvironment = jsonQLContext.getIntegrator().getJdbcEnvironment();
        if (Objects.nonNull(dialect)) {
            this.dialect = dialect;
        } else {
            this.dialect = jdbcEnvironment.getDialect();
        }
        this.standardTableExporter = new StandardTableExporter(this.dialect);
        this.standardUniqueKeyExporter = new StandardUniqueKeyExporter(this.dialect);
        this.standardIndexExporter = new StandardIndexExporter(this.dialect);
        generationContext = SqlStringGenerationContextImpl.forTests(jdbcEnvironment);
    }

    public static HibernateOperations forTable(JsonQLContext context, Table table) {
        return forTable(context, table, null);
    }

    public static HibernateOperations forTable(JsonQLContext context, Table table, Dialect dialect) {
        return new HibernateOperations(context, table, dialect);
    }

    public List<String> createTable() {
        List<String> sqls = new ArrayList<>();
        String[] sqlCreateStrings = standardTableExporter.getSqlCreateStrings(table, metadata, generationContext);
        CollectionUtils.addAll(sqls, sqlCreateStrings);
        table.getUniqueKeys().forEach((s, uniqueKey) -> {
            String[] uniqueKeyCreateStrings = standardUniqueKeyExporter.getSqlCreateStrings(uniqueKey, metadata, generationContext);
            CollectionUtils.addAll(sqls, uniqueKeyCreateStrings);
        });
        table.getIndexes().forEach((s, index) -> {
            String[] indexCreateStrings = standardIndexExporter.getSqlCreateStrings(index, metadata, generationContext);
            CollectionUtils.addAll(sqls, indexCreateStrings);
        });
        if (log.isDebugEnabled()) {
            sqls.forEach(log::debug);
        }
        return sqls;
    }

    public List<String> dropTable() {
        String[] sqlDropStrings = standardTableExporter.getSqlDropStrings(table, metadata, generationContext);
        List<String> sqls = Arrays.stream(sqlDropStrings).toList();
        if (log.isDebugEnabled()) {
            sqls.forEach(log::debug);
        }
        return sqls;
    }

    public String addColumn(String columnName) {
        final String tableName = generationContext.format(new QualifiedTableName(null, null, table.getNameIdentifier()));

        StringBuilder root = new StringBuilder(dialect.getAlterTableString(tableName))
            .append(' ')
            .append(dialect.getAddColumnString());

        Column column = table.getColumn(new Identifier(columnName, false));
        StringBuilder alter = new StringBuilder(root.toString())
            .append(' ')
            .append(column.getQuotedName(dialect))
            .append(' ')
            .append(column.getSqlType(metadata));

        String defaultValue = column.getDefaultValue();
        if (defaultValue != null) {
            alter.append(" default ").append(defaultValue);
        }

        if (column.isNullable()) {
            alter.append(dialect.getNullColumnString());
        } else {
            alter.append(" not null");
        }

        if (column.isUnique()) {
            MetadataBuildingContext context = integrator.getMetadataBuildingContext();
            final String keyName = context.getBuildingOptions().getImplicitNamingStrategy()
                .determineUniqueKeyName(new ImplicitUniqueKeyNameSource() {
                    @Override
                    public Identifier getTableName() {
                        return table.getNameIdentifier();
                    }

                    @Override
                    public List<Identifier> getColumnNames() {
                        return singletonList(column.getNameIdentifier(context));
                    }

                    @Override
                    public Identifier getUserProvidedIdentifier() {
                        return null;
                    }

                    @Override
                    public MetadataBuildingContext getBuildingContext() {
                        return context;
                    }
                })
                .render(context.getMetadataCollector().getDatabase().getDialect());
            UniqueKey uk = table.getOrCreateUniqueKey(keyName);
            uk.addColumn(column);
            alter.append(dialect.getUniqueDelegate()
                .getColumnDefinitionUniquenessFragment(column, generationContext));
        }

        if (column.hasCheckConstraint() && dialect.supportsColumnCheck()) {
            alter.append(" check(")
                .append(column.getCheckConstraint())
                .append(")");
        }

        String columnComment = column.getComment();
        if (columnComment != null) {
            alter.append(dialect.getColumnComment(columnComment));
        }
        String sql = alter.toString();
        if (log.isDebugEnabled()) {
            log.debug(sql);
        }
        return sql;
    }

    public String dropColumn(String columnName) {
        Column column = table.getColumn(new Identifier(columnName, false));
        StringBuilder alert = new StringBuilder(dialect.getAlterTableString(dialect.getAlterTableString(table.getName())))
            .append(" drop column ")
            .append(column.getQuotedName(dialect));
        String sql = alert.toString();
        if (log.isDebugEnabled()) {
            log.debug(sql);
        }
        return sql;
    }

    public List<String> addIndex(String indexName) {
        Index index = table.getIndex(indexName);
        List<String> sqls = Arrays.stream(standardIndexExporter.getSqlCreateStrings(index, metadata, generationContext)).toList();
        if (log.isDebugEnabled()) {
            sqls.forEach(log::debug);
        }
        return sqls;
    }

    public List<String> dropIndex(String indexName) {
        Index index = table.getIndex(indexName);
        List<String> sqls = Arrays.stream(standardIndexExporter.getSqlDropStrings(index, metadata, generationContext)).toList();
        if (log.isDebugEnabled()) {
            sqls.forEach(log::debug);
        }
        return sqls;
    }

    public List<String> addUniqueKey(String uniqueKeyName) {
        UniqueKey uniqueKey = table.getUniqueKey(uniqueKeyName);
        List<String> sqls = Arrays.stream(standardUniqueKeyExporter.getSqlCreateStrings(uniqueKey, metadata, generationContext)).toList();
        if (log.isDebugEnabled()) {
            sqls.forEach(log::debug);
        }
        return sqls;
    }

    public List<String> dropUniqueKey(String uniqueKeyName) {
        UniqueKey uniqueKey = table.getUniqueKey(uniqueKeyName);
        List<String> sqls = Arrays.stream(standardUniqueKeyExporter.getSqlDropStrings(uniqueKey, metadata, generationContext)).toList();
        if (log.isDebugEnabled()) {
            sqls.forEach(log::debug);
        }
        return sqls;
    }

    public ColumnModifier columnModifier(String columnName) {
        return ColumnModifier.of(dialect, metadata, table, table.getColumn(Identifier.toIdentifier(columnName)));
    }

}
