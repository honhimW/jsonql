package io.github.honhimw.jsonql.hibernate6;

import org.hibernate.mapping.Table;

import java.util.HashMap;
import java.util.Map;

/**
 * @author hon_him
 * @since 2024-07-01
 */

public class TableSupports {

    public static Table get(String tableName) {
        return TABLE_MAP.get(tableName);
    }

    public static Map<String, Table> getTableMap() {
        return TABLE_MAP;
    }

    private static final Map<String, Table> TABLE_MAP;

    static  {
        Map<String, Table> tableMap = new HashMap<>();
        tableMap.put("brand_introduction", TableBuilder.builder("brand_introduction")
            .addColumn(columnBuilder -> columnBuilder
                .name("id")
                .privateKey(true)
                .type(String.class))
            .addColumn(columnBuilder -> columnBuilder
                .name("introduction")
                .type(String.class))
            .addColumn(columnBuilder -> columnBuilder
                .name("logo")
                .type(String.class))
            .addColumn(columnBuilder -> columnBuilder
                .name("title")
                .type(String.class))
            .build());
        tableMap.put("material_supplier", TableBuilder.builder("material_supplier")
            .addColumn(columnBuilder -> columnBuilder
                .name("id")
                .privateKey(true)
                .type(String.class))
            .addColumn(columnBuilder -> columnBuilder
                .name("contact")
                .type(String.class))
            .addColumn(columnBuilder -> columnBuilder
                .name("status")
                .type(String.class))
            .addColumn(columnBuilder -> columnBuilder
                .name("title")
                .type(String.class))
            .addColumn(columnBuilder -> columnBuilder
                .name("brand")
                .type(String.class))
            .build());
        tableMap.put("material", TableBuilder.builder("material")
            .addColumn(columnBuilder -> columnBuilder
                .name("id")
                .privateKey(true)
                .type(String.class))
            .addColumn(columnBuilder -> columnBuilder
                .name("supplier")
                .type(String.class))
            .addColumn(columnBuilder -> columnBuilder
                .name("title")
                .type(String.class))
            .build());
        TABLE_MAP = Map.copyOf(tableMap);
    }

}
