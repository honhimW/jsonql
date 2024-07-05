package io.github.honhimw.jsonql.hibernate6;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.boot.spi.MetadataBuildingContext;
import org.hibernate.mapping.*;
import org.hibernate.type.Type;

import java.sql.SQLType;
import java.util.List;
import java.util.Map;
import java.util.*;
import java.util.function.Consumer;

/**
 * @author hon_him
 * @since 2024-06-28
 */

public class TableHelper {

    private final Table _table;

    private MetadataBuildingContext metadataBuildingContext;

    private TableHelper(Table table) {
        this._table = table;
        this.metadataBuildingContext = MetadataExtractorIntegrator.INSTANCE.getMetadataBuildingContext();
    }

    public static TableHelper of(Table table) {
        return new TableHelper(table);
    }

    public TableHelper addColumn(Consumer<ColumnBuilder> cbc) {
        ColumnBuilder columnBuilder = new ColumnBuilder();
        cbc.accept(columnBuilder);
        Col col = columnBuilder.build();
        Column column = new Column();
        column.setName(col.name());
        Optional.ofNullable(col.length()).ifPresent(column::setLength);
        column.setNullable(col.nullable());
        column.setDefaultValue(col.defaultValue());
        if (Objects.nonNull(col.type())) {
            SimpleValue value = new Any(metadataBuildingContext, _table);
            if (col.generated) {
                value.setIdentifierGeneratorStrategy("native");
                _table.setIdentifierValue(value);
            }
            value.setTypeName(col.type().getName());
            value.addColumn(column);
            column.setValue(value);
        }
        if (col.privateKey()) {
            PrimaryKey primaryKey = _table.getPrimaryKey();
            String primaryKeyName = primaryKey.getName();
            primaryKeyName += "_" + col.name();
            primaryKey.setName(primaryKeyName);
            primaryKey.addColumn(column);
        }
        column.setComment(col.comment());
        _table.addColumn(column);
        return this;
    }

    public TableHelper addIndex(String... columnNames) {
        Index _index = new Index();
        _index.setTable(_table);
        _index.setName("idx");
        List<Column> list = _table.getColumns().stream().filter(column -> ArrayUtils.contains(columnNames, column.getName())).toList();
        if (CollectionUtils.isNotEmpty(list)) {
            list.forEach(column -> {
                String indexName = _index.getName();
                indexName += "_" + column.getName();
                _index.setName(indexName);
                _index.addColumn(column);
            });
            _table.addIndex(_index);
        }
        return this;
    }

    public TableHelper addUnique(String... columnNames) {
        List<Column> list = _table.getColumns().stream().filter(column -> ArrayUtils.contains(columnNames, column.getName())).toList();
        UniqueKey uniqueKey = new UniqueKey();
        uniqueKey.setTable(_table);
        uniqueKey.setName("unq");
        if (CollectionUtils.isNotEmpty(list)) {
            list.forEach(column -> {
                String uniqueKeyName = uniqueKey.getName();
                uniqueKeyName += "_" + column.getName();
                uniqueKey.setName(uniqueKeyName);
                uniqueKey.addColumn(column);
            });
            _table.addUniqueKey(uniqueKey);
        }
        return this;
    }

    public TableHelper addForeign(Table joinTable, Map<String, String> mappings) {
        List<Column> referencedTableColumns = new ArrayList<>(_table.getColumns());
        List<Column> joinColumns = new ArrayList<>();
        List<Column> referencedColumns = new ArrayList<>();

        mappings.forEach((joinColumnName, referencedColumnName) -> {
            Column joinColumn = _table.getColumns().stream().filter(column -> StringUtils.equals(joinColumnName, column.getName())).findFirst().orElseThrow();
            Column referencedColumn = referencedTableColumns.stream().filter(column -> StringUtils.equals(referencedColumnName, column.getName())).findFirst().orElseThrow();
            joinColumns.add(joinColumn);
            referencedColumns.add(referencedColumn);
        });

        StringBuilder foreignKeyName = new StringBuilder("fk");
        if (CollectionUtils.isNotEmpty(joinColumns)) {
            for (Column column : joinColumns) {
                foreignKeyName.append("_").append(column.getName());
            }
            ForeignKey foreignKey = _table.createForeignKey(foreignKeyName.toString(), joinColumns, joinTable.getName(), null, referencedColumns);
            foreignKey.setReferencedTable(joinTable);
//                foreignKey.disableCreation();
        }
        return this;
    }

    public TableHelper metadataBuildingContext(MetadataBuildingContext metadataBuildingContext) {
        this.metadataBuildingContext = metadataBuildingContext;
        return this;
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

        public ColumnBuilder type(Type type) {
            this.type = type;
            return this;
        }

        public ColumnBuilder type(int type) {
            this.type = TypeConvertUtils.type2hibernate(type);
            return this;
        }

        public ColumnBuilder type(Class<?> type) {
            if (Objects.nonNull(type)) {
                this.type = TypeConvertUtils.type2hibernate(type);
            }
            return this;
        }

        public ColumnBuilder type(SQLType type) {
            if (Objects.nonNull(type)) {
                this.type = TypeConvertUtils.jdbc2hibernate(type);
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


}
