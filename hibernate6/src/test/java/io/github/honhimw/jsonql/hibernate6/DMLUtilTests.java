package io.github.honhimw.jsonql.hibernate6;

import lombok.SneakyThrows;
import org.hibernate.mapping.Table;
import org.junit.jupiter.api.Test;

/**
 * @author hon_him
 * @since 2024-07-04
 */

public class DMLUtilTests extends DataSourceBase {

    @Test
    @SneakyThrows
    void dml() {
        Table table = TableSupports.get("brand_introduction");
        DMLUtils instance = DMLUtils.getInstance(em, table);
        SQLHolder sqlHolder = instance.select().applyQuery((root, query, cb) -> {
            query.select(root.get("title"))
                .where(cb.equal(root.get("id"), "1"));
        }).jdbcQL();
        log.info(sqlHolder.toString());
    }

}
