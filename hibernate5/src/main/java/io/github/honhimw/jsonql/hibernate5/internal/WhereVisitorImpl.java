package io.github.honhimw.jsonql.hibernate5.internal;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.honhimw.jsonql.common.visitor.WhereVisitor;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * @author hon_him
 * @since 2024-01-25
 */

class WhereVisitorImpl extends WhereVisitor {

    private final VisitContext ctx;
    private final boolean and;

    WhereVisitorImpl(VisitContext ctx, boolean and) {
        super(null);
        this.ctx = ctx;
        this.and = and;
    }

    @Override
    public void visitStart() {
        ctx.getWhereStack().push(and);
    }

    @Override
    public WhereVisitor visitAnd(ArrayNode next) {
        return new WhereVisitorImpl(ctx, true);
    }

    @Override
    public WhereVisitor visitOr(ArrayNode next) {
        return new WhereVisitorImpl(ctx, false);
    }

    @Override
    public void visitNext(ObjectNode conditionNode) {
        ctx.getWhereStack().push(true);
    }

    @Override
    public void visitAfterNext(ObjectNode next) {
        ctx.getWhereStack().pop();
    }

    @Override
    public void visitCondition(String name, String operation, Object value) {
        BiFunction<Root<?>, CriteriaBuilder, Predicate> predicateBuilder = buildSinglePredicate(name, operation, value);
        ctx.getWhereStack().addPredicate(predicateBuilder);
    }

    @Override
    public void visitEnd() {
        ctx.getWhereStack().pop();
        if (ctx.getWhereStack().size() == 1) {
            BiFunction<Root<?>, CriteriaBuilder, Predicate> peek = ctx.getWhereStack().peek();
            ctx.configurerSelect(selectBuilder -> selectBuilder.applyQuery((root, query, cb) -> query.where(peek.apply(root, cb))));
            ctx.configurerUpdate(updateBuilder -> updateBuilder.applyUpdate((root, update, cb) -> update.where(peek.apply(root, cb))));
            ctx.configurerDelete(deleteBuilder -> deleteBuilder.applyDelete((root, delete, cb) -> delete.where(peek.apply(root, cb))));
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private BiFunction<Root<?>, CriteriaBuilder, Predicate> buildSinglePredicate(String fieldName, String operatorName, Object valParam) {
        if (Objects.isNull(valParam) && !"$eqn".equalsIgnoreCase(operatorName) &&
            !"$nen".equalsIgnoreCase(operatorName)) {

            return (root, cb) -> cb.isTrue(cb.literal(true));
        }

        return (root, cb) -> {
            Predicate predicate;
            switch (operatorName) {
                case "=", "$eq" -> predicate = cb.equal(ctx.getPath(root, fieldName), valParam);
                case "$ne" -> predicate = cb.notEqual(ctx.getPath(root, fieldName), valParam);
                case "$gt" -> predicate = cb.greaterThan(ctx.getPath(root, fieldName), (Comparable) valParam);
                case "$lt" -> predicate = cb.lessThan(ctx.getPath(root, fieldName), (Comparable) valParam);
                case "$ge" -> predicate = cb.greaterThanOrEqualTo(ctx.getPath(root, fieldName), (Comparable) valParam);
                case "$le" -> predicate = cb.lessThanOrEqualTo(ctx.getPath(root, fieldName), (Comparable) valParam);
                case "like" -> predicate = cb.like(ctx.getPath(root, fieldName), "%" + valParam + "%");
                case "like$" -> predicate = cb.like(ctx.getPath(root, fieldName), valParam + "%");
                case "in" -> {
                    CriteriaBuilder.In<Object> in = cb.in(ctx.getPath(root, fieldName));
                    Collection<?> values = (Collection<?>) valParam;
                    for (Object value : values) {
                        in.value(value);
                    }
                    predicate = in;
                }
                case "$eqn" -> predicate = cb.isNull(ctx.getPath(root, fieldName));
                case "$nen" -> predicate = cb.isNotNull(ctx.getPath(root, fieldName));
                default -> throw new IllegalArgumentException("unknown operator: [%s]".formatted(operatorName));
            }
            return predicate;
        };
    }

    private BiFunction<Root<?>, CriteriaBuilder, Predicate> merge(List<BiFunction<Root<?>, CriteriaBuilder, Predicate>> subPredicates, boolean and) {
        return (root, cb) -> {
            List<Predicate> ps = new ArrayList<>(subPredicates.size());
            for (BiFunction<Root<?>, CriteriaBuilder, Predicate> _subPredicate : subPredicates) {
                Predicate apply = _subPredicate.apply(root, cb);
                ps.add(apply);
            }
            if (and) {
                return cb.and(ps.toArray(Predicate[]::new));
            } else {
                return cb.or(ps.toArray(Predicate[]::new));
            }
        };
    }

}
