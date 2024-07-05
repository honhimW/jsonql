package org.hibernate.query.criteria.internal;

import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.hibernate.query.criteria.internal.CriteriaBuilderImpl;
import org.hibernate.query.criteria.internal.CriteriaDeleteImpl;
import org.hibernate.query.criteria.internal.compile.CriteriaInterpretation;
import org.hibernate.query.criteria.internal.compile.RenderingContext;

import jakarta.persistence.metamodel.EntityType;

/**
 * @author hon_him
 * @since 2024-06-28
 */

public class DelegateCriteriaDelete<T> extends CriteriaDeleteImpl<T> {

    private final CriteriaDeleteImpl<T> delegate;

    public DelegateCriteriaDelete(CriteriaDeleteImpl<T> delegate) {
        super(delegate.criteriaBuilder());
        this.delegate = delegate;
    }

    @Override
    public CriteriaDelete<T> where(Expression<Boolean> restriction) {
        return delegate.where(restriction);
    }

    @Override
    public CriteriaDelete<T> where(Predicate... restrictions) {
        return delegate.where(restrictions);
    }

    @Override
    public String renderQuery(RenderingContext renderingContext) {
        return delegate.renderQuery(renderingContext);
    }

    @Override
    public CriteriaBuilderImpl criteriaBuilder() {
        return delegate.criteriaBuilder();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Root from(Class<T> entityClass) {
        return delegate.from(entityClass);
    }

    @Override
    public Root<T> from(EntityType<T> entityType) {
        return delegate.from(entityType);
    }

    @Override
    public Root<T> getRoot() {
        return delegate.getRoot();
    }

    @Override
    public void setRestriction(Expression<Boolean> restriction) {
        delegate.setRestriction(restriction);
    }

    @Override
    public void setRestriction(Predicate... restrictions) {
        delegate.setRestriction(restrictions);
    }

    @Override
    public Predicate getRestriction() {
        return delegate.getRestriction();
    }

    @Override
    public <U> Subquery<U> subquery(Class<U> type) {
        return delegate.subquery(type);
    }

    @Override
    public void validate() {
        delegate.validate();
    }

    @Override
    public CriteriaInterpretation interpret(RenderingContext renderingContext) {
        return delegate.interpret(renderingContext);
    }

    @Override
    public void renderRoot(StringBuilder jpaql, RenderingContext renderingContext) {
        delegate.renderRoot(jpaql, renderingContext);
    }

    @Override
    public void renderRestrictions(StringBuilder jpaql, RenderingContext renderingContext) {
        delegate.renderRestrictions(jpaql, renderingContext);
    }
}
