package io.github.honhimw.jsonql.hibernate6;

import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.initialization.qual.Initialized;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.UnknownKeyFor;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.internal.BootstrapContextImpl;
import org.hibernate.boot.internal.InFlightMetadataCollectorImpl;
import org.hibernate.boot.internal.MetadataBuilderImpl;
import org.hibernate.boot.internal.MetadataBuildingContextRootImpl;
import org.hibernate.boot.model.relational.Database;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.spi.BootstrapContext;
import org.hibernate.boot.spi.InFlightMetadataCollector;
import org.hibernate.boot.spi.MetadataBuildingContext;
import org.hibernate.boot.spi.MetadataBuildingOptions;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

import java.util.Objects;

/**
 * @author hon_him
 * @since 2024-06-28
 */

@Getter
public class MetadataExtractorIntegrator implements org.hibernate.integrator.spi.Integrator {

    // using final if running on framework like spring
    public static MetadataExtractorIntegrator INSTANCE = new MetadataExtractorIntegrator();

    public MetadataExtractorIntegrator() {
    }

    public MetadataExtractorIntegrator(String dataSourceId) {
        this.dataSourceId = dataSourceId;
    }

    @Setter
    private String dataSourceId;

    private Database database;

    private Metadata metadata;

    private JdbcEnvironment jdbcEnvironment;

    private BootstrapContext bootstrapContext;

    private SessionFactoryImplementor sessionFactory;

    private InFlightMetadataCollector inFlightMetadataCollector;

    private MetadataBuildingContext metadataBuildingContext;

    @Override
    public void integrate(
        @UnknownKeyFor @NonNull @Initialized Metadata metadata,
        @UnknownKeyFor @NonNull @Initialized BootstrapContext bootstrapContext,
        @UnknownKeyFor @NonNull @Initialized SessionFactoryImplementor sessionFactory) {
        this.bootstrapContext = bootstrapContext;
        this.sessionFactory = sessionFactory;
        this.database = metadata.getDatabase();
        this.metadata = metadata;
        this.jdbcEnvironment = metadata.getDatabase().getJdbcEnvironment();
    }

    @Override
    public void disintegrate(
        SessionFactoryImplementor sessionFactory,
        SessionFactoryServiceRegistry serviceRegistry) {
    }

    public MetadataBuildingOptions getMetadataBuildingOptions() {
        return bootstrapContext.getMetadataBuildingOptions();
    }

    public InFlightMetadataCollector getInFlightMetadataCollector() {
        if (Objects.nonNull(inFlightMetadataCollector)) {
            return inFlightMetadataCollector;
        }
        inFlightMetadataCollector = new InFlightMetadataCollectorImpl(bootstrapContext, getMetadataBuildingOptions());
        return inFlightMetadataCollector;
    }

    public MetadataBuildingContext getMetadataBuildingContext() {
        if (Objects.nonNull(metadataBuildingContext)) {
            return metadataBuildingContext;
        }
        metadataBuildingContext = new MetadataBuildingContextRootImpl("", bootstrapContext, getMetadataBuildingOptions(), getInFlightMetadataCollector());
        return metadataBuildingContext;
    }

}
