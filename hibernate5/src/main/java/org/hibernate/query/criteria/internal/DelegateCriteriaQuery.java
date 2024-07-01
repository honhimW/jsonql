package org.hibernate.query.criteria.internal;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.jpa.spi.HibernateEntityManagerImplementor;
import org.hibernate.query.criteria.internal.compile.*;
import org.hibernate.query.criteria.internal.path.DelegateRoot;
import org.hibernate.query.criteria.internal.path.RootImpl;
import org.hibernate.query.spi.QueryImplementor;
import org.hibernate.sql.ast.Clause;
import org.hibernate.type.Type;

import javax.persistence.Tuple;
import javax.persistence.criteria.*;
import javax.persistence.metamodel.EntityType;
import java.util.*;

/**
 * @author hon_him
 * @since 2024-06-28
 */
public class DelegateCriteriaQuery<T> extends CriteriaQueryImpl<T> {

    private final CriteriaQueryImpl<T> delegate;

    private final DelegateQueryStrcuture<T> delegateQueryStructure;

    public DelegateCriteriaQuery(CriteriaQueryImpl<T> delegate) {
        super(delegate.criteriaBuilder(), delegate.getResultType());
        this.delegate = delegate;
        this.delegateQueryStructure = new DelegateQueryStrcuture<>(this, delegate.criteriaBuilder());
    }

    @Override
    public QueryStructure<T> getQueryStructure() {
        return delegateQueryStructure;
    }

    @Override
    public Class<T> getResultType() {
        return delegate.getResultType();
    }

    @Override
    public CriteriaQuery<T> distinct(boolean applyDistinction) {
        getQueryStructure().setDistinct(applyDistinction);
        return this;
    }

    @Override
    public boolean isDistinct() {
        return getQueryStructure().isDistinct();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Selection<T> getSelection() {
        return (Selection<T>) getQueryStructure().getSelection();
    }

    @Override
    public void applySelection(Selection<? extends T> selection) {
        getQueryStructure().setSelection(selection);
        delegate.applySelection(selection);
    }

    @Override
    public CriteriaQuery<T> select(Selection<? extends T> selection) {
        applySelection(selection);
        return this;
    }

    @Override
    public CriteriaQuery<T> multiselect(Selection<?>... selections) {
        return multiselect(Arrays.asList(selections));
    }

    @SuppressWarnings("unchecked")
    @Override
    public CriteriaQuery<T> multiselect(List<Selection<?>> selections) {
        final Selection<? extends T> selection;

        if (Tuple.class.isAssignableFrom(getResultType())) {
            selection = (Selection<? extends T>) criteriaBuilder().tuple(selections);
        } else if (getResultType().isArray()) {
            selection = criteriaBuilder().array(getResultType(), selections);
        } else if (Object.class.equals(getResultType())) {
            switch (selections.size()) {
                case 0: {
                    throw new IllegalArgumentException(
                        "empty selections passed to criteria query typed as Object"
                    );
                }
                case 1: {
                    selection = (Selection<? extends T>) selections.get(0);
                    break;
                }
                default: {
                    selection = (Selection<? extends T>) criteriaBuilder().array(selections);
                }
            }
        } else {
            selection = criteriaBuilder().construct(getResultType(), selections);
        }
        applySelection(selection);
        return this;
    }

    @Override
    public Set<Root<?>> getRoots() {
        return getQueryStructure().getRoots();
    }

    @Override
    public <X> Root<X> from(EntityType<X> entityType) {
        DelegateRoot<X> root = new DelegateRoot<>(new RootImpl<>(criteriaBuilder(), entityType));
        Set<Root<?>> roots = getQueryStructure().getRoots();
        roots.add(root);
        return root;
    }

    @Override
    public <X> Root<X> from(Class<X> entityClass) {
        return getQueryStructure().from(entityClass);
    }

    @Override
    public Predicate getRestriction() {
        return getQueryStructure().getRestriction();
    }

    @Override
    public CriteriaQuery<T> where(Expression<Boolean> expression) {
        getQueryStructure().setRestriction(criteriaBuilder().wrap(expression));
        return this;
    }

    @Override
    public CriteriaQuery<T> where(Predicate... predicates) {
        getQueryStructure().setRestriction(criteriaBuilder().and(predicates));
        return this;
    }

    @Override
    public List<Expression<?>> getGroupList() {
        return getQueryStructure().getGroupings();
    }

    @Override
    public CriteriaQuery<T> groupBy(Expression<?>... groupings) {
        getQueryStructure().setGroupings(groupings);
        return this;
    }

    @Override
    public CriteriaQuery<T> groupBy(List<Expression<?>> groupings) {
        getQueryStructure().setGroupings(groupings);
        return this;
    }

    @Override
    public Predicate getGroupRestriction() {
        return getQueryStructure().getHaving();
    }

    @Override
    public CriteriaQuery<T> having(Expression<Boolean> expression) {
        getQueryStructure().setHaving(criteriaBuilder().wrap(expression));
        return this;
    }

    @Override
    public CriteriaQuery<T> having(Predicate... predicates) {
        getQueryStructure().setHaving(criteriaBuilder().and(predicates));
        return this;
    }

    @Override
    public List<Order> getOrderList() {
        return delegate.getOrderList();
    }

    @Override
    public CriteriaQuery<T> orderBy(Order... orders) {
        return delegate.orderBy(orders);
    }

    @Override
    public CriteriaQuery<T> orderBy(List<Order> orders) {
        return delegate.orderBy(orders);
    }

    @Override
    public Set<ParameterExpression<?>> getParameters() {
        return getQueryStructure().getParameters();
    }

    @Override
    public <U> Subquery<U> subquery(Class<U> subqueryType) {
        return getQueryStructure().subquery(subqueryType);
    }

    @Override
    public void validate() {
        // getRoots() is explicitly supposed to return empty if none defined, no need to check for null
        if (getRoots().isEmpty()) {
            throw new IllegalStateException("No criteria query roots were specified");
        }

        // if there is not an explicit selection, there is an *implicit* selection of the root entity provided only
        // a single query root was defined.
        if (getSelection() == null && !hasImplicitSelection()) {
            throw new IllegalStateException("No explicit selection and an implicit one could not be determined");
        }
    }

    private boolean hasImplicitSelection() {
        if (getRoots().size() != 1) {
            return false;
        }

        Root<?> root = getRoots().iterator().next();
        Class<?> javaType = root.getModel().getJavaType();
        return javaType == null || javaType == getResultType();

        // if we get here, the query defined no selection but defined a single root of the same type as the
        // criteria query return, so we use that as the implicit selection
        //
        // todo : should we put an implicit marker in the selection to this fact to make later processing easier?
    }

    @SuppressWarnings({"unchecked", "rawtypes", "deprecation"})
    @Override
    public CriteriaInterpretation interpret(RenderingContext renderingContext) {
        final StringBuilder jpaqlBuffer = new StringBuilder();

        getQueryStructure().render( jpaqlBuffer, renderingContext );

        renderOrderByClause( renderingContext, jpaqlBuffer );

        final String jpaqlString = jpaqlBuffer.toString();

        return new CriteriaInterpretation() {
            @Override
            @SuppressWarnings("unchecked")
            public QueryImplementor buildCompiledQuery(
                SharedSessionContractImplementor entityManager,
                final InterpretedParameterMetadata parameterMetadata) {

                final Map<String,Class> implicitParameterTypes = extractTypeMap( parameterMetadata.implicitParameterBindings() );

                QueryImplementor<T> jpaqlQuery = entityManager.createQuery(
                    jpaqlString,
                    getResultType(),
                    getSelection(),
                    new HibernateEntityManagerImplementor.QueryOptions() {
                        @Override
                        public List<ValueHandlerFactory.ValueHandler> getValueHandlers() {
                            SelectionImplementor selection = (SelectionImplementor) getQueryStructure().getSelection();
                            return selection == null
                                ? null
                                : selection.getValueHandlers();
                        }

                        @Override
                        public Map<String, Class> getNamedParameterExplicitTypes() {
                            return implicitParameterTypes;
                        }

                        @Override
                        public ResultMetadataValidator getResultMetadataValidator() {
                            return new ResultMetadataValidator() {
                                @Override
                                public void validate(Type[] returnTypes) {
                                    SelectionImplementor selection = (SelectionImplementor) getQueryStructure().getSelection();
                                    if ( selection != null ) {
                                        if ( selection.isCompoundSelection() ) {
                                            if ( returnTypes.length != selection.getCompoundSelectionItems().size() ) {
                                                throw new IllegalStateException(
                                                    "Number of return values [" + returnTypes.length +
                                                        "] did not match expected [" +
                                                        selection.getCompoundSelectionItems().size() + "]"
                                                );
                                            }
                                        }
                                        else {
                                            if ( returnTypes.length > 1 ) {
                                                throw new IllegalStateException(
                                                    "Number of return values [" + returnTypes.length +
                                                        "] did not match expected [1]"
                                                );
                                            }
                                        }
                                    }
                                }
                            };
                        }
                    }
                );

                for ( ImplicitParameterBinding implicitParameterBinding : parameterMetadata.implicitParameterBindings() ) {
                    implicitParameterBinding.bind( jpaqlQuery );
                }

                return new CriteriaQueryTypeQueryAdapter(
                    entityManager,
                    jpaqlQuery,
                    parameterMetadata.explicitParameterInfoMap()
                );

            }

            private Map<String, Class> extractTypeMap(List<ImplicitParameterBinding> implicitParameterBindings) {
                final HashMap<String,Class> map = new HashMap<String, Class>();
                for ( ImplicitParameterBinding implicitParameter : implicitParameterBindings ) {
                    map.put( implicitParameter.getParameterName(), implicitParameter.getJavaType() );
                }
                return map;
            }
        };
    }

    @Override
    public void renderOrderByClause(RenderingContext renderingContext, StringBuilder jpaqlBuffer) {
        if ( getOrderList().isEmpty() ) {
            return;
        }

        renderingContext.getClauseStack().push( Clause.ORDER );
        try {
            jpaqlBuffer.append( " order by " );
            String sep = "";
            for ( Order orderSpec : getOrderList() ) {
                jpaqlBuffer.append( sep )
                    .append( ( (Renderable) orderSpec.getExpression() ).render( renderingContext ) )
                    .append( orderSpec.isAscending() ? " asc" : " desc" );
                if ( orderSpec instanceof OrderImpl ) {
                    Boolean nullsFirst = ( (OrderImpl) orderSpec ).getNullsFirst();
                    if ( nullsFirst != null ) {
                        if ( nullsFirst ) {
                            jpaqlBuffer.append( " nulls first" );
                        }
                        else {
                            jpaqlBuffer.append( " nulls last" );
                        }
                    }
                }
                sep = ", ";
            }
        }
        finally {
            renderingContext.getClauseStack().pop();
        }
    }

    @Override
    public CriteriaBuilderImpl criteriaBuilder() {
        return delegate.criteriaBuilder();
    }
}
