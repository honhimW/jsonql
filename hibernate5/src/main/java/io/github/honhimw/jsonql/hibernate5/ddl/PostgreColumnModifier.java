package io.github.honhimw.jsonql.hibernate5.ddl;

import org.hibernate.boot.Metadata;
import org.hibernate.dialect.Dialect;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Table;
import org.hibernate.type.Type;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

/**
 * @author hon_him
 * @since 2023-11-23
 */

public class PostgreColumnModifier extends ColumnModifier {
    public PostgreColumnModifier(Dialect dialect, Metadata metadata, Table table, Column column) {
        super(dialect, metadata, table, column);
    }

    @Override
    public List<String> alter(Column newColumn) {
        List<String> alter = super.alter(newColumn);
        String commentSql = "comment on column %s is '%s'".formatted(newColumn.getName(), newColumn.getComment());
        alter.add(commentSql);
        return alter;
    }

    @Override
    public String type(Type type, @Nullable Integer length) {
        Column column = new Column();
        if (Objects.nonNull(length)) {
            column.setLength(length);
        }
        String alterTableString = alterTableString();
        return "%s alter column %s type %s".formatted(alterTableString, column.getQuotedName(dialect), dialect.getTypeName(type.sqlTypes(metadata)[0], column.getLength(), column.getPrecision(), column.getScale()));
    }
}
