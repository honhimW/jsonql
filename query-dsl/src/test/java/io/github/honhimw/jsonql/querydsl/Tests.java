package io.github.honhimw.jsonql.querydsl;

import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.*;
import com.querydsl.sql.*;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

/**
 * @author hon_him
 * @since 2024-07-18
 */

public class Tests {

    @Test
    @SneakyThrows
    void test() {
        SQLTemplates dialect = new H2Templates();
        SQLQuery<Void> sqlQuery = new SQLQuery<>(dialect);
        Expression<?> titleField = ConstantImpl.create("title");
        Expression<?> table = ConstantImpl.create("material");
        new PathMetadata()
        Expressions.simplePath()
        SimplePath<Integer> idPath = Expressions.path(Integer.class, "id");
        BooleanExpression eq = idPath.eq(1);
        SQLQuery<?> where = sqlQuery
            .select(titleField)
            .from(table)
            .where(eq, eq);
        SQLBindings sql = where.getSQL();
        SQLHolder sqlHolder = new SQLHolder(sql.getSQL(), sql.getNullFriendlyBindings());
        System.out.println(sqlHolder);
    }

}
