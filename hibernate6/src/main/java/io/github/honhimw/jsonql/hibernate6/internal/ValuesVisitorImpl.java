package io.github.honhimw.jsonql.hibernate6.internal;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.jdi.*;
import io.github.honhimw.jsonql.common.visitor.ValuesVisitor;
import io.github.honhimw.jsonql.hibernate6.CompileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.mapping.Column;
import org.hibernate.type.Type;

import java.util.Objects;

/**
 * @author hon_him
 * @since 2024-01-25
 */

final class ValuesVisitorImpl extends ValuesVisitor {

    private final VisitContext ctx;

    ValuesVisitorImpl(VisitContext ctx) {
        super(null);
        this.ctx = ctx;
    }

    @Override
    public void visitNext(String name, JsonNode value) {
        Column column = ctx.getRootTable().getColumn(Identifier.toIdentifier(CompileUtils.getFinalFieldName(name)));
        Validate.validState(Objects.nonNull(column), "column named: [%s] is not exists.".formatted(name));
        String columnName = column.getName();
        Type type = column.getValue().getType();
//        Object unwrapValue = tryCompatibility(name, type, value);
        CompileUtils.typeValidate(name, type, value, column.isNullable());
        Object unwrapValue = CompileUtils.unwrapNode(value);
        if (value.isTextual() && unwrapValue instanceof String stringValue) {
            unwrapValue = ctx.renderContext(stringValue);
        }
        final Object finalValue = unwrapValue;
        ctx.configurerInsert(insertBuilder -> insertBuilder.applyInsert((root, insert, cb) -> insert.value(root.get(columnName), finalValue)));
    }

    @Override
    public void visitEnd() {
    }

    private Object tryCompatibility(String name, Type type, JsonNode origin) {
        if (type instanceof BooleanType) {
            if (origin.isBoolean()) {
                return origin.booleanValue();
            } else {
                String text = origin.asText();
                if (StringUtils.equalsIgnoreCase(text, "true")) {
                    return true;
                } else if (StringUtils.equalsIgnoreCase(text, "false")) {
                    return false;
                } else {
                    throw new IllegalArgumentException("column[%s] require a BOOLEAN type, but [%s] is provided, and incompatible as [%s].".formatted(name, origin.getNodeType().name(), text));
                }
            }
        } else if (type instanceof IntegerType) {
            if (origin.isInt()) {
                return origin.intValue();
            } else {
                String text = origin.asText();
                try {
                    return Integer.parseInt(text);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("column[%s] require a INTEGER type, but [%s] is provided, and incompatible as [%s].".formatted(name, origin.getNodeType().name(), text));
                }
            }
        } else if (type instanceof FloatType) {
            if (origin.isFloat()) {
                return origin.floatValue();
            } else {
                String text = origin.asText();
                try {
                    return Float.parseFloat(text);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("column[%s] require a FLOAT type, but [%s] is provided, and incompatible as [%s].".formatted(name, origin.getNodeType().name(), text));
                }
            }
        } else if (type instanceof LongType) {
            if (origin.isLong()) {
                return origin.longValue();
            } else {
                String text = origin.asText();
                try {
                    return Long.parseLong(text);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("column[%s] require a LONG type, but [%s] is provided, and incompatible as [%s].".formatted(name, origin.getNodeType().name(), text));
                }
            }
        } else if (type instanceof DoubleType) {
            if (origin.isDouble()) {
                return origin.doubleValue();
            } else {
                String text = origin.asText();
                try {
                    return Double.parseDouble(text);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("column[%s] require a DOUBLE type, but [%s] is provided, and incompatible as [%s].".formatted(name, origin.getNodeType().name(), text));
                }
            }
        } else {
            return origin.asText();
        }
    }

}
