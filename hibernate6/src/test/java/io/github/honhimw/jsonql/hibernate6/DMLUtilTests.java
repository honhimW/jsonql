package io.github.honhimw.jsonql.hibernate6;

import com.google.common.collect.Tables;
import jakarta.persistence.criteria.Join;
import lombok.SneakyThrows;
import org.h2.engine.Mode;
import org.hibernate.mapping.Table;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;

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
        Table table = TableSupports.get("material");
        TableHelper tableHelper = TableHelper.of(table, integrator.getMetadataBuildingContext());
        tableHelper.addForeign(TableSupports.get("material_supplier"), Map.of("supplier", "id"));
        DMLUtils instance = DMLUtils.getInstance(em, table, integrator);
        SQLHolder sqlHolder = instance.select().applyQuery((root, query, cb) -> {
            Join<Object, Object> materialSupplier = root.join("material_supplier");
            materialSupplier.on(cb.equal(root.get("supplier"), materialSupplier.get("id")));
            query.select(root.get("title"))
                .where(cb.equal(root.get("id"), "1"), cb.notEqual(root.get("status"), "disabled"));
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
                .set(root.get("logo"), "https://some-logo.svg")
                .where(cb.equal(root.get("id"), "1"));
        }).jdbcQL();
        log.info(sqlHolder.toString());
    }

    @Test
    @SneakyThrows
    void dmlDelete() {
        Table table = TableSupports.get("brand_introduction");
        DMLUtils instance = DMLUtils.getInstance(em, table);
        SQLHolder sqlHolder = instance.delete().applyDelete((root, delete, cb) -> {
            delete.where(cb.equal(root.get("id"), "1"));
        }).jdbcQL();
        log.info(sqlHolder.toString());
    }

    @Test
    @SneakyThrows
    void dmlInsert() {
        Table table = TableSupports.get("brand_introduction");
        DMLUtils instance = DMLUtils.getInstance(em, table);
        SQLHolder sqlHolder = instance.insert().applyInsert((root, insert, nb) -> {
            insert
                .setInsertionTargetPaths(root.get("title"), root.get("logo"))
                .values(
                    nb.values(nb.value("foo"), nb.value("a")),
                    nb.values(nb.value("bar"), nb.value("b"))
                );
        }).jdbcQL();
        log.info(sqlHolder.toString());
    }

}
