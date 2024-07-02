package io.github.honhimw.jsonql.hibernate5.datasource;

import io.github.honhimw.jsonql.hibernate5.ddl.HibernateOperations;
import io.github.honhimw.jsonql.hibernate5.supports.JsonQL;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.h2.engine.Mode;
import org.hibernate.mapping.Table;
import org.junit.jupiter.api.Test;

import java.util.List;

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

}
