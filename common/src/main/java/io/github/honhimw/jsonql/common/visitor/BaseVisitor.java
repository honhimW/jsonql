package io.github.honhimw.jsonql.common.visitor;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author hon_him
 * @since 2024-01-26
 */

public abstract class BaseVisitor {

    protected final BaseVisitor bv;

    public BaseVisitor(BaseVisitor bv) {
        this.bv = bv;
    }

    public void visitStart() {
        if (this.bv != null) {
            bv.visitStart();
        }
    }

    public void visitSyntax(JsonNode rootNode) {
        if (this.bv != null) {
            bv.visitSyntax(rootNode);
        }
    }

    public void visitEnd() {
        if (this.bv != null) {
            bv.visitEnd();
        }
    }

}
