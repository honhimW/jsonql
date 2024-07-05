package io.github.honhimw.jsonql.hibernate6.internal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.github.honhimw.jsonql.common.Nodes;
import io.github.honhimw.jsonql.common.visitor.JoinVisitor;
import io.github.honhimw.jsonql.common.visitor.SelectVisitor;
import io.github.honhimw.jsonql.common.visitor.WhereVisitor;
import io.github.honhimw.jsonql.hibernate6.CompileUtils;
import io.github.honhimw.jsonql.hibernate6.DMLUtils;
import jakarta.persistence.criteria.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Table;
import org.hibernate.query.criteria.internal.PlainExpression;
import org.hibernate.query.criteria.internal.WildcardSelection;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author hon_him
 * @since 2024-01-25
 */

class SelectVisitorImpl extends SelectVisitor {

    private static final Pattern SIMPLE_FIELD = Pattern.compile("^[a-zA-Z_][a-zA-Z_0-9]*$");
    private static final Pattern FIELD_WITH_REF = Pattern.compile("^(?<ref>[a-zA-Z_][a-zA-Z_0-9]*)\\.(?<field>[a-zA-Z_][a-zA-Z_0-9]*)$");

    private final VisitContext ctx;

    SelectVisitorImpl(VisitContext ctx) {
        super(null);
        this.ctx = ctx;
    }

    @Override
    public void visitSyntax(JsonNode rootNode) {
        CompileUtils._assert(rootNode.isObject());
    }

    @Override
    public void visitStart() {
    }

    @Override
    public void visitRoot(TextNode root) {
        String rootTableName = root.asText();
        Validate.validState(StringUtils.isNotBlank(rootTableName), "CRUD operation should always have a root table.");
        Table rootTable = ctx.getTableMetaCache().buildTable(ctx.resolveTableName(rootTableName));
        ctx.setRootTable(rootTable);
        ctx.configurerSelect(selectBuilder -> selectBuilder.applyQuery((root1, query, cb) -> {
            ctx.forRoot(root1).put(rootTable.getName(), root1);
            ctx.forRootAlias(root1).put(rootTable.getName(), root1);
        }));
    }

    @Override
    public void visitRootAlias(TextNode rootAlias) {
        String rootTableAlias = rootAlias.asText();
        ctx.configurerSelect(selectBuilder -> selectBuilder.applyQuery((root, query, cb) -> {
            root.alias(rootTableAlias);
            ctx.forRootAlias(root).put(rootTableAlias, root);
        }));
    }

    @Override
    public void visitDistinct(boolean distinct) {
        ctx.configurerSelect(selectBuilder -> selectBuilder.applyQuery((root, query, cb) -> query.distinct(distinct)));
    }

    @Override
    public void visitSelection(ArrayNode selections) {
        if (Objects.isNull(selections)) {
            ctx.configurerSelect(selectBuilder -> selectBuilder.applyQuery((root, query, cb) -> query.multiselect(WildcardSelection.INSTANCE)));
        } else {
            List<String> selectionColumns = new ArrayList<>();
            selections.forEach(node -> selectionColumns.add(node.asText()));
            ctx.configurerSelect(selectBuilder -> selectBuilder.applyQuery((root, query, cb) -> {
                List<Selection<?>> _selections = new ArrayList<>();
                for (String selectionColumn : selectionColumns) {
                    _selections.add(getExpression(root, cb, selectionColumn));
                }
                query.multiselect(_selections);
            }));
        }
    }

    @Override
    public WhereVisitor visitWhere(ObjectNode where) {
        return visitWhere(false, where);
    }

    @Override
    public WhereVisitor visitWhere(boolean queryDeleted, ObjectNode where) {
        if (!queryDeleted) {
            Table rootTable = ctx.getRootTable();
            Column logicDelete = rootTable.getColumn(Identifier.toIdentifier(CompileUtils.LOGIC_DELETE_FIELD));
            if (Objects.nonNull(logicDelete)) {
                ObjectNode deleteCondition = where.withObject("/" + CompileUtils.LOGIC_DELETE_FIELD);
                deleteCondition.putNull(Nodes.IS_NULL.key());
            }
        }
        return new WhereVisitorImpl(ctx, true);
    }

    @Override
    public void visitPage(int page, int size) {
        CompileUtils._assert(page > 0, "page must greater than 0.");
        CompileUtils._assert(0 < size && size < 5000, "pageSize must greater than 0 and less than 5000.");
        ctx.configurerSelect(selectBuilder -> selectBuilder.applyPage(page, size));
    }

    @Override
    public void visitGroupBy(ArrayNode groupBys) {
        List<String> groupByColumns = new ArrayList<>();
        groupBys.forEach(node -> groupByColumns.add(node.textValue()));
        ctx.configurerSelect(selectBuilder -> selectBuilder.applyQuery((root, query, cb) -> {
            List<Expression<?>> groups = new ArrayList<>();
            for (String groupByColumn : groupByColumns) {
                groups.add(getExpression(root, cb, groupByColumn));
            }
            query.groupBy(groups);
        }));
    }

    @Override
    public void visitOrderBy(ArrayNode orderBys) {
        List<String> orderByColumns = new ArrayList<>();
        orderBys.forEach(node -> orderByColumns.add(node.textValue()));
        ctx.configurerSelect(selectBuilder -> selectBuilder.applyQuery((root, query, cb) -> {
            List<Order> orders = new ArrayList<>();
            Selection<?> selection = query.getSelection();
            Map<String, Expression<?>> plainExpressions = new HashMap<>();
            if (selection.isCompoundSelection()) {
                List<Selection<?>> compoundSelectionItems = selection.getCompoundSelectionItems();
                for (Selection<?> compoundSelectionItem : compoundSelectionItems) {
                    if (compoundSelectionItem instanceof PlainExpression plainExpression) {
                        String alias = plainExpression.getAlias();
                        if (StringUtils.isNotBlank(alias)) {
                            plainExpressions.put(alias, plainExpression);
                        }
                    }
                }
            }
            for (String text : orderByColumns) {
                boolean desc = StringUtils.startsWith(text, "-");
                text = StringUtils.removeStart(text, "+");
                text = StringUtils.removeStart(text, "-");
                Expression<?> expression = plainExpressions.get(text);
                if (Objects.isNull(expression)) {
                    expression = getExpression(root, cb, text);
                }
                Order order = cb.asc(expression);
                if (desc) {
                    order = order.reverse();
                }
                orders.add(order);
            }
            query.orderBy(orders);
        }));
    }

    @Override
    public JoinVisitor visitJoin(JsonNode join) {
        return new JoinVisitorImpl(ctx);
    }

    @Override
    public void visitEnd() {
        ctx.configurerSelect(selectBuilder -> selectBuilder.applyQuery((root, query, cb) -> query.where(ctx.getWhereStack().peek().apply(root, cb))));
        DMLUtils.SelectBuilder selectBuilder = ctx.initSelectBuilder();
        ctx.getSelectConfigurer().accept(selectBuilder);
    }

    private Expression<?> getExpression(Root<?> root, CriteriaBuilder cb, String plain) {
        Matcher simpleMatcher = SIMPLE_FIELD.matcher(plain);
        if (simpleMatcher.find()) {
            return root.get(plain);
        } else {
            Matcher aliasMatcher = PlainExpression.ALIAS.matcher(plain);
            if (aliasMatcher.find()) {
                PlainExpression plainExpression = new PlainExpression(cb, plain);
                plainExpression.alias(aliasMatcher.group(PlainExpression.ARG));
                return plainExpression;
            } else {
                Matcher refMatcher = FIELD_WITH_REF.matcher(plain);
                if (refMatcher.find()) {
                    String ref = refMatcher.group("ref");
                    String field = refMatcher.group("field");
                    From<?, ?> from = ctx.forRootAlias(root).get(ref);
                    Objects.requireNonNull(from, "ref: [%s] does not exists.".formatted(ref));
                    return from.get(CompileUtils.getFinalFieldName(field));
                } else {
                    return new PlainExpression(cb, plain);
                }
            }
        }
    }

}
