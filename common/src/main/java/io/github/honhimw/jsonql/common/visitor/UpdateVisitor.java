package io.github.honhimw.jsonql.common.visitor;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author hon_him
 * @since 2024-01-25
 */

public abstract class UpdateVisitor extends QueryVisitor {

    public static final String OPERATOR = "update";

    protected final UpdateVisitor sv;

    public UpdateVisitor(UpdateVisitor sv) {
        super(sv);
        this.sv = sv;
    }

    public void visitSet(String name, JsonNode value) {
        if (this.sv != null) {
            sv.visitSet(name, value);
        }
    }

    public void visitSet(String name, Object value) {
        if (this.sv != null) {
            sv.visitSet(name, value);
        }
    }

}
