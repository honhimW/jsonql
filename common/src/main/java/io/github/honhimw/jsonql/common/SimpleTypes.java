package io.github.honhimw.jsonql.common;

import lombok.Getter;

import java.sql.JDBCType;
import java.util.HashMap;
import java.util.Map;

/**
 * @author hon_him
 * @since 2024-07-15
 */

@Getter
public enum SimpleTypes {

    BOOLEAN("boolean", JDBCType.BOOLEAN),
    INTEGER("i32", JDBCType.INTEGER),
    LONG("i64", JDBCType.BIGINT),
    DECIMAL("decimal", JDBCType.DECIMAL),
    DOUBLE("f64", JDBCType.DOUBLE),
    STRING("string", JDBCType.NVARCHAR),
    TEXT("text", JDBCType.LONGNVARCHAR),
    TIMESTAMP("timestamp", JDBCType.TIMESTAMP),
    ;

    private final String name;

    private final JDBCType jdbcType;

    SimpleTypes(String name, JDBCType jdbcType) {
        this.name = name;
        this.jdbcType = jdbcType;
    }

    private static final Map<String, SimpleTypes> NAME_MAP;
    private static final Map<JDBCType, SimpleTypes> JDBC_TYPE_MAP;

    static {
        Map<String, SimpleTypes> tmpNameMap = new HashMap<>();
        Map<JDBCType, SimpleTypes> tmpJdbcTypeMap = new HashMap<>();
        for (SimpleTypes type : SimpleTypes.values()) {
            tmpNameMap.put(type.name, type);
            tmpJdbcTypeMap.put(type.jdbcType, type);
        }
        NAME_MAP = Map.copyOf(tmpNameMap);
        JDBC_TYPE_MAP = Map.copyOf(tmpJdbcTypeMap);
    }

    public static SimpleTypes ofName(String name) {
        return NAME_MAP.get(name);
    }


    public static SimpleTypes ofJDBC(JDBCType jdbcType) {
        return JDBC_TYPE_MAP.get(jdbcType);
    }

    public static SimpleTypes ofJDBC(int jdbcType) {
        return JDBC_TYPE_MAP.get(JDBCType.valueOf(jdbcType));
    }

}
