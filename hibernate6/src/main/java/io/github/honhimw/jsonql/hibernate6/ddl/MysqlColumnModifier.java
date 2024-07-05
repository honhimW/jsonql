package io.github.honhimw.jsonql.hibernate6.ddl;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.boot.Metadata;
import org.hibernate.dialect.Dialect;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Table;

import jakarta.annotation.Nullable;
import java.sql.JDBCType;
import java.util.List;
import java.util.Objects;

/**
 * @author hon_him
 * @since 2023-11-23
 */

public class MysqlColumnModifier extends ColumnModifier {

    public MysqlColumnModifier(Dialect dialect, Metadata metadata, Table table, Column column) {
        super(dialect, metadata, table, column);
    }

    @Override
    public List<String> alter(Column newColumn) {
        String alterTableString = alterTableString();

        String newName = newColumn.getName();
        String newSqlType = typeStr(newColumn);
        boolean newNullable = newColumn.isNullable();
        String newDefaultValue = newColumn.getDefaultValue();
        String newComment = newColumn.getComment();
        String typePart = "%s %s %s %s".formatted(
            newSqlType,
            nullableString(newNullable),
            StringUtils.isBlank(newDefaultValue) ? "" : "default " + newDefaultValue,
            StringUtils.isBlank(newComment) ? "" : dialect.getColumnComment(newComment)
        );


        return List.of("%s change %s %s %s".formatted(alterTableString, column.getName(), newName, typePart));
    }

    @Override
    public String rename(String newName) {
        String alterTableString = alterTableString();
        return "%s change %s %s".formatted(alterTableString, column.getQuotedName(), quote(newName));
    }
}
