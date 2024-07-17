package io.github.honhimw.jsonql.hibernate6;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.honhimw.jsonql.common.JsonUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author hon_him
 * @since 2024-07-02
 */

public class TestsBase {

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    protected ObjectMapper MAPPER = JsonUtils.getObjectMapper();

    protected void print(String name, Collection<?> lines) {
        if (Objects.isNull(name) || name.isBlank()) {
            name = "UnTitled";
        }
        StringBuilder sb = new StringBuilder();
        sb.append('\n').append("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<").append(name).append(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>").append('\n');
        for (Object line : lines) {
            sb.append(line).append('\n');
        }
        sb.append("-----------------------------").append("-".repeat(name.length())).append("-----------------------------");
        log.info(sb.toString());
    }

    protected void printTable(String name, List<Map<String, Object>> resultSet) {
        if (CollectionUtils.isEmpty(resultSet)) {
            return;
        }
        if (Objects.isNull(name) || name.isBlank()) {
            name = "UnTitled";
        }
        Map<String, Integer> widths = new LinkedHashMap<>();
        for (Map<String, Object> kv : resultSet) {
            kv.forEach((k, v) -> {
                int keyLen = k.length();
                int valueLen = String.valueOf(v).length();
                int max = Math.max(keyLen, valueLen);
                widths.compute(k, (k1, v1) -> {
                    if (Objects.nonNull(v1)) {
                        return Math.max(v1, max);
                    }
                    return max;
                });
            });
        }
        StringBuilder sb = new StringBuilder();
        sb.append('\n').append("> ").append(name).append('\n').append("+");
        widths.forEach((key, value) -> {
            sb.append("-".repeat(value + 2));
            sb.append("+");
        });
        sb.append('\n');
        sb.append("|");
        widths.forEach((key, value) -> {
            sb.append(" ");
            sb.append(key);
            sb.append(" ");
            sb.append(" ".repeat(value - key.length()));
            sb.append("|");
        });
        sb.append('\n').append("+");
        widths.forEach((key, value) -> {
            sb.append("-".repeat(value + 2));
            sb.append("+");
        });
        sb.append('\n');
        for (Map<String, Object> line : resultSet) {
            sb.append("|");
            line.forEach((k, v) -> {
                sb.append(" ");
                sb.append(v);
                sb.append(" ");
                sb.append(" ".repeat(widths.get(k) - String.valueOf(v).length()));
                sb.append("|");
            });
            sb.append('\n');
        }
        sb.append("+");
        widths.forEach((key, value) -> {
            sb.append("-".repeat(value + 2));
            sb.append("+");
        });
        sb.append('\n');
        log.info(sb.toString());
    }

}
