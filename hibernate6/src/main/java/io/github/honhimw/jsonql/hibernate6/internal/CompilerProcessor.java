package io.github.honhimw.jsonql.hibernate6.internal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.github.honhimw.jsonql.common.JsonUtils;
import io.github.honhimw.jsonql.common.Nodes;
import io.github.honhimw.jsonql.common.visitor.*;
import io.github.honhimw.jsonql.hibernate6.CompileUtils;
import io.github.honhimw.jsonql.hibernate6.DMLUtils;
import io.github.honhimw.jsonql.hibernate6.meta.SQLHolder;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @author hon_him
 * @since 2024-02-20
 */

class CompilerProcessor {

    private final VisitContext ctx;

    private final JsonNode rootNode;

    private final CRUDVisitor crudVisitor;

    CompilerProcessor(VisitContext ctx, JsonNode rootNode, CRUDVisitor crudVisitor) {
        this.ctx = ctx;
        this.rootNode = rootNode;
        this.crudVisitor = crudVisitor;
    }

    List<SQLHolder> process() {
        crudVisitor.visitStart();
        TextNode operationNode = rootNode.at(Nodes.OPERATION.path()).require();
        crudVisitor.visitOperation(operationNode);
        String operation = operationNode.asText();
        if (StringUtils.equalsIgnoreCase(operation, SelectVisitor.OPERATOR)) {
            SelectVisitor selectVisitor = crudVisitor.visitSelect();
            compileSelect(rootNode, selectVisitor);
            crudVisitor.visitEnd();
            DMLUtils.SelectBuilder selectBuilder = ctx.getSelectBuilder();
            DMLUtils.Tuple2<String, List<Object>> tuple = selectBuilder.jdbcQL();
            boolean count = rootNode.at(Nodes.COUNT.path()).asBoolean(false);
            if (count) {
                DMLUtils.Tuple2<String, List<Object>> countTuple = selectBuilder.countJdbcQL();
                return List.of(new SQLHolder(tuple._1(), tuple._2()), new SQLHolder(countTuple._1(), countTuple._2()));
            } else {
                return List.of(new SQLHolder(tuple._1(), tuple._2()));
            }
        } else if (StringUtils.equalsIgnoreCase(operation, InsertVisitor.OPERATOR)) {
            InsertVisitor insertVisitor = crudVisitor.visitInsert();
            compileInsert(rootNode, insertVisitor);
            crudVisitor.visitEnd();
            DMLUtils.InsertBuilder insertBuilder = ctx.getInsertBuilder();
            DMLUtils.Tuple2<String, List<Object>> tuple = insertBuilder.jdbcQL();
            return List.of(new SQLHolder(tuple._1(), tuple._2()));
        } else if (StringUtils.equalsIgnoreCase(operation, UpdateVisitor.OPERATOR)) {
            UpdateVisitor updateVisitor = crudVisitor.visitUpdate();
            compileUpdate(rootNode, updateVisitor);
            crudVisitor.visitEnd();
            DMLUtils.UpdateBuilder updateBuilder = ctx.getUpdateBuilder();
            DMLUtils.Tuple2<String, List<Object>> tuple = updateBuilder.jdbcQL();
            return List.of(new SQLHolder(tuple._1(), tuple._2()));
        } else if (StringUtils.equalsIgnoreCase(operation, DeleteVisitor.OPERATOR)) {
            DeleteVisitor deleteVisitor = crudVisitor.visitDelete();
            compileDelete(rootNode, deleteVisitor);
            crudVisitor.visitEnd();
            DMLUtils.DeleteBuilder deleteBuilder = ctx.getDeleteBuilder();
            DMLUtils.Tuple2<String, List<Object>> tuple = deleteBuilder.jdbcQL();
            return List.of(new SQLHolder(tuple._1(), tuple._2()));
        } else if (StringUtils.equalsIgnoreCase(operation, "logic_delete")) {
            UpdateVisitor updateVisitor = crudVisitor.visitUpdate();
            ObjectNode _rootNode = rootNode.require();
            _rootNode.remove(Nodes.DATA.key());
            ObjectNode data = rootNode.withObject(Nodes.DATA.path());
            data.putPOJO(CompileUtils.LOGIC_DELETE_FIELD, ctx.now());
            compileUpdate(rootNode, updateVisitor);
            crudVisitor.visitEnd();
            DMLUtils.UpdateBuilder updateBuilder = ctx.getUpdateBuilder();
            DMLUtils.Tuple2<String, List<Object>> tuple = updateBuilder.jdbcQL();
            return List.of(new SQLHolder(tuple._1(), tuple._2()));
        }

        throw new IllegalArgumentException("unrecognizable operation");
    }

    private void compileInsert(JsonNode rootNode, InsertVisitor insertVisitor) {
        if (ctx.isEmbeddedDB()) {
            ObjectNode dataNode = rootNode.withObject(Nodes.DATA.path());
//            dataNode.remove("created_at");
//            dataNode.putPOJO("created_at", ctx.now());
        }
        insertVisitor.visitStart();
        insertVisitor.visitRoot(rootNode.at(Nodes.TABLE.path()).require());
        JsonNode aliasNode = rootNode.at(Nodes.ALIAS.path());
        if (aliasNode.isTextual()) {
            insertVisitor.visitRootAlias(aliasNode.require());
        }
        ObjectNode data = rootNode.at(Nodes.DATA.path()).require();
        ValuesVisitor valuesVisitor = insertVisitor.visitValues(data);
        valuesVisitor.visitStart();
        data.fields().forEachRemaining(entry -> valuesVisitor.visitNext(entry.getKey(), entry.getValue()));
        valuesVisitor.visitEnd();
        insertVisitor.visitEnd();
    }

    private void compileDelete(JsonNode rootNode, DeleteVisitor deleteVisitor) {
        deleteVisitor.visitStart();
        deleteVisitor.visitRoot(rootNode.at(Nodes.TABLE.path()).require());
        JsonNode aliasNode = rootNode.at(Nodes.ALIAS.path());
        if (aliasNode.isTextual()) {
            deleteVisitor.visitRootAlias(aliasNode.require());
        }
        ObjectNode conditionNode = getConditionNode(rootNode);
        WhereVisitor whereVisitor = deleteVisitor.visitWhere(conditionNode);
        whereVisitor.visitStart();
        compileWhere(whereVisitor, conditionNode);
        whereVisitor.visitEnd();
        deleteVisitor.visitEnd();
    }

    private void compileUpdate(JsonNode rootNode, UpdateVisitor updateVisitor) {
        if (ctx.isEmbeddedDB()) {
            ObjectNode dataNode = rootNode.withObject(Nodes.DATA.path());
//            dataNode.remove("updated_at");
//            dataNode.putPOJO("updated_at", ctx.now());
        }
        updateVisitor.visitStart();
        updateVisitor.visitSyntax(rootNode);
        updateVisitor.visitRoot(rootNode.at(Nodes.TABLE.path()).require());
        JsonNode aliasNode = rootNode.at(Nodes.ALIAS.path());
        if (aliasNode.isTextual()) {
            updateVisitor.visitRootAlias(aliasNode.require());
        }
        ObjectNode conditionNode = getConditionNode(rootNode);
        ObjectNode dataNode = rootNode.at(Nodes.DATA.path()).require();
        dataNode.fields().forEachRemaining(entry -> {
            String key = entry.getKey();
            JsonNode value = entry.getValue();
            updateVisitor.visitSet(key, value);
        });

        WhereVisitor whereVisitor = updateVisitor.visitWhere(conditionNode);
        whereVisitor.visitStart();
        compileWhere(whereVisitor, conditionNode);
        whereVisitor.visitEnd();
        updateVisitor.visitEnd();
    }

    private void compileSelect(JsonNode rootNode, SelectVisitor selectVisitor) {
        selectVisitor.visitStart();
        selectVisitor.visitRoot(rootNode.at(Nodes.TABLE.path()).require());
        JsonNode aliasNode = rootNode.at(Nodes.ALIAS.path());
        if (aliasNode.isTextual()) {
            selectVisitor.visitRootAlias(aliasNode.require());
        }

        boolean distinct = rootNode.at(Nodes.DISTINCT.path()).asBoolean(false);
        selectVisitor.visitDistinct(distinct);

        JsonNode selectionsNode = rootNode.at(Nodes.SELECTIONS.path());
        selectVisitor.visitSelection(selectionsNode.isArray() ? selectionsNode.require() : null);

        ObjectNode conditionNode = getConditionNode(rootNode);
        JsonNode pageNode = rootNode.at(Nodes.PAGE.path());
        JsonNode pageSizeNode = rootNode.at(Nodes.PAGE_SIZE.path());

        if (!JsonUtils.isMissingOrNull(pageNode) && !JsonUtils.isMissingOrNull(pageSizeNode)) {
            ObjectNode _rootNode = rootNode.require();
            _rootNode.put(Nodes.COUNT.key(), true);
            selectVisitor.visitPage(pageNode.asInt(1), pageSizeNode.asInt(20));
        }


        JsonNode joinNode = rootNode.at(Nodes.JOIN.path());
        if (joinNode.isArray() && !joinNode.isEmpty()) {
            JoinVisitor joinVisitor = selectVisitor.visitJoin(joinNode);
            joinVisitor.visitStart();
            joinVisitor.visitSyntax(rootNode);
            ArrayNode arrayNode = joinNode.require();
            arrayNode.forEach(node -> joinVisitor.visitNext(node.require()));
            joinVisitor.visitEnd();
        }

        JsonNode groupByNode = rootNode.at(Nodes.GROUP_BY.path());
        if (groupByNode.isArray() && !groupByNode.isEmpty()) {
            selectVisitor.visitGroupBy(groupByNode.require());
        }

        JsonNode orderByNode = rootNode.at(Nodes.ORDER_BY.path());
        if (orderByNode.isArray() && !orderByNode.isEmpty()) {
            selectVisitor.visitOrderBy(orderByNode.require());
        }

        boolean queryDeleted = false;
        JsonNode queryDeletedNode = rootNode.at(Nodes.QUERY_DELETED.path());
        if (queryDeletedNode.isBoolean()) {
            queryDeleted = queryDeletedNode.asBoolean();
        }

        WhereVisitor whereVisitor = selectVisitor.visitWhere(queryDeleted, conditionNode);
        whereVisitor.visitStart();
        compileWhere(whereVisitor, conditionNode);
        whereVisitor.visitEnd();

        selectVisitor.visitEnd();
    }

    private void compileWhere(WhereVisitor whereVisitor, ObjectNode conditionNode) {
        whereVisitor.visitNext(conditionNode);
        conditionNode.fields().forEachRemaining(entry -> {
            String key = entry.getKey();
            JsonNode value = entry.getValue();
            if (StringUtils.equalsIgnoreCase(key, Nodes.AND.key())) {
                CompileUtils._assert(value.isArray(), "syntax error, 'and' value must be an array type value.");
                ArrayNode arrayNode = value.require();
                WhereVisitor _subWhereVisitor = whereVisitor.visitAnd(arrayNode);
                _subWhereVisitor.visitStart();
                arrayNode.forEach(node -> {
                    CompileUtils._assert(node.isObject(), "conditions in 'and' must be object type values.");
                    ObjectNode objectNode = node.require();
                    compileWhere(_subWhereVisitor, objectNode);
                });
                _subWhereVisitor.visitEnd();
            } else if (StringUtils.equalsIgnoreCase(key, Nodes.OR.key())) {
                CompileUtils._assert(value.isArray(), "syntax error, 'or' value must be an array type value.");
                ArrayNode arrayNode = value.require();
                WhereVisitor _subWhereVisitor = whereVisitor.visitOr(arrayNode);
                _subWhereVisitor.visitStart();
                arrayNode.forEach(node -> {
                    CompileUtils._assert(node.isObject(), "conditions in 'or' must be object type values.");
                    ObjectNode objectNode = node.require();
                    compileWhere(_subWhereVisitor, objectNode);
                });
                _subWhereVisitor.visitEnd();
            } else if (value.isObject()) {
                ObjectNode objectNode = value.require();
                whereVisitor.visitNext(objectNode);
                objectNode.fields().forEachRemaining(_entry -> {
                    String operation = _entry.getKey();
                    JsonNode value1 = _entry.getValue();
                    Object unwrapValue = CompileUtils.unwrapNode(value1);
                    if (value1.isTextual() && unwrapValue instanceof String stringValue) {
                        unwrapValue = ctx.renderContext(stringValue);
                    }
                    whereVisitor.visitCondition(key, operation, unwrapValue);
                });
                whereVisitor.visitAfterNext(objectNode);
            } else {
                whereVisitor.visitCondition(key, Nodes.EQUAL.key(), CompileUtils.unwrapNode(value));
            }
        });
        whereVisitor.visitAfterNext(conditionNode);
    }

    private ObjectNode getConditionNode(JsonNode rootNode) {
        JsonNode conditionNode = rootNode.at(Nodes.CONDITION.path());
        if (conditionNode.isObject()) {
            return conditionNode.require();
        } else if (conditionNode.isMissingNode()) {
            return rootNode.withObject(Nodes.CONDITION.path());
        } else if (conditionNode.isNull()) {
            return rootNode.withObject(Nodes.CONDITION.path(), JsonNode.OverwriteMode.NULLS, false);
        } else {
            throw new IllegalArgumentException("/condition argument must be an object.");
        }
    }

}
