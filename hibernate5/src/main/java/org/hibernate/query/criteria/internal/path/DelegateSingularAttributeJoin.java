package org.hibernate.query.criteria.internal.path;

import org.hibernate.query.criteria.internal.*;
import org.hibernate.query.criteria.internal.compile.RenderingContext;
import org.hibernate.query.criteria.internal.path.SingularAttributeJoin;

import javax.persistence.criteria.*;
import javax.persistence.metamodel.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author hon_him
 * @since 2024-06-28
 */

public class DelegateSingularAttributeJoin<O, X> extends SingularAttributeJoin<O, X> {

    private final SingularAttributeJoin<O, X> delegate;

    public DelegateSingularAttributeJoin(SingularAttributeJoin<O, X> delegate) {
        super(delegate.criteriaBuilder(), delegate.getJavaType(), delegate.getPathSource(), delegate.getAttribute(), delegate.getJoinType());
        this.delegate = delegate;
    }

    @Override
    public SingularAttribute<? super O, ?> getAttribute() {
        return delegate.getAttribute();
    }

    @Override
    public SingularAttributeJoin<O, X> correlateTo(CriteriaSubqueryImpl subquery) {
        return delegate.correlateTo(subquery);
    }

    @Override
    public FromImplementor<O, X> createCorrelationDelegate() {
        return delegate.createCorrelationDelegate();
    }

    @Override
    public boolean canBeJoinSource() {
        return delegate.canBeJoinSource();
    }

    @Override
    public ManagedType<? super X> locateManagedType() {
        return delegate.locateManagedType();
    }

    @Override
    public Bindable<X> getModel() {
        return delegate.getModel();
    }

    @Override
    public <T extends X> SingularAttributeJoin<O, T> treatAs(Class<T> treatAsType) {
        return delegate.treatAs(treatAsType);
    }

    @Override
    public JoinType getJoinType() {
        return delegate.getJoinType();
    }

    @Override
    public From<?, O> getParent() {
        return delegate.getParent();
    }

    @Override
    public String renderTableExpression(RenderingContext renderingContext) {
        return delegate.renderTableExpression(renderingContext);
    }

    @Override
    public JoinImplementor<O, X> on(Predicate... restrictions) {
        return delegate.on(restrictions);
    }

    @Override
    public JoinImplementor<O, X> on(Expression<Boolean> restriction) {
        return delegate.on(restriction);
    }

    @Override
    public Predicate getOn() {
        return delegate.getOn();
    }

    @Override
    public PathSource<O> getPathSource() {
        return delegate.getPathSource();
    }

    @Override
    public String getPathIdentifier() {
        return delegate.getPathIdentifier();
    }

    @Override
    public boolean canBeDereferenced() {
        return delegate.canBeDereferenced();
    }

    @Override
    public void prepareAlias(RenderingContext renderingContext) {
        delegate.prepareAlias(renderingContext);
    }

    @Override
    public String render(RenderingContext renderingContext) {
        return delegate.render(renderingContext);
    }

    @Override
    public Attribute<X, ?> locateAttributeInternal(String name) {
        return delegate.locateAttributeInternal(name);
    }

    @Override
    public boolean isCorrelated() {
        return delegate.isCorrelated();
    }

    @Override
    public FromImplementor<O, X> getCorrelationParent() {
        return delegate.getCorrelationParent();
    }

    @Override
    public void prepareCorrelationDelegate(FromImplementor<O, X> parent) {
        delegate.prepareCorrelationDelegate(parent);
    }

    @Override
    public String getAlias() {
        return delegate.getAlias();
    }

    @Override
    public RuntimeException illegalJoin() {
        return delegate.illegalJoin();
    }

    @Override
    public Set<Join<X, ?>> getJoins() {
        return delegate.getJoins();
    }

    @Override
    public <Y> Join<X, Y> join(SingularAttribute<? super X, Y> singularAttribute) {
        return delegate.join(singularAttribute);
    }

    @Override
    public <Y> Join<X, Y> join(SingularAttribute<? super X, Y> attribute, JoinType jt) {
        return delegate.join(attribute, jt);
    }

    @Override
    public <Y> CollectionJoin<X, Y> join(CollectionAttribute<? super X, Y> collection) {
        return delegate.join(collection);
    }

    @Override
    public <Y> CollectionJoin<X, Y> join(CollectionAttribute<? super X, Y> collection, JoinType jt) {
        return delegate.join(collection, jt);
    }

    @Override
    public <Y> SetJoin<X, Y> join(SetAttribute<? super X, Y> set) {
        return delegate.join(set);
    }

    @Override
    public <Y> SetJoin<X, Y> join(SetAttribute<? super X, Y> set, JoinType jt) {
        return delegate.join(set, jt);
    }

    @Override
    public <Y> ListJoin<X, Y> join(ListAttribute<? super X, Y> list) {
        return delegate.join(list);
    }

    @Override
    public <Y> ListJoin<X, Y> join(ListAttribute<? super X, Y> list, JoinType jt) {
        return delegate.join(list, jt);
    }

    @Override
    public <K, V> MapJoin<X, K, V> join(MapAttribute<? super X, K, V> map) {
        return delegate.join(map);
    }

    @Override
    public <K, V> MapJoin<X, K, V> join(MapAttribute<? super X, K, V> map, JoinType jt) {
        return delegate.join(map, jt);
    }

    @Override
    public <X1, Y> Join<X1, Y> join(String attributeName) {
        return delegate.join(attributeName);
    }

    @Override
    public <X1, Y> Join<X1, Y> join(String attributeName, JoinType jt) {
        return delegate.join(attributeName, jt);
    }

    @Override
    public <X1, Y> CollectionJoin<X1, Y> joinCollection(String attributeName) {
        return delegate.joinCollection(attributeName);
    }

    @Override
    public <X1, Y> CollectionJoin<X1, Y> joinCollection(String attributeName, JoinType jt) {
        return delegate.joinCollection(attributeName, jt);
    }

    @Override
    public <X1, Y> SetJoin<X1, Y> joinSet(String attributeName) {
        return delegate.joinSet(attributeName);
    }

    @Override
    public <X1, Y> SetJoin<X1, Y> joinSet(String attributeName, JoinType jt) {
        return delegate.joinSet(attributeName, jt);
    }

    @Override
    public <X1, Y> ListJoin<X1, Y> joinList(String attributeName) {
        return delegate.joinList(attributeName);
    }

    @Override
    public <X1, Y> ListJoin<X1, Y> joinList(String attributeName, JoinType jt) {
        return delegate.joinList(attributeName, jt);
    }

    @Override
    public <X1, K, V> MapJoin<X1, K, V> joinMap(String attributeName) {
        return delegate.joinMap(attributeName);
    }

    @Override
    public <X1, K, V> MapJoin<X1, K, V> joinMap(String attributeName, JoinType jt) {
        return delegate.joinMap(attributeName, jt);
    }

    @Override
    public boolean canBeFetchSource() {
        return delegate.canBeFetchSource();
    }

    @Override
    public RuntimeException illegalFetch() {
        return delegate.illegalFetch();
    }

    @Override
    public Set<Fetch<X, ?>> getFetches() {
        return delegate.getFetches();
    }

    @Override
    public <Y> Fetch<X, Y> fetch(SingularAttribute<? super X, Y> singularAttribute) {
        return delegate.fetch(singularAttribute);
    }

    @Override
    public <Y> Fetch<X, Y> fetch(SingularAttribute<? super X, Y> attribute, JoinType jt) {
        return delegate.fetch(attribute, jt);
    }

    @Override
    public <Y> Fetch<X, Y> fetch(PluralAttribute<? super X, ?, Y> pluralAttribute) {
        return delegate.fetch(pluralAttribute);
    }

    @Override
    public <Y> Fetch<X, Y> fetch(PluralAttribute<? super X, ?, Y> pluralAttribute, JoinType jt) {
        return delegate.fetch(pluralAttribute, jt);
    }

    @Override
    public <X1, Y> Fetch<X1, Y> fetch(String attributeName) {
        return delegate.fetch(attributeName);
    }

    @Override
    public <X1, Y> Fetch<X1, Y> fetch(String attributeName, JoinType jt) {
        return delegate.fetch(attributeName, jt);
    }

    @Override
    public boolean canBeReplacedByCorrelatedParentInSubQuery() {
        return delegate.canBeReplacedByCorrelatedParentInSubQuery();
    }

    @Override
    public PathSource<?> getParentPath() {
        return delegate.getParentPath();
    }

    @Override
    public Expression<Class<? extends X>> type() {
        return delegate.type();
    }

    @Override
    public <Y> Path<Y> get(SingularAttribute<? super X, Y> attribute) {
        return delegate.get(attribute);
    }

    @Override
    public PathSource getPathSourceForSubPaths() {
        return delegate.getPathSourceForSubPaths();
    }

    @Override
    public <E, C extends Collection<E>> Expression<C> get(PluralAttribute<X, C, E> attribute) {
        return delegate.get(attribute);
    }

    @Override
    public <K, V, M extends Map<K, V>> Expression<M> get(MapAttribute<X, K, V> attribute) {
        return delegate.get(attribute);
    }

    @Override
    public <Y> Path<Y> get(String attributeName) {
        return delegate.get(attributeName);
    }

    @Override
    public void registerParameters(ParameterRegistry registry) {
        delegate.registerParameters(registry);
    }

    @Override
    public <X1> Expression<X1> as(Class<X1> type) {
        return delegate.as(type);
    }

    @Override
    public Predicate isNull() {
        return delegate.isNull();
    }

    @Override
    public Predicate isNotNull() {
        return delegate.isNotNull();
    }

    @Override
    public Predicate in(Object... values) {
        return delegate.in(values);
    }

    @Override
    public Predicate in(Expression<?>... values) {
        return delegate.in(values);
    }

    @Override
    public Predicate in(Collection<?> values) {
        return delegate.in(values);
    }

    @Override
    public Predicate in(Expression<Collection<?>> values) {
        return delegate.in(values);
    }

    @Override
    public ExpressionImplementor<Long> asLong() {
        return delegate.asLong();
    }

    @Override
    public ExpressionImplementor<Integer> asInteger() {
        return delegate.asInteger();
    }

    @Override
    public ExpressionImplementor<Float> asFloat() {
        return delegate.asFloat();
    }

    @Override
    public ExpressionImplementor<Double> asDouble() {
        return delegate.asDouble();
    }

    @Override
    public ExpressionImplementor<BigDecimal> asBigDecimal() {
        return delegate.asBigDecimal();
    }

    @Override
    public ExpressionImplementor<BigInteger> asBigInteger() {
        return delegate.asBigInteger();
    }

    @Override
    public ExpressionImplementor<String> asString() {
        return delegate.asString();
    }

    @Override
    public Selection<X> alias(String alias) {
        return delegate.alias(alias);
    }

    @Override
    public boolean isCompoundSelection() {
        return delegate.isCompoundSelection();
    }

    @Override
    public List<ValueHandlerFactory.ValueHandler> getValueHandlers() {
        return delegate.getValueHandlers();
    }

    @Override
    public List<Selection<?>> getCompoundSelectionItems() {
        return delegate.getCompoundSelectionItems();
    }

    @Override
    public ValueHandlerFactory.ValueHandler<X> getValueHandler() {
        return delegate.getValueHandler();
    }
}
