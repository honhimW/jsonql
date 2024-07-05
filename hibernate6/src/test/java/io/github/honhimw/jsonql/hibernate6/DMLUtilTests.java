package io.github.honhimw.jsonql.hibernate6;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

/**
 * @author hon_him
 * @since 2024-07-04
 */

public class DMLUtilTests {

    @Test
    @SneakyThrows
    void dml() {
        DMLUtils instance = DMLUtils.getInstance(null, null);
        instance.select().applyQuery((root, query, cb) -> {
            root.get("");
        });
    }

}
