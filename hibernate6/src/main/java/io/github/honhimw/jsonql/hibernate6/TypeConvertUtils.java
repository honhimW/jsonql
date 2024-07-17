package io.github.honhimw.jsonql.hibernate6;

import org.hibernate.type.BasicTypeReference;
import org.hibernate.type.SqlTypes;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;
import org.hibernate.type.descriptor.java.*;
import org.hibernate.type.descriptor.jdbc.*;
import org.hibernate.type.internal.NamedBasicTypeImpl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.JDBCType;
import java.sql.SQLType;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author hon_him
 * @since 2024-06-28
 */

public class TypeConvertUtils {

    private static final Map<SQLType, BasicTypeReference<?>> SQL_TYPE_TYPE_MAP;
    private static final Map<Class<?>, BasicTypeReference<?>> JAVA_TYPE_TYPE_MAP;
    private static final Map<Class<?>, JDBCType> JAVA_TYPE_JDBC_TYPE_MAP;

    static {
        Map<SQLType, BasicTypeReference<?>> sqlTypeMap = new HashMap<>();
        sqlTypeMap.put(JDBCType.BIT, StandardBasicTypes.BOOLEAN);
        sqlTypeMap.put(JDBCType.TINYINT, StandardBasicTypes.NUMERIC_BOOLEAN);
        sqlTypeMap.put(JDBCType.BOOLEAN, StandardBasicTypes.BOOLEAN);
        sqlTypeMap.put(JDBCType.SMALLINT, StandardBasicTypes.SHORT);
        sqlTypeMap.put(JDBCType.INTEGER, StandardBasicTypes.INTEGER);
        sqlTypeMap.put(JDBCType.BIGINT, StandardBasicTypes.BIG_INTEGER);
        sqlTypeMap.put(JDBCType.FLOAT, StandardBasicTypes.FLOAT);
        sqlTypeMap.put(JDBCType.DOUBLE, StandardBasicTypes.DOUBLE);
        sqlTypeMap.put(JDBCType.DECIMAL, StandardBasicTypes.BIG_DECIMAL);
        sqlTypeMap.put(JDBCType.CHAR, StandardBasicTypes.CHARACTER);
        sqlTypeMap.put(JDBCType.VARCHAR, StandardBasicTypes.STRING);
        sqlTypeMap.put(JDBCType.LONGVARCHAR, StandardBasicTypes.TEXT);
        sqlTypeMap.put(JDBCType.DATE, StandardBasicTypes.DATE);
        sqlTypeMap.put(JDBCType.TIME, StandardBasicTypes.TIME);
        sqlTypeMap.put(JDBCType.TIMESTAMP, StandardBasicTypes.TIMESTAMP);
        sqlTypeMap.put(JDBCType.BINARY, new BasicTypeReference<>(
            "binary",
            byte[].class,
            SqlTypes.BINARY
        ));
        sqlTypeMap.put(JDBCType.VARBINARY, StandardBasicTypes.BINARY);
        sqlTypeMap.put(JDBCType.LONGVARBINARY, new BasicTypeReference<>(
            "binary",
            byte[].class,
            SqlTypes.LONGVARBINARY
        ));
        sqlTypeMap.put(JDBCType.BLOB, StandardBasicTypes.BLOB);
        sqlTypeMap.put(JDBCType.CLOB, StandardBasicTypes.CLOB);
        sqlTypeMap.put(JDBCType.NCHAR, StandardBasicTypes.CHARACTER_NCHAR);
        sqlTypeMap.put(JDBCType.NVARCHAR, StandardBasicTypes.NSTRING);
        sqlTypeMap.put(JDBCType.NCLOB, StandardBasicTypes.NCLOB);
        sqlTypeMap.put(JDBCType.TIME_WITH_TIMEZONE, StandardBasicTypes.OFFSET_TIME_WITH_TIMEZONE);
        sqlTypeMap.put(JDBCType.TIMESTAMP_WITH_TIMEZONE, StandardBasicTypes.OFFSET_DATE_TIME_WITH_TIMEZONE);
        SQL_TYPE_TYPE_MAP = Map.copyOf(sqlTypeMap);

        Map<Class<?>, BasicTypeReference<?>> javaTypeMap = new HashMap<>();
        javaTypeMap.put(String.class, StandardBasicTypes.STRING);
        javaTypeMap.put(Byte.class, StandardBasicTypes.BYTE);
        javaTypeMap.put(Short.class, StandardBasicTypes.SHORT);
        javaTypeMap.put(Integer.class, StandardBasicTypes.INTEGER);
        javaTypeMap.put(Long.class, StandardBasicTypes.LONG);
        javaTypeMap.put(Float.class, StandardBasicTypes.FLOAT);
        javaTypeMap.put(Double.class, StandardBasicTypes.DOUBLE);
        javaTypeMap.put(Character.class, StandardBasicTypes.CHARACTER);
        javaTypeMap.put(Boolean.class, StandardBasicTypes.BOOLEAN);
        javaTypeMap.put(Date.class, StandardBasicTypes.DATE);
        javaTypeMap.put(LocalDate.class, StandardBasicTypes.LOCAL_DATE);
        javaTypeMap.put(LocalDateTime.class, StandardBasicTypes.LOCAL_DATE_TIME);
        javaTypeMap.put(Instant.class, StandardBasicTypes.INSTANT);
        javaTypeMap.put(BigInteger.class, StandardBasicTypes.BIG_INTEGER);
        javaTypeMap.put(BigDecimal.class, StandardBasicTypes.BIG_DECIMAL);
        JAVA_TYPE_TYPE_MAP = Map.copyOf(javaTypeMap);

        Map<Class<?>, JDBCType> javaJDBCTypeMap = new HashMap<>();
        javaJDBCTypeMap.put(String.class, JDBCType.valueOf(StandardBasicTypes.STRING.getSqlTypeCode()));
        javaJDBCTypeMap.put(Byte.class, JDBCType.valueOf(StandardBasicTypes.BYTE.getSqlTypeCode()));
        javaJDBCTypeMap.put(Short.class, JDBCType.valueOf(StandardBasicTypes.SHORT.getSqlTypeCode()));
        javaJDBCTypeMap.put(Integer.class, JDBCType.valueOf(StandardBasicTypes.INTEGER.getSqlTypeCode()));
        javaJDBCTypeMap.put(Long.class, JDBCType.valueOf(StandardBasicTypes.LONG.getSqlTypeCode()));
        javaJDBCTypeMap.put(Float.class, JDBCType.valueOf(StandardBasicTypes.FLOAT.getSqlTypeCode()));
        javaJDBCTypeMap.put(Double.class, JDBCType.valueOf(StandardBasicTypes.DOUBLE.getSqlTypeCode()));
        javaJDBCTypeMap.put(Character.class, JDBCType.valueOf(StandardBasicTypes.CHARACTER.getSqlTypeCode()));
        javaJDBCTypeMap.put(Boolean.class, JDBCType.valueOf(StandardBasicTypes.BOOLEAN.getSqlTypeCode()));
        javaJDBCTypeMap.put(Date.class, JDBCType.valueOf(StandardBasicTypes.DATE.getSqlTypeCode()));
        javaJDBCTypeMap.put(LocalDate.class, JDBCType.valueOf(StandardBasicTypes.LOCAL_DATE.getSqlTypeCode()));
        javaJDBCTypeMap.put(LocalDateTime.class, JDBCType.valueOf(StandardBasicTypes.LOCAL_DATE_TIME.getSqlTypeCode()));
//        javaJDBCTypeMap.put(Instant.class, JDBCType.valueOf(StandardBasicTypes.INSTANT.getSqlTypeCode()));
        javaJDBCTypeMap.put(BigInteger.class, JDBCType.valueOf(StandardBasicTypes.BIG_INTEGER.getSqlTypeCode()));
        javaJDBCTypeMap.put(BigDecimal.class, JDBCType.valueOf(StandardBasicTypes.BIG_DECIMAL.getSqlTypeCode()));
        JAVA_TYPE_JDBC_TYPE_MAP = Map.copyOf(javaJDBCTypeMap);
    }

    /**
     * @see JDBCType
     */
    public static BasicTypeReference<?> jdbc2hibernate(SQLType sqlType) {
        BasicTypeReference<?> type = SQL_TYPE_TYPE_MAP.get(sqlType);
        if (Objects.nonNull(type)) {
            return type;
        }
        throw new IllegalArgumentException("SQLType: [%s] is not supported.".formatted(sqlType.getName()));
    }

    /**
     * @param type type code
     * @see java.sql.Types
     */
    public static BasicTypeReference<?> type2hibernate(int type) {
        JDBCType jdbcType = JDBCType.valueOf(type);
        BasicTypeReference<?> _type = SQL_TYPE_TYPE_MAP.get(jdbcType);
        if (Objects.nonNull(_type)) {
            return _type;
        }
        throw new IllegalArgumentException("SQLType: [%s] is not supported.".formatted(jdbcType.getName()));
    }

    public static BasicTypeReference<?> type2hibernate(Class<?> type) {
        BasicTypeReference<?> _type = JAVA_TYPE_TYPE_MAP.get(type);
        if (Objects.nonNull(_type)) {
            return _type;
        }
        throw new IllegalArgumentException("SQLType: [%s] is not supported.".formatted(type.getName()));
    }

    public static JDBCType type2jdbc(Class<?> type) {
        JDBCType _type = JAVA_TYPE_JDBC_TYPE_MAP.get(type);
        if (Objects.nonNull(_type)) {
            return _type;
        }
        throw new IllegalArgumentException("SQLType: [%s] is not supported.".formatted(type.getName()));
    }

    public static BasicTypeReference<?> simpleType2Hibernate(String type) {
        type = type.toLowerCase();
        BasicTypeReference<?> _t;
        switch (type) {
            case "boolean" -> _t = StandardBasicTypes.BOOLEAN;
            case "i32" -> _t = StandardBasicTypes.INTEGER;
            case "i64" -> _t = StandardBasicTypes.LONG;
            case "double" -> _t = StandardBasicTypes.DOUBLE;
            case "varchar" -> _t = StandardBasicTypes.STRING;
            case "text" -> _t = StandardBasicTypes.TEXT;
            case "date" -> _t = StandardBasicTypes.LOCAL_DATE_TIME;
            case "timestamp" -> _t = StandardBasicTypes.TIMESTAMP;
            default -> throw new IllegalArgumentException(type);
        }
        return _t;
    }

    public static String jdbc2SimpleType(int typeCode) {
        return jdbc2SimpleType(JDBCType.valueOf(typeCode));
    }

    public static String jdbc2SimpleType(JDBCType jdbcType) {
        return switch (jdbcType) {
            case BOOLEAN -> "boolean";
            case INTEGER -> "i32";
            case BIGINT -> "i64";
            case DOUBLE -> "double";
            case VARCHAR -> "varchar";
            case LONGVARCHAR -> "text";
            case TIMESTAMP -> "date";
            default -> throw new IllegalArgumentException("unsupported type: %s".formatted(jdbcType.getName()));
        };
    }

    public static Type jdbc2HType(int jdbcType) {
        return jdbc2HType(JDBCType.valueOf(jdbcType));
    }

    public static Type jdbc2HType(JDBCType jdbcType) {
        return switch (jdbcType) {
            case BOOLEAN -> new NamedBasicTypeImpl<>(BooleanJavaType.INSTANCE, BooleanJdbcType.INSTANCE, StandardBasicTypes.BOOLEAN.getName());
            case INTEGER -> new NamedBasicTypeImpl<>(IntegerJavaType.INSTANCE, IntegerJdbcType.INSTANCE, StandardBasicTypes.INTEGER.getName());
            case DOUBLE -> new NamedBasicTypeImpl<>(DoubleJavaType.INSTANCE, NumericJdbcType.INSTANCE, StandardBasicTypes.DOUBLE.getName());
            case BIGINT -> new NamedBasicTypeImpl<>(BigIntegerJavaType.INSTANCE, BigIntJdbcType.INSTANCE, StandardBasicTypes.BIG_INTEGER.getName());
            case VARCHAR, NVARCHAR -> new NamedBasicTypeImpl<>(StringJavaType.INSTANCE, NVarcharJdbcType.INSTANCE, StandardBasicTypes.NSTRING.getName());
            case LONGVARCHAR, LONGNVARCHAR -> new NamedBasicTypeImpl<>(StringJavaType.INSTANCE, LongVarcharJdbcType.INSTANCE, StandardBasicTypes.NTEXT.getName());
            case TIMESTAMP -> new NamedBasicTypeImpl<>(JdbcTimestampJavaType.INSTANCE, TimestampJdbcType.INSTANCE, StandardBasicTypes.TIMESTAMP.getName());
            default -> throw new IllegalArgumentException("unsupported type: %s".formatted(jdbcType.getName()));
        };
    }

}
