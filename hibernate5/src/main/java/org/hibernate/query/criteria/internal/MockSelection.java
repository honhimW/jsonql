package org.hibernate.query.criteria.internal;

import javax.persistence.criteria.Selection;
import java.util.List;

/**
 * @author hon_him
 * @since 2024-06-28
 */

public class MockSelection<X> implements Selection<X> {

    private String name;


    public MockSelection(String name) {
        this.name = name;
    }

    @Override
    public Selection<X> alias(String name) {
        this.name = name;
        return this;
    }

    @Override
    public boolean isCompoundSelection() {
        return false;
    }

    @Override
    public List<Selection<?>> getCompoundSelectionItems() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Class<? extends X> getJavaType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getAlias() {
        return name;
    }
}
