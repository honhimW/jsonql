package io.github.honhimw.jsonql.hibernate6.internal;

import com.fasterxml.jackson.databind.node.TextNode;
import io.github.honhimw.jsonql.common.visitor.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

/**
 * @author hon_him
 * @since 2024-01-25
 */

final class CRUDVisitorImpl extends CRUDVisitor {

    public static final String[] OPERATIONS = {"select", "insert", "update", "delete", "logic_delete"};

    private final VisitContext ctx;

    public CRUDVisitorImpl(VisitContext ctx) {
        super(null);
        this.ctx = ctx;
    }

    @Override
    public void visitOperation(TextNode operation) {
        String text = operation.asText();
        Validate.validState(StringUtils.equalsAnyIgnoreCase(text, OPERATIONS), "unknown operation: %s".formatted(text));
    }

    @Override
    public InsertVisitor visitInsert() {
        return new InsertVisitorImpl(ctx);
    }

    @Override
    public SelectVisitor visitSelect() {
        return new SelectVisitorImpl(ctx);
    }

    @Override
    public UpdateVisitor visitUpdate() {
        return new UpdateVisitorImpl(ctx);
    }

    @Override
    public DeleteVisitor visitDelete() {
        return new DeleteVisitorImpl(ctx);
    }

    @Override
    public void visitRoot(TextNode root) {
        super.visitRoot(root);
    }

    @Override
    public void visitRootAlias(TextNode rootAlias) {
        super.visitRootAlias(rootAlias);
    }
}
