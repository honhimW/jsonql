package io.github.honhimw.jsonql.hibernate6.ddl;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.boot.Metadata;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.engine.jdbc.Size;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Table;

import jakarta.annotation.Nullable;
import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author hon_him
 * @since 2023-11-22
 */

public class ColumnModifier {

    protected final Dialect dialect;

    protected final Metadata metadata;

    protected final Table table;

    protected final Column column;


    public ColumnModifier(Dialect dialect, Metadata metadata, Table table, Column column) {
        this.dialect = dialect;
        this.metadata = metadata;
        this.table = table;
        this.column = column;
    }

    public List<String> alter(Column newColumn) {
        String alterTableString = alterTableString();

        String name = column.getName();
        String newName = newColumn.getName();
        String newSqlType = typeStr(newColumn);
        boolean newNullable = newColumn.isNullable();
        String newDefaultValue = newColumn.getDefaultValue();

        List<String> sqls = new ArrayList<>(2);

        if (!StringUtils.equals(name, newName)) {
            String renameSql = rename(newName);
            sqls.add(renameSql);
        }

        String typeSql = "%s alter column %s type %s %s %s".formatted(
            alterTableString,
            quote(newName),
            newSqlType,
            nullableString(newNullable),
            StringUtils.isBlank(newDefaultValue) ? "drop default" : "set default " + newDefaultValue
        );
        sqls.add(typeSql);
        return sqls;
    }

    public String rename(String newName) {
        String alterTableString = alterTableString();
        return "%s rename %s to %s".formatted(alterTableString, column.getQuotedName(dialect), quote(newName));
    }

    public String type(JDBCType type, @Nullable Integer length) {
        Column column = new Column();
        if (Objects.nonNull(length)) {
            column.setLength(length);
        }
        column.setSqlTypeCode(type.getVendorTypeNumber());
        String alterTableString = alterTableString();
        if (dialect.supportsAlterColumnType()) {
            String alterColumnTypeString = dialect.getAlterColumnTypeString(column.getQuotedName(), typeStr(column), "");
            return "%s %s".formatted(alterTableString, alterColumnTypeString);
        } else {
            String typeName = dialect.getAlterColumnTypeString(column.getQuotedName(dialect), typeStr(column), "");
            return "%s alter column %s %s".formatted(alterTableString, column.getQuotedName(dialect), typeName);
        }
    }

    public String nullable(boolean nullable) {
        String alterTableString = alterTableString();
        return "%s alter column %s drop %s".formatted(alterTableString, column.getQuotedName(dialect), nullable ? "null" : "not null");
    }

    public String defaultValue(String defaultValue) {
        String alterTableString = alterTableString();
        if (Objects.isNull(defaultValue)) {
            return "%s alter column %s drop default".formatted(alterTableString, column.getQuotedName(dialect));
        } else {
            return "%s alter column %s set default %s".formatted(alterTableString, column.getQuotedName(dialect), defaultValue);
        }
    }

    protected String typeStr(Column column) {
        return metadata.getDatabase().getTypeConfiguration().getDdlTypeRegistry()
            .getTypeName(column.getSqlTypeCode(),
            Size.precision(column.getPrecision(), column.getScale())
                .setLength(column.getLength()),
            null);
    }

    public static ColumnModifier of(Dialect dialect, Metadata metadata, Table table, Column column) {
        if (dialect instanceof PostgreSQLDialect) {
            return new PostgreColumnModifier(dialect, metadata, table, column);
        } else if (dialect instanceof MySQLDialect) {
            return new MysqlColumnModifier(dialect, metadata, table, column);
        } else if (dialect instanceof H2Dialect) {
            return new H2ColumnModifier(dialect, metadata, table, column);
        } else {
            return new ColumnModifier(dialect, metadata, table, column);
        }
    }

    protected String alterTableString() {
        return dialect.getAlterTableString(table.getName());
    }

    protected String nullableString(boolean nullable) {
        return nullable ? dialect.getNullColumnString() : "not null";
    }

    protected String quote(String name) {
        return dialect.openQuote() + name + dialect.closeQuote();
    }

}
