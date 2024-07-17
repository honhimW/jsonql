package io.github.honhimw.jsonql.hibernate6.ddl;

import org.hibernate.boot.Metadata;
import org.hibernate.dialect.Dialect;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Table;

import java.util.List;

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
}
