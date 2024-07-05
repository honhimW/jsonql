package io.github.honhimw.jsonql.hibernate5.datasource;

import io.github.honhimw.jsonql.hibernate5.ddl.HibernateOperations;
import io.github.honhimw.jsonql.hibernate5.meta.MockTableMetaCache;
import io.github.honhimw.jsonql.hibernate5.supports.JsonQLHelper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.h2.engine.Mode;
import org.hibernate.mapping.Table;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hon_him
 * @since 2024-07-02
 */
@Slf4j
public class JsonQLTests {

    @Test
    @SneakyThrows
    void fastRun() {
        Mode.ModeEnum mode = Mode.ModeEnum.Oracle;
        String url = "jdbc:h2:mem:test;MODE\\=%s;DB_CLOSE_DELAY\\=-1;IGNORECASE\\=FALSE;DATABASE_TO_UPPER\\=FALSE".formatted(mode.name());
        Map<String, Table> tableMap = new HashMap<>();
        JsonQLHelper jsonQL = JsonQLHelper.builder()
            .driverClassName("org.h2.Driver")
            .url(url)
            .tableMetaCache(new MockTableMetaCache(tableMap))
            .build();
        Table brandIntroduction = TableSupports.get("brand_introduction");
        HibernateOperations hibernateOperations = HibernateOperations.forTable(brandIntroduction);
        List<String> create = hibernateOperations.createTable();
        jsonQL.getDataSourceContext().getSessionContract().doWork(connection -> {
            for (String sql : create) {
                connection.prepareStatement(sql).execute();
            }
        });

        tableMap.putAll(TableSupports.getTableMap());

        Object o = jsonQL.executeDml("""
            {
              "operation": "select",
              "table": "brand_introduction",
              "count": false,
              "condition": {
                "title": {
                  "!contains": "o"
                }
              }
            }
            """);
        List<Map<String, Object>> resultSet = (List<Map<String, Object>>) o;
    }

    @Test
    @SneakyThrows
    void fastSelect() {
        Mode.ModeEnum mode = Mode.ModeEnum.Oracle;
        String url = "jdbc:h2:mem:test;MODE\\=%s;DB_CLOSE_DELAY\\=-1;IGNORECASE\\=FALSE;DATABASE_TO_UPPER\\=FALSE".formatted(mode.name());
        JsonQLHelper jsonQL = JsonQLHelper.builder()
            .driverClassName("org.h2.Driver")
            .url(url)
            .build();
        Object o = jsonQL.executeDml("""
            {
              "operation": "select",
              "table": "brand_introduction",
              "count": false,
              "condition": {
                "title": {
                  "!contains": "o"
                }
              }
            }
            """);
        List<Map<String, Object>> resultSet = (List<Map<String, Object>>) o;
    }

}
