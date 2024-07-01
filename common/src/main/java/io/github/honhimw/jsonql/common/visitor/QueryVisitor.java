package io.github.honhimw.jsonql.common.visitor;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author hon_him
 * @since 2024-01-25
 */

public abstract class QueryVisitor extends CRUDVisitor {

    protected final QueryVisitor qv;

    public QueryVisitor(QueryVisitor qv) {
        super(qv);
        this.qv = qv;
    }

    public WhereVisitor visitWhere(ObjectNode where) {
        if (this.qv != null) {
            return this.qv.visitWhere(where);
        }
        return null;
    }

    public WhereVisitor visitWhere(boolean queryDeleted, ObjectNode where) {
        if (this.qv != null) {
            return this.qv.visitWhere(queryDeleted, where);
        }
        return null;
    }

}
