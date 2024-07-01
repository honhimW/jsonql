package io.github.honhimw.jsonql.hibernate5.datasource;

import io.github.honhimw.jsonql.common.JsonUtils;
import io.github.honhimw.jsonql.hibernate5.JDBCUtils;
import io.github.honhimw.jsonql.hibernate5.ddl.ColumnModifier;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.SimpleValue;
import org.hibernate.type.StringNVarcharType;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

/**
 * @author hon_him
 * @since 2024-06-28
 */

public class H2Tests extends DDLBase {

    @Test
    void createTable() {
        List<String> create = getOps("brand_introduction").createTable();
        create.forEach(System.out::println);
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

    @Test
    void dropTable() {
        List<String> drop = getOps("brand_introduction").dropTable();
        drop.forEach(System.out::println);
    }

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
        alter.forEach(System.out::println);
    }

    @Test
    void test() {
        sessionContract.doWork(connection -> {
            ResultSet resultSet = connection.prepareStatement("show databases").executeQuery();
            List<Map<String, Object>> maps = JDBCUtils.extractResult(resultSet);
            System.out.println(JsonUtils.toPrettyJson(maps));
        });
    }

}
