package io.github.honhimw.jsonql.hibernate5;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.honhimw.jsonql.common.NodeKeys;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author hon_him
 * @since 2024-06-28
 */

@SuppressWarnings("unused")
public class JsonQLBuilder {

    private final ObjectNode root;

    private JsonQLBuilder(ObjectNode root) {
        this.root = root;
    }

    public static JsonQLBuilder newInstance(ObjectNode rootNode) {
        return new JsonQLBuilder(rootNode);
    }

    private static JsonPointer toPointer(String field) {
        return JsonPointer.compile(JsonPointer.SEPARATOR + field);
    }

    public Select select() {
        return new Select(root);
    }

    public String select(Consumer<Select> builder) {
        Select select = new Select(root);
        builder.accept(select);
        return select.build();
    }

    public interface StatementBuilder {

        String build();

    }

    public static class Select implements StatementBuilder {

        private static final String OPERATION = "select";

        private final ObjectNode node;

        private Select(ObjectNode node) {
            this.node = node;
            node.put(NodeKeys.OPERATION, OPERATION);
        }

        public Select selection(Consumer<SelectionClause> selectionBuilder) {
            ArrayNode arrayNode = node.withArray(toPointer(NodeKeys.COLUMNS));
            SelectionClause selectionClause = new SelectionClause(arrayNode);
            selectionBuilder.accept(selectionClause);
            return this;
        }

        public Select from(String from) {
            node.put(NodeKeys.TABLE, from);
            return this;
        }

        public Select join(Consumer<JoinClause> joinBuilder) {
            ArrayNode joinArrayNode = node.withArray(toPointer(NodeKeys.JOIN));
            JoinClause joinClause = new JoinClause(joinArrayNode);
            joinBuilder.accept(joinClause);
            return this;
        }

        public Select where(Consumer<WhereClause> whereBuilder) {
            ObjectNode where = node.withObject(toPointer(NodeKeys.CONDITION));
            WhereClause whereClause = new WhereClause(where);
            whereBuilder.accept(whereClause);
            return this;
        }

        public Select groupBy(Collection<String> columns) {
            if (CollectionUtils.isNotEmpty(columns)) {
                ArrayNode groupBy = node.withArray(toPointer(NodeKeys.GROUP_BY));
                for (String column : columns) {
                    groupBy.add(column);
                }
            }
            return this;
        }

        public Select groupBy(String... columns) {
            if (ArrayUtils.isNotEmpty(columns)) {
                ArrayNode groupBy = node.withArray(toPointer(NodeKeys.GROUP_BY));
                for (String column : columns) {
                    groupBy.add(column);
                }
            }
            return this;
        }

        public Select count(boolean count) {
            node.put(NodeKeys.COUNT, count);
            return this;
        }

        public Select page(int no, int size) {
            Validate.validState(no >= 0, "page no must >= 0");
            Validate.validState(0 < size && size <= 5000, "page size must between 0 and 5000");
            node.put(NodeKeys.page, no);
            node.put(NodeKeys.pageSize, size);
            return this;
        }

        public Select desc(Collection<String> columns) {
            if (CollectionUtils.isNotEmpty(columns)) {
                ArrayNode groupBy = node.withArray(toPointer(NodeKeys.ORDER_BY_DESC));
                for (String column : columns) {
                    groupBy.add(column);
                }
            }
            return this;
        }

        public Select desc(String... columns) {
            if (ArrayUtils.isNotEmpty(columns)) {
                ArrayNode groupBy = node.withArray(toPointer(NodeKeys.ORDER_BY_DESC));
                for (String column : columns) {
                    groupBy.add(column);
                }
            }
            return this;
        }

        @Override
        public String build() {
            return node.toString();
        }

        public static class SelectionClause {

            private final ArrayNode selectionNode;

            private SelectionClause(ArrayNode selectionNode) {
                this.selectionNode = selectionNode;
            }

            public SelectionClause all() {
                selectionNode.removeAll();
                return this;
            }

            public SelectionClause add(String selection) {
                selectionNode.add(selection);
                return this;
            }

            public SelectionClause add(String selection, String alias) {
                selectionNode.add("%s as %s".formatted(selection, alias));
                return this;
            }
        }

        public static class JoinClause {

            private final ArrayNode join;

            private JoinClause(ArrayNode join) {
                this.join = join;
            }

            /**
             * default
             */
            public JoinClause inner(Consumer<JoinMeta> builder) {
                ObjectNode objectNode = join.addObject();
                JoinMeta joinMeta = new JoinMeta(NodeKeys.INNER, objectNode);
                builder.accept(joinMeta);
                return this;
            }

            public JoinClause left(Consumer<JoinMeta> builder) {
                ObjectNode objectNode = join.addObject();
                JoinMeta joinMeta = new JoinMeta(NodeKeys.LEFT, objectNode);
                builder.accept(joinMeta);
                return this;
            }

            public JoinClause right(Consumer<JoinMeta> builder) {
                ObjectNode objectNode = join.addObject();
                JoinMeta joinMeta = new JoinMeta(
                    NodeKeys.RIGHT, objectNode);
                builder.accept(joinMeta);
                return this;
            }

            public static class JoinMeta {

                private final ObjectNode meta;

                private JoinMeta(String joinType, ObjectNode meta) {
                    this.meta = meta;
                    meta.put(NodeKeys.TYPE, joinType);
                }

                public JoinMeta handleTable(String handleTable) {
                    meta.put(NodeKeys.HANDLE_TABLE, handleTable);
                    return this;
                }

                public JoinMeta referencedTable(String referencedTable) {
                    meta.put(NodeKeys.TABLE, referencedTable);
                    return this;
                }

                public JoinMeta on(String joinColumn, String referencedColumn) {
                    meta.put(NodeKeys.JOIN_COLUMN, joinColumn);
                    meta.put(NodeKeys.REFERENCED_COLUMN, referencedColumn);
                    return this;
                }

                public JoinMeta groupBy(Collection<String> columns) {
                    if (CollectionUtils.isNotEmpty(columns)) {
                        ArrayNode groupBy = meta.withArray(toPointer(NodeKeys.GROUP_BY));
                        for (String column : columns) {
                            groupBy.add(column);
                        }
                    }
                    return this;
                }

                public JoinMeta groupBy(String... columns) {
                    if (ArrayUtils.isNotEmpty(columns)) {
                        ArrayNode groupBy = meta.withArray(toPointer(NodeKeys.GROUP_BY));
                        for (String column : columns) {
                            groupBy.add(column);
                        }
                    }
                    return this;
                }

            }


        }


        public static class WhereClause {

            private final WhereClause parent;

            private final JsonNode where;

            private WhereClause(JsonNode where) {
                this.parent = null;
                this.where = where;
            }

            private WhereClause(WhereClause parent, JsonNode where) {
                this.parent = parent;
                this.where = where;
            }

            public WhereClause equalTo(String field, Object value) {
                if (where instanceof ObjectNode objectNode) {
                    objectNode.putPOJO(field, value);
                } else if (where instanceof ArrayNode arrayNode) {
                    ObjectNode objectNode = arrayNode.addObject();
                    objectNode.putPOJO(field, value);
                }
                return this;
            }

            public WhereClause unequal(String field, Object value) {
                ObjectNode with = objectNode(field);
                with.putPOJO("$ne", value);
                return this;
            }

            public WhereClause gt(String field, Object value) {
                ObjectNode with = objectNode(field);
                with.putPOJO("$gt", value);
                return this;
            }

            public WhereClause ge(String field, Object value) {
                ObjectNode with = objectNode(field);
                with.putPOJO("$ge", value);
                return this;
            }

            public WhereClause lt(String field, Object value) {
                ObjectNode with = objectNode(field);
                with.putPOJO("lt", value);
                return this;
            }

            public WhereClause le(String field, Object value) {
                ObjectNode with = objectNode(field);
                with.putPOJO("$le", value);
                return this;
            }

            public WhereClause contain(String field, String value) {
                ObjectNode with = objectNode(field);
                with.putPOJO("like", value);
                return this;
            }

            public WhereClause startWith(String field, String value) {
                ObjectNode with = objectNode(field);
                with.putPOJO("like$", value);
                return this;
            }

            public WhereClause in(String field, Collection<?> values) {
                if (Objects.isNull(values) || values.isEmpty()) {
                    return this;
                }
                ObjectNode with = objectNode(field);
                ArrayNode arrayNode = with.withArray(toPointer("in"));
                for (Object value : values) {
                    arrayNode.addPOJO(value);
                }
                return this;
            }

            public WhereClause in(String field, Object... values) {
                if (Objects.nonNull(values)) {
                    return in(field, Arrays.stream(values).collect(Collectors.toList()));
                }
                return this;
            }

            public WhereClause isNull(String field) {
                ObjectNode with = objectNode(field);
                with.putNull("$eqn");
                return this;
            }

            public WhereClause notNull(String field) {
                ObjectNode with = objectNode(field);
                with.putNull("$nen");
                return this;
            }

            /**
             * @return child clause
             * @see #parent()
             */
            public WhereClause and(Consumer<WhereClause> sub) {
                ArrayNode and;
                if (where instanceof ArrayNode arrayNode) {
                    ObjectNode objectNode = arrayNode.addObject();
                    and = objectNode.withArray(toPointer("and"));
                } else {
                    and = where.withArray(toPointer("and"));
                }
                WhereClause subWhereClause = new WhereClause(this, and);
                sub.accept(subWhereClause);
                return this;
            }

            /**
             * @return child clause
             * @see #parent()
             */
            public WhereClause or(Consumer<WhereClause> sub) {
                ArrayNode or;
                if (where instanceof ArrayNode arrayNode) {
                    ObjectNode objectNode = arrayNode.addObject();
                    or = objectNode.withArray(toPointer("or"));
                } else {
                    or = where.withArray(toPointer("or"));
                }
                WhereClause subWhereClause = new WhereClause(this, or);
                sub.accept(subWhereClause);
                return this;
            }

            public WhereClause parent() {
                if (Objects.nonNull(parent)) {
                    return parent;
                } else {
                    throw new IllegalArgumentException("current node is the very root of where clause");
                }
            }

            private ObjectNode objectNode(String field) {
                if (where instanceof ObjectNode objectNode) {
                    return objectNode.withObject(toPointer(field));
                } else if (where instanceof ArrayNode arrayNode) {
                    ObjectNode objectNode = arrayNode.addObject();
                    return objectNode.withObject(toPointer(field));
                } else {
                    throw new IllegalArgumentException("current node neither objectNode nor arrayNode");
                }
            }

        }

    }

}

