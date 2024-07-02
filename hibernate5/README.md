# Hibernate5 Implementation

## Quick start

### DDL
```java
import io.github.honhimw.jsonql.hibernate5.supports.JsonQL;

public static void createTable() {
    Mode.ModeEnum mode = Mode.ModeEnum.Oracle;
    String url = "jdbc:h2:mem:test;MODE\\=%s;DB_CLOSE_DELAY\\=-1;IGNORECASE\\=FALSE;DATABASE_TO_UPPER\\=FALSE".formatted(mode.name());
    JsonQL jsonQL = JsonQL.builder()
        .driverClassName("org.h2.Driver")
        .url(url)
        .build();
    Table brandIntroduction = TableSupports.get("brand_introduction");
    HibernateOperations hibernateOperations = HibernateOperations.forTable(brandIntroduction);
    List<String> create = hibernateOperations.createTable();
    jsonQL.getSessionContract().doWork(connection -> {
        for (String sql : create) {
            connection.prepareStatement(sql).execute();
        }
    });
}
```

### DML
```java
// select * from brand_introduction where title.title not like '%o%';
public static void select() {
    JsonQLCompiler jsonQLCompiler = new JsonQLCompiler(em, mockTableMetaCache);
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
        List<Map<String, Object>> resultSet = (List<Map<String, Object>>) o;
    }
}
```