package io.github.honhimw.jsonql.hibernate6;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author hon_him
 * @since 2024-01-24
 */

public record SQLHolder(String sql, List<Object> parameters) {

    @Override
    public String toString() {
        return "SQL: %s; Params: [%s]".formatted(sql, parameters.stream().map(String::valueOf).collect(Collectors.joining(",")));
    }
}
