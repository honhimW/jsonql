package org.hibernate.query.criteria.internal;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.query.criteria.internal.CriteriaBuilderImpl;
import org.hibernate.query.criteria.internal.compile.RenderingContext;
import org.hibernate.query.criteria.internal.expression.LiteralExpression;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author hon_him
 * @since 2024-06-28
 */

public class PlainExpression extends LiteralExpression<String> {

    public static final String ARG = "alias";

    public static final Pattern ALIAS = Pattern.compile("((as)|(AS)) (?<alias>.+)$");

    public PlainExpression(CriteriaBuilder criteriaBuilder, String literal) {
        super((CriteriaBuilderImpl) criteriaBuilder, String.class, literal);
    }

    public PlainExpression(CriteriaBuilderImpl criteriaBuilder, String literal) {
        super(criteriaBuilder, String.class, literal);
    }

    @Override
    public String getAlias() {
        String alias = super.getAlias();
        if (StringUtils.isBlank(alias)) {
            Matcher matcher = ALIAS.matcher(getLiteral());
            if (matcher.find()) {
                alias = matcher.group("alias");
                setAlias(alias);
            }
        }
        return alias;
    }

    @Override
    public String render(RenderingContext renderingContext) {
        return getLiteral();
    }
}
