package io.github.honhimw.jsonql.common.visitor;

import com.fasterxml.jackson.databind.node.TextNode;

/**
 * @author hon_him
 * @since 2024-01-25
 */

public abstract class CRUDVisitor extends BaseVisitor {

    protected final CRUDVisitor cv;

    public CRUDVisitor(CRUDVisitor cv) {
        super(cv);
        this.cv = cv;
    }

    public void visitOperation(TextNode operation) {
        if (this.cv != null) {
            cv.visitOperation(operation);
        }
    }

    public InsertVisitor visitInsert() {
        if (this.cv != null) {
            return cv.visitInsert();
        }
        return null;
    }

    public SelectVisitor visitSelect() {
        if (this.cv != null) {
            return cv.visitSelect();
        }
        return null;
    }

    public UpdateVisitor visitUpdate() {
        if (this.cv != null) {
            return cv.visitUpdate();
        }
        return null;
    }

    public DeleteVisitor visitDelete() {
        if (this.cv != null) {
            return cv.visitDelete();
        }
        return null;
    }

    public void visitRoot(TextNode root) {
        if (this.cv != null) {
            cv.visitRoot(root);
        }
    }

    public void visitRootAlias(TextNode rootAlias) {
        if (this.cv != null) {
            cv.visitRootAlias(rootAlias);
        }
    }

}
