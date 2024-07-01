package io.github.honhimw.jsonql.hibernate5.datasource;

import io.github.honhimw.jsonql.hibernate5.ddl.HibernateOperations;
import lombok.SneakyThrows;
import org.hibernate.dialect.Dialect;
import org.hibernate.mapping.Table;

import java.util.Objects;

/**
 * @author hon_him
 * @since 2024-07-01
 */

public abstract class DDLBase extends DataSourceBase {

    @SneakyThrows
    protected HibernateOperations getOps(String tableName, Dialect... dialect) {
        Table table = TableSupports.get(tableName);
        Dialect _dialect;
        if (Objects.nonNull(dialect) && dialect.length > 0) {
            _dialect = dialect[0];
            return HibernateOperations.forTable(table, _dialect);
        } else {
            return HibernateOperations.forTable(table);
        }
    }

    protected Table getTable(String tableName) {
        return TableSupports.get(tableName);
    }
}
