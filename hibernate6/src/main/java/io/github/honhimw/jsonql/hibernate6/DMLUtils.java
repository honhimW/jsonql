package io.github.honhimw.jsonql.hibernate6;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.honhimw.jsonql.common.FakePreparedStatement;
import io.github.honhimw.jsonql.common.JsonUtils;
import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.EntityType;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.hibernate.SharedSessionContract;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.model.relational.SqlStringGenerationContext;
import org.hibernate.boot.spi.BootstrapContext;
import org.hibernate.boot.spi.MetadataBuildingContext;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.boot.spi.SessionFactoryOptions;
import org.hibernate.cache.internal.DisabledCaching;
import org.hibernate.cache.spi.CacheImplementor;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.pagination.LimitHandler;
import org.hibernate.dialect.pagination.LimitHelper;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.internal.util.collections.Stack;
import org.hibernate.internal.util.collections.StandardStack;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.ForeignKey;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.*;
import org.hibernate.metamodel.AttributeClassification;
import org.hibernate.metamodel.internal.JpaMetaModelPopulationSetting;
import org.hibernate.metamodel.internal.JpaStaticMetaModelPopulationSetting;
import org.hibernate.metamodel.internal.MetadataContext;
import org.hibernate.metamodel.model.domain.internal.AttributeContainer;
import org.hibernate.metamodel.model.domain.internal.BasicTypeImpl;
import org.hibernate.metamodel.model.domain.internal.EntityTypeImpl;
import org.hibernate.metamodel.model.domain.internal.SingularAttributeImpl;
import org.hibernate.metamodel.spi.MappingMetamodelImplementor;
import org.hibernate.metamodel.spi.RuntimeModelCreationContext;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.LiteralHandlingMode;
import org.hibernate.query.criteria.internal.*;
import org.hibernate.query.criteria.internal.compile.ExplicitParameterInfo;
import org.hibernate.query.criteria.internal.compile.ImplicitParameterBinding;
import org.hibernate.query.criteria.internal.compile.RenderingContext;
import org.hibernate.query.criteria.internal.expression.ParameterExpressionImpl;
import org.hibernate.query.criteria.internal.expression.function.FunctionExpression;
import org.hibernate.query.spi.Limit;
import org.hibernate.query.sqm.SqmQuerySource;
import org.hibernate.query.sqm.function.SqmFunctionRegistry;
import org.hibernate.query.sqm.tree.expression.SqmParameter;
import org.hibernate.query.sqm.tree.predicate.SqmWhereClause;
import org.hibernate.query.sqm.tree.select.SqmQuerySpec;
import org.hibernate.query.sqm.tree.select.SqmSelectStatement;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.sql.ast.Clause;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.descriptor.java.spi.UnknownBasicJavaType;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.hibernate.type.spi.TypeConfiguration;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.*;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author hon_him
 * @since 2024-07-01
 */

@SuppressWarnings({"unused"})
@Slf4j

public class DMLUtils {

    private final EntityManager em;

    private final Table table;

    private EntityType<Object> entityType;

    private Dialect dialect;

    private MetadataExtractorIntegrator integrator;

    @Setter
    private int firstPageNumber = 1;

    public static DMLUtils getInstance(EntityManager em, Table table) {
        return getInstance(em, table, MetadataExtractorIntegrator.INSTANCE);
    }

    public static DMLUtils getInstance(EntityManager em, Table table, MetadataExtractorIntegrator integrator) {
        return new DMLUtils(em, table, integrator);
    }

    private DMLUtils(EntityManager em, Table table, MetadataExtractorIntegrator integrator) {
        this.em = em;
        this.table = table;
        init(integrator);
    }

    public CriteriaBuilder getCriteriaBuilder() {
        return em.getCriteriaBuilder();
    }

    public SelectBuilder select() {
        return new SelectBuilder(dialect, em, entityType, firstPageNumber);
    }

    public UpdateBuilder update() {
        return new UpdateBuilder(dialect, em, entityType);
    }

    public DeleteBuilder delete() {
        return new DeleteBuilder(dialect, em, entityType);
    }

    public InsertBuilder insert() {
        return new InsertBuilder(dialect, em, entityType);
    }

    private void init(MetadataExtractorIntegrator integrator) {
        this.integrator = integrator;
        Metadata metadata = integrator.getMetadata();
        JdbcEnvironment jdbcEnvironment = integrator.getJdbcEnvironment();
        MetadataBuildingContext metadataBuildingContext = integrator.getMetadataBuildingContext();
        SessionFactoryImplementor sessionFactory = integrator.getSessionFactory();
        dialect = jdbcEnvironment.getDialect();

        this.entityType = initEntity(table, metadataBuildingContext, sessionFactory);
    }

    private EntityTypeImpl<Object> initEntity(Table table, MetadataBuildingContext metadataBuildingContext, SessionFactoryImplementor sessionFactory) {
        RootClass rootClass = new RootClass(metadataBuildingContext);
        rootClass.setJpaEntityName(table.getName());
        EntityTypeImpl<Object> entityType = new EntityTypeImpl<>(null, null, rootClass, sessionFactory.getJpaMetamodel());
        AttributeContainer.InFlightAccess<Object> inFlightAccess = entityType.getInFlightAccess();

        Collection<ForeignKey> foreignKeys = table.getForeignKeys().values();
        List<EntityTypeImpl<Object>> joinEntityTypes = foreignKeys.stream()
            .map(foreignKey -> {
                Table joinTable = foreignKey.getReferencedTable();
                return initEntity(joinTable, metadataBuildingContext, sessionFactory);
            })
            .toList();

        List<Column> foreignColumns = foreignKeys.stream()
            .map(Constraint::getColumns)
            .flatMap(Collection::stream)
            .toList();
        Collection<Column> rootColumns = table.getColumns();
        for (EntityTypeImpl<Object> joinEntityType : joinEntityTypes) {
            MetadataContext metadataContext = metadataContext();
            SingularAttributeImpl<Object, Object> attribute = new SingularAttributeImpl<>(
                entityType,
                joinEntityType.getName(),
                AttributeClassification.MANY_TO_ONE,
                joinEntityType,
//                new UnknownBasicJavaType<>(Object.class, "anonymous"),
                null,
                null,
                false,
                false,
                false,
                false,
                metadataContext
            );
//            Property property = new Property();
//            property.setName(joinEntityType.getName());
//            AttributeFactory.buildAttribute(entityType, null, metadataContext);
            inFlightAccess.addAttribute(attribute);
        }

        for (Column rootColumn : rootColumns) {
//            if (foreignColumns.contains(rootColumn)) {
//                continue;
//            }
            PrimaryKey primaryKey = table.getPrimaryKey();
            boolean isId = Objects.nonNull(primaryKey) && primaryKey.containsColumn(rootColumn);
            Class<?> javaType = String.class;
            Value value = rootColumn.getValue();
            if (Objects.nonNull(value)) {
                javaType = value.getType().getReturnedClass();
            }
            JavaType<?> _javaType = new UnknownBasicJavaType<>(javaType);
            JdbcType _jdbcType = integrator.getDatabase().getTypeConfiguration().getJdbcTypeRegistry().getDescriptor(TypeConvertUtils.type2jdbc(javaType).getVendorTypeNumber());
            SingularAttributeImpl<Object, ?> attribute = new SingularAttributeImpl<>(
                entityType,
                rootColumn.getName(),
                AttributeClassification.BASIC,
                new BasicTypeImpl<>(_javaType, _jdbcType),
                null,
                null,
                isId,
                false,
                rootColumn.isNullable(),
                false,
                metadataContext()
            );
            inFlightAccess.addAttribute(attribute);
        }
        return entityType;
    }

    private MetadataContext metadataContext() {
        return new MetadataContext(
            integrator.getSessionFactory().getJpaMetamodel(),
            integrator.getSessionFactory().getMappingMetamodel(),
            integrator.getInFlightMetadataCollector(),
            JpaStaticMetaModelPopulationSetting.DISABLED,
            JpaMetaModelPopulationSetting.DISABLED,
            runtimeModelCreationContext()
        );
    }

    private RuntimeModelCreationContext runtimeModelCreationContext() {
        return new RuntimeModelCreationContext() {
            @Override
            public BootstrapContext getBootstrapContext() {
                return integrator.getBootstrapContext();
            }

            @Override
            public SessionFactoryImplementor getSessionFactory() {
                // this is bad, we're not yet fully-initialized
                return integrator.getSessionFactory();
            }

            @Override
            public MetadataImplementor getBootModel() {
                return integrator.getInFlightMetadataCollector();
            }

            @Override
            public MappingMetamodelImplementor getDomainModel() {
                return integrator.getSessionFactory().getMappingMetamodel();
            }

            @Override
            public CacheImplementor getCache() {
                return new DisabledCaching(integrator.getSessionFactory());
            }

            @Override
            public Map<String, Object> getSettings() {
                return Map.of();
            }

            @Override
            public Dialect getDialect() {
                return getJdbcServices().getDialect();
            }

            @Override
            public SqmFunctionRegistry getFunctionRegistry() {
                return integrator.getSessionFactory().getQueryEngine().getSqmFunctionRegistry();
            }

            @Override
            public TypeConfiguration getTypeConfiguration() {
                return integrator.getDatabase().getTypeConfiguration();
            }

            @Override
            public SessionFactoryOptions getSessionFactoryOptions() {
                return integrator.getSessionFactory().getSessionFactoryOptions();
            }

            @Override
            public JdbcServices getJdbcServices() {
                return integrator.getSessionFactory().getJdbcServices();
            }

            @Override
            public SqlStringGenerationContext getSqlStringGenerationContext() {
                return integrator.getSessionFactory().getSqlStringGenerationContext();
            }

            @Override
            public ServiceRegistry getServiceRegistry() {
                return integrator.getDatabase().getServiceRegistry();
            }
        };
    }

    /**
     * Query
     */
    public static class SelectBuilder {

        private final EntityManager em;

        private final CriteriaBuilder cb;

        private final EntityType<Object> entityType;

        private final int firstPageNumber;

        private Dialect dialect;

        private Limit limit = Limit.NONE;

        private Integer pageNumber;

        private Integer pageSize;

        private QueryConsumer queryConsumer;

        private SelectBuilder(Dialect dialect, EntityManager em, EntityType<Object> entityType, int firstPageNumber) {
            this.dialect = dialect;
            this.em = em;
            this.entityType = entityType;
            this.firstPageNumber = firstPageNumber;
            this.cb = em.getCriteriaBuilder();
        }

        public SelectBuilder dialect(Dialect dialect) {
            this.dialect = dialect;
            return this;
        }

        public SelectBuilder applyQuery(QueryConsumer queryConsumer) {
            if (Objects.isNull(this.queryConsumer)) {
                this.queryConsumer = queryConsumer;
            } else {
                this.queryConsumer = this.queryConsumer.andThen(queryConsumer);
            }
            return this;
        }

        public SelectBuilder applyPage(int pageNumber, int pageSize) {
            limit = new Limit();
            int firstRow = firstRow(pageNumber, pageSize);
            int maxRows = maxRows(firstRow, pageSize);

            limit.setFirstRow(firstRow);
            limit.setMaxRows(maxRows);
            this.pageNumber = pageNumber;
            this.pageSize = pageSize;
            return this;
        }

        private int firstRow(int pageNumber, int pageSize) {
            return Math.max(pageNumber - firstPageNumber, 0) * pageSize;
        }

        private int maxRows(int firstRow, int pageSize) {
            return pageSize;
//            return firstRow + pageSize;
        }

        public Tuple2<String, Map<String, Object>> jpaQL() {
            return jpaQL(tupleCriteriaQuery());
        }

        public Tuple2<String, List<Object>> jdbcQL() {
            return jdbcQL(tupleCriteriaQuery(), true);
        }

        public String sql() {
            return sql(tupleCriteriaQuery(), true);
        }

        public Tuple2<String, Map<String, Object>> countJpaQL() {
            return jpaQL(countCriteriaQuery());
        }

        public Tuple2<String, List<Object>> countJdbcQL() {
            return jdbcQL(countCriteriaQuery(), false);
        }

        public String countSQL() {
            return sql(countCriteriaQuery(), false);
        }

        private CriteriaQuery<Tuple> tupleCriteriaQuery() {
            CriteriaQuery<Tuple> tupleQuery = cb.createTupleQuery();
            Root<Object> root = tupleQuery.from(entityType);
            if (Objects.nonNull(this.queryConsumer)) {
                queryConsumer.accept(root, tupleQuery, cb);
            }

            return tupleQuery;
        }

        private CriteriaQuery<Long> countCriteriaQuery() {
            CriteriaQuery<Long> query = cb.createQuery(Long.class);
            Root<Object> root = query.from(entityType);

            if (Objects.nonNull(this.queryConsumer)) {
                queryConsumer.accept(root, query, cb);
            }

            if (query.isDistinct()) {
                query.select(cb.countDistinct(root));
            } else {
                query.select(cb.count(root));
            }
            return query;
        }

        private Tuple2<String, Map<String, Object>> jpaQL(CriteriaQuery<?> criteriaQuery) {
            StringBuilder jpaqlQuery = new StringBuilder();
            SharedSessionContractImplementor entityManager = (SharedSessionContractImplementor) em.getDelegate();
            SqmSelectStatement<?> selectStatement = (SqmSelectStatement<?>) criteriaQuery;
            SqmQuerySpec<?> querySpec = selectStatement.getQuerySpec();
            querySpec.appendHqlString(jpaqlQuery);
            Set<SqmParameter<?>> sqmParameters = selectStatement.getSqmParameters();
            for (SqmParameter<?> sqmParameter : sqmParameters) {
                String name = sqmParameter.getName();
                Integer position = sqmParameter.getPosition();
            }

            Tuple2<RenderingContext, List<ImplicitParameterBinding>> t2 = renderingContext(entityManager, dialect);
            RenderingContext renderingContext = t2._1();
            List<ImplicitParameterBinding> implicitParameterBindings = t2._2();
            QueryStructure<?> queryStructure = criteriaQuery.getQueryStructure();
            queryStructure.render(jpaqlQuery, renderingContext);
            renderOrderByClause(criteriaQuery, renderingContext, jpaqlQuery);
            String jpaQL = jpaqlQuery.toString();
            if (log.isDebugEnabled()) {
                log.debug("jpaQL: {}", jpaQL);
            }
            Map<String, Object> params = implicitParameterBindings.stream()
                .filter(binding -> binding instanceof LiteralImplicitParameterBinding)
                .map(binding -> (LiteralImplicitParameterBinding) binding)
                .collect(Collectors.toMap(LiteralImplicitParameterBinding::getParameterName, LiteralImplicitParameterBinding::getLiteral));
            return Tuples.of(jpaQL, params);
        }

        private Tuple2<String, List<Object>> jdbcQL(DelegateCriteriaQuery<?> criteriaQuery, boolean doLimit) {
            Tuple2<String, Map<String, Object>> stringMapTuple2 = jpaQL(criteriaQuery);
            String jpaQL = stringMapTuple2._1();
            Map<String, Object> params = stringMapTuple2._2();

            Tuple2<String, List<Object>> sqlNArgs = sqlTemplate(jpaQL, params);
            String jdbcQL = sqlNArgs._1();
            List<Object> parameters = sqlNArgs._2();

            FakePreparedStatement fakePreparedStatement = new FakePreparedStatement();
            int col = 1;
            LimitHandler limitHandler = dialect.getLimitHandler();
            if (doLimit) {
                limitHandler.processSql(jdbcQL, limit);
                if (log.isDebugEnabled()) {
                    log.debug("with limit SQL: {}", jdbcQL);
                }
                try {
                    col += limitHandler.bindLimitParametersAtStartOfQuery(limit, fakePreparedStatement, col);
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }
            for (Object parameter : parameters) {
                try {
                    fakePreparedStatement.setObject(col, parameter);
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
                col++;
            }
            if (doLimit) {
                try {
                    col += limitHandler.bindLimitParametersAtEndOfQuery(limit, fakePreparedStatement, col);
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }

            parameters = fakePreparedStatement.getParams();
            fakePreparedStatement.close();
            return Tuples.of(jdbcQL, parameters);
        }

        private String sql(DelegateCriteriaQuery<?> criteriaQuery, boolean limit) {
            Tuple2<String, List<Object>> stringListTuple2 = jdbcQL(criteriaQuery, limit);
            String jdbcQL = stringListTuple2._1();
            List<Object> parameters = stringListTuple2._2();
            String nativeSQL = format(jdbcQL, parameters);
            if (log.isDebugEnabled()) {
                log.debug("final SQL: {}", nativeSQL);
            }

            return nativeSQL;
        }

        public List<SelectionResult> getResult() {
            DelegateCriteriaQuery<Tuple> tupleDelegateCriteriaQuery = tupleCriteriaQuery();
            Selection<?> selection = tupleDelegateCriteriaQuery.getSelection();
            if (selection.isCompoundSelection()) {
                List<Selection<?>> compoundSelectionItems = selection.getCompoundSelectionItems();
                int size = compoundSelectionItems.size();
                Tuple2<String, List<Object>> stringListTuple2 = jdbcQL(tupleDelegateCriteriaQuery, false);
                String sql = stringListTuple2._1();
                List<Object> parameters = stringListTuple2._2();
                Query nativeQuery = em.createNativeQuery(sql);
                for (int i = 1; i <= parameters.size(); i++) {
                    nativeQuery.setParameter(i, parameters.get(i - 1));
                }

                if (Objects.nonNull(rowSelection)) {
                    if (LimitHelper.hasFirstRow(rowSelection)) {
                        nativeQuery.setFirstResult(rowSelection.getFirstRow());
                    }
                    if (LimitHelper.hasMaxRows(rowSelection)) {
                        nativeQuery.setMaxResults(rowSelection.getMaxRows());
                    }
                }

                Object o = nativeQuery.getSingleResult();

                if (o.getClass().isArray()) {
                    Object[] objects = (Object[]) o;
                    Validate.validState(size == objects.length, "selection-items size should equal to result-elements size.");
                    List<SelectionResult> selectionResults = new ArrayList<>(size);
                    for (int i = 0; i < size; i++) {
                        SelectionResult element = new SelectionResult(compoundSelectionItems.get(i), objects[i]);
                        selectionResults.add(i, element);
                    }
                    return selectionResults;
                }
            }
            return Collections.emptyList();
        }

        public List<List<SelectionResult>> getResults() {
            DelegateCriteriaQuery<Tuple> tupleDelegateCriteriaQuery = tupleCriteriaQuery();
            Selection<?> selection = tupleDelegateCriteriaQuery.getSelection();
            AtomicReference<List<List<SelectionResult>>> ref = new AtomicReference<>();
            if (selection.isCompoundSelection()) {
                List<Selection<?>> compoundSelectionItems = selection.getCompoundSelectionItems();
                Tuple2<String, List<Object>> stringListTuple2 = jdbcQL(tupleDelegateCriteriaQuery, false);
                String sql = stringListTuple2._1();
                if (Objects.nonNull(rowSelection)) {
                    dialect.getLimitHandler().processSql(sql, rowSelection);
                }
                List<Object> parameters = stringListTuple2._2();
                SharedSessionContract sessionContract = em.unwrap(SharedSessionContract.class);

                sessionContract.doWork(connection -> {
                    PreparedStatement preparedStatement = connection.prepareStatement(sql);
                    for (int i = 1; i <= parameters.size(); i++) {
                        preparedStatement.setObject(i, parameters.get(i - 1));
                    }

                    ResultSet resultSet = preparedStatement.executeQuery();
                    ResultSetMetaData metaData = resultSet.getMetaData();
                    int columnCount = metaData.getColumnCount();
                    List<Selection<?>> selectionItems = compoundSelectionItems;
                    if (selectionItems.size() == 1 && selectionItems.get(0) instanceof WildcardSelection) {
                        selectionItems = new ArrayList<>();
                        for (int i = 1; i <= columnCount; i++) {
                            String columnName = metaData.getColumnLabel(i);
                            selectionItems.add(new MockSelection<>(columnName));
                        }
                    }
                    List<List<SelectionResult>> results = new ArrayList<>();
                    while (resultSet.next()) {
                        List<SelectionResult> selectionResults = new ArrayList<>();
                        for (int i = 1; i <= columnCount; i++) {
                            Object columnValue = resultSet.getObject(i);
                            SelectionResult selectionResult = new SelectionResult(selectionItems.get(i - 1), columnValue);
                            selectionResults.add(selectionResult);
                        }
                        results.add(selectionResults);
                    }
                    ref.set(results);
                });
            }
            return ref.get();
        }

        public List<Map<String, Object>> getResultsMap() {
            List<List<SelectionResult>> result = getResults();
            List<Map<String, Object>> results = new ArrayList<>(result.size());
            for (List<SelectionResult> selectionResults : result) {
                Map<String, Object> map = new HashMap<>();
                for (SelectionResult selectionResult : selectionResults) {
                    map.put(selectionResult.getName(), selectionResult.getValue());
                }
                results.add(map);
            }
            return results;
        }

        public Map<String, Object> getResultMap() {
            List<SelectionResult> result = getResult();
            Map<String, Object> map = new HashMap<>();
            for (SelectionResult selectionResult : result) {
                map.put(selectionResult.getName(), selectionResult.getValue());
            }
            return map;
        }

        public <T> List<T> getResults(Class<T> type) {
            try {
                ObjectMapper objectMapper = JsonUtils.getObjectMapper();
                List<List<SelectionResult>> result = getResults();
                List<T> results = new ArrayList<>(result.size());
                for (List<SelectionResult> selectionResults : result) {
                    ObjectNode objectNode = objectMapper.createObjectNode();
                    for (SelectionResult selectionResult : selectionResults) {
                        objectNode.putPOJO(selectionResult.getName(), selectionResult.getValue());
                    }
                    T t = objectMapper.treeToValue(objectNode, type);
                    results.add(t);
                }
                return results;
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        public <T> T getResult(Class<T> type) {
            try {
                ObjectMapper objectMapper = JsonUtils.getObjectMapper();
                List<SelectionResult> result = getResult();
                ObjectNode objectNode = objectMapper.createObjectNode();
                for (SelectionResult selectionResult : result) {
                    objectNode.putPOJO(selectionResult.getName(), selectionResult.getValue());
                }
                return objectMapper.treeToValue(objectNode, type);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        public Long count() {
            String sql = countSQL();
            Query nativeQuery = em.createNativeQuery(sql);
            Object o = nativeQuery.getSingleResult();

            if (o.getClass().isArray()) {
                Object[] objects = (Object[]) o;
                o = objects[0];
            }
            if (o instanceof Number number) {
                return number.longValue();
            } else {
                throw new ArithmeticException();
            }
        }

        protected void renderOrderByClause(CriteriaQuery<?> criteriaQuery, RenderingContext renderingContext, StringBuilder jpaqlBuffer) {
            SqmSelectStatement<?> _criteriaQuery = (SqmSelectStatement<?>) criteriaQuery;
            SqmQuerySpec<?> querySpec = _criteriaQuery.getQuerySpec();
            SqmWhereClause whereClause = querySpec.getWhereClause();
            List<Order> orderList = criteriaQuery.getOrderList();
            if (orderList.isEmpty()) {
                return;
            }

            renderingContext.getClauseStack().push(Clause.ORDER);
            try {
                jpaqlBuffer.append(" order by ");
                String sep = "";
                for (Order orderSpec : orderList) {
                    jpaqlBuffer.append(sep)
                        .append(((Renderable) orderSpec.getExpression()).render(renderingContext))
                        .append(orderSpec.isAscending() ? " asc" : " desc");
                    if (orderSpec instanceof OrderImpl) {
                        Boolean nullsFirst = ((OrderImpl) orderSpec).getNullsFirst();
                        if (nullsFirst != null) {
                            if (nullsFirst) {
                                jpaqlBuffer.append(" nulls first");
                            } else {
                                jpaqlBuffer.append(" nulls last");
                            }
                        }
                    }
                    sep = ", ";
                }
            } finally {
                renderingContext.getClauseStack().pop();
            }
        }

    }

    public static class UpdateBuilder {

        private final EntityManager em;

        private final CriteriaBuilder cb;

        private final EntityType<Object> entityType;

        private final DelegateCriteriaUpdate<Object> criteriaUpdate;

        private final Root<Object> root;

        private Dialect dialect;

        private UpdateBuilder(Dialect dialect, EntityManager em, EntityType<Object> entityType) {
            this.dialect = dialect;
            this.em = em;
            this.entityType = entityType;
            this.cb = em.getCriteriaBuilder();
            this.criteriaUpdate = new DelegateCriteriaUpdate<>((CriteriaUpdateImpl<Object>) cb.createCriteriaUpdate(Object.class));
            this.root = criteriaUpdate.from(entityType);
        }

        public UpdateBuilder dialect(Dialect dialect) {
            this.dialect = dialect;
            return this;
        }

        public UpdateBuilder applyUpdate(UpdateConsumer consumer) {
            consumer.accept(root, criteriaUpdate, cb);
            return this;
        }

        public Tuple2<String, Map<String, Object>> jpaQL() {
            return jpaQL(criteriaUpdate);
        }

        public Tuple2<String, List<Object>> jdbcQL() {
            return jdbcQL(criteriaUpdate);
        }

        public String sql() {
            return sql(criteriaUpdate);
        }

        private Tuple2<String, Map<String, Object>> jpaQL(DelegateCriteriaUpdate<?> criteriaUpdate) {
            SharedSessionContractImplementor entityManager = (SharedSessionContractImplementor) em.getDelegate();
            Tuple2<RenderingContext, List<ImplicitParameterBinding>> t2 = renderingContext(entityManager, dialect);
            RenderingContext renderingContext = t2._1();
            List<ImplicitParameterBinding> implicitParameterBindings = t2._2();
            String jpaQL = criteriaUpdate.renderQuery(renderingContext);
            if (log.isDebugEnabled()) {
                log.debug("jpaQL: {}", jpaQL);
            }
            Map<String, Object> params = implicitParameterBindings.stream()
                .filter(binding -> binding instanceof LiteralImplicitParameterBinding)
                .map(binding -> (LiteralImplicitParameterBinding) binding)
                .collect(Collectors.toMap(LiteralImplicitParameterBinding::getParameterName, LiteralImplicitParameterBinding::getLiteral));
            return Tuples.of(jpaQL, params);
        }

        private Tuple2<String, List<Object>> jdbcQL(DelegateCriteriaUpdate<?> criteriaUpdate) {
            Tuple2<String, Map<String, Object>> stringMapTuple2 = jpaQL(criteriaUpdate);
            String jpaQL = stringMapTuple2._1();
            Map<String, Object> params = stringMapTuple2._2();
            Tuple2<String, List<Object>> sqlNArgs = sqlTemplate(jpaQL, params);
            if (log.isDebugEnabled()) {
                log.debug("jdbcQL: {}", sqlNArgs._1());
            }
            return sqlNArgs;
        }

        private String sql(DelegateCriteriaUpdate<?> criteriaUpdate) {
            Tuple2<String, List<Object>> stringListTuple2 = jdbcQL(criteriaUpdate);
            String jdbcQL = stringListTuple2._1();
            List<Object> parameters = stringListTuple2._2();
            String sql = format(jdbcQL, parameters);
            if (log.isDebugEnabled()) {
                log.debug("final SQL: {}", sql);
            }
            return sql;
        }

        public int execute() {
            Tuple2<String, List<Object>> stringListTuple2 = jdbcQL();
            String sql = stringListTuple2._1();
            List<Object> parameters = stringListTuple2._2();
            Query nativeQuery = em.createNativeQuery(sql);
            for (int i = 1; i <= parameters.size(); i++) {
                nativeQuery.setParameter(i, parameters.get(i - 1));
            }
            return nativeQuery.executeUpdate();
        }

    }

    public static class InsertBuilder {

        private final EntityManager em;

        private final CriteriaBuilder cb;

        private final EntityType<Object> entityType;

        private final CriteriaInsertImpl<Object> criteriaInsert;

        private final Root<Object> root;

        private Dialect dialect;

        private boolean generatedValue = false;

        private InsertBuilder(Dialect dialect, EntityManager em, EntityType<Object> entityType) {
            this.dialect = dialect;
            this.em = em;
            this.entityType = entityType;
            this.cb = em.getCriteriaBuilder();
            this.criteriaInsert = new CriteriaInsertImpl<>((CriteriaBuilderImpl) cb);
            this.root = criteriaInsert.from(entityType);
        }

        public InsertBuilder dialect(Dialect dialect) {
            this.dialect = dialect;
            return this;
        }

        public InsertBuilder generatedValue(boolean flag) {
            this.generatedValue = flag;
            return this;
        }

        public InsertBuilder applyInsert(InsertConsumer consumer) {
            consumer.accept(root, criteriaInsert, cb);
            return this;
        }

        public Tuple2<String, Map<String, Object>> jpaQL() {
            return jpaQL(criteriaInsert);
        }

        public Tuple2<String, List<Object>> jdbcQL() {
            return jdbcQL(criteriaInsert);
        }

        public String sql() {
            return sql(criteriaInsert);
        }

        private Tuple2<String, Map<String, Object>> jpaQL(CriteriaInsertImpl<?> criteriaInsert) {
            SharedSessionContractImplementor entityManager = (SharedSessionContractImplementor) em.getDelegate();
            Tuple2<RenderingContext, List<ImplicitParameterBinding>> t2 = renderingContext(entityManager, dialect);
            RenderingContext renderingContext = t2._1();
            List<ImplicitParameterBinding> implicitParameterBindings = t2._2();
            String jpaQL = criteriaInsert.renderQuery(renderingContext);
            if (log.isDebugEnabled()) {
                log.debug("jpaQL: {}", jpaQL);
            }
            Map<String, Object> params = implicitParameterBindings.stream()
                .filter(binding -> binding instanceof LiteralImplicitParameterBinding)
                .map(binding -> (LiteralImplicitParameterBinding) binding)
                .collect(Collectors.toMap(LiteralImplicitParameterBinding::getParameterName, LiteralImplicitParameterBinding::getLiteral));
            return Tuples.of(jpaQL, params);
        }

        private Tuple2<String, List<Object>> jdbcQL(CriteriaInsertImpl<?> criteriaInsert) {
            Tuple2<String, Map<String, Object>> stringMapTuple2 = jpaQL(criteriaInsert);
            String jpaQL = stringMapTuple2._1();
            Map<String, Object> params = stringMapTuple2._2();
            Tuple2<String, List<Object>> sqlNArgs = sqlTemplate(jpaQL, params);
            if (log.isDebugEnabled()) {
                log.debug("jdbcQL: {}", sqlNArgs._1());
            }
            return sqlNArgs;
        }

        private String sql(CriteriaInsertImpl<?> criteriaInsert) {
            Tuple2<String, List<Object>> stringListTuple2 = jdbcQL(criteriaInsert);
            String jdbcQL = stringListTuple2._1();
            List<Object> parameters = stringListTuple2._2();
            String sql = format(jdbcQL, parameters);
            if (log.isDebugEnabled()) {
                log.debug("final SQL: {}", sql);
            }
            return sql;
        }

        public int execute() {
            Tuple2<String, List<Object>> stringListTuple2 = jdbcQL();
            String sql = stringListTuple2._1();
            List<Object> parameters = stringListTuple2._2();
            SharedSessionContract sessionContract = em.unwrap(SharedSessionContract.class);
            AtomicInteger ref = new AtomicInteger();
            sessionContract.doWork(connection -> {
                try (PreparedStatement statement = connection.prepareStatement(sql, generatedValue ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS)) {
                    for (int i = 1; i <= parameters.size(); i++) {
                        statement.setObject(i, parameters.get(i - 1));
                    }
                    int count = statement.executeUpdate();
                    if (generatedValue) {
                        ResultSet rs = statement.getGeneratedKeys();
                        if (rs.next()) {
                            ref.set(rs.getInt(1));
                        } else {
                            ref.set(count);
                        }
                    } else {
                        ref.set(count);
                    }
                }
            });
            return ref.get();
        }

    }

    public static class DeleteBuilder {

        private final EntityManager em;

        private final CriteriaBuilder cb;

        private final EntityType<Object> entityType;

        private final DelegateCriteriaDelete<Object> criteriaDelete;

        private final Root<Object> root;

        private Dialect dialect;

        private DeleteBuilder(Dialect dialect, EntityManager em, EntityType<Object> entityType) {
            this.dialect = dialect;
            this.em = em;
            this.entityType = entityType;
            this.cb = em.getCriteriaBuilder();
            this.criteriaDelete = new DelegateCriteriaDelete<>((CriteriaDeleteImpl<Object>) cb.createCriteriaDelete(Object.class));
            this.root = criteriaDelete.from(entityType);
        }

        public DeleteBuilder dialect(Dialect dialect) {
            this.dialect = dialect;
            return this;
        }

        public DeleteBuilder applyDelete(DeleteConsumer consumer) {
            consumer.accept(root, criteriaDelete, cb);
            return this;
        }

        public Tuple2<String, Map<String, Object>> jpaQL() {
            return jpaQL(criteriaDelete);
        }

        public Tuple2<String, List<Object>> jdbcQL() {
            return jdbcQL(criteriaDelete);
        }

        public String sql() {
            return sql(criteriaDelete);
        }

        private Tuple2<String, Map<String, Object>> jpaQL(DelegateCriteriaDelete<?> criteriaDelete) {
            SharedSessionContractImplementor entityManager = (SharedSessionContractImplementor) em.getDelegate();
            Tuple2<RenderingContext, List<ImplicitParameterBinding>> t2 = renderingContext(entityManager, dialect);
            RenderingContext renderingContext = t2._1();
            List<ImplicitParameterBinding> implicitParameterBindings = t2._2();
            String jpaQL = criteriaDelete.renderQuery(renderingContext);
            if (log.isDebugEnabled()) {
                log.debug("jpaQL: {}", jpaQL);
            }
            Map<String, Object> params = implicitParameterBindings.stream()
                .filter(binding -> binding instanceof LiteralImplicitParameterBinding)
                .map(binding -> (LiteralImplicitParameterBinding) binding)
                .collect(Collectors.toMap(LiteralImplicitParameterBinding::getParameterName, LiteralImplicitParameterBinding::getLiteral));
            return Tuples.of(jpaQL, params);
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        private Tuple2<String, List<Object>> jdbcQL(DelegateCriteriaDelete<?> criteriaDelete) {
            DelegateCriteriaDelete<?> renderRoot = new DelegateCriteriaDelete(criteriaDelete) {
                @Override
                public String renderQuery(RenderingContext renderingContext) {
                    final StringBuilder jpaql = new StringBuilder("delete ");
                    renderRoot(jpaql, renderingContext);
                    renderRestrictions(jpaql, renderingContext);
                    return jpaql.toString();
                }

                @Override
                public void renderRoot(StringBuilder jpaql, RenderingContext renderingContext) {
                    Root root = getRoot();
                    String tableName = root.getModel().getName();
                    if (root instanceof FromImplementor<?, ?> fromImplementor) {
                        ((FromImplementor<?, ?>) root).prepareAlias(renderingContext);
                    }
                    String alias = root.getAlias();
                    jpaql
                        .append(alias)
                        .append(" from ")
                        .append(tableName)
                        .append(" as ")
                        .append(alias);
                }
            };
            Tuple2<String, Map<String, Object>> stringMapTuple2 = jpaQL(renderRoot);
            String jpaQL = stringMapTuple2._1();
            Map<String, Object> params = stringMapTuple2._2();
            Tuple2<String, List<Object>> sqlNArgs = sqlTemplate(jpaQL, params);
            if (log.isDebugEnabled()) {
                log.debug("jdbcQL: {}", sqlNArgs._1());
            }
            return sqlNArgs;
        }

        private String sql(DelegateCriteriaDelete<?> criteriaDelete) {
            Tuple2<String, List<Object>> stringListTuple2 = jdbcQL(criteriaDelete);
            String jdbcQL = stringListTuple2._1();
            List<Object> parameters = stringListTuple2._2();
            String sql = format(jdbcQL, parameters);
            if (log.isDebugEnabled()) {
                log.debug("final SQL: {}", sql);
            }
            return sql;
        }

        public int execute() {
            Tuple2<String, List<Object>> stringListTuple2 = jdbcQL();
            String sql = stringListTuple2._1();
            List<Object> parameters = stringListTuple2._2();
            Query nativeQuery = em.createNativeQuery(sql);
            for (int i = 1; i <= parameters.size(); i++) {
                nativeQuery.setParameter(i, parameters.get(i - 1));
            }
            return nativeQuery.executeUpdate();
        }

    }

    public interface QueryConsumer {
        void accept(Root<Object> root, CriteriaQuery<?> query, CriteriaBuilder cb);

        default QueryConsumer andThen(QueryConsumer after) {
            return (root, query, cb) -> {
                accept(root, query, cb);
                after.accept(root, query, cb);
            };
        }
    }

    public interface UpdateConsumer {
        void accept(Root<Object> root, CriteriaUpdate<?> update, CriteriaBuilder cb);

        default UpdateConsumer andThen(UpdateConsumer after) {
            return (root, update, cb) -> {
                accept(root, update, cb);
                after.accept(root, update, cb);
            };
        }
    }

    public interface InsertConsumer {
        void accept(Root<Object> root, CriteriaInsert<?> insert, CriteriaBuilder cb);

        default InsertConsumer andThen(InsertConsumer after) {
            return (root, insert, cb) -> {
                accept(root, insert, cb);
                after.accept(root, insert, cb);
            };
        }
    }

    public interface DeleteConsumer {
        void accept(Root<Object> root, CriteriaDelete<?> delete, CriteriaBuilder cb);

        default DeleteConsumer andThen(DeleteConsumer after) {
            return (root, delete, cb) -> {
                accept(root, delete, cb);
                after.accept(root, delete, cb);
            };
        }
    }

    @Getter
    public static class SelectionResult {
        private final Selection<?> selection;
        private final Object value;

        public SelectionResult(Selection<?> selection, Object value) {
            this.selection = selection;
            this.value = value;
        }

        public String getName() {
            String alias = selection.getAlias();
            if (StringUtils.isNotBlank(alias)) {
                return alias;
            } else if (selection instanceof PathImplementor<?> pathImplementor) {
                return pathImplementor.getAttribute().getName();
            } else if (selection instanceof FunctionExpression<?> functionExpression) {
                return functionExpression.getFunctionName();
            } else if (selection instanceof Parameter<?> parameter) {
                return parameter.getName();
            } else {
                throw new IllegalArgumentException("selection does not have a name.");
            }
        }

        public Class<?> getType() {
            return selection.getJavaType();
        }

    }

    @SuppressWarnings("rawtypes")
    private static Tuple2<RenderingContext, List<ImplicitParameterBinding>> renderingContext(SharedSessionContractImplementor entityManager, Dialect dialect) {
        final SessionFactoryImplementor sessionFactory = entityManager.getFactory();
        final Map<ParameterExpression<?>, ExplicitParameterInfo<?>> explicitParameterInfoMap = new HashMap<>();
        final List<ImplicitParameterBinding> implicitParameterBindings = new ArrayList<>();
        RenderingContext renderingContext = new RenderingContext() {
            private int aliasCount;
            private int explicitParameterCount;

            private final Stack<Clause> clauseStack = new StandardStack<>();
            private final Stack<FunctionExpression> functionContextStack = new StandardStack<>();

            @Override
            public String generateAlias() {
                return "g_a_" + aliasCount++;
            }

            public String generateParameterName() {
                return "param" + explicitParameterCount++;
            }

            @Override
            public Stack<Clause> getClauseStack() {
                return clauseStack;
            }

            @Override
            public Stack<FunctionExpression> getFunctionStack() {
                return functionContextStack;
            }

            @Override
            @SuppressWarnings("unchecked")
            public ExplicitParameterInfo registerExplicitParameter(ParameterExpression<?> criteriaQueryParameter) {
                ExplicitParameterInfo parameterInfo = explicitParameterInfoMap.get(criteriaQueryParameter);
                if (parameterInfo == null) {
                    if (StringHelper.isNotEmpty(criteriaQueryParameter.getName()) && !((ParameterExpressionImpl) criteriaQueryParameter).isNameGenerated()) {
                        parameterInfo = new ExplicitParameterInfo(
                            criteriaQueryParameter.getName(),
                            null,
                            criteriaQueryParameter.getJavaType()
                        );
                    } else if (criteriaQueryParameter.getPosition() != null) {
                        parameterInfo = new ExplicitParameterInfo(
                            null,
                            criteriaQueryParameter.getPosition(),
                            criteriaQueryParameter.getJavaType()
                        );
                    } else {
                        parameterInfo = new ExplicitParameterInfo(
                            generateParameterName(),
                            null,
                            criteriaQueryParameter.getJavaType()
                        );
                    }

                    explicitParameterInfoMap.put(criteriaQueryParameter, parameterInfo);
                }

                return parameterInfo;
            }

            public String registerLiteralParameterBinding(final Object literal, final Class javaType) {
                final String parameterName = generateParameterName();
                final ImplicitParameterBinding binding = new LiteralImplicitParameterBinding(parameterName, javaType, literal);
                implicitParameterBindings.add(binding);
                return parameterName;
            }

            @SuppressWarnings("deprecation")
            public String getCastType(Class javaType) {
                SessionFactoryImplementor factory = entityManager.getFactory();
                org.hibernate.type.Type hibernateType = factory.getTypeResolver().heuristicType(javaType.getName());
                if (hibernateType == null) {
                    throw new IllegalArgumentException(
                        "Could not convert java type [" + javaType.getName() + "] to Hibernate type"
                    );
                }
                return hibernateType.getName();
            }

            @Override
            public Dialect getDialect() {
                return dialect;
            }

            @Override
            public LiteralHandlingMode getCriteriaLiteralHandlingMode() {
//                return criteriaLiteralHandlingMode;
                return LiteralHandlingMode.BIND;
            }
        };
        return Tuples.of(renderingContext, implicitParameterBindings);
    }

    private static Tuple2<String, List<Object>> sqlTemplate(String jpaQL, Map<String, Object> map) {
        List<Object> params = new ArrayList<>();
        StringBuilder sb = new StringBuilder(jpaQL);
        int index = 0;
        while (index < sb.length()) {
            if (sb.charAt(index) == ':') {
                int end = index + 1;
                while (end < sb.length() && Character.isLetterOrDigit(sb.charAt(end))) {
                    end++;
                }
                String key = sb.substring(index + 1, end);
                if (map.containsKey(key)) {
                    sb.replace(index, end, "?");
                    Object e = map.get(key);
                    params.add(e);
                }
            }
            index++;
        }
        return Tuples.of(sb.toString(), params);
    }

    private static String format(String template, List<Object> args) {
        StringBuilder sb = new StringBuilder(template);
        int index = sb.indexOf("?");
        while (index != -1) {
            Object value = args.remove(0);
            if (value instanceof CharSequence charSequence) {
                value = "'%s'".formatted(charSequence);
            }
            String str = String.valueOf(value);
            sb.replace(index, index + 1, str);
            index = sb.indexOf("?", index + str.length());
        }
        return sb.toString();
    }

    @Getter
    private static class LiteralImplicitParameterBinding implements ImplicitParameterBinding {

        private final String parameterName;

        private final Class<?> javaType;

        private final Object literal;

        public LiteralImplicitParameterBinding(String parameterName, Class<?> javaType, Object literal) {
            this.parameterName = parameterName;
            this.javaType = javaType;
            this.literal = literal;
        }

        public void bind(TypedQuery typedQuery) {
            if (literal instanceof Parameter) {
                return;
            }
            typedQuery.setParameter(parameterName, literal);
        }

    }

    private interface Tuples {
        static <T1, T2> Tuple2<T1, T2> of(T1 t1, T2 t2) {
            return new Tuple2<>(t1, t2);
        }
    }


    public static class Tuple2<T1, T2> implements Tuples {
        private final T1 _1;
        private final T2 _2;

        private Tuple2(T1 _1, T2 _2) {
            this._1 = _1;
            this._2 = _2;
        }

        public T1 _1() {
            return _1;
        }

        public T2 _2() {
            return _2;
        }
    }

}
