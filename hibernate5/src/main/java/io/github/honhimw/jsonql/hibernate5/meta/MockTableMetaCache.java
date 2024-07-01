package io.github.honhimw.jsonql.hibernate5.meta;

import org.hibernate.mapping.Table;

import java.util.Map;

/**
 * @author hon_him
 * @since 2024-01-30
 */

public class MockTableMetaCache implements TableMetaCache {

    private final Map<String, Table> tableMap;

    public MockTableMetaCache(Map<String, Table> tableMap) {
        this.tableMap = tableMap;
    }

    @Override
    public Table buildTable(String fullTableName) {
        return tableMap.get(fullTableName);
    }
}
