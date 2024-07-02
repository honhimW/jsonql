package io.github.honhimw.jsonql.hibernate5.supports;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariDataSource;
import io.github.honhimw.jsonql.common.JsonUtils;
import io.github.honhimw.jsonql.hibernate5.DMLUtils;
import io.github.honhimw.jsonql.hibernate5.JsonQLExecutor;
import io.github.honhimw.jsonql.hibernate5.MetadataExtractorIntegrator;
import io.github.honhimw.jsonql.hibernate5.MutablePersistenceUnitInfo;
import io.github.honhimw.jsonql.hibernate5.internal.JsonQLCompiler;
import io.github.honhimw.jsonql.hibernate5.meta.SQLHolder;
import io.github.honhimw.jsonql.hibernate5.meta.TableMetaCache;
import lombok.Getter;
import org.hibernate.SharedSessionContract;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.hibernate.jpa.boot.spi.IntegratorProvider;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.*;

/**
 * @author hon_him
 * @since 2024-07-02
 */

@Getter
public class JsonQL implements AutoCloseable {

    @Override
    public void close() throws Exception {
        em.close();
    }

    private final String driverClassName;
    private final String url;
    private final String username;
    private final String password;

    private final HikariDataSource dataSource;

    private final EntityManager em;

    private final SharedSessionContract sessionContract;

    private final MetadataExtractorIntegrator integrator;

    private final TableMetaCache tableMetaCache;

    private final JsonQLCompiler compiler;

    private final JsonQLExecutor executor;

    private final ObjectMapper mapper;

    private JsonQL(Builder builder) {
        driverClassName = builder.driverClassName;
        url = builder.url;
        username = builder.username;
        password = builder.password;
        tableMetaCache = builder.tableMetaCache;

        dataSource = new HikariDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setJdbcUrl(url);
        Optional.ofNullable(username).ifPresent(dataSource::setUsername);
        Optional.ofNullable(password).ifPresent(dataSource::setPassword);

        dataSource.addDataSourceProperty("databaseName", "master");
        dataSource.addDataSourceProperty("encrypt", false);
        HibernatePersistenceProvider hibernatePersistenceProvider = new HibernatePersistenceProvider();
        MutablePersistenceUnitInfo info = new MutablePersistenceUnitInfo() {
            @Override
            public String getPersistenceUnitName() {
                return "mssql";
            }

            @Override
            public DataSource getNonJtaDataSource() {
                return dataSource;
            }

            @Nonnull
            @Override
            public ClassLoader getNewTempClassLoader() {
                return getClass().getClassLoader();
            }
        };
        Map<String, Object> hibernateProperties = new HashMap<>();
        integrator = new MetadataExtractorIntegrator();
        hibernateProperties.put("hibernate.integrator_provider", (IntegratorProvider) () -> Collections.singletonList(integrator));
        EntityManagerFactory containerEntityManagerFactory = hibernatePersistenceProvider.createContainerEntityManagerFactory(info, hibernateProperties);
        em = containerEntityManagerFactory.createEntityManager();
        sessionContract = em.unwrap(SharedSessionContractImplementor.class);
        MetadataExtractorIntegrator.INSTANCE = integrator;
        compiler = new JsonQLCompiler(em, tableMetaCache);
        executor = new JsonQLExecutor(compiler);
        mapper = JsonUtils.getObjectMapper();
    }

    public List<SQLHolder> compile(String jsonData) throws JsonProcessingException {
        return compiler.compile(mapper.readTree(jsonData));
    }

    public List<SQLHolder> compile(JsonNode rootNode) {
        return compiler.compile(rootNode);
    }

    public Object executeDml(String jsonData) throws JsonProcessingException {
        return executor.executeDml(jsonData);
    }

    public Object executeDml(JsonNode jsonNode) {
        return executor.executeDml(jsonNode);
    }

    public static Builder builder() {
        return new Builder();
    }


    /**
     * {@code JsonQL} builder static inner class.
     */
    public static final class Builder {
        private String driverClassName;
        private String url;
        private String username;
        private String password;
        private TableMetaCache tableMetaCache;

        private Builder() {
        }

        /**
         * Sets the {@code driverClassName} and returns a reference to this Builder enabling method chaining.
         *
         * @param val the {@code driverClassName} to set
         * @return a reference to this Builder
         */
        public Builder driverClassName(String val) {
            driverClassName = val;
            return this;
        }

        /**
         * Sets the {@code url} and returns a reference to this Builder enabling method chaining.
         *
         * @param val the {@code url} to set
         * @return a reference to this Builder
         */
        public Builder url(String val) {
            url = val;
            return this;
        }

        /**
         * Sets the {@code username} and returns a reference to this Builder enabling method chaining.
         *
         * @param val the {@code username} to set
         * @return a reference to this Builder
         */
        public Builder username(String val) {
            username = val;
            return this;
        }

        /**
         * Sets the {@code password} and returns a reference to this Builder enabling method chaining.
         *
         * @param val the {@code password} to set
         * @return a reference to this Builder
         */
        public Builder password(String val) {
            password = val;
            return this;
        }

        /**
         * Sets the {@code password} and returns a reference to this Builder enabling method chaining.
         *
         * @param val the {@code password} to set
         * @return a reference to this Builder
         */
        public Builder tableMetaCache(TableMetaCache val) {
            tableMetaCache = val;
            return this;
        }

        /**
         * Returns a {@code JsonQL} built from the parameters previously set.
         *
         * @return a {@code JsonQL} built with parameters of this {@code JsonQL.Builder}
         */
        public JsonQL build() {
            return new JsonQL(this);
        }
    }
}
