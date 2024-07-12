package io.github.honhimw.jsonql.hibernate6.supports;

import jakarta.persistence.criteria.CriteriaBuilder;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.query.sqm.internal.SqmCriteriaNodeBuilder;
import org.hibernate.query.sqm.tree.expression.SqmLiteral;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author hon_him
 * @since 2024-06-28
 */

public class PlainExpression extends SqmLiteral<String> {

    public static final String ARG = "alias";

    public static final Pattern ALIAS = Pattern.compile("((as)|(AS)) (?<alias>.+)$");

    public PlainExpression(CriteriaBuilder criteriaBuilder, String literal) {
        super(literal, null, (SqmCriteriaNodeBuilder) criteriaBuilder);
    }

    public PlainExpression(SqmCriteriaNodeBuilder criteriaBuilder, String literal) {
        super(literal, null, criteriaBuilder);
    }

    @Override
    public String getAlias() {
        String alias = super.getAlias();
        if (StringUtils.isBlank(alias)) {

            Matcher matcher = ALIAS.matcher(getLiteralValue());
            if (matcher.find()) {
                alias = matcher.group("alias");
                setAlias(alias);
            }
        }
        return alias;
    }
}
