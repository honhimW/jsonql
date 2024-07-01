package io.github.honhimw.jsonql.hibernate5.internal;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.github.honhimw.jsonql.common.visitor.InsertVisitor;
import io.github.honhimw.jsonql.common.visitor.ValuesVisitor;
import io.github.honhimw.jsonql.hibernate5.DMLUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.hibernate.mapping.Table;

/**
 * @author hon_him
 * @since 2024-01-25
 */

final class InsertVisitorImpl extends InsertVisitor {

    private final VisitContext ctx;

    InsertVisitorImpl(VisitContext ctx) {
        super(null);
        this.ctx = ctx;
    }

    @Override
    public void visitRoot(TextNode root) {
        String rootTableName = root.asText();
        Validate.validState(StringUtils.isNotBlank(rootTableName), "CRUD operation should always have a root table.");
        Table rootTable = ctx.getTableMetaCache().buildTable(ctx.resolveTableName(rootTableName));

        ctx.setRootTable(rootTable);
    }

    @Override
    public void visitRootAlias(TextNode rootAlias) {
        String rootTableAlias = rootAlias.asText();
        ctx.configurerInsert(insertBuilder -> insertBuilder.applyInsert((root, insert, cb) -> root.alias(rootTableAlias)));
    }

    @Override
    public ValuesVisitor visitValues(ObjectNode values) {
        return new ValuesVisitorImpl(ctx);
    }

    @Override
    public void visitEnd() {
        DMLUtils.InsertBuilder insertBuilder = ctx.initInsertBuilder();
        ctx.getInsertConfigurer().accept(insertBuilder);
    }
}
