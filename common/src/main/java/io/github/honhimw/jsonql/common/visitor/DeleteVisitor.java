package io.github.honhimw.jsonql.common.visitor;

/**
 * @author hon_him
 * @since 2024-01-25
 */

public abstract class DeleteVisitor extends QueryVisitor {

    public static final String OPERATOR = "delete";

    protected final DeleteVisitor sv;

    public DeleteVisitor(DeleteVisitor sv) {
        super(sv);
        this.sv = sv;
    }


}
