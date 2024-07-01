package io.github.honhimw.jsonql.common.visitor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * @author hon_him
 * @since 2024-01-25
 */

public abstract class SelectVisitor extends QueryVisitor {

    public static final String OPERATOR = "select";

    protected final SelectVisitor sv;

    public SelectVisitor(SelectVisitor sv) {
        super(sv);
        this.sv = sv;
    }

    public void visitDistinct(boolean distinct) {
        if (this.sv != null) {
            sv.visitDistinct(distinct);
        }
    }

    public void visitSelection(ArrayNode selections) {
        if (this.sv != null) {
            sv.visitSelection(selections);
        }
    }

    public void visitPage(int page, int size) {
        if (this.sv != null) {
            this.sv.visitPage(page, size);
        }
    }

    public JoinVisitor visitJoin(JsonNode join) {
        if (this.sv != null) {
            return this.sv.visitJoin(join);
        }
        return null;
    }

    public void visitGroupBy(ArrayNode groupBys) {
        if (this.sv != null) {
            sv.visitGroupBy(groupBys);
        }
    }

    public void visitOrderBy(ArrayNode orderBys) {
        if (this.sv != null) {
            sv.visitOrderBy(orderBys);
        }
    }


}
