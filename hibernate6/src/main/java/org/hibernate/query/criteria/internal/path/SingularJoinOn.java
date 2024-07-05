package org.hibernate.query.criteria.internal.path;

import org.hibernate.query.criteria.internal.FromImplementor;
import org.hibernate.query.criteria.internal.compile.RenderingContext;
import org.hibernate.query.criteria.internal.predicate.PredicateImplementor;

import jakarta.persistence.criteria.Join;
import java.util.HashSet;
import java.util.Set;

/**
 * @author hon_him
 * @since 2024-06-28
 */

public class SingularJoinOn<O, X> extends DelegateSingularAttributeJoin<O, X> {

    public SingularJoinOn(SingularAttributeJoin<O, X> delegate) {
        super(delegate);
    }

    @Override
    public String renderTableExpression(RenderingContext renderingContext) {
        prepareAlias(renderingContext);
        ((FromImplementor<?, ?>) getParent()).prepareAlias(renderingContext);
        StringBuilder tableExpression = new StringBuilder();
        tableExpression
            .append(getAttribute().getName())
            .append(" as ")
            .append(getAlias());

        if (getOn() != null) {
            tableExpression.append(" on ")
                .append(((PredicateImplementor) getOn()).render(renderingContext));
        }
        return tableExpression.toString();
    }

    @Override
    public Set<Join<X, ?>> getJoins() {
        Set<Join<X, ?>> joins = super.getJoins();
        Set<Join<X, ?>> mut = new HashSet<>();
        for (Join<X, ?> join : joins) {
            if (join instanceof SingularAttributeJoin<X, ?> singularAttributeJoin) {
                SingularJoinOn<X, ?> xSingularJoinOn = new SingularJoinOn<>(singularAttributeJoin);
                mut.add(xSingularJoinOn);
            } else {
                mut.add(join);
            }
        }
        return mut;
    }
}
