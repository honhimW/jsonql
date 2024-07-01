package io.github.honhimw.jsonql.hibernate5.internal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.honhimw.jsonql.hibernate5.CompileUtils;
import io.github.honhimw.jsonql.hibernate5.DMLUtils;
import io.github.honhimw.jsonql.hibernate5.MetadataExtractorIntegrator;
import io.github.honhimw.jsonql.hibernate5.meta.TableMetaCache;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.mapping.Table;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author hon_him
 * @since 2024-06-28
 */

@Setter
@Getter
class VisitContext {

    private static final Pattern SIMPLE_FIELD = Pattern.compile("^[a-zA-Z_][a-zA-Z_0-9]*$");
    private static final Pattern FIELD_WITH_REF = Pattern.compile("^(?<ref>[a-zA-Z_][a-zA-Z_0-9]*)\\.(?<field>[a-zA-Z_][a-zA-Z_0-9]*)$");

    private final EntityManager em;
    private final TableMetaCache tableMetaCache;
    private final MetadataExtractorIntegrator integrator;

    private LocalDateTime now;

    public VisitContext(EntityManager em, TableMetaCache tableMetaCache, MetadataExtractorIntegrator integrator) {
        this.em = em;
        this.tableMetaCache = tableMetaCache;
        this.integrator = integrator;
    }

    private Table rootTable;

    public void setRootTable(Table rootTable) {
        this.rootTable = rootTable;
        tableMap.put(rootTable.getName(), rootTable);
    }

    public LocalDateTime now() {
        if (Objects.isNull(now)) {
            now = LocalDateTime.now();
        }
        return now;
    }

    private final Map<String, Table> tableMap = new HashMap<>();

    @Getter(AccessLevel.NONE)
    private final Map<Root<?>, Map<String, From<?, ?>>> rootFromMap = new HashMap<>();
    @Getter(AccessLevel.NONE)
    private final Map<Root<?>, Map<String, From<?, ?>>> rootAliasFromMap = new HashMap<>();

    public Map<String, From<?, ?>> forRoot(Root<?> root) {
        Map<String, From<?, ?>> map = rootFromMap.get(root);
        if (Objects.isNull(map)) {
            map = new HashMap<>();
            rootFromMap.put(root, map);
        }
        return map;
    }

    public Map<String, From<?, ?>> forRootAlias(Root<?> root) {
        Map<String, From<?, ?>> map = rootAliasFromMap.get(root);
        if (Objects.isNull(map)) {
            map = new HashMap<>();
            rootAliasFromMap.put(root, map);
        }
        return map;
    }

    @Setter(AccessLevel.NONE)
    private DMLUtils.InsertBuilder insertBuilder;
    @Setter(AccessLevel.NONE)
    private DMLUtils.DeleteBuilder deleteBuilder;
    @Setter(AccessLevel.NONE)
    private DMLUtils.UpdateBuilder updateBuilder;
    @Setter(AccessLevel.NONE)
    private DMLUtils.SelectBuilder selectBuilder;
    @Setter(AccessLevel.NONE)
    private Consumer<DMLUtils.InsertBuilder> insertConfigurer = insertBuilder -> {
    };
    @Setter(AccessLevel.NONE)
    private Consumer<DMLUtils.DeleteBuilder> deleteConfigurer = deleteBuilder -> {
    };
    @Setter(AccessLevel.NONE)
    private Consumer<DMLUtils.UpdateBuilder> updateConfigurer = updateBuilder -> {
    };
    @Setter(AccessLevel.NONE)
    private Consumer<DMLUtils.SelectBuilder> selectConfigurer = selectBuilder -> {
    };

    private BiConsumer<Root<?>, CriteriaBuilder> joinClause;

    private final WhereStack whereStack = WhereStack.getInstance();

    public DMLUtils.InsertBuilder initInsertBuilder() {
        this.insertBuilder = DMLUtils.getInstance(em, rootTable, integrator).insert();
        return this.insertBuilder;
    }

    public DMLUtils.DeleteBuilder initDeleteBuilder() {
        this.deleteBuilder = DMLUtils.getInstance(em, rootTable, integrator).delete();
        return deleteBuilder;
    }

    public DMLUtils.UpdateBuilder initUpdateBuilder() {
        this.updateBuilder = DMLUtils.getInstance(em, rootTable, integrator).update();
        return updateBuilder;
    }

    public DMLUtils.SelectBuilder initSelectBuilder() {
        this.selectBuilder = DMLUtils.getInstance(em, rootTable, integrator).select();
        return selectBuilder;
    }

    public void configurerInsert(Consumer<DMLUtils.InsertBuilder> configurer) {
        this.insertConfigurer = this.insertConfigurer.andThen(configurer);
    }

    public void configurerDelete(Consumer<DMLUtils.DeleteBuilder> configurer) {
        this.deleteConfigurer = this.deleteConfigurer.andThen(configurer);
    }

    public void configurerUpdate(Consumer<DMLUtils.UpdateBuilder> configurer) {
        this.updateConfigurer = this.updateConfigurer.andThen(configurer);
    }

    public void configurerSelect(Consumer<DMLUtils.SelectBuilder> configurer) {
        this.selectConfigurer = this.selectConfigurer.andThen(configurer);
    }

    public <T> Path<T> getPath(Root<?> root, String plain) {
        Matcher simpleMatcher = SIMPLE_FIELD.matcher(plain);
        if (simpleMatcher.find()) {
            return root.get(CompileUtils.getFinalFieldName(plain));
        } else {
            Matcher refMatcher = FIELD_WITH_REF.matcher(plain);
            if (refMatcher.find()) {
                String ref = refMatcher.group("ref");
                String field = refMatcher.group("field");
                From<?, ?> from = forRootAlias(root).get(ref);
                Objects.requireNonNull(from, "ref: [%s] does not exists.".formatted(ref));
                return from.get(CompileUtils.getFinalFieldName(field));
            } else {
                throw new IllegalArgumentException("Unknown field expression[%s].".formatted(plain));
            }
        }
    }

    public String resolveTableName(String tableName) {
        if (isEmbeddedDB()) {
            return CompileUtils.getFinalTableName(tableName);
        } else if (Objects.nonNull(integrator.getDataSourceId())) {
            return "%s:%s".formatted(integrator.getDataSourceId(), tableName);
        }
        return tableName;
    }

    public boolean isEmbeddedDB() {
        return integrator == MetadataExtractorIntegrator.INSTANCE;
    }

    private ObjectNode contextNode;

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("^_\\{(?<var>[a-zA-Z0-9_]+)}_$");

    public boolean isPlaceholder(String pattern) {
        return PLACEHOLDER_PATTERN.matcher(pattern).find();
    }

    public Object renderContext(String placeholder) {
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(placeholder);
        if (!matcher.find()) {
            return placeholder;
        }
        if (Objects.nonNull(contextNode)) {
            String var = matcher.group("var");
            JsonNode at = contextNode.at("/" + var);
            if (at.isMissingNode()) {
                throw new IllegalArgumentException("argument with name[%s] dose not exists.".formatted(var));
            }
            return CompileUtils.unwrapNode(at);
        }
        return null;
    }

}
