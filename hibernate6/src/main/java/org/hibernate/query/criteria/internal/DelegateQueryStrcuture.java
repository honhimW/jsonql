package org.hibernate.query.criteria.internal;

import org.hibernate.query.criteria.internal.compile.RenderingContext;
import org.hibernate.sql.ast.Clause;

import jakarta.persistence.criteria.AbstractQuery;

/**
 * @author hon_him
 * @since 2024-06-28
 */

public class DelegateQueryStrcuture<T> extends QueryStructure<T> {

    /**
     * TODO: may support setting up
     */
    private final boolean selectAllByWildcard = true;
    
    public DelegateQueryStrcuture(AbstractQuery<T> owner, CriteriaBuilderImpl criteriaBuilder) {
        super(owner, criteriaBuilder);
    }

    @Override
    protected void renderSelectClause(StringBuilder jpaqlQuery, RenderingContext renderingContext) {
        if (selectAllByWildcard) {
            renderingContext.getClauseStack().push(Clause.SELECT);

            try {
                jpaqlQuery.append("select ");

                if (isDistinct()) {
                    jpaqlQuery.append("distinct ");
                }

                if (getSelection() == null) {
                    jpaqlQuery.append("*");
                } else {
                    jpaqlQuery.append(((Renderable) getSelection()).render(renderingContext));
                }
            } finally {
                renderingContext.getClauseStack().pop();
            }
        } else {
            super.renderSelectClause(jpaqlQuery, renderingContext);
        }

    }
}
