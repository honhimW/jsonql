package io.github.honhimw.jsonql.hibernate5.datasource;

import io.github.honhimw.jsonql.hibernate5.JDBCUtils;
import io.github.honhimw.jsonql.hibernate5.JsonQLExecutor;
import io.github.honhimw.jsonql.hibernate5.ddl.ColumnModifier;
import io.github.honhimw.jsonql.hibernate5.internal.JsonQLCompiler;
import io.github.honhimw.jsonql.hibernate5.meta.SQLHolder;
import lombok.SneakyThrows;
import org.h2.engine.Mode;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.SimpleValue;
import org.hibernate.type.StringNVarcharType;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.*;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

/**
 * @author hon_him
 * @since 2024-06-28
 */

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class H2Tests extends DDLBase {

    @BeforeAll
    static void init() {
        Mode.ModeEnum mode = Mode.ModeEnum.MSSQLServer;
        String url = "jdbc:h2:mem:test;MODE\\=%s;DB_CLOSE_DELAY\\=-1;IGNORECASE\\=FALSE;DATABASE_TO_UPPER\\=FALSE".formatted(mode.name());
        buildDatasource(
            "org.h2.Driver",
            url,
            null, null
        );
    }

    @AfterAll
    static void clean() {
        destroyDatasource();
    }

    @Order(0)
    @Test
    void createTable() {
        List<String> create = getOps("brand_introduction").createTable();
        create.forEach(log::info);
        sessionContract.doWork(connection -> {
            for (String sql : create) {
                connection.prepareStatement(sql).execute();
            }
        });

        List<Map<String, Object>> maps = sessionContract.doReturningWork(connection -> {
            ResultSet resultSet = connection.prepareStatement("select count(*) as c from brand_introduction").executeQuery();
            return JDBCUtils.extractResult(resultSet);
        });
        assert maps.size() == 1;
        assert maps.get(0).get("c").equals(0L);
    }

    @Order(1)
    @Test
    void test() {
        sessionContract.doWork(connection -> {
            ResultSet resultSet = connection.prepareStatement("show databases").executeQuery();
            List<Map<String, Object>> maps = JDBCUtils.extractResult(resultSet);
            printTable("show databases", maps);
        });
    }

    @Order(2)
    @Test
    void modifyColumn() {
        ColumnModifier columnModifier = getOps("brand_introduction").columnModifier("title");
        Column newColumn = new Column();
        newColumn.setName("title2");
        newColumn.setLength(1024);
        newColumn.setComment("modify");
        SimpleValue value = new SimpleValue(integrator.getMetadataBuildingContext(), getTable("brand_introduction"));
        value.setTypeName(StringNVarcharType.INSTANCE.getName());
        newColumn.setValue(value);
        newColumn.setNullable(false);
        List<String> alter = columnModifier.alter(newColumn);
        print("modify column", alter);
    }

    @Order(3)
    @Test
    @SneakyThrows
    void insert() {
        JsonQLCompiler jsonQLCompiler = new JsonQLCompiler(em, mockTableMetaCache);
        {
            @Language("json")
            String jsonQL = """
                {
                  "operation": "insert",
                  "table": "brand_introduction",
                  "data": {
                    "id": 1,
                    "title": "foo"
                  }
                }
                """;
            Object o = new JsonQLExecutor(jsonQLCompiler).executeDml(jsonQL);
            log.info("insert result: {}", o);
        }
        {
            @Language("json")
            String jsonQL = """
                {
                  "operation": "insert",
                  "table": "brand_introduction",
                  "data": {
                    "id": 2,
                    "title": "bar",
                    "logo": "hello"
                  }
                }
                """;
            Object o = new JsonQLExecutor(jsonQLCompiler).executeDml(jsonQL);
            log.info("insert result: {}", o);
        }
    }

    @Order(4)
    @Test
    @SneakyThrows
    void select() {
        JsonQLCompiler jsonQLCompiler = new JsonQLCompiler(em, mockTableMetaCache);
        {
            @Language("json")
            String select = """
                {
                  "operation": "select",
                  "table": "brand_introduction",
                  "count": false
                }
                """;
            List<SQLHolder> compile = jsonQLCompiler.compile(MAPPER.readTree(select));
            Object o = sessionContract.doReturningWork(connection -> JsonQLExecutor.executeDmlQuery(compile, connection));
            printTable(compile.get(0).toString(), ((List<Map<String, Object>>) o));
        }
        {
            @Language("json")
            String select = """
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
                """;
            List<SQLHolder> compile = jsonQLCompiler.compile(MAPPER.readTree(select));
            Object o = sessionContract.doReturningWork(connection -> JsonQLExecutor.executeDmlQuery(compile, connection));
            printTable(compile.get(0).toString(), ((List<Map<String, Object>>) o));
        }
    }

    @Order(Integer.MAX_VALUE)
    @Test
    void dropTable() {
        List<String> drop = getOps("brand_introduction").dropTable();
        print("drop table", drop);
    }

}
