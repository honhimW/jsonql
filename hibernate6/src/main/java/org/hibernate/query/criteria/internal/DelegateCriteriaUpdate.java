package org.hibernate.query.criteria.internal;

import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.hibernate.dialect.SQLServerDialect;
import org.hibernate.query.criteria.internal.CriteriaBuilderImpl;
import org.hibernate.query.criteria.internal.CriteriaUpdateImpl;
import org.hibernate.query.criteria.internal.FromImplementor;
import org.hibernate.query.criteria.internal.compile.CriteriaInterpretation;
import org.hibernate.query.criteria.internal.compile.RenderingContext;

import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;

/**
 * @author hon_him
 * @since 2024-06-28
 */

public class DelegateCriteriaUpdate<T> extends CriteriaUpdateImpl<T> {

    private final CriteriaUpdateImpl<T> delegate;

    public DelegateCriteriaUpdate(CriteriaUpdateImpl<T> delegate) {
        super(delegate.criteriaBuilder());
        this.delegate = delegate;
    }

    @Override
    public <Y, X extends Y> CriteriaUpdate<T> set(SingularAttribute<? super T, Y> singularAttribute, X value) {
        return delegate.set(singularAttribute, value);
    }

    @Override
    public <Y> CriteriaUpdate<T> set(SingularAttribute<? super T, Y> singularAttribute, Expression<? extends Y> value) {
        return delegate.set(singularAttribute, value);
    }

    @Override
    public <Y, X extends Y> CriteriaUpdate<T> set(Path<Y> attributePath, X value) {
        return delegate.set(attributePath, value);
    }

    @Override
    public <Y> CriteriaUpdate<T> set(Path<Y> attributePath, Expression<? extends Y> value) {
        return delegate.set(attributePath, value);
    }

    @Override
    public CriteriaUpdate<T> set(String attributeName, Object value) {
        return delegate.set(attributeName, value);
    }

    @Override
    public <Y> void addAssignment(Path<Y> attributePath, Expression<? extends Y> value) {
        delegate.addAssignment(attributePath, value);
    }

    @Override
    public CriteriaUpdate<T> where(Expression<Boolean> restriction) {
        return delegate.where(restriction);
    }

    @Override
    public CriteriaUpdate<T> where(Predicate... restrictions) {
        return delegate.where(restrictions);
    }

    @Override
    public void validate() {
        delegate.validate();
    }

    @Override
    public String renderQuery(RenderingContext renderingContext) {
        String sql = delegate.renderQuery(renderingContext);
        if (renderingContext.getDialect() instanceof SQLServerDialect) {
            String tableExpression = ((FromImplementor<?, ?>) getRoot()).renderTableExpression(renderingContext);
            sql = sql.replaceFirst(tableExpression, getRoot().getAlias());
            int where = sql.indexOf(" where ");
            if (where != -1) {
                sql = sql.replaceFirst(" where ", " from " + tableExpression + " where ");
            } else {
                sql += " from " + tableExpression;
            }
        }
        return sql;
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
