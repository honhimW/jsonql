package io.github.honhimw.jsonql.common.visitor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hon_him
 * @since 2024-01-30
 */

public class TraceCRUDVisitor extends CRUDVisitor {

    private static final Logger log = LoggerFactory.getLogger(TraceCRUDVisitor.class);

    public TraceCRUDVisitor(CRUDVisitor cv) {
        super(cv);
    }

    private static void _log(String format, Object... params) {
        if (log.isTraceEnabled()) {
            log.trace(format, params);
        }
    }

    private static void before(String type, String name) {
        _log("before {}-visitor:{}", type, name);
    }

    private static void after(String type, String name) {
        _log("after {}-visitor:{}", type, name);
    }

    @Override
    public void visitStart() {
        before("crud", "start");
        super.visitStart();
        after("crud", "start");
    }

    @Override
    public void visitSyntax(JsonNode rootNode) {
        before("crud", "syntax");
        super.visitSyntax(rootNode);
        after("crud", "syntax");
    }

    @Override
    public void visitEnd() {
        before("crud", "end");
        super.visitEnd();
        after("crud", "end");
    }

    @Override
    public void visitOperation(TextNode operation) {
        before("crud", "operation");
        super.visitOperation(operation);
        after("crud", "operation");
    }

    @Override
    public InsertVisitor visitInsert() {
        before("crud", "insert");
        InsertVisitor insertVisitor = super.visitInsert();
        return new InsertVisitor(insertVisitor) {
            @Override
            public void visitStart() {
                before("insert", "start");
                super.visitStart();
                after("insert", "start");
            }

            @Override
            public void visitSyntax(JsonNode rootNode) {
                before("insert", "syntax");
                super.visitSyntax(rootNode);
                after("insert", "syntax");
            }

            @Override
            public void visitEnd() {
                before("insert", "end");
                super.visitEnd();
                after("insert", "end");
                after("crud", "insert");
            }

            @Override
            public void visitRoot(TextNode root) {
                before("insert", "root");
                super.visitRoot(root);
                after("insert", "root");
            }

            @Override
            public void visitRootAlias(TextNode rootAlias) {
                before("insert", "root-alias");
                super.visitRootAlias(rootAlias);
                after("insert", "root-alias");
            }

            @Override
            public ValuesVisitor visitValues(ObjectNode values) {
                before("insert", "values");
                ValuesVisitor valuesVisitor = super.visitValues(values);
                return new ValuesVisitor(valuesVisitor) {
                    @Override
                    public void visitStart() {
                        before("values", "start");
                        super.visitStart();
                        after("values", "start");
                    }

                    @Override
                    public void visitSyntax(JsonNode rootNode) {
                        before("values", "syntax");
                        super.visitSyntax(rootNode);
                        after("values", "syntax");
                    }

                    @Override
                    public void visitEnd() {
                        before("values", "end");
                        super.visitEnd();
                        after("values", "end");
                        after("insert", "values");
                    }

                    @Override
                    public void visitNext(String name, JsonNode value) {
                        before("values", "next");
                        super.visitNext(name, value);
                        after("values", "next");
                    }
                };
            }
        };
    }

    @Override
    public SelectVisitor visitSelect() {
        before("crud", "select");
        SelectVisitor selectVisitor = super.visitSelect();
        return new SelectVisitor(selectVisitor) {
            @Override
            public void visitStart() {
                before("select", "start");
                super.visitStart();
                after("select", "start");
            }

            @Override
            public void visitSyntax(JsonNode rootNode) {
                before("select", "syntax");
                super.visitSyntax(rootNode);
                after("select", "syntax");
            }

            @Override
            public void visitEnd() {
                before("select", "end");
                super.visitEnd();
                after("select", "end");
                after("crud", "select");
            }

            @Override
            public void visitRoot(TextNode root) {
                before("select", "root");
                super.visitRoot(root);
                after("select", "root");
            }

            @Override
            public void visitRootAlias(TextNode rootAlias) {
                before("select", "root-alias");
                super.visitRootAlias(rootAlias);
                after("select", "root-alias");
            }

            @Override
            public void visitDistinct(boolean distinct) {
                before("select", "distinct");
                super.visitDistinct(distinct);
                after("select", "distinct");
            }

            @Override
            public WhereVisitor visitWhere(ObjectNode where) {
                before("select", "where");
                WhereVisitor whereVisitor = super.visitWhere(where);
                return new TraceWhereVisitor(whereVisitor) {
                    @Override
                    public void visitEnd() {
                        super.visitEnd();
                        after("select", "where");
                    }
                };
            }

            @Override
            public void visitSelection(ArrayNode selections) {
                before("select", "selection");
                super.visitSelection(selections);
                after("select", "selection");
            }

            @Override
            public void visitPage(int page, int size) {
                before("select", "page");
                super.visitPage(page, size);
                after("select", "page");
            }

            @Override
            public JoinVisitor visitJoin(JsonNode join) {
                before("select", "join");
                JoinVisitor joinVisitor = super.visitJoin(join);
                return new JoinVisitor(joinVisitor) {
                    @Override
                    public void visitStart() {
                        before("join", "start");
                        super.visitStart();
                        after("join", "start");
                    }

                    @Override
                    public void visitSyntax(JsonNode rootNode) {
                        before("join", "syntax");
                        super.visitSyntax(rootNode);
                        after("join", "syntax");
                    }

                    @Override
                    public void visitEnd() {
                        before("join", "end");
                        super.visitEnd();
                        after("join", "end");
                        after("select", "end");
                    }

                    @Override
                    public void visitNext(ObjectNode join) {
                        before("join", "next");
                        super.visitNext(join);
                        after("join", "next");
                    }
                };
            }

            @Override
            public void visitGroupBy(ArrayNode groupBys) {
                before("select", "groupBy");
                super.visitGroupBy(groupBys);
                after("select", "groupBy");
            }

            @Override
            public void visitOrderBy(ArrayNode orderBys) {
                before("select", "orderBy");
                super.visitOrderBy(orderBys);
                after("select", "orderBy");
            }
        };
    }

    @Override
    public UpdateVisitor visitUpdate() {
        before("crud", "update");
        UpdateVisitor updateVisitor = super.visitUpdate();

        return new UpdateVisitor(updateVisitor) {
            @Override
            public void visitStart() {
                before("update", "start");
                super.visitStart();
                after("update", "start");
            }

            @Override
            public void visitSyntax(JsonNode rootNode) {
                before("update", "syntax");
                super.visitSyntax(rootNode);
                after("update", "syntax");
            }

            @Override
            public void visitEnd() {
                before("update", "end");
                super.visitEnd();
                after("update", "end");
                after("crud", "update");
            }

            @Override
            public void visitRoot(TextNode root) {
                before("update", "root");
                super.visitRoot(root);
                after("update", "root");
            }

            @Override
            public void visitRootAlias(TextNode rootAlias) {
                before("update", "root-alias");
                super.visitRootAlias(rootAlias);
                after("update", "root-alias");
            }

            @Override
            public WhereVisitor visitWhere(ObjectNode where) {
                before("update", "where");
                WhereVisitor whereVisitor = super.visitWhere(where);
                return new TraceWhereVisitor(whereVisitor) {
                    @Override
                    public void visitEnd() {
                        super.visitEnd();
                        after("update", "where");
                    }
                };
            }

            @Override
            public void visitSet(String name, Object value) {
                before("update", "set");
                super.visitSet(name, value);
                after("update", "set");
            }
        };
    }

    @Override
    public DeleteVisitor visitDelete() {
        before("crud", "delete");
        DeleteVisitor deleteVisitor = super.visitDelete();
        return new DeleteVisitor(deleteVisitor) {
            @Override
            public void visitStart() {
                before("delete", "start");
                super.visitStart();
                after("delete", "start");
            }

            @Override
            public void visitSyntax(JsonNode rootNode) {
                before("delete", "syntax");
                super.visitSyntax(rootNode);
                after("delete", "syntax");
            }

            @Override
            public void visitEnd() {
                before("delete", "end");
                super.visitEnd();
                after("delete", "end");
                after("crud", "delete");
            }

            @Override
            public void visitRoot(TextNode root) {
                before("delete", "root");
                super.visitRoot(root);
                after("delete", "root");
            }

            @Override
            public void visitRootAlias(TextNode rootAlias) {
                before("delete", "root-alias");
                super.visitRootAlias(rootAlias);
                after("delete", "root-alias");
            }

            @Override
            public WhereVisitor visitWhere(ObjectNode where) {
                before("delete", "where");
                WhereVisitor whereVisitor = super.visitWhere(where);
                return new TraceWhereVisitor(whereVisitor) {
                    @Override
                    public void visitEnd() {
                        super.visitEnd();
                        after("delete", "where");
                    }
                };
            }
        };
    }

    public static class TraceWhereVisitor extends WhereVisitor {

        private final int depth;
        private final String indent;

        public TraceWhereVisitor(WhereVisitor wv) {
            this(wv, 0);
        }

        public TraceWhereVisitor(WhereVisitor wv, int depth) {
            super(wv);
            this.depth = depth;
            this.indent = "-".repeat(depth);
        }

        @Override
        public void visitStart() {
            before(indent + "where", "start");
            super.visitStart();
            after(indent + "where", "start");
        }

        @Override
        public void visitSyntax(JsonNode rootNode) {
            before(indent + "where", "syntax");
            super.visitSyntax(rootNode);
            after(indent + "where", "syntax");
        }

        @Override
        public void visitEnd() {
            before(indent + "where", "end");
            super.visitEnd();
            after(indent + "where", "end");
        }

        @Override
        public WhereVisitor visitAnd(ArrayNode next) {
            before(indent + "where", "and");
            WhereVisitor whereVisitor = super.visitAnd(next);
            return new TraceWhereVisitor(whereVisitor, depth + 1) {
                @Override
                public void visitEnd() {
                    super.visitEnd();
                    after(indent + "where", "and");
                }
            };
        }

        @Override
        public WhereVisitor visitOr(ArrayNode next) {
            before(indent + "where", "or");
            WhereVisitor whereVisitor = super.visitOr(next);
            return new TraceWhereVisitor(whereVisitor, depth + 1) {
                @Override
                public void visitEnd() {
                    super.visitEnd();
                    after(indent + "where", "and");
                }
            };
        }

        @Override
        public void visitNext(ObjectNode next) {
            before(indent + "where", "next");
            super.visitNext(next);
            after(indent + "where", "next");
        }

        @Override
        public void visitAfterNext(ObjectNode next) {
            before(indent + "where", "after-next");
            super.visitAfterNext(next);
            after(indent + "where", "after-next");
        }

        @Override
        public void visitCondition(String name, String operation, Object value) {
            before(indent + "where", "condition");
            super.visitCondition(name, operation, value);
            after(indent + "where", "condition");
        }

    }

}
