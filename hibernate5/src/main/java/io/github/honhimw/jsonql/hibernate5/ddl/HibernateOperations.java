package io.github.honhimw.jsonql.hibernate5.ddl;

import io.github.honhimw.jsonql.hibernate5.MetadataExtractorIntegrator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.relational.QualifiedTableName;
import org.hibernate.boot.model.relational.SqlStringGenerationContext;
import org.hibernate.boot.model.relational.internal.SqlStringGenerationContextImpl;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.hibernate.mapping.*;
import org.hibernate.tool.schema.internal.StandardIndexExporter;
import org.hibernate.tool.schema.internal.StandardTableExporter;
import org.hibernate.tool.schema.internal.StandardUniqueKeyExporter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author hon_him
 * @since 2023-11-20
 */

@Slf4j
public class HibernateOperations {

    private final Table table;

    private final Metadata metadata;
    private final JdbcEnvironment jdbcEnvironment;
    private final Dialect dialect;

    private final StandardTableExporter standardTableExporter;
    private final StandardUniqueKeyExporter standardUniqueKeyExporter;
    private final StandardIndexExporter standardIndexExporter;
    private final SqlStringGenerationContext context;

    private HibernateOperations(Table table, Dialect dialect) {
        this.table = table;
        this.metadata = MetadataExtractorIntegrator.INSTANCE.getMetadata();
        this.jdbcEnvironment = MetadataExtractorIntegrator.INSTANCE.getJdbcEnvironment();
        if (Objects.nonNull(dialect)) {
            this.dialect = dialect;
        } else {
            this.dialect = jdbcEnvironment.getDialect();
        }
        this.standardTableExporter = new StandardTableExporter(this.dialect);
        this.standardUniqueKeyExporter = new StandardUniqueKeyExporter(this.dialect);
        this.standardIndexExporter = new StandardIndexExporter(this.dialect);
        context = SqlStringGenerationContextImpl.forTests(jdbcEnvironment);
    }

    public static HibernateOperations forTable(Table table) {
        return forTable(table, null);
    }

    public static HibernateOperations forTable(Table table, Dialect dialect) {
        return new HibernateOperations(table, dialect);
    }

    public List<String> createTable() {
        List<String> sqls = new ArrayList<>();
        String[] sqlCreateStrings = standardTableExporter.getSqlCreateStrings(table, metadata, context);
        CollectionUtils.addAll(sqls, sqlCreateStrings);
        table.getUniqueKeyIterator().forEachRemaining(uniqueKey -> {
            String[] uniqueKeyCreateStrings = standardUniqueKeyExporter.getSqlCreateStrings(uniqueKey, metadata, context);
            CollectionUtils.addAll(sqls, uniqueKeyCreateStrings);
        });
        table.getIndexIterator().forEachRemaining(index -> {
            String[] indexCreateStrings = standardIndexExporter.getSqlCreateStrings(index, metadata, context);
            CollectionUtils.addAll(sqls, indexCreateStrings);
        });
        if (log.isDebugEnabled()) {
            sqls.forEach(log::debug);
        }
        return sqls;
    }

    public List<String> dropTable() {
        String[] sqlDropStrings = standardTableExporter.getSqlDropStrings(table, metadata, context);
        List<String> sqls = Arrays.stream(sqlDropStrings).toList();
        if (log.isDebugEnabled()) {
            sqls.forEach(log::debug);
        }
        return sqls;
    }

    public String addColumn(String columnName) {
        final String tableName = context.format(new QualifiedTableName(null, null, table.getNameIdentifier()));

        StringBuilder root = new StringBuilder(dialect.getAlterTableString(tableName))
            .append(' ')
            .append(dialect.getAddColumnString());

        Column column = table.getColumn(new Identifier(columnName, false));
        StringBuilder alter = new StringBuilder(root.toString())
            .append(' ')
            .append(column.getQuotedName(dialect))
            .append(' ')
            .append(column.getSqlType(dialect, metadata));

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
            String keyName = Constraint.generateName("UK_", table, column);
            UniqueKey uk = table.getOrCreateUniqueKey(keyName);
            uk.addColumn(column);
            alter.append(dialect.getUniqueDelegate()
                .getColumnDefinitionUniquenessFragment(column, context));
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
        List<String> sqls = Arrays.stream(standardIndexExporter.getSqlCreateStrings(index, metadata, context)).toList();
        if (log.isDebugEnabled()) {
            sqls.forEach(log::debug);
        }
        return sqls;
    }

    public List<String> dropIndex(String indexName) {
        Index index = table.getIndex(indexName);
        List<String> sqls = Arrays.stream(standardIndexExporter.getSqlDropStrings(index, metadata, context)).toList();
        if (log.isDebugEnabled()) {
            sqls.forEach(log::debug);
        }
        return sqls;
    }

    public List<String> addUniqueKey(String uniqueKeyName) {
        UniqueKey uniqueKey = table.getUniqueKey(uniqueKeyName);
        List<String> sqls = Arrays.stream(standardUniqueKeyExporter.getSqlCreateStrings(uniqueKey, metadata, context)).toList();
        if (log.isDebugEnabled()) {
            sqls.forEach(log::debug);
        }
        return sqls;
    }

    public List<String> dropUniqueKey(String uniqueKeyName) {
        UniqueKey uniqueKey = table.getUniqueKey(uniqueKeyName);
        List<String> sqls = Arrays.stream(standardUniqueKeyExporter.getSqlDropStrings(uniqueKey, metadata, context)).toList();
        if (log.isDebugEnabled()) {
            sqls.forEach(log::debug);
        }
        return sqls;
    }

    public ColumnModifier columnModifier(String columnName) {
        return ColumnModifier.of(dialect, metadata, table, table.getColumn(Identifier.toIdentifier(columnName)));
    }

}
