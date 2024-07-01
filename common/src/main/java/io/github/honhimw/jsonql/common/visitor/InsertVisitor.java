package io.github.honhimw.jsonql.common.visitor;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

/**
 * @author hon_him
 * @since 2024-01-25
 */

public abstract class InsertVisitor extends CRUDVisitor {

    public static final String OPERATOR = "insert";

    protected final InsertVisitor iv;

    public InsertVisitor(InsertVisitor iv) {
        super(iv);
        this.iv = iv;
    }

    @Override
    public final void visitOperation(TextNode operation) {
        String text = operation.asText();
        Validate.validState(StringUtils.equalsIgnoreCase(OPERATOR, text), "InsertVisitor operator should always be 'insert'.");
    }

    public ValuesVisitor visitValues(ObjectNode values) {
        return this.iv != null ? this.iv.visitValues(values) : null;
    }


}
