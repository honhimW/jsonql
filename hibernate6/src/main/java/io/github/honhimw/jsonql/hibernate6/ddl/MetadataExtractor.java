package io.github.honhimw.jsonql.hibernate6.ddl;

import io.github.honhimw.jsonql.hibernate6.MetadataExtractorIntegrator;
import io.github.honhimw.jsonql.hibernate6.TableBuilder;
import io.github.honhimw.jsonql.hibernate6.TypeConvertUtils;
import org.apache.commons.collections4.IteratorUtils;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.relational.SqlStringGenerationContext;
import org.hibernate.boot.model.relational.internal.SqlStringGenerationContextImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.hibernate.mapping.Table;
import org.hibernate.resource.transaction.spi.DdlTransactionIsolator;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.hibernate.tool.schema.extract.internal.TableInformationImpl;
import org.hibernate.tool.schema.extract.spi.ColumnInformation;
import org.hibernate.tool.schema.extract.spi.DatabaseInformation;
import org.hibernate.tool.schema.extract.spi.PrimaryKeyInformation;
import org.hibernate.tool.schema.extract.spi.TableInformation;
import org.hibernate.tool.schema.internal.Helper;
import org.hibernate.tool.schema.internal.HibernateSchemaManagementTool;
import org.hibernate.tool.schema.internal.exec.JdbcContext;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author hon_him
 * @since 2024-02-01
 */

public class MetadataExtractor {

    private final MetadataExtractorIntegrator integrator;

    private DatabaseInformation databaseInformation;

    public MetadataExtractor(MetadataExtractorIntegrator integrator) {
        this.integrator = integrator;
    }

    public DatabaseInformation getDatabaseInformation() {
        if (this.databaseInformation == null) {
            HibernateSchemaManagementTool tool = new HibernateSchemaManagementTool();
            ServiceRegistryImplementor serviceRegistry = integrator.getSessionFactory().getServiceRegistry();
            tool.injectServices(serviceRegistry);
            Metadata metadata = integrator.getMetadata();
            SqlStringGenerationContext sqlStringGenerationContext = SqlStringGenerationContextImpl.fromConfigurationMap(
                tool.getServiceRegistry().getService(JdbcEnvironment.class),
                metadata.getDatabase(),
                Map.of()
            );
            final JdbcContext jdbcContext = tool.resolveJdbcContext(Map.of());

            final DdlTransactionIsolator isolator = tool.getDdlTransactionIsolator(jdbcContext);

            databaseInformation = Helper.buildDatabaseInformation(
                tool.getServiceRegistry(),
                isolator,
                sqlStringGenerationContext,
                tool
            );
        }
        return databaseInformation;
    }

    public TableInformation getTableInformation(String tableName) {
        return getDatabaseInformation().getTableInformation(null, null, Identifier.toIdentifier(tableName));
    }

    public List<ColumnMetaData> getTableMeta(String tableName) {
        TableInformation tableInformation = getTableInformation(tableName);
        Field columnsField = null;
        try {
            columnsField = TableInformationImpl.class.getDeclaredField("columns");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        Objects.requireNonNull(columnsField);
        columnsField.trySetAccessible();

        Map<Identifier, ColumnInformation> columns = null;
        try {
            columns = (Map<Identifier, ColumnInformation>) columnsField.get(tableInformation);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        PrimaryKeyInformation primaryKey = tableInformation.getPrimaryKey();
        List<ColumnInformation> pkColumns = IteratorUtils.toList(primaryKey.getColumns().iterator());
        List<ColumnMetaData> fields = new ArrayList<>();
        Objects.requireNonNull(columns);
        columns.forEach((identifier, columnInformation) -> {
            String columnName = identifier.getCanonicalName();
            ColumnMetaData field = new ColumnMetaData(
                columnName,
                pkColumns.contains(columnInformation),
                TypeConvertUtils.jdbc2SimpleType(columnInformation.getTypeCode()),
                columnInformation.getColumnSize(),
                columnInformation.getNullable().toBoolean(false)
            );
            fields.add(field);
        });

        return fields;
    }

    public Table getTable(String tableName) {
        List<ColumnMetaData> tableMeta = getTableMeta(tableName);
        TableBuilder builder = TableBuilder.builder(tableName);
        for (ColumnMetaData columnMetaData : tableMeta) {
            builder.addColumn(columnBuilder -> columnBuilder
                .name(columnMetaData.name)
                .type(TypeConvertUtils.simpleType2Hibernate(columnMetaData.type).getSqlTypeCode())
                .length(columnMetaData.length)
                .nullable(columnMetaData.nullable)
            );
        }
        return builder.build();
    }

    public record ColumnMetaData(String name, boolean primaryKey, String type, int length, boolean nullable) {

    }

}
