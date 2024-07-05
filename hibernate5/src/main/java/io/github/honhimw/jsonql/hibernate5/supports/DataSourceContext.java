package io.github.honhimw.jsonql.hibernate5.supports;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariDataSource;
import io.github.honhimw.jsonql.common.JsonUtils;
import io.github.honhimw.jsonql.hibernate5.MetadataExtractorIntegrator;
import io.github.honhimw.jsonql.hibernate5.MutablePersistenceUnitInfo;
import io.github.honhimw.jsonql.hibernate5.ddl.MetadataExtractor;
import lombok.Getter;
import org.hibernate.SharedSessionContract;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.hibernate.jpa.boot.spi.IntegratorProvider;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author hon_him
 * @since 2024-07-03
 */

@Getter
public class DataSourceContext implements AutoCloseable {

    private final String driverClassName;
    private final String url;
    private final String username;
    private final String password;
    private final Map<String, Object> driverProperties;

    private final HikariDataSource dataSource;

    private final EntityManager em;

    private final SharedSessionContract sessionContract;

    private final MetadataExtractorIntegrator integrator;

    private final MetadataExtractor metadataExtractor;

    private final ObjectMapper mapper;

    private DataSourceContext(Builder builder) {
        driverClassName = builder.driverClassName;
        url = builder.url;
        username = builder.username;
        password = builder.password;
        mapper = builder.mapper;
        driverProperties = Map.copyOf(builder.driverProperties);

        dataSource = new HikariDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setJdbcUrl(url);
        Optional.ofNullable(username).ifPresent(dataSource::setUsername);
        Optional.ofNullable(password).ifPresent(dataSource::setPassword);

        builder.driverProperties.forEach(dataSource::addDataSourceProperty);
        HibernatePersistenceProvider hibernatePersistenceProvider = new HibernatePersistenceProvider();
        MutablePersistenceUnitInfo info = new MutablePersistenceUnitInfo() {
            @Override
            public String getPersistenceUnitName() {
                return "none";
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
        metadataExtractor = new MetadataExtractor(integrator);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void close() throws Exception {
        em.close();
    }


    /**
     * {@code JsonQLContext} builder static inner class.
     */
    public static final class Builder {
        private String driverClassName;
        private String url;
        private String username;
        private String password;
        private ObjectMapper mapper = JsonUtils.getObjectMapper();
        private final Map<String, Object> driverProperties = new HashMap<>();

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
         * Sets the {@code mapper} and returns a reference to this Builder enabling method chaining.
         *
         * @param val the {@code mapper} to set
         * @return a reference to this Builder
         */
        public Builder mapper(ObjectMapper val) {
            mapper = val;
            return this;
        }

        /**
         * Sets the {@code driverProperties} and returns a reference to this Builder enabling method chaining.
         *
         * @param val the {@code driverProperties} to set
         * @return a reference to this Builder
         */
        public Builder driverProperties(Map<String, Object> val) {
            driverProperties.putAll(val);
            return this;
        }

        /**
         * Sets the {@code driverProperties} and returns a reference to this Builder enabling method chaining.
         *
         * @param val the {@code driverProperties} to set
         * @return a reference to this Builder
         */
        public Builder driverProperty(String key, Object val) {
            driverProperties.put(key, val);
            return this;
        }

        /**
         * Returns a {@code JsonQLContext} built from the parameters previously set.
         *
         * @return a {@code JsonQLContext} built with parameters of this {@code JsonQLContext.Builder}
         */
        public DataSourceContext build() {
            return new DataSourceContext(this);
        }
    }
}
