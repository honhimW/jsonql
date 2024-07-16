package io.github.honhimw.jsonql.hibernate6;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.honhimw.jsonql.common.FakePreparedStatement;
import io.github.honhimw.jsonql.common.JsonUtils;
import io.github.honhimw.jsonql.hibernate6.supports.MockSelection;
import io.github.honhimw.jsonql.hibernate6.supports.WildcardSelection;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Parameter;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.EntityType;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.initialization.qual.Initialized;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.UnknownKeyFor;
import org.hibernate.SharedSessionContract;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.model.NamedEntityGraphDefinition;
import org.hibernate.boot.model.relational.SqlStringGenerationContext;
import org.hibernate.boot.spi.*;
import org.hibernate.cache.internal.DisabledCaching;
import org.hibernate.cache.spi.CacheImplementor;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.pagination.LimitLimitHandler;
import org.hibernate.engine.OptimisticLockStyle;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.spi.SessionFactoryDelegatingImpl;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.Generator;
import org.hibernate.id.Assigned;
import org.hibernate.mapping.*;
import org.hibernate.metamodel.AttributeClassification;
import org.hibernate.metamodel.MappingMetamodel;
import org.hibernate.metamodel.internal.JpaMetaModelPopulationSetting;
import org.hibernate.metamodel.internal.JpaStaticMetaModelPopulationSetting;
import org.hibernate.metamodel.internal.MetadataContext;
import org.hibernate.metamodel.internal.RuntimeMetamodelsImpl;
import org.hibernate.metamodel.mapping.MappingModelExpressible;
import org.hibernate.metamodel.model.domain.EmbeddableDomainType;
import org.hibernate.metamodel.model.domain.EntityDomainType;
import org.hibernate.metamodel.model.domain.IdentifiableDomainType;
import org.hibernate.metamodel.model.domain.ManagedDomainType;
import org.hibernate.metamodel.model.domain.internal.AttributeContainer;
import org.hibernate.metamodel.model.domain.internal.*;
import org.hibernate.metamodel.spi.MappingMetamodelImplementor;
import org.hibernate.metamodel.spi.RuntimeMetamodelsImplementor;
import org.hibernate.metamodel.spi.RuntimeModelCreationContext;
import org.hibernate.query.Page;
import org.hibernate.query.criteria.JpaCriteriaInsertValues;
import org.hibernate.query.criteria.JpaFunction;
import org.hibernate.query.criteria.JpaRoot;
import org.hibernate.query.internal.ParameterMetadataImpl;
import org.hibernate.query.internal.QueryParameterBindingsImpl;
import org.hibernate.query.spi.Limit;
import org.hibernate.query.spi.QueryOptions;
import org.hibernate.query.spi.QueryParameterBindings;
import org.hibernate.query.spi.QueryParameterImplementor;
import org.hibernate.query.sqm.NodeBuilder;
import org.hibernate.query.sqm.function.SqmFunctionRegistry;
import org.hibernate.query.sqm.internal.DomainParameterXref;
import org.hibernate.query.sqm.internal.SqmSelectionQueryImpl;
import org.hibernate.query.sqm.internal.SqmUtil;
import org.hibernate.query.sqm.spi.SqmParameterMappingModelResolutionAccess;
import org.hibernate.query.sqm.sql.SqmTranslation;
import org.hibernate.query.sqm.sql.SqmTranslator;
import org.hibernate.query.sqm.sql.SqmTranslatorFactory;
import org.hibernate.query.sqm.sql.StandardSqmTranslatorFactory;
import org.hibernate.query.sqm.tree.SqmDmlStatement;
import org.hibernate.query.sqm.tree.delete.SqmDeleteStatement;
import org.hibernate.query.sqm.tree.domain.SqmPath;
import org.hibernate.query.sqm.tree.expression.JpaCriteriaParameter;
import org.hibernate.query.sqm.tree.expression.SqmJpaCriteriaParameterWrapper;
import org.hibernate.query.sqm.tree.expression.SqmParameter;
import org.hibernate.query.sqm.tree.from.SqmRoot;
import org.hibernate.query.sqm.tree.insert.SqmInsertValuesStatement;
import org.hibernate.query.sqm.tree.select.SqmSelectStatement;
import org.hibernate.query.sqm.tree.update.SqmUpdateStatement;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.sql.ast.SqlAstTranslator;
import org.hibernate.sql.ast.SqlAstTranslatorFactory;
import org.hibernate.sql.ast.spi.SqlAstCreationContext;
import org.hibernate.sql.ast.tree.MutationStatement;
import org.hibernate.sql.ast.tree.select.SelectStatement;
import org.hibernate.sql.exec.internal.BaseExecutionContext;
import org.hibernate.sql.exec.spi.*;
import org.hibernate.type.descriptor.java.EnumJavaType;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.descriptor.java.ObjectJavaType;
import org.hibernate.type.descriptor.java.spi.DynamicModelJavaType;
import org.hibernate.type.descriptor.java.spi.EntityJavaType;
import org.hibernate.type.descriptor.java.spi.UnknownBasicJavaType;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.hibernate.type.spi.TypeConfiguration;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

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

    private RuntimeModelCreationContext runtimeModelCreationContext;

    private MappingMetamodelImpl mappingMetamodel;

    private JpaMetamodelImpl jpaMetamodel;

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
        return new SelectBuilder(this, firstPageNumber);
    }

    public UpdateBuilder update() {
        return new UpdateBuilder(this, entityType);
    }

    public DeleteBuilder delete() {
        return new DeleteBuilder(this, entityType);
    }

    public InsertBuilder insert() {
        return new InsertBuilder(this, entityType);
    }

    private void init(MetadataExtractorIntegrator integrator) {
        this.integrator = integrator;
        Metadata metadata = integrator.getMetadata();
        JdbcEnvironment jdbcEnvironment = integrator.getJdbcEnvironment();
        MetadataBuildingContext metadataBuildingContext = integrator.getMetadataBuildingContext();
        SessionFactoryImplementor sessionFactory = integrator.getSessionFactory();
        dialect = jdbcEnvironment.getDialect();

        mappingMetamodel = new MappingMetamodelImpl(sessionFactory.getTypeConfiguration(), sessionFactory.getServiceRegistry());
        jpaMetamodel = new JpaMetamodelImpl(sessionFactory.getTypeConfiguration(), mappingMetamodel, sessionFactory.getServiceRegistry());
        runtimeModelCreationContext();
        this.entityType = initEntity(table, metadataBuildingContext, sessionFactory, mappingMetamodel);
        mappingMetamodel.finishInitialization(runtimeModelCreationContext());
        Set<EntityType<?>> entities = mappingMetamodel.getEntities();
        this.entityType = mappingMetamodel.entity(table.getName());
    }

    private final List<PersistentClass> persistentClasses = new ArrayList<>();

    private EntityTypeImpl<Object> initEntity(Table table, MetadataBuildingContext metadataBuildingContext, SessionFactoryImplementor sessionFactory, MappingMetamodelImpl mappingMetamodel) {
        RootClass rootClass = new RootClass(metadataBuildingContext);
        persistentClasses.add(rootClass);
        Table _table = new Table("dml-utils", table.getName());
        table.getColumns().forEach(_table::addColumn);
        TableHelper.of(_table).addColumn(columnBuilder -> columnBuilder.name(RandomStringUtils.randomAlphanumeric(10)).privateKey(true).type(String.class));
        rootClass.setJpaEntityName(_table.getName());
        rootClass.setEntityName(_table.getName());
        rootClass.setTable(_table);
        rootClass.setOptimisticLockStyle(OptimisticLockStyle.NONE);
        _table.getColumns().forEach(column -> {
            Property property = new Property();
            property.setName(column.getName());
            property.setValue(column.getValue());
            rootClass.addProperty(property);
        });

        PrimaryKey _pk = _table.getPrimaryKey();
        Column column = _pk.getColumn(0);
        if (column.getValue() instanceof KeyValue kv) {
            rootClass.setIdentifier(kv);
            Property property = new Property();
            property.setName(column.getName());
            property.setValue(kv);
            rootClass.setIdentifierProperty(property);
            rootClass.setDeclaredIdentifierProperty(property);
        }
        EntityTypeImpl<Object> entityType = new EntityTypeImpl<>(ObjectJavaType.INSTANCE, jpaMetamodel) {
            @Override
            public String getHibernateEntityName() {
                return table.getName();
            }

            @Override
            protected boolean isIdMappingRequired() {
                return false;
            }

        };
        AttributeContainer.InFlightAccess<Object> inFlightAccess = entityType.getInFlightAccess();

        Collection<ForeignKey> foreignKeys = table.getForeignKeys().values();
        List<EntityTypeImpl<Object>> joinEntityTypes = foreignKeys.stream()
            .map(foreignKey -> {
                Table joinTable = foreignKey.getReferencedTable();
                return initEntity(joinTable, metadataBuildingContext, sessionFactory, mappingMetamodel);
            })
            .toList();

        List<Column> foreignColumns = foreignKeys.stream()
            .map(Constraint::getColumns)
            .flatMap(Collection::stream)
            .toList();
        Collection<Column> rootColumns = table.getColumns();
        for (EntityTypeImpl<Object> joinEntityType : joinEntityTypes) {
            MetadataContext metadataContext = metadataContext(entityType);
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
                metadataContext(entityType)
            );
            inFlightAccess.addAttribute(attribute);
        }
        return entityType;
    }

    private MetadataContext metadataContext(EntityTypeImpl<?> entityType) {
        return new MetadataContext(
            integrator.getSessionFactory().getJpaMetamodel(),
            integrator.getSessionFactory().getMappingMetamodel(),
            ((MetadataImplementor) integrator.getMetadata()),
            JpaStaticMetaModelPopulationSetting.DISABLED,
            JpaMetaModelPopulationSetting.DISABLED,
            runtimeModelCreationContext()
        );
    }

    private RuntimeModelCreationContext runtimeModelCreationContext() {
        if (Objects.isNull(runtimeModelCreationContext)) {
            runtimeModelCreationContext = new RuntimeModelCreationContext() {
                @Override
                public BootstrapContext getBootstrapContext() {
                    return integrator.getBootstrapContext();
                }

                @Override
                public SessionFactoryImplementor getSessionFactory() {
                    // this is bad, we're not yet fully-initialized
                    return new SessionFactoryDelegatingImpl(integrator.getSessionFactory()) {
                        @Override
                        public @UnknownKeyFor @NonNull @Initialized Generator getGenerator(@UnknownKeyFor @NonNull @Initialized String rootEntityName) {
                            return new Assigned();
                        }

                        @Override
                        public @UnknownKeyFor @NonNull @Initialized RuntimeMetamodelsImplementor getRuntimeMetamodels() {
                            RuntimeMetamodelsImpl runtimeMetamodels = new RuntimeMetamodelsImpl();
                            runtimeMetamodels.setMappingMetamodel(mappingMetamodel);
                            runtimeMetamodels.setJpaMetamodel(jpaMetamodel);
                            return runtimeMetamodels;
                        }
                    };
                }

                @Override
                public MetadataImplementor getBootModel() {
                    return new AbstractDelegatingMetadata((MetadataImplementor) integrator.getMetadata()) {
                        @Override
                        public Collection<PersistentClass> getEntityBindings() {
                            return persistentClasses;
                        }

                        @Override
                        public PersistentClass getEntityBinding(String entityName) {
                            return getEntityBindings().stream()
                                .filter(persistentClass -> StringUtils.equals(persistentClass.getEntityName(), entityName))
                                .findFirst().orElse(null);
                        }
                    };
                }

                @Override
                public MappingMetamodelImplementor getDomainModel() {
                    return mappingMetamodel;
//                    return integrator.getSessionFactory().getMappingMetamodel();
                }

                @Override
                public CacheImplementor getCache() {
                    return new DisabledCaching(integrator.getSessionFactory());
                }

                @Override
                public Map<String, Object> getSettings() {
                    return Map.of(
//                        "hibernate.jpa.static_metamodel.population", "DISABLED",
//                        "hibernate.jpa.metamodel.population", "DISABLED"
                    );
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
        return runtimeModelCreationContext;
    }

    /**
     * Query
     */
    public static class SelectBuilder {

        private final DMLUtils _ref;

        private final CriteriaBuilder cb;

        private final EntityType<Object> entityType;

        private final int firstPageNumber;

        private Dialect dialect;

        private Limit limit = Limit.NONE;

        private Integer pageNumber;

        private Integer pageSize;

        private QueryConsumer queryConsumer;

        private SelectBuilder(DMLUtils dmlUtils, int firstPageNumber) {
            this._ref = dmlUtils;
            this.dialect = dmlUtils.dialect;
            this.entityType = dmlUtils.entityType;
            this.firstPageNumber = firstPageNumber;
            this.cb = dmlUtils.em.getCriteriaBuilder();
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

        public String hql() {
            return hql(tupleCriteriaQuery());
        }

        public SQLHolder jdbcQL() {
            return jdbcQL(tupleCriteriaQuery(), true);
        }

        public String sql() {
            return sql(tupleCriteriaQuery(), true);
        }

        public String countJpaQL() {
            return hql(countCriteriaQuery());
        }

        public SQLHolder countJdbcQL() {
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

        private String hql(CriteriaQuery<?> criteriaQuery) {
            StringBuilder hqlQuery = new StringBuilder();
            SharedSessionContractImplementor entityManager = (SharedSessionContractImplementor) _ref.em.getDelegate();
            SqmSelectStatement<?> selectStatement = (SqmSelectStatement<?>) criteriaQuery;
            selectStatement.appendHqlString(hqlQuery);
            Set<SqmParameter<?>> sqmParameters = selectStatement.getSqmParameters();
            for (SqmParameter<?> sqmParameter : sqmParameters) {
                String name = sqmParameter.getName();
                Integer position = sqmParameter.getPosition();
            }

            String hql = hqlQuery.toString();
            if (log.isDebugEnabled()) {
                log.debug("hql: {}", hql);
            }
            return hql;
        }

        private SQLHolder jdbcQL(CriteriaQuery<?> criteriaQuery, boolean doLimit) {
            SqlAstTranslatorFactory sqlAstTranslatorFactory = dialect.getSqlAstTranslatorFactory();
            SqmTranslatorFactory sqmTranslatorFactory = dialect.getSqmTranslatorFactory();

            SqmSelectStatement<?> sqmSelectStatement = (SqmSelectStatement<?>) criteriaQuery;
            SqmSelectionQueryImpl<?> sqmSelectionQuery = new SqmSelectionQueryImpl<>(sqmSelectStatement, null, _ref.em.unwrap(SharedSessionContractImplementor.class));
            if (Objects.nonNull(pageSize)) {
                sqmSelectionQuery.setPage(Page.page(pageSize, Math.max(pageNumber - firstPageNumber, 0)));
            }
            // Note: sqmStatement is different from sqmSelectStatement, it is a copy of sqmSelectStatement
            SqmSelectStatement<?> sqmStatement = sqmSelectionQuery.getSqmStatement();
            if (Objects.isNull(sqmTranslatorFactory)) {
                sqmTranslatorFactory = new StandardSqmTranslatorFactory();
            }
            SqmTranslator<SelectStatement> selectTranslator = sqmTranslatorFactory.createSelectTranslator(
                sqmStatement,
                sqmSelectionQuery.getQueryOptions(),
                sqmSelectionQuery.getDomainParameterXref(),
                sqmSelectionQuery.getQueryParameterBindings(),
                sqmSelectionQuery.getLoadQueryInfluencers(),
                new SqlAstCreationContext() {
                    @Override
                    public SessionFactoryImplementor getSessionFactory() {
                        return new SessionFactoryDelegatingImpl(_ref.runtimeModelCreationContext.getSessionFactory()) {
                            @Override
                            public @UnknownKeyFor @NonNull @Initialized RuntimeMetamodelsImplementor getRuntimeMetamodels() {
                                RuntimeMetamodelsImpl runtimeMetamodels = new RuntimeMetamodelsImpl();
                                runtimeMetamodels.setJpaMetamodel(getJpaMetamodel());
                                runtimeMetamodels.setMappingMetamodel(_ref.runtimeModelCreationContext.getDomainModel());
                                return runtimeMetamodels;
                            }
                        };
                    }

                    @Override
                    public MappingMetamodelImplementor getMappingMetamodel() {
                        return _ref.runtimeModelCreationContext.getDomainModel();
                    }

                    @Override
                    public ServiceRegistry getServiceRegistry() {
                        return _ref.runtimeModelCreationContext.getServiceRegistry();
                    }

                    @Override
                    public Integer getMaximumFetchDepth() {
                        return 0;
                    }
                },
                false
            );

            SqmTranslation<SelectStatement> sqmTranslation = selectTranslator.translate();
            SelectStatement sqlAst = sqmTranslation.getSqlAst();

            SqlAstTranslator<JdbcOperationQuerySelect> sqlAstTranslator = sqlAstTranslatorFactory
                .buildSelectTranslator(_ref.integrator.getSessionFactory(), sqlAst);

            Map<QueryParameterImplementor<?>, Map<SqmParameter<?>, List<JdbcParametersList>>> jdbcParamsXref = SqmUtil
                .generateJdbcParamsXref(sqmSelectionQuery.getDomainParameterXref(), sqmTranslation::getJdbcParamsBySqmParam);

            JdbcParameterBindings jdbcParameterBindings = SqmUtil.createJdbcParameterBindings(
                sqmSelectionQuery.getQueryParameterBindings(),
                sqmSelectionQuery.getDomainParameterXref(),
                jdbcParamsXref,
                _ref.integrator.getSessionFactory().getRuntimeMetamodels().getMappingMetamodel(),
                sqmTranslation.getFromClauseAccess()::findTableGroup,
                new SqmParameterMappingModelResolutionAccess() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public <T> MappingModelExpressible<T> getResolvedMappingModelType(SqmParameter<T> parameter) {
                        return (MappingModelExpressible<T>) sqmTranslation.getSqmParameterMappingModelTypeResolutions().get(parameter);
                    }
                },
                _ref.em.unwrap(SharedSessionContractImplementor.class)
            );

            JdbcOperationQuerySelect translate = sqlAstTranslator.translate(jdbcParameterBindings, sqmSelectionQuery.getQueryOptions());
            List<JdbcParameterBinder> parameterBinders = translate.getParameterBinders();
            FakePreparedStatement fakePreparedStatement = new FakePreparedStatement();
            for (int i = 0; i < parameterBinders.size(); i++) {
                JdbcParameterBinder parameterBinder = parameterBinders.get(i);
                try {
                    parameterBinder.bindParameterValue(fakePreparedStatement, i + 1, jdbcParameterBindings, new BaseExecutionContext(_ref.em.unwrap(SharedSessionContractImplementor.class)) {
                        @Override
                        public QueryOptions getQueryOptions() {
                            return sqmSelectionQuery.getQueryOptions();
                        }

                        @Override
                        public QueryParameterBindings getQueryParameterBindings() {
                            return sqmSelectionQuery.getQueryParameterBindings();
                        }
                    });
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            List<Object> parameters = fakePreparedStatement.getParams();
            String jdbcQL = translate.getSqlString();
            fakePreparedStatement.close();
            return new SQLHolder(jdbcQL, parameters);
        }

        private String sql(CriteriaQuery<?> criteriaQuery, boolean limit) {
            SQLHolder sqlHolder = jdbcQL(criteriaQuery, limit);
            String jdbcQL = sqlHolder.sql();
            List<Object> parameters = sqlHolder.parameters();
            String nativeSQL = format(jdbcQL, parameters);
            if (log.isDebugEnabled()) {
                log.debug("final SQL: {}", nativeSQL);
            }

            return nativeSQL;
        }

        public List<SelectionResult> getResult() {
            CriteriaQuery<Tuple> tupleCriteriaQuery = tupleCriteriaQuery();
            Selection<?> selection = tupleCriteriaQuery.getSelection();
            if (selection.isCompoundSelection()) {
                List<Selection<?>> compoundSelectionItems = selection.getCompoundSelectionItems();
                int size = compoundSelectionItems.size();
                SQLHolder sqlHolder = jdbcQL(tupleCriteriaQuery, false);
                String sql = sqlHolder.sql();
                List<Object> parameters = sqlHolder.parameters();
                Query nativeQuery = _ref.em.createNativeQuery(sql);
                for (int i = 1; i <= parameters.size(); i++) {
                    nativeQuery.setParameter(i, parameters.get(i - 1));
                }

                if (Objects.nonNull(limit)) {
                    if (LimitLimitHandler.hasFirstRow(limit)) {
                        nativeQuery.setFirstResult(limit.getFirstRow());
                    }
                    if (LimitLimitHandler.hasMaxRows(limit)) {
                        nativeQuery.setMaxResults(limit.getMaxRows());
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
            CriteriaQuery<Tuple> tupleCriteriaQuery = tupleCriteriaQuery();
            Selection<?> selection = tupleCriteriaQuery.getSelection();
            AtomicReference<List<List<SelectionResult>>> ref = new AtomicReference<>();
            if (selection.isCompoundSelection()) {
                List<Selection<?>> compoundSelectionItems = selection.getCompoundSelectionItems();
                SQLHolder sqlHolder = jdbcQL(tupleCriteriaQuery, false);
                String sql = sqlHolder.sql();
                if (Objects.nonNull(limit)) {
                    dialect.getLimitHandler().processSql(sql, limit);
                }
                List<Object> parameters = sqlHolder.parameters();
                SharedSessionContract sessionContract = _ref.em.unwrap(SharedSessionContract.class);

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
            Query nativeQuery = _ref.em.createNativeQuery(sql);
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

    }

    public static class UpdateBuilder {

        private final DMLUtils _ref;

        private final CriteriaBuilder cb;

        private final EntityType<Object> entityType;

        private Dialect dialect;

        private UpdateConsumer updateConsumer;

        private UpdateBuilder(DMLUtils dmlUtils, EntityType<Object> entityType) {
            this._ref = dmlUtils;
            this.dialect = _ref.dialect;
            this.entityType = entityType;
            this.cb = _ref.getCriteriaBuilder();
        }

        public UpdateBuilder dialect(Dialect dialect) {
            this.dialect = dialect;
            return this;
        }

        public UpdateBuilder applyUpdate(UpdateConsumer consumer) {
            this.updateConsumer = consumer;
            return this;
        }

        public SQLHolder jdbcQL() {
            CriteriaUpdate<Object> criteria = cb.createCriteriaUpdate(Object.class);
            Root<Object> from = criteria.from(entityType);
            updateConsumer.accept(from, criteria, cb);
            SqmUpdateStatement<?> sqmUpdateStatement = (SqmUpdateStatement<?>) criteria;
            SqmTranslatorFactory sqmTranslatorFactory = dialect.getSqmTranslatorFactory();
            if (Objects.isNull(sqmTranslatorFactory)) {
                sqmTranslatorFactory = new StandardSqmTranslatorFactory();
            }
            try {
                SQLHolder sqlHolder = mutationStatement(sqmUpdateStatement, _ref.integrator, sqmTranslatorFactory, dialect.getSqlAstTranslatorFactory());
                if (log.isDebugEnabled()) {
                    log.debug("jdbc QL: {}", sqlHolder.sql());
                }
                return sqlHolder;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        public String sql() {
            SQLHolder sqlHolder = jdbcQL();
            String sql = format(sqlHolder.sql(), sqlHolder.parameters());
            if (log.isDebugEnabled()) {
                log.debug("final SQL: {}", sql);
            }
            return sql;
        }

        public int execute() {
            SQLHolder sqlHolder = jdbcQL();
            String sql = sqlHolder.sql();
            List<Object> parameters = sqlHolder.parameters();
            Query nativeQuery = _ref.em.createNativeQuery(sql);
            for (int i = 1; i <= parameters.size(); i++) {
                nativeQuery.setParameter(i, parameters.get(i - 1));
            }
            return nativeQuery.executeUpdate();
        }

    }

    public static class InsertBuilder {

        private final DMLUtils _ref;

        private final CriteriaBuilder cb;

        private final EntityType<Object> entityType;

        private Dialect dialect;

        private InsertConsumer insertConsumer;

        private boolean generatedValue = false;

        private InsertBuilder(DMLUtils dmlUtils, EntityType<Object> entityType) {
            this._ref = dmlUtils;
            this.dialect = _ref.dialect;
            this.entityType = entityType;
            this.cb = _ref.getCriteriaBuilder();
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
            this.insertConsumer = consumer;
            return this;
        }

        public SQLHolder jdbcQL() {
            NodeBuilder nb = (NodeBuilder) cb;
            SqmInsertValuesStatement<Object> criteria = nb.createCriteriaInsertValues(Object.class);
            SqmRoot<Object> root = new SqmRoot<>(((EntityDomainType<Object>) entityType), null, false, nb);
            criteria.setTarget(root);
            insertConsumer.accept(root, criteria, nb);
            SqmTranslatorFactory sqmTranslatorFactory = dialect.getSqmTranslatorFactory();
            if (Objects.isNull(sqmTranslatorFactory)) {
                sqmTranslatorFactory = new StandardSqmTranslatorFactory();
            }
            try {
                SQLHolder sqlHolder = mutationStatement(criteria, _ref.integrator, sqmTranslatorFactory, dialect.getSqlAstTranslatorFactory());
                if (log.isDebugEnabled()) {
                    log.debug("jdbc QL: {}", sqlHolder.sql());
                }
                return sqlHolder;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        public String sql() {
            SQLHolder sqlHolder = jdbcQL();
            String sql = format(sqlHolder.sql(), sqlHolder.parameters());
            if (log.isDebugEnabled()) {
                log.debug("final SQL: {}", sql);
            }
            return sql;
        }

        public int execute() {
            SQLHolder sqlHolder = jdbcQL();
            String sql = sqlHolder.sql();
            List<Object> parameters = sqlHolder.parameters();
            SharedSessionContract sessionContract = _ref.em.unwrap(SharedSessionContract.class);
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

        private final DMLUtils _ref;

        private final CriteriaBuilder cb;

        private final EntityType<Object> entityType;

        private Dialect dialect;

        private DeleteConsumer deleteConsumer;

        private DeleteBuilder(DMLUtils dmlUtils, EntityType<Object> entityType) {
            this._ref = dmlUtils;
            this.dialect = _ref.dialect;
            this.entityType = entityType;
            this.cb = _ref.getCriteriaBuilder();
        }

        public DeleteBuilder dialect(Dialect dialect) {
            this.dialect = dialect;
            return this;
        }

        public DeleteBuilder applyDelete(DeleteConsumer consumer) {
            this.deleteConsumer = consumer;
            return this;
        }

        public SQLHolder jdbcQL() {
            CriteriaDelete<Object> criteria = cb.createCriteriaDelete(Object.class);
            Root<Object> from = criteria.from(entityType);
            deleteConsumer.accept(from, criteria, cb);
            SqmDeleteStatement<?> sqmDmlStatement = (SqmDeleteStatement<?>) criteria;
            SqmTranslatorFactory sqmTranslatorFactory = dialect.getSqmTranslatorFactory();
            if (Objects.isNull(sqmTranslatorFactory)) {
                sqmTranslatorFactory = new StandardSqmTranslatorFactory();
            }
            try {
                SQLHolder sqlHolder = mutationStatement(sqmDmlStatement, _ref.integrator, sqmTranslatorFactory, dialect.getSqlAstTranslatorFactory());
                if (log.isDebugEnabled()) {
                    log.debug("jdbc QL: {}", sqlHolder.sql());
                }
                return sqlHolder;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        public String sql() {
            SQLHolder sqlHolder = jdbcQL();
            String sql = format(sqlHolder.sql(), sqlHolder.parameters());
            if (log.isDebugEnabled()) {
                log.debug("final SQL: {}", sql);
            }
            return sql;
        }

        public int execute() {
            SQLHolder sqlHolder = jdbcQL();
            String sql = sqlHolder.sql();
            List<Object> parameters = sqlHolder.parameters();
            Query nativeQuery = _ref.em.createNativeQuery(sql);
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
        void accept(JpaRoot<Object> root, JpaCriteriaInsertValues<?> insert, NodeBuilder nb);

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
            } else if (selection instanceof SqmPath<?> sqmPath) {
                return sqmPath.getNavigablePath().getLocalName();
            } else if (selection instanceof JpaFunction<?> jpaFunction) {
                return jpaFunction.getFunctionName();
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

    private static SQLHolder mutationStatement(
        SqmDmlStatement<?> sqmDmlStatement,
        MetadataExtractorIntegrator integrator,
        SqmTranslatorFactory sqmTranslatorFactory,
        SqlAstTranslatorFactory sqlAstTranslatorFactory
    ) throws SQLException {
        QueryOptions queryOptions = QueryOptions.READ_WRITE;
        DomainParameterXref domainParameterXref = DomainParameterXref.from(sqmDmlStatement);
        ParameterMetadataImpl parameterMetadata = domainParameterXref.hasParameters()
            ? new ParameterMetadataImpl(domainParameterXref.getQueryParameters())
            : ParameterMetadataImpl.EMPTY;
        QueryParameterBindingsImpl parameterBindings = QueryParameterBindingsImpl.from(parameterMetadata, integrator.getSessionFactory());

        SharedSessionContractImplementor sharedSessionContractImplementor = integrator.getSessionFactory().unwrap(SharedSessionContractImplementor.class);

        for (SqmParameter<?> sqmParameter : domainParameterXref.getParameterResolutions().getSqmParameters()) {
            if (sqmParameter instanceof SqmJpaCriteriaParameterWrapper<?>) {
                bindCriteriaParameter((SqmJpaCriteriaParameterWrapper<?>) sqmParameter, parameterBindings);
            }
        }

        SqmTranslator<? extends MutationStatement> mutationTranslator = sqmTranslatorFactory.createMutationTranslator(
            sqmDmlStatement,
            queryOptions,
            domainParameterXref,
            parameterBindings,
            sharedSessionContractImplementor.getLoadQueryInfluencers(),
            integrator.getSessionFactory()
        );


        SqmTranslation<? extends MutationStatement> sqmTranslation = mutationTranslator.translate();
        MutationStatement sqlAst = sqmTranslation.getSqlAst();

        SqlAstTranslator<? extends JdbcOperationQueryMutation> sqlAstTranslator = sqlAstTranslatorFactory
            .buildMutationTranslator(integrator.getSessionFactory(), sqlAst);


        Map<QueryParameterImplementor<?>, Map<SqmParameter<?>, List<JdbcParametersList>>> jdbcParamsXref = SqmUtil
            .generateJdbcParamsXref(domainParameterXref, sqmTranslation::getJdbcParamsBySqmParam);

        JdbcParameterBindings jdbcParameterBindings = SqmUtil.createJdbcParameterBindings(
            parameterBindings,
            domainParameterXref,
            jdbcParamsXref,
            integrator.getSessionFactory().getRuntimeMetamodels().getMappingMetamodel(),
            sqmTranslation.getFromClauseAccess()::findTableGroup,
            new SqmParameterMappingModelResolutionAccess() {
                @Override
                @SuppressWarnings("unchecked")
                public <T> MappingModelExpressible<T> getResolvedMappingModelType(SqmParameter<T> parameter) {
                    return (MappingModelExpressible<T>) sqmTranslation.getSqmParameterMappingModelTypeResolutions().get(parameter);
                }
            },
            sharedSessionContractImplementor
        );

        JdbcOperationQueryMutation translate = sqlAstTranslator.translate(jdbcParameterBindings, queryOptions);
        List<JdbcParameterBinder> parameterBinders = translate.getParameterBinders();
        FakePreparedStatement preparedStatement = new FakePreparedStatement();
        for (int i = 0; i < parameterBinders.size(); i++) {
            JdbcParameterBinder parameterBinder = parameterBinders.get(i);
            parameterBinder.bindParameterValue(preparedStatement, i + 1, jdbcParameterBindings, new BaseExecutionContext(sharedSessionContractImplementor) {
                @Override
                public QueryOptions getQueryOptions() {
                    return queryOptions;
                }

                @Override
                public QueryParameterBindings getQueryParameterBindings() {
                    return parameterBindings;
                }
            });
        }
        List<Object> params = preparedStatement.getParams();
        return new SQLHolder(translate.getSqlString(), params);
    }

    private static <T> void bindCriteriaParameter(SqmJpaCriteriaParameterWrapper<T> sqmParameter, QueryParameterBindingsImpl parameterBindings) {
        final JpaCriteriaParameter<T> jpaCriteriaParameter = sqmParameter.getJpaCriteriaParameter();
        final T value = jpaCriteriaParameter.getValue();
        // We don't set a null value, unless the type is also null which
        // is the case when using HibernateCriteriaBuilder.value
        if (value != null || jpaCriteriaParameter.getNodeType() == null) {
            // Use the anticipated type for binding the value if possible
            parameterBindings
                .getBinding(jpaCriteriaParameter)
                .setBindValue(value, jpaCriteriaParameter.getAnticipatedType());
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
