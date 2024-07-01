package io.github.honhimw.jsonql.common.visitor;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author hon_him
 * @since 2024-01-25
 */

public abstract class JoinVisitor extends BaseVisitor {

    protected final JoinVisitor jv;

    public JoinVisitor(JoinVisitor jv) {
        super(jv);
        this.jv = jv;
    }

    public void visitNext(ObjectNode join) {
        if (this.jv != null) {
            jv.visitNext(join);
        }
    }

}
