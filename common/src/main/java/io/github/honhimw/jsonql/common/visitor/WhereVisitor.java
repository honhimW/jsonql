package io.github.honhimw.jsonql.common.visitor;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author hon_him
 * @since 2024-01-25
 */

public abstract class WhereVisitor extends BaseVisitor {

    protected final WhereVisitor wv;

    public WhereVisitor(WhereVisitor wv) {
        super(wv);
        this.wv = wv;
    }

    public WhereVisitor visitAnd(ArrayNode next) {
        if (this.wv != null) {
            return wv.visitAnd(next);
        }
        return null;
    }

    public WhereVisitor visitOr(ArrayNode next) {
        if (this.wv != null) {
            return wv.visitOr(next);
        }
        return null;
    }

    public void visitNext(ObjectNode next) {
        if (this.wv != null) {
            wv.visitNext(next);
        }
    }

    public void visitAfterNext(ObjectNode next) {
        if (this.wv != null) {
            wv.visitAfterNext(next);
        }
    }

    public void visitCondition(String name, String operation, Object value) {
        if (this.wv != null) {
            wv.visitCondition(name, operation, value);
        }
    }

}
