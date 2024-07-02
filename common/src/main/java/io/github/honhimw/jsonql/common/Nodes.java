package io.github.honhimw.jsonql.common;

import com.fasterxml.jackson.databind.node.JsonNodeType;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * @author hon_him
 * @since 2024-07-02
 */

public enum Nodes {

    OPERATION("operation", JsonNodeType.STRING),
    CONDITION("condition", JsonNodeType.OBJECT),
    DATA("data", JsonNodeType.OBJECT),
    EFFECT_MAX_ROWS("effectMaxRows", JsonNodeType.NUMBER),
    SELECTIONS("selections", JsonNodeType.ARRAY),
    COUNT("count", JsonNodeType.BOOLEAN),
    DISTINCT("distinct", JsonNodeType.BOOLEAN),
    PAGE("page", JsonNodeType.NUMBER),
    PAGE_SIZE("pageSize", JsonNodeType.NUMBER),
    JOIN("join", JsonNodeType.ARRAY),
    TYPE("type", JsonNodeType.STRING),
    INNER("inner", JsonNodeType.NULL),
    LEFT("left", JsonNodeType.NULL),
    RIGHT("right", JsonNodeType.NULL),
    TABLE("table", JsonNodeType.STRING),
    ALIAS("alias", JsonNodeType.STRING),
    HANDLE_TABLE("handleTable", JsonNodeType.STRING),
    JOIN_COLUMN("joinColumn", JsonNodeType.STRING),
    REFERENCED_COLUMN("referencedColumn", JsonNodeType.STRING),
    GROUP_BY("groupBy", JsonNodeType.ARRAY),
    ORDER_BY("orderBy", JsonNodeType.ARRAY),
    QUERY_DELETED("queryDeleted", JsonNodeType.BOOLEAN),
    EQUAL("=", JsonNodeType.POJO),
    NOT_EQUAL("!=", JsonNodeType.POJO),
    GT(">", JsonNodeType.POJO),
    LT("<", JsonNodeType.POJO),
    GE(">=", JsonNodeType.POJO),
    LE("<=", JsonNodeType.POJO),
    IN("in", JsonNodeType.ARRAY),
    CONTAINS("contains", JsonNodeType.STRING),
    STARTS_WITH("starts", JsonNodeType.STRING),
    ENDS_WITH("ends", JsonNodeType.STRING),
    IS_NULL("null", JsonNodeType.NULL),
    NOT_NULL("notnull", JsonNodeType.NULL),
    AND("and", JsonNodeType.OBJECT),
    OR("or", JsonNodeType.OBJECT),
    ;

    private final String key;

    private final String path;

    private final JsonNodeType jsonNodeType;

    public String key() {
        return key;
    }

    public String path() {
        return path;
    }

    public JsonNodeType jsonType() {
        return jsonNodeType;
    }

    public String negKey() {
        return "!" + key;
    }

    Nodes(String key, JsonNodeType jsonNodeType) {
        this.key = key;
        this.path = "/" + key;
        this.jsonNodeType = jsonNodeType;
    }

    private static final Map<String, Nodes> CACHE;

    public static Nodes of(String key) {
        return CACHE.get(key);
    }

    public static Nodes of(String key, Nodes defaultValue) {
        return CACHE.getOrDefault(key, defaultValue);
    }

    static {
        Map<String, Nodes> map = new HashMap<>();
        for (Nodes value : Nodes.values()) {
            map.put(value.key(), value);
        }
        CACHE = Map.copyOf(map);
    }
}
