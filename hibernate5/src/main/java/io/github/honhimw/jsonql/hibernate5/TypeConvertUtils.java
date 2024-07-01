package io.github.honhimw.jsonql.hibernate5;

import org.hibernate.type.*;

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

    private static final Map<SQLType, Type> SQL_TYPE_TYPE_MAP;
    private static final Map<Class<?>, Type> JAVA_TYPE_TYPE_MAP;

    static {
        Map<SQLType, Type> sqlTypeMap = new HashMap<>();
        sqlTypeMap.put(JDBCType.BIT, BooleanType.INSTANCE);
        sqlTypeMap.put(JDBCType.TINYINT, BooleanType.INSTANCE);
        sqlTypeMap.put(JDBCType.SMALLINT, ShortType.INSTANCE);
        sqlTypeMap.put(JDBCType.INTEGER, IntegerType.INSTANCE);
        sqlTypeMap.put(JDBCType.BIGINT, BigIntegerType.INSTANCE);
        sqlTypeMap.put(JDBCType.FLOAT, FloatType.INSTANCE);
        sqlTypeMap.put(JDBCType.DOUBLE, DoubleType.INSTANCE);
        sqlTypeMap.put(JDBCType.DECIMAL, BigDecimalType.INSTANCE);
        sqlTypeMap.put(JDBCType.CHAR, CharacterType.INSTANCE);
        sqlTypeMap.put(JDBCType.VARCHAR, StringType.INSTANCE);
        sqlTypeMap.put(JDBCType.LONGVARCHAR, TextType.INSTANCE);
        sqlTypeMap.put(JDBCType.DATE, DateType.INSTANCE);
        sqlTypeMap.put(JDBCType.TIME, TimeType.INSTANCE);
        sqlTypeMap.put(JDBCType.TIMESTAMP, TimestampType.INSTANCE);
        sqlTypeMap.put(JDBCType.BINARY, BinaryType.INSTANCE);
        sqlTypeMap.put(JDBCType.VARBINARY, BinaryType.INSTANCE);
        sqlTypeMap.put(JDBCType.LONGVARBINARY, BinaryType.INSTANCE);
        sqlTypeMap.put(JDBCType.BLOB, BlobType.INSTANCE);
        sqlTypeMap.put(JDBCType.CLOB, ClobType.INSTANCE);
        sqlTypeMap.put(JDBCType.BOOLEAN, BooleanType.INSTANCE);
        sqlTypeMap.put(JDBCType.NCHAR, CharacterNCharType.INSTANCE);
        sqlTypeMap.put(JDBCType.NVARCHAR, StringNVarcharType.INSTANCE);
        sqlTypeMap.put(JDBCType.NCLOB, NClobType.INSTANCE);
        sqlTypeMap.put(JDBCType.TIME_WITH_TIMEZONE, TimeType.INSTANCE);
        sqlTypeMap.put(JDBCType.TIMESTAMP_WITH_TIMEZONE, TimestampType.INSTANCE);
        SQL_TYPE_TYPE_MAP = Map.copyOf(sqlTypeMap);

        Map<Class<?>, Type> javaTypeMap = new HashMap<>();
        javaTypeMap.put(String.class, StringType.INSTANCE);
        javaTypeMap.put(Byte.class, ByteType.INSTANCE);
        javaTypeMap.put(Short.class, ShortType.INSTANCE);
        javaTypeMap.put(Integer.class, IntegerType.INSTANCE);
        javaTypeMap.put(Long.class, LongType.INSTANCE);
        javaTypeMap.put(Float.class, FloatType.INSTANCE);
        javaTypeMap.put(Double.class, DoubleType.INSTANCE);
        javaTypeMap.put(Character.class, CharacterType.INSTANCE);
        javaTypeMap.put(Boolean.class, BooleanType.INSTANCE);
        javaTypeMap.put(Date.class, DateType.INSTANCE);
        javaTypeMap.put(LocalDate.class, LocalDateType.INSTANCE);
        javaTypeMap.put(LocalDateTime.class, LocalDateTimeType.INSTANCE);
        javaTypeMap.put(Instant.class, InstantType.INSTANCE);
        javaTypeMap.put(BigInteger.class, BigIntegerType.INSTANCE);
        javaTypeMap.put(BigDecimal.class, BigDecimalType.INSTANCE);
        JAVA_TYPE_TYPE_MAP = Map.copyOf(javaTypeMap);
    }

    /**
     * @see JDBCType
     */
    public static Type jdbc2hibernate(SQLType sqlType) {
        Type type = SQL_TYPE_TYPE_MAP.get(sqlType);
        if (Objects.nonNull(type)) {
            return type;
        }
        throw new IllegalArgumentException("SQLType: [%s] is not supported.".formatted(sqlType.getName()));
    }

    /**
     * @param type type code
     * @see java.sql.Types
     */
    public static Type type2hibernate(int type) {
        JDBCType jdbcType = JDBCType.valueOf(type);
        Type _type = SQL_TYPE_TYPE_MAP.get(jdbcType);
        if (Objects.nonNull(_type)) {
            return _type;
        }
        throw new IllegalArgumentException("SQLType: [%s] is not supported.".formatted(jdbcType.getName()));
    }

    public static Type type2hibernate(Class<?> type) {
        Type _type = JAVA_TYPE_TYPE_MAP.get(type);
        if (Objects.nonNull(_type)) {
            return _type;
        }
        throw new IllegalArgumentException("SQLType: [%s] is not supported.".formatted(type.getName()));
    }

    public static Type simpleType2Hibernate(String type) {
        type = type.toLowerCase();
        Type _t;
        switch (type) {
            case "boolean" -> _t = BooleanType.INSTANCE;
            case "i32" -> _t = IntegerType.INSTANCE;
            case "i64" -> _t = LongType.INSTANCE;
            case "double" -> _t = DoubleType.INSTANCE;
            case "varchar" -> _t = StringNVarcharType.INSTANCE;
            case "text" -> _t = TextType.INSTANCE;
            case "date" -> _t = LocalDateTimeType.INSTANCE;
            case "timestamp" -> _t = DbTimestampType.INSTANCE;
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
            case DOUBLE -> "double";
            case BIGINT -> "i64";
            case VARCHAR -> "varchar";
            case LONGVARCHAR -> "text";
            case TIMESTAMP -> "date";
            default -> throw new IllegalArgumentException("unsupported type: %s".formatted(jdbcType.getName()));
        };
    }

}
