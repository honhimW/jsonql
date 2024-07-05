package io.github.honhimw.jsonql.hibernate6;

import com.zaxxer.hikari.HikariDataSource;
import io.github.honhimw.jsonql.hibernate6.supports.DataSourceContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import lombok.SneakyThrows;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.time.LocalDateTime;

/**
 * @author hon_him
 * @since 2024-07-05
 */

public class SimpleTests {

    @Test
    @SneakyThrows
    void run() {
        DataSourceContext dataSourceContext = DataSourceContext.builder()
            .driverClassName("org.h2.Driver")
            .url("jdbc:h2:mem:test")
            .build();

        EntityManager em = dataSourceContext.getEm();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        @Language("SQL")
        String sql = """
        create table account (
            id int primary key,
            age int,
            name varchar(255)
        )
        """;

        HikariDataSource dataSource = dataSourceContext.getDataSource();
        Connection connection = dataSource.getConnection();
        boolean execute = connection.prepareStatement(sql).execute();

        Predicate currentTimestamp = cb.greaterThan(cb.function("current_timestamp", LocalDateTime.class), LocalDateTime.now());

        System.out.println(currentTimestamp);
    }


}
