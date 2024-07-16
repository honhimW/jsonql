package io.github.honhimw.jsonql.hibernate6;

import org.hibernate.metamodel.model.domain.internal.EntityTypeImpl;
import org.hibernate.metamodel.model.domain.spi.JpaMetamodelImplementor;
import org.hibernate.type.descriptor.java.JavaType;

/**
 * @author hon_him
 * @since 2024-07-15
 */

public class IEntityTypeImpl<T> extends EntityTypeImpl<T> {

    public IEntityTypeImpl(JavaType<T> javaTypeDescriptor, JpaMetamodelImplementor metamodel) {
        super(javaTypeDescriptor, metamodel);
    }

    @Override
    protected boolean isIdMappingRequired() {
        return false;
    }
}
