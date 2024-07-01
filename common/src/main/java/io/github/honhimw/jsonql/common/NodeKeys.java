package io.github.honhimw.jsonql.common;

/**
 * @author hon_him
 * @since 2024-06-28
 */

public interface NodeKeys {

    String OPERATION = "operation";

    String CONDITION = "condition";

    String DATA = "data";

    String COLUMNS = "columns";

    String COUNT = "count";

    String page = "page";

    String pageSize = "pageSize";

    String JOIN = "join";

    String TYPE = "type";

    String INNER = "inner";

    String LEFT = "left";

    String RIGHT = "right";

    String TABLE = "table";

    String HANDLE_TABLE = "handleTable";

    String JOIN_COLUMN = "joinColumn";

    String REFERENCED_COLUMN = "referencedColumn";

    String GROUP_BY = "groupBy";

    String QUERY_DELETED = "queryDeleted";

    String ORDER_BY_DESC = "orderByDesc";

    String EQUAL = "$eq";

    String NOT_EQUAL = "$ne";

    String GT = "$gt";

    String LT = "$lt";

    String GE = "$ge";

    String LE = "$le";

    String LiKE = "like";

    String LIKE$ = "like$";

    String IN = "$in";

    String IS_NULL = "$eqn";

    String NOT_NULL = "$nen";

    String AND = "and";

    String OR = "or";

}
