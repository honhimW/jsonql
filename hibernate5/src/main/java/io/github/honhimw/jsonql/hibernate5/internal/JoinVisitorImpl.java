package io.github.honhimw.jsonql.hibernate5.internal;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.honhimw.jsonql.common.Nodes;
import io.github.honhimw.jsonql.common.visitor.JoinVisitor;
import io.github.honhimw.jsonql.hibernate5.CompileUtils;
import io.github.honhimw.jsonql.hibernate5.TableHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.hibernate.mapping.Table;

import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import java.util.Map;

/**
 * @author hon_him
 * @since 2024-01-25
 */

class JoinVisitorImpl extends JoinVisitor {

    private final VisitContext ctx;

    JoinVisitorImpl(VisitContext ctx) {
        super(null);
        this.ctx = ctx;
    }

    @Override
    public void visitStart() {
        ctx.setJoinClause((root, cb) -> {
        });
    }

    @Override
    public void visitNext(ObjectNode join) {
        String handleTable = join.at(Nodes.HANDLE_TABLE.path()).asText();
        String joinColumn = join.at(Nodes.JOIN_COLUMN.path()).asText();
        String type = join.at(Nodes.TYPE.path()).asText(Nodes.INNER.key());
        String joinTable = join.at(Nodes.TABLE.path()).asText();
        String alias = join.at(Nodes.ALIAS.path()).asText();
        String referencedColumn = join.at(Nodes.REFERENCED_COLUMN.path()).asText();

        final String finalHandleTable;
        if (StringUtils.isNotBlank(handleTable)) {
            finalHandleTable = ctx.resolveTableName(handleTable);
        } else {
            finalHandleTable = ctx.getRootTable().getName();
        }
        final String finalJoinColumn = CompileUtils.getFinalFieldName(joinColumn);
        final String finalJoinTable = ctx.resolveTableName(joinTable);
        final String finalReferencedColumn = CompileUtils.getFinalFieldName(referencedColumn);
        final JoinType joinType;
        switch (Nodes.of(type, Nodes.INNER)) {
            case LEFT -> joinType = JoinType.LEFT;
            case RIGHT -> joinType = JoinType.RIGHT;
            default -> joinType = JoinType.INNER;
        }

        Map<String, Table> tableMap = ctx.getTableMap();

        final Table _handleTable = tableMap.computeIfAbsent(finalHandleTable, s -> ctx.getTableMetaCache().buildTable(finalHandleTable));
        final Table _joinTable = tableMap.computeIfAbsent(finalJoinTable, s -> ctx.getTableMetaCache().buildTable(finalJoinTable));

        TableHelper tableHelper = TableHelper.of(_handleTable);
        tableHelper.addForeign(_joinTable, Map.of(finalJoinColumn, finalReferencedColumn));

        final boolean isRoot = _handleTable == ctx.getRootTable();

        ctx.setJoinClause(ctx.getJoinClause().andThen((root, cb) -> {
            From<?, ?> from;
            if (isRoot) {
                from = ctx.forRoot(root).computeIfAbsent(finalJoinTable, _table -> {
                    Join<?, ?> _join = root.join(_table, joinType);
                    setAlias(_join, alias);
                    Path<Object> joinId = _join.get(finalReferencedColumn);
                    _join.on(cb.equal(joinId, root.get(finalJoinColumn)));
                    return _join;
                });
            } else {
                From<?, ?> _handle = ctx.forRoot(root).computeIfAbsent(finalHandleTable, root::join);
                from = ctx.forRoot(root).computeIfAbsent(finalJoinTable, _table -> {
                    Join<?, ?> _join = _handle.join(_table, joinType);
                    setAlias(_join, alias);
                    Path<Object> joinId = _join.get(finalReferencedColumn);
                    _join.on(cb.equal(joinId, _handle.get(finalJoinColumn)));
                    return _join;
                });
            }
            String _alias = from.getAlias();
            if (StringUtils.isNotBlank(_alias)) {
                ctx.forRootAlias(root).put(_alias, from);
            } else {
                ctx.forRootAlias(root).put(joinTable, from);
            }
        }));
    }

    @Override
    public void visitEnd() {
        ctx.configurerSelect(selectBuilder -> selectBuilder.applyQuery((root, query, cb) -> ctx.getJoinClause().accept(root, cb)));
    }

    private void setAlias(From<?, ?> from, String alias) {
        if (StringUtils.isNotBlank(alias)) {
            Validate.validState(!StringUtils.startsWith(alias, "g_a_"), "Illegal alias naming.");
            from.alias(alias);
        }
    }
}
