package io.github.honhimw.jsonql.hibernate6.internal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.honhimw.jsonql.common.visitor.CRUDVisitor;
import io.github.honhimw.jsonql.hibernate6.MetadataExtractorIntegrator;
import io.github.honhimw.jsonql.hibernate6.meta.SQLHolder;
import io.github.honhimw.jsonql.hibernate6.meta.TableMetaCache;
import lombok.Getter;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author hon_him
 * @since 2024-01-25
 */

public class JsonQLCompiler {

    @Getter
    private final EntityManager em;

    private final TableMetaCache tableMetaCache;

    @Getter
    private final MetadataExtractorIntegrator integrator;

    private Function<CRUDVisitor, CRUDVisitor> customVisitor;

    public JsonQLCompiler(EntityManager em, TableMetaCache tableMetaCache) {
        this(em, tableMetaCache, MetadataExtractorIntegrator.INSTANCE);
    }

    public JsonQLCompiler(EntityManager em, TableMetaCache tableMetaCache, MetadataExtractorIntegrator integrator) {
        this.em = em;
        this.tableMetaCache = tableMetaCache;
        this.integrator = integrator;
    }


    public void acceptVisitor(Function<CRUDVisitor, CRUDVisitor> custom) {
        this.customVisitor = custom;
    }

    public List<SQLHolder> compile(JsonNode rootNode) {
        return compile(rootNode, null);
    }

    public List<SQLHolder> compile(JsonNode rootNode, ObjectNode contextNode) {
        VisitContext ctx = new VisitContext(em, tableMetaCache, integrator);

        if (Objects.nonNull(contextNode)) {
            ctx.setContextNode(contextNode);
        }
        CRUDVisitor crudVisitor = new CRUDVisitorImpl(ctx);

        if (this.customVisitor != null) {
            crudVisitor = customVisitor.apply(crudVisitor);
        }

        CompilerProcessor compilerProcessor = new CompilerProcessor(ctx, rootNode, crudVisitor);
        return compilerProcessor.process();
    }

}
