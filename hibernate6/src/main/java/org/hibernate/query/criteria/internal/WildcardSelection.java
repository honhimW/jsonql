package org.hibernate.query.criteria.internal;

import org.hibernate.query.criteria.internal.compile.RenderingContext;

import jakarta.persistence.criteria.Selection;
import java.util.List;

/**
 * @author hon_him
 * @since 2024-06-28
 */

public class WildcardSelection implements Selection<Object[]>, Renderable {

    public static final WildcardSelection INSTANCE = new WildcardSelection();

    @Override
    public Selection<Object[]> alias(String name) {
        return this;
    }

    @Override
    public boolean isCompoundSelection() {
        return false;
    }

    @Override
    public List<Selection<?>> getCompoundSelectionItems() {
        return null;
    }

    @Override
    public Class<? extends Object[]> getJavaType() {
        return Object[].class;
    }

    @Override
    public String getAlias() {
        return "*";
    }

    @Override
    public String render(RenderingContext renderingContext) {
        return "*";
    }
}
