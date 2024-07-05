package org.hibernate.query.criteria.internal;

import org.hibernate.query.criteria.internal.compile.RenderingContext;
import org.hibernate.query.criteria.internal.path.SingularAttributePath;
import org.hibernate.sql.ast.Clause;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.metamodel.SingularAttribute;
import java.util.ArrayList;
import java.util.List;

/**
 * @author hon_him
 * @since 2024-06-28
 */

@SuppressWarnings({"unchecked", "rawtypes"})
public class CriteriaInsertImpl<T> extends AbstractManipulationCriteriaQuery<T> implements CriteriaInsert<T> {

    private final List<Assignment<?>> assignments = new ArrayList<>();

    public CriteriaInsertImpl(CriteriaBuilderImpl criteriaBuilder) {
        super(criteriaBuilder);
    }

    @Override
    public <Y, X extends Y> CriteriaInsert<T> value(SingularAttribute<? super T, Y> singularAttribute, X value) {
        final Path<Y> attributePath = getRoot().get(singularAttribute);
        final Expression valueExpression = value == null
            ? criteriaBuilder().nullLiteral(attributePath.getJavaType())
            : criteriaBuilder().literal(value);
        addAssignment(attributePath, valueExpression);
        return this;
    }

    @Override
    public <Y> CriteriaInsert<T> value(SingularAttribute<? super T, Y> singularAttribute, Expression<? extends Y> value) {
        addAssignment(getRoot().get(singularAttribute), value);
        return this;
    }

    @Override
    public <Y, X extends Y> CriteriaInsert<T> value(Path<Y> attributePath, X value) {
        final Expression valueExpression = value == null
            ? criteriaBuilder().nullLiteral(attributePath.getJavaType())
            : criteriaBuilder().literal(value);
        addAssignment(attributePath, valueExpression);
        return this;
    }

    @Override
    public <Y> CriteriaInsert<T> value(Path<Y> attributePath, Expression<? extends Y> value) {
        addAssignment(attributePath, value);
        return this;
    }

    @Override
    public CriteriaInsert<T> value(String attributeName, Object value) {
        return null;
    }

    protected <Y> void addAssignment(Path<Y> attributePath, Expression<? extends Y> value) {
        if (!(attributePath instanceof PathImplementor)) {
            throw new IllegalArgumentException("Unexpected path implementation type : " + attributePath.getClass().getName());
        }
        if (!(attributePath instanceof SingularAttributePath)) {
            throw new IllegalArgumentException(
                "Attribute path for assignment must represent a singular attribute ["
                    + ((PathImplementor) attributePath).getPathIdentifier() + "]"
            );
        }
        if (value == null) {
            throw new IllegalArgumentException("Assignment value expression cannot be null. Did you mean to pass null as a literal?");
        }
        assignments.add(new Assignment<>((SingularAttributePath<Y>) attributePath, value));
    }

    @Override
    public String renderQuery(RenderingContext renderingContext) {
        final StringBuilder jpaql = new StringBuilder("insert into ");
        renderRoot(jpaql, renderingContext);
        renderColumn(jpaql, renderingContext);
        return jpaql.toString();
    }

    @Override
    protected void renderRoot(StringBuilder jpaql, RenderingContext renderingContext) {
        jpaql.append(getRoot().getModel().getName());
    }

    protected void renderColumn(StringBuilder jpaql, RenderingContext renderingContext) {
        renderingContext.getClauseStack().push(Clause.INSERT);
        try {
            jpaql.append(" (");
            boolean first = true;
            for (Assignment<?> assignment : assignments) {
                if (!first) {
                    jpaql.append(", ");
                }
                jpaql.append(renderingContext.getDialect().openQuote());
                jpaql.append(assignment.attributePath.getAttribute().getName());
                jpaql.append(renderingContext.getDialect().closeQuote());
                first = false;
            }
            jpaql.append(") values (");
            first = true;
            for (Assignment<?> assignment : assignments) {
                if (!first) {
                    jpaql.append(", ");
                }
                jpaql.append(assignment.value.render(renderingContext));
                first = false;
            }
            jpaql.append(')');
        } finally {
            renderingContext.getClauseStack().pop();
        }

    }

    private static class Assignment<A> {
        private final SingularAttributePath<A> attributePath;
        private final ExpressionImplementor<? extends A> value;

        private Assignment(SingularAttributePath<A> attributePath, Expression<? extends A> value) {
            this.attributePath = attributePath;
            this.value = (ExpressionImplementor) value;
        }
    }

}
