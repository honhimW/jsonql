package org.hibernate.query.criteria.internal;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;

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
