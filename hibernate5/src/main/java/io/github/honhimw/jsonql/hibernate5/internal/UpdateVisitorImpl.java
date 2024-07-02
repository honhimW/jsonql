package io.github.honhimw.jsonql.hibernate5.internal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.github.honhimw.jsonql.common.JsonUtils;
import io.github.honhimw.jsonql.common.Nodes;
import io.github.honhimw.jsonql.common.visitor.UpdateVisitor;
import io.github.honhimw.jsonql.common.visitor.WhereVisitor;
import io.github.honhimw.jsonql.hibernate5.CompileUtils;
import io.github.honhimw.jsonql.hibernate5.DMLUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Table;
import org.hibernate.type.Type;

import java.util.Objects;

/**
 * @author hon_him
 * @since 2024-01-25
 */

class UpdateVisitorImpl extends UpdateVisitor {

    private final VisitContext ctx;

    public UpdateVisitorImpl(VisitContext ctx) {
        super(null);
        this.ctx = ctx;
    }

    @Override
    public void visitStart() {
    }

    @Override
    public void visitSyntax(JsonNode rootNode) {
        CompileUtils._assert(JsonUtils.isMissingOrNull(rootNode.at(Nodes.JOIN.path())), "[join] argument is not allowed in update statement, please use a simple where clause for update such as `id`.");
        CompileUtils._assert(JsonUtils.isMissingOrNull(rootNode.at(Nodes.ORDER_BY.path())), "[orderBy] argument is not allowed in update statement.");
        CompileUtils._assert(JsonUtils.isMissingOrNull(rootNode.at(Nodes.GROUP_BY.path())), "[groupBy] argument is not allowed in update statement.");
    }

    @Override
    public void visitRoot(TextNode root) {
        String rootTableName = root.asText();
        Validate.validState(StringUtils.isNotBlank(rootTableName), "CRUD operation should always have a root table.");
        Table rootTable = ctx.getTableMetaCache().buildTable(ctx.resolveTableName(rootTableName));
        ctx.setRootTable(rootTable);
    }

    @Override
    public void visitSet(String name, JsonNode value) {
        Column column = ctx.getRootTable().getColumn(Identifier.toIdentifier(CompileUtils.getFinalFieldName(name)));
        Validate.validState(Objects.nonNull(column), "column named: [%s] is not exists.".formatted(name));
        String columnName = column.getName();
        Type type = column.getValue().getType();
//        Object unwrapValue = tryCompatibility(name, type, value);
        CompileUtils.typeValidate(name, type, value, column.isNullable());
        Object unwrapValue = CompileUtils.unwrapNode(value);
        if (value.isTextual() && unwrapValue instanceof String stringValue) {
            unwrapValue = ctx.renderContext(stringValue);
        }
        final Object finalValue = unwrapValue;
        ctx.configurerUpdate(updateBuilder -> updateBuilder.applyUpdate((root, update, cb) -> update.set(root.get(columnName), finalValue)));
    }

    @Override
    public void visitSet(String name, Object value) {
        ctx.configurerUpdate(updateBuilder -> updateBuilder.applyUpdate((root, update, cb) -> update.set(root.get(CompileUtils.getFinalFieldName(name)), value)));
    }

    @Override
    public WhereVisitor visitWhere(ObjectNode where) {
        return new WhereVisitorImpl(ctx, true);
    }

    @Override
    public void visitEnd() {
        ctx.configurerUpdate(updateBuilder -> updateBuilder.applyUpdate((root, update, cb) -> update.where(ctx.getWhereStack().peek().apply(root, cb))));
        DMLUtils.UpdateBuilder updateBuilder = ctx.initUpdateBuilder();
        ctx.getUpdateConfigurer().accept(updateBuilder);
    }
}
