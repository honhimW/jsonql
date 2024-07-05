package org.hibernate.query.criteria.internal.path;

import org.hibernate.query.criteria.internal.CriteriaBuilderImpl;
import org.hibernate.query.criteria.internal.compile.RenderingContext;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.metamodel.EntityType;
import java.util.HashSet;
import java.util.Set;

/**
 * @author hon_him
 * @since 2024-06-28
 */

public class DelegateRoot<X> extends RootImpl<X> {

    private final RootImpl<X> delegate;

    public DelegateRoot(RootImpl<X> delegate) {
        super(delegate.criteriaBuilder(), delegate.getEntityType());
        this.delegate = delegate;
    }

    public DelegateRoot(CriteriaBuilderImpl criteriaBuilder, EntityType<X> entityType) {
        super(criteriaBuilder, entityType);
        this.delegate = null;
    }

    @Override
    public void prepareAlias(RenderingContext renderingContext) {
        super.prepareAlias(renderingContext);
    }

    @Override
    public <Y> Path<Y> get(String attributeName) {
        return super.get(attributeName);
    }

    @Override
    public Set<Join<X, ?>> getJoins() {
        Set<Join<X, ?>> joins = super.getJoins();
        return mutJoins(joins);
    }

    @Override
    public String render(RenderingContext renderingContext) {
        return super.render(renderingContext);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private <T> Set<Join<T, ?>> mutJoins(Set<Join<T, ?>> joins) {
        Set<Join<T, ?>> mut = new HashSet<>(joins.size());
        for (Join<T, ?> join : joins) {
            if (join instanceof SingularAttributeJoin<T, ?> original) {
                SingularAttributeJoin<T, ?> singularAttributeJoin = new SingularJoinOn(original);
                mut.add(singularAttributeJoin);
            } else {
                mut.add(join);
            }
        }
        return mut;
    }
}
