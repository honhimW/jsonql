package io.github.honhimw.jsonql.hibernate6;

import lombok.SneakyThrows;
import org.h2.engine.Mode;
import org.hibernate.mapping.Table;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author hon_him
 * @since 2024-07-04
 */

public class DMLUtilTests extends DataSourceBase {

    @BeforeAll
    static void init() {
        Mode.ModeEnum mode = Mode.ModeEnum.MSSQLServer;
        String url = "jdbc:h2:mem:test;MODE\\=%s;DB_CLOSE_DELAY\\=-1;IGNORECASE\\=FALSE;DATABASE_TO_UPPER\\=FALSE".formatted(mode.name());
        buildDatasource(
            "org.h2.Driver",
            url,
            null, null
        );
    }

    @AfterAll
    static void clean() {
        destroyDatasource();
    }

    @Test
    @SneakyThrows
    void dmlSelect() {
        Table table = TableSupports.get("brand_introduction");
        DMLUtils instance = DMLUtils.getInstance(em, table);
        SQLHolder sqlHolder = instance.select().applyQuery((root, query, cb) -> {
            query.select(root.get("title"))
                .where(cb.equal(root.get("id"), "1"));
        }).jdbcQL();
        log.info(sqlHolder.toString());
    }

    @Test
    @SneakyThrows
    void dmlUpdate() {
        Table table = TableSupports.get("brand_introduction");
        DMLUtils instance = DMLUtils.getInstance(em, table);
        SQLHolder sqlHolder = instance.update().applyUpdate((root, update, cb) -> {
            update.set(root.get("title"), "title")
                .set(root.get("content"), "content")
                .where(cb.equal(root.get("id"), "1"));
        }).jdbcQL();
        log.info(sqlHolder.toString());
    }

}
