package org.hibernate.query.criteria.internal;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;

/**
 * @author hon_him
 * @since 2024-06-28
 */

public interface CriteriaInsert<T> {

    Root<T> from(Class<T> entityClass);

    Root<T> from(EntityType<T> entity);

    Root<T> getRoot();

    <Y, X extends Y> CriteriaInsert<T> value(SingularAttribute<? super T, Y> attribute, X value);

    <Y> CriteriaInsert<T> value( SingularAttribute<? super T, Y> attribute, Expression<? extends Y> value);

    <Y, X extends Y> CriteriaInsert<T> value(Path<Y> attribute, X value);

    <Y> CriteriaInsert<T> value(Path<Y> attribute, Expression<? extends Y> value);

    CriteriaInsert<T> value(String attributeName, Object value);

}
