package io.github.honhimw.jsonql.hibernate5.ddl;

import org.hibernate.boot.Metadata;
import org.hibernate.dialect.Dialect;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Table;

/**
 * @author hon_him
 * @since 2023-11-23
 */

public class H2ColumnModifier extends ColumnModifier {
    public H2ColumnModifier(Dialect dialect, Metadata metadata, Table table, Column column) {
        super(dialect, metadata, table, column);
    }

    @Override
    public String rename(String newName) {
        String alterTableString = alterTableString();
        return "%s alter column %s rename to %s".formatted(alterTableString, column.getQuotedName(dialect), quote(newName));
    }
}
