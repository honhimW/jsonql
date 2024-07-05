package io.github.honhimw.jsonql.hibernate6;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.POJONode;
import com.google.common.base.CaseFormat;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.hibernate.query.BindableType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author hon_him
 * @since 2024-06-28
 */

public class CompileUtils {

    public static final String LOGIC_DELETE_FIELD = "deleted";

    public static String getFinalTableName(String tableName) {
//        tableName = camelToSnake(tableName);
        return tableName;
    }

    public static String getFinalFieldName(String fieldName) {
//        fieldName = camelToSnake(fieldName);
        return substringAfterLast(fieldName, ".");
    }

    public static String snakeToCamel(String input) {
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, input);
    }

    public static String camelToSnake(String input) {
        if (StringUtils.isBlank(input)) {
            return input;
        }
        if (StringUtils.startsWith(input, "#raw#")) {
            return input.substring(5);
        }
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, input);
    }

    public static String substringAfterLast(final String str, final String separator) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }
        if (StringUtils.isEmpty(separator)) {
            return StringUtils.EMPTY;
        }
        final int pos = str.lastIndexOf(separator);
        if (pos == StringUtils.INDEX_NOT_FOUND) {
            return str;
        }
        if (pos == str.length() - separator.length()) {
            return StringUtils.EMPTY;
        }
        return str.substring(pos + separator.length());
    }

    public static Object unwrapNode(JsonNode jsonNode) {
        if (jsonNode.isTextual()) {
            return jsonNode.asText();
        } else if (jsonNode.isInt()) {
            return jsonNode.asInt();
        } else if (jsonNode.isLong()) {
            return jsonNode.asLong();
        } else if (jsonNode.isFloat()) {
            return jsonNode.floatValue();
        } else if (jsonNode.isDouble()) {
            return jsonNode.doubleValue();
        } else if (jsonNode.isBoolean()) {
            return jsonNode.asBoolean();
        } else if (jsonNode.isArray()) {
            List<Object> paramList = new ArrayList<>();
            Iterator<JsonNode> elements = jsonNode.elements();
            while (elements.hasNext()) {
                JsonNode next = elements.next();
                paramList.add(unwrapNode(next));
            }
            return paramList;
        } else if (jsonNode.isNull()) {
            return null;
        } else if (jsonNode.isPojo()) {
            POJONode pojo = jsonNode.require();
            return pojo.getPojo();
        } else {
            throw new IllegalArgumentException();
        }
    }

    public static void _assert(boolean status) {
        _assert(status, "illegal status");
    }

    public static void _assert(boolean status, String message) {
        Validate.validState(status, message);
    }

    public static void typeValidate(String name, BindableType<?> bindableType, JsonNode value, boolean nullable) {
        if (nullable && (value.isNull() || value.isMissingNode())) {
            return;
        }
        Class<?> type = bindableType.getBindableJavaType();

        if (String.class.isAssignableFrom(type)) {
            valid(value.isTextual(), "column[%s] require a STRING type, but [%s] is provided.".formatted(name, value.getNodeType().name()));
        } else if (LocalDateTime.class.isAssignableFrom(type)) {
            valid(value.isTextual() || value.isPojo(), "column[%s] require a STRING/DATE type, but [%s] is provided.".formatted(name, value.getNodeType().name()));
        } else if (Boolean.class.isAssignableFrom(type)) {
            valid(value.isBoolean(), "column[%s] require a BOOLEAN type, but [%s] is provided.".formatted(name, value.getNodeType().name()));
        } else if (Integer.class.isAssignableFrom(type)) {
            valid(value.isIntegralNumber() || value.isBigDecimal(), "column[%s] require a INTEGER type, but [%s] is provided.".formatted(name, value.getNodeType().name()));
        } else if (Long.class.isAssignableFrom(type)) {
            valid(value.isIntegralNumber() || value.isBigDecimal(), "column[%s] require a LONG type, but [%s] is provided.".formatted(name, value.getNodeType().name()));
        } else if (Double.class.isAssignableFrom(type)) {
            valid(value.isNumber(), "column[%s] require a DOUBLE type, but [%s] is provided.".formatted(name, value.getNodeType().name()));
        }
    }

    public static void valid(boolean flag, String message) {
        if (!flag) {
            throw new IllegalArgumentException(message);
        }
    }

}
