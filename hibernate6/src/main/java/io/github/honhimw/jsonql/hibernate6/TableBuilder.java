package io.github.honhimw.jsonql.hibernate6;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.boot.spi.MetadataBuildingContext;
import org.hibernate.dialect.Dialect;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.mapping.*;
import org.hibernate.type.Type;

import java.sql.JDBCType;
import java.sql.SQLType;
import java.util.List;
import java.util.Map;
import java.util.*;
import java.util.function.Consumer;

/**
 * @author hon_him
 * @since 2024-07-01
 */

public class TableBuilder {

    private final String name;

    private final List<Col> columns = new ArrayList<>();
    private final List<List<String>> indexs = new ArrayList<>();
    private final List<List<String>> uniques = new ArrayList<>();
    private final List<Foreign> foreigns = new ArrayList<>();

    private record Foreign(Table joinTable, Map<String, String> columnMapping) {
    }


    private String comment;

    private MetadataBuildingContext metadataBuildingContext;

    private TableBuilder(String name) {
        this.name = name;
        this.metadataBuildingContext = MetadataExtractorIntegrator.INSTANCE.getMetadataBuildingContext();
    }

    private TableBuilder(String name, MetadataExtractorIntegrator integrator) {
        this.name = name;
        this.metadataBuildingContext = integrator.getMetadataBuildingContext();
    }

    public static TableBuilder builder(String tableName) {
        return new TableBuilder(tableName);
    }

    public static TableBuilder builder(String tableName, MetadataExtractorIntegrator integrator) {
        return new TableBuilder(tableName, integrator);
    }

    public TableBuilder addColumn(Consumer<ColumnBuilder> cbc) {
        ColumnBuilder columnBuilder = new ColumnBuilder();
        cbc.accept(columnBuilder);
        Col col = columnBuilder.build();
        columns.add(col);
        return this;
    }

    public TableBuilder editColumn(String columnName, Consumer<ColumnBuilder> cbc) {
        columns.stream()
            .filter(col -> StringUtils.equals(columnName, col.name()))
            .findFirst()
            .ifPresentOrElse(col -> {
                ColumnBuilder columnBuilder = new ColumnBuilder();
                columnBuilder
                    .name(col.name())
                    .type(col.type())
                    .length(col.length())
                    .nullable(col.nullable())
                    .defaultValue(col.defaultValue())
                    .comment(col.comment())
                    .privateKey(col.privateKey())
                    .generated(col.generated())
                ;
                cbc.accept(columnBuilder);
                columns.add(columnBuilder.build());
            }, () -> {
                ColumnBuilder columnBuilder = new ColumnBuilder();
                columnBuilder.name(columnName);
                cbc.accept(columnBuilder);
                columns.add(columnBuilder.build());
            });
        return this;
    }

    public TableBuilder addIndex(String... columnNames) {
        if (ArrayUtils.isNotEmpty(columnNames)) {
            indexs.add(List.of(columnNames));
        }
        return this;
    }

    public TableBuilder addUnique(String... columnNames) {
        if (ArrayUtils.isNotEmpty(columnNames)) {
            uniques.add(List.of(columnNames));
        }
        return this;
    }

    public TableBuilder addForeign(Table joinTable, Map<String, String> columnMapping) {
        Foreign foreign = new Foreign(joinTable, columnMapping);
        foreigns.add(foreign);
        return this;
    }

    public TableBuilder metadataBuildingContext(MetadataBuildingContext metadataBuildingContext) {
        this.metadataBuildingContext = metadataBuildingContext;
        return this;
    }

    public TableBuilder comment(String comment) {
        this.comment = comment;
        return this;
    }

    public Table build() {
        Table table = new Table(name, name);
        table.setComment(comment);

        PrimaryKey primaryKey = new PrimaryKey(table);
        primaryKey.setName("pk");

        List<Column> _columns = columns.stream().map(col -> {
            Column column = new IColumn();
            column.setName(col.name());
            Optional.ofNullable(col.length()).ifPresent(column::setLength);
            column.setNullable(col.nullable());
            column.setDefaultValue(col.defaultValue());
            if (Objects.nonNull(col.type())) {
                BasicValue value = new BasicValue(metadataBuildingContext, table);
                if (col.generated) {
                    value.setIdentifierGeneratorStrategy("native");
                    table.setIdentifierValue(value);
                }
                value.setTypeName(col.type().getName());
                value.addColumn(column);
                column.setValue(value);
            }
            if (col.privateKey()) {
                String primaryKeyName = primaryKey.getName();
                primaryKeyName += "_" + col.name();
                primaryKey.setName(primaryKeyName);
                primaryKey.addColumn(column);
                table.setPrimaryKey(primaryKey);
            }
            column.setComment(col.comment());
            return column;
        }).toList();
        for (List<String> index : indexs) {
            Index _index = new Index();
            _index.setTable(table);
            _index.setName("idx");
            List<Column> list = _columns.stream().filter(column -> index.contains(column.getName())).toList();
            if (CollectionUtils.isNotEmpty(list)) {
                list.forEach(column -> {
                    String indexName = _index.getName();
                    indexName += "_" + column.getName();
                    _index.setName(indexName);
                    _index.addColumn(column);
                });
                table.addIndex(_index);
            }
        }
        for (List<String> unique : uniques) {
            List<Column> list = _columns.stream().filter(column -> unique.contains(column.getName())).toList();
            UniqueKey uniqueKey = new UniqueKey();
            uniqueKey.setTable(table);
            uniqueKey.setName("unq");
            if (CollectionUtils.isNotEmpty(list)) {
                list.forEach(column -> {
                    String uniqueKeyName = uniqueKey.getName();
                    uniqueKeyName += "_" + column.getName();
                    uniqueKey.setName(uniqueKeyName);
                    uniqueKey.addColumn(column);
                });
                table.addUniqueKey(uniqueKey);
            }
        }
        for (Foreign foreign : foreigns) {
            Table joinTable = foreign.joinTable();
            Map<String, String> mappings = foreign.columnMapping();
            List<Column> referencedTableColumns = new ArrayList<>(joinTable.getColumns());
            List<Column> joinColumns = new ArrayList<>();
            List<Column> referencedColumns = new ArrayList<>();

            mappings.forEach((joinColumnName, referencedColumnName) -> {
                Column joinColumn = _columns.stream().filter(column -> StringUtils.equals(joinColumnName, column.getName())).findFirst().orElseThrow();
                Column referencedColumn = referencedTableColumns.stream().filter(column -> StringUtils.equals(referencedColumnName, column.getName())).findFirst().orElseThrow();
                joinColumns.add(joinColumn);
                referencedColumns.add(referencedColumn);
            });

            StringBuilder foreignKeyName = new StringBuilder("fk");
            if (CollectionUtils.isNotEmpty(joinColumns)) {
                for (Column column : joinColumns) {
                    foreignKeyName.append("_").append(column.getName());
                }
                ForeignKey foreignKey = table.createForeignKey(foreignKeyName.toString(), joinColumns, joinTable.getName(), null, referencedColumns);
                foreignKey.setReferencedTable(joinTable);
//                foreignKey.disableCreation();
            }
        }
        _columns.forEach(table::addColumn);
        return table;
    }

    private record Col(String name, Type type, Integer length, boolean nullable, String defaultValue, String comment,
                       Boolean privateKey, Boolean generated) {
    }

    public static class ColumnBuilder {

        private String name;

        private Type type;

        private Integer length;

        private Boolean nullable = true;

        private String defaultValue;

        private String comment;

        private Boolean privateKey = false;

        private Boolean generated = false;

        private ColumnBuilder() {
        }

        public ColumnBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ColumnBuilder type(JDBCType type) {
            this.type = TypeConvertUtils.jdbc2HType(type);
            return this;
        }

        public ColumnBuilder type(int type) {
            this.type = TypeConvertUtils.jdbc2HType(type);
            return this;
        }

        public ColumnBuilder type(Class<?> type) {
            if (Objects.nonNull(type)) {
                this.type = TypeConvertUtils.jdbc2HType(TypeConvertUtils.type2jdbc(type));
            }
            return this;
        }

        public ColumnBuilder type(SQLType type) {
            if (Objects.nonNull(type)) {
                this.type = TypeConvertUtils.jdbc2HType(type.getVendorTypeNumber());
            }
            return this;
        }

        public ColumnBuilder type(Type type) {
            if (Objects.nonNull(type)) {
                this.type = type;
            }
            return this;
        }

        public ColumnBuilder length(Integer length) {
            this.length = length;
            return this;
        }

        public ColumnBuilder nullable(boolean nullable) {
            this.nullable = nullable;
            return this;
        }

        public ColumnBuilder defaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public ColumnBuilder comment(String comment) {
            this.comment = comment;
            return this;
        }

        public ColumnBuilder privateKey(boolean privateKey) {
            this.privateKey = privateKey;
            return this;
        }

        public ColumnBuilder generated(boolean generated) {
            this.generated = generated;
            return this;
        }

        private Col build() {
            return new Col(name, type, length, nullable, defaultValue, comment, privateKey, generated);
        }

    }

    public static class IColumn extends Column {

        /**
         * Make isQuoted() return true.
         */
        @Override
        public void setName(String name) {
            if (StringHelper.isNotEmpty(name)) {
                if (Dialect.QUOTE.indexOf(name.charAt(0)) == -1) {
                    name = "`" + name + "`";
                }
            }
            super.setName(name);
        }
    }

}
