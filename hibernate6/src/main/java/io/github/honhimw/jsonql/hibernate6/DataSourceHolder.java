package io.github.honhimw.jsonql.hibernate6;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.honhimw.jsonql.common.DataSourceInfo;
import io.github.honhimw.jsonql.common.JDBCDriverType;
import io.github.honhimw.jsonql.hibernate6.internal.JsonQLCompiler;
import io.github.honhimw.jsonql.hibernate6.meta.TableMetaCache;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.Validate;
import org.hibernate.SharedSessionContract;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.hibernate.jpa.boot.spi.IntegratorProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.Nonnull;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.time.Duration;
import java.util.*;

/**
 * @author hon_him
 * @since 2024-02-02
 */
@Slf4j
@Getter
public class DataSourceHolder implements AutoCloseable {

    private static final Logger P6SPY_LOGGER = LoggerFactory.getLogger("driver.logging");

    private final HikariDataSource dataSource;

    private final EntityManager em;

    private final SharedSessionContract sessionContract;

    private final MetadataExtractorIntegrator integrator;

    private JsonQLCompiler compiler;

    public DataSourceHolder(HikariDataSource dataSource, EntityManager em, SharedSessionContract sessionContract, MetadataExtractorIntegrator integrator) {
        this.dataSource = dataSource;
        this.em = em;
        this.sessionContract = sessionContract;
        this.integrator = integrator;
    }

    public static DataSourceHolder getInstance(DataSourceInfo dto) {
        DataSourceHolder instance = getInstance(dto.getDriverType(), dto.getHost(), dto.getPort(), dto.getUsername(), dto.getPassword(), dto.getProperties());
        instance.getIntegrator().setDataSourceId(dto.getId());
        return instance;
    }

    public static DataSourceHolder getInstance(JDBCDriverType driverType, String host, int port, String username, String password, Map<String, Object> properties) {

        HikariDataSource dataSource = createDataSource(host, port, username, password, properties, driverType);

        HibernatePersistenceProvider hibernatePersistenceProvider = new HibernatePersistenceProvider();
        MutablePersistenceUnitInfo info = new MutablePersistenceUnitInfo() {
            @Override
            public String getPersistenceUnitName() {
                return "jdbc:%s://%s:%d".formatted(driverType.getProtocol(), host, port);
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
        MetadataExtractorIntegrator integrator = new MetadataExtractorIntegrator();
        hibernateProperties.put("hibernate.integrator_provider", (IntegratorProvider) () -> Collections.singletonList(integrator));
        hibernateProperties.put(AvailableSettings.SHOW_SQL, "true");
        EntityManagerFactory containerEntityManagerFactory = hibernatePersistenceProvider.createContainerEntityManagerFactory(info, hibernateProperties);
        EntityManager em = containerEntityManagerFactory.createEntityManager();
        SharedSessionContractImplementor sessionContract = em.unwrap(SharedSessionContractImplementor.class);

        Validate.validState(em.isOpen(), "EntityManager under an unavailable state.");

        return new DataSourceHolder(dataSource, em, sessionContract, integrator);
    }

    private static HikariDataSource createDataSource(String host, int port, String username, String password, Map<String, Object> properties, JDBCDriverType driverType) {
        String protocol = driverType.getProtocol();
        String urlFormatter = driverType.getUrlFormatter();
        String url = urlFormatter.formatted(protocol, host, port);
        String driver = driverType.getDriver();
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName(driver);
        hikariConfig.setJdbcUrl(url);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);
        Properties dataSourceProperties = new Properties();
        if (MapUtils.isNotEmpty(properties)) {
            dataSourceProperties.putAll(properties);
            hikariConfig.setDataSourceProperties(dataSourceProperties);
        }
        hikariConfig.setAutoCommit(true);
        hikariConfig.setAllowPoolSuspension(true);
        hikariConfig.setConnectionTimeout(Duration.ofSeconds(4).toMillis());
        hikariConfig.setMaximumPoolSize(16);
        hikariConfig.setMinimumIdle(2);
        hikariConfig.setIdleTimeout(Duration.ofSeconds(30).toMillis());
        hikariConfig.setMaxLifetime(Duration.ofSeconds(50).toMillis());
        try {
            Class.forName("com.p6spy.engine.spy.P6SpyDriver");
            hikariConfig.setDriverClassName("com.p6spy.engine.spy.P6SpyDriver");
            hikariConfig.setJdbcUrl(hikariConfig.getJdbcUrl().replaceFirst("jdbc", "jdbc:p6spy"));
        } catch (Exception e) {
            log.warn("P6SpyDriver is not found.");
        }
        if (driverType.validatable()) {
            hikariConfig.setConnectionTestQuery(driverType.getValidationQuery());
        }
        HikariDataSource hikariDataSource = new HikariDataSource(hikariConfig);
        configDataSource(hikariDataSource, driverType);
        return hikariDataSource;
    }

    private static void configDataSource(HikariDataSource hikari, JDBCDriverType driverType) {
    }

    public JsonQLCompiler getCompiler(TableMetaCache tableMetaCache) {
        if (Objects.isNull(compiler)) {
            compiler = new JsonQLCompiler(em, tableMetaCache, integrator);
        }
        return compiler;
    }

    @Override
    public void close() throws Exception {
        em.close();
        dataSource.close();
    }

}
