package io.github.honhimw.jsonql.hibernate6.internal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.github.honhimw.jsonql.common.JsonUtils;
import io.github.honhimw.jsonql.common.Nodes;
import io.github.honhimw.jsonql.common.visitor.DeleteVisitor;
import io.github.honhimw.jsonql.common.visitor.WhereVisitor;
import io.github.honhimw.jsonql.hibernate6.CompileUtils;
import io.github.honhimw.jsonql.hibernate6.DMLUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.hibernate.mapping.Table;

/**
 * @author hon_him
 * @since 2024-01-25
 */

class DeleteVisitorImpl extends DeleteVisitor {

    private final VisitContext ctx;

    DeleteVisitorImpl(VisitContext ctx) {
        super(null);
        this.ctx = ctx;
    }

    @Override
    public void visitSyntax(JsonNode rootNode) {
        CompileUtils._assert(rootNode.at(Nodes.TABLE.path()).isTextual(), "[/table] must be a textual value.");
        CompileUtils._assert(rootNode.at(Nodes.OPERATION.path()).isTextual(), "[/operation] must be a textual value.");
        JsonNode alias = rootNode.at(Nodes.ALIAS.path());
        if (!alias.isMissingNode()) {
            CompileUtils._assert(alias.isTextual(), "[alias] must be a textual value.");
        }
        CompileUtils._assert(JsonUtils.isMissingOrNull(rootNode.at(Nodes.JOIN.path())), "[join] argument is not allowed in delete statement, please use a simple where clause for update such as `id`.");
        CompileUtils._assert(JsonUtils.isMissingOrNull(rootNode.at(Nodes.SELECTIONS.path())), "[selections] argument is not allowed in delete statement.");
        CompileUtils._assert(JsonUtils.isMissingOrNull(rootNode.at(Nodes.ORDER_BY.path())), "[orderBy] argument is not allowed in delete statement.");
        CompileUtils._assert(JsonUtils.isMissingOrNull(rootNode.at(Nodes.GROUP_BY.path())), "[groupBy] argument is not allowed in delete statement.");
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
        ctx.configurerDelete(deleteBuilder -> deleteBuilder.applyDelete((root, delete, cb) -> root.alias(rootTableAlias)));
    }

    @Override
    public WhereVisitor visitWhere(ObjectNode where) {
        return new WhereVisitorImpl(ctx, true);
    }

    @Override
    public void visitEnd() {
        ctx.configurerDelete(deleteBuilder -> deleteBuilder.applyDelete((root, delete, cb) -> delete.where(ctx.getWhereStack().peek().apply(root, cb))));
        DMLUtils.DeleteBuilder deleteBuilder = ctx.initDeleteBuilder();
        ctx.getDeleteConfigurer().accept(deleteBuilder);
    }
}
