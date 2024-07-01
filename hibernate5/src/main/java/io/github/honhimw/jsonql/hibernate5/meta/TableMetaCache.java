package io.github.honhimw.jsonql.hibernate5.meta;

import org.hibernate.mapping.Table;

/**
 * @author hon_him
 * @since 2024-01-30
 */

public interface TableMetaCache {

    Table buildTable(String fullTableName);

}
