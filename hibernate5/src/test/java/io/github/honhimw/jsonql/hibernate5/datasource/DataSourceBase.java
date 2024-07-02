package io.github.honhimw.jsonql.hibernate5.datasource;

import com.zaxxer.hikari.HikariDataSource;
import io.github.honhimw.jsonql.hibernate5.MetadataExtractorIntegrator;
import io.github.honhimw.jsonql.hibernate5.MutablePersistenceUnitInfo;
import io.github.honhimw.jsonql.hibernate5.TableBuilder;
import io.github.honhimw.jsonql.hibernate5.meta.MockTableMetaCache;
import org.hibernate.SharedSessionContract;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.hibernate.jpa.boot.spi.IntegratorProvider;
import org.hibernate.mapping.Table;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author hon_him
 * @since 2024-07-01
 */

public abstract class DataSourceBase extends TestsBase {

    protected static HikariDataSource dataSource;

    protected static EntityManager em;

    protected static SharedSessionContract sessionContract;

    protected static MetadataExtractorIntegrator integrator;

    protected static MockTableMetaCache mockTableMetaCache;

    public static void buildDatasource(
        @Nonnull String driverClassName,
        @Nonnull String url,
        @Nullable String username,
        @Nullable String password
    ) {
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

        Map<String, Table> tableMap = new HashMap<>();
        tableMap.put("brand_introduction", TableBuilder.builder("brand_introduction")
            .addColumn(columnBuilder -> columnBuilder
                .name("id")
                .privateKey(true)
                .type(String.class))
            .addColumn(columnBuilder -> columnBuilder
                .name("introduction")
                .type(String.class))
            .addColumn(columnBuilder -> columnBuilder
                .name("logo")
                .type(String.class))
            .addColumn(columnBuilder -> columnBuilder
                .name("title")
                .type(String.class))
            .build());
        tableMap.put("material_supplier", TableBuilder.builder("material_supplier")
            .addColumn(columnBuilder -> columnBuilder
                .name("id")
                .privateKey(true)
                .type(String.class))
            .addColumn(columnBuilder -> columnBuilder
                .name("contact")
                .type(String.class))
            .addColumn(columnBuilder -> columnBuilder
                .name("status")
                .type(String.class))
            .addColumn(columnBuilder -> columnBuilder
                .name("title")
                .type(String.class))
            .build());
        tableMap.put("material", TableBuilder.builder("material")
            .addColumn(columnBuilder -> columnBuilder
                .name("id")
                .privateKey(true)
                .type(String.class))
            .addColumn(columnBuilder -> columnBuilder
                .name("supplier")
                .type(String.class))
            .addColumn(columnBuilder -> columnBuilder
                .name("title")
                .type(String.class))
            .build());

        mockTableMetaCache = new MockTableMetaCache(tableMap);

        assert em.isOpen();
        em.getTransaction().begin();
    }

    protected static void destroyDatasource() {
        em.close();
    }

}
