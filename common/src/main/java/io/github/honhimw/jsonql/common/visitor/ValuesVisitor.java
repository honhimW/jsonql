package io.github.honhimw.jsonql.common.visitor;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author hon_him
 * @since 2024-01-25
 */

public abstract class ValuesVisitor extends BaseVisitor {

    protected final ValuesVisitor vv;

    public ValuesVisitor(ValuesVisitor vv) {
        super(vv);
        this.vv = vv;
    }

    public void visitNext(String name, JsonNode value) {
        if (this.vv != null) {
            vv.visitNext(name, value);
        }
    }

}
