package io.github.honhimw.jsonql.hibernate6;

import org.apache.commons.lang3.Validate;

import jakarta.annotation.Nullable;
import jakarta.persistence.SharedCacheMode;
import jakarta.persistence.ValidationMode;
import jakarta.persistence.spi.ClassTransformer;
import jakarta.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author hon_him
 * @since 2024-06-28
 */

@SuppressWarnings("all")
public class MutablePersistenceUnitInfo implements SmartPersistenceUnitInfo {
    @Nullable
    private String persistenceUnitName;

    @Nullable
    private String persistenceProviderClassName;

    @Nullable
    private PersistenceUnitTransactionType transactionType;

    @Nullable
    private DataSource nonJtaDataSource;

    @Nullable
    private DataSource jtaDataSource;

    private final List<String> mappingFileNames = new ArrayList<>();

    private final List<URL> jarFileUrls = new ArrayList<>();

    @Nullable
    private URL persistenceUnitRootUrl;

    private final List<String> managedClassNames = new ArrayList<>();

    private final List<String> managedPackages = new ArrayList<>();

    private boolean excludeUnlistedClasses = false;

    private SharedCacheMode sharedCacheMode = SharedCacheMode.UNSPECIFIED;

    private ValidationMode validationMode = ValidationMode.AUTO;

    private Properties properties = new Properties();

    private String persistenceXMLSchemaVersion = "2.0";

    @Nullable
    private String persistenceProviderPackageName;


    public void setPersistenceUnitName(@Nullable String persistenceUnitName) {
        this.persistenceUnitName = persistenceUnitName;
    }

    @Override
    @Nullable
    public String getPersistenceUnitName() {
        return this.persistenceUnitName;
    }

    public void setPersistenceProviderClassName(@Nullable String persistenceProviderClassName) {
        this.persistenceProviderClassName = persistenceProviderClassName;
    }

    @Override
    @Nullable
    public String getPersistenceProviderClassName() {
        return this.persistenceProviderClassName;
    }

    public void setTransactionType(PersistenceUnitTransactionType transactionType) {
        this.transactionType = transactionType;
    }

    @Override
    public PersistenceUnitTransactionType getTransactionType() {
        if (this.transactionType != null) {
            return this.transactionType;
        }
        else {
            return (this.jtaDataSource != null ?
                PersistenceUnitTransactionType.JTA : PersistenceUnitTransactionType.RESOURCE_LOCAL);
        }
    }

    public void setJtaDataSource(@Nullable DataSource jtaDataSource) {
        this.jtaDataSource = jtaDataSource;
    }

    @Override
    @Nullable
    public DataSource getJtaDataSource() {
        return this.jtaDataSource;
    }

    public void setNonJtaDataSource(@Nullable DataSource nonJtaDataSource) {
        this.nonJtaDataSource = nonJtaDataSource;
    }

    @Override
    @Nullable
    public DataSource getNonJtaDataSource() {
        return this.nonJtaDataSource;
    }

    public void addMappingFileName(String mappingFileName) {
        this.mappingFileNames.add(mappingFileName);
    }

    @Override
    public List<String> getMappingFileNames() {
        return this.mappingFileNames;
    }

    public void addJarFileUrl(URL jarFileUrl) {
        this.jarFileUrls.add(jarFileUrl);
    }

    @Override
    public List<URL> getJarFileUrls() {
        return this.jarFileUrls;
    }

    public void setPersistenceUnitRootUrl(@Nullable URL persistenceUnitRootUrl) {
        this.persistenceUnitRootUrl = persistenceUnitRootUrl;
    }

    @Override
    @Nullable
    public URL getPersistenceUnitRootUrl() {
        return this.persistenceUnitRootUrl;
    }

    @Override
    public List<String> getManagedClassNames() {
        return this.managedClassNames;
    }

    public void setExcludeUnlistedClasses(boolean excludeUnlistedClasses) {
        this.excludeUnlistedClasses = excludeUnlistedClasses;
    }

    @Override
    public boolean excludeUnlistedClasses() {
        return this.excludeUnlistedClasses;
    }

    public void setSharedCacheMode(SharedCacheMode sharedCacheMode) {
        this.sharedCacheMode = sharedCacheMode;
    }

    @Override
    public SharedCacheMode getSharedCacheMode() {
        return this.sharedCacheMode;
    }

    public void setValidationMode(ValidationMode validationMode) {
        this.validationMode = validationMode;
    }

    @Override
    public ValidationMode getValidationMode() {
        return this.validationMode;
    }

    public void addProperty(String name, String value) {
        this.properties.setProperty(name, value);
    }

    public void setProperties(Properties properties) {
        Validate.notNull(properties, "Properties must not be null");
        this.properties = properties;
    }

    @Override
    public Properties getProperties() {
        return this.properties;
    }

    public void setPersistenceXMLSchemaVersion(String persistenceXMLSchemaVersion) {
        this.persistenceXMLSchemaVersion = persistenceXMLSchemaVersion;
    }

    @Override
    public String getPersistenceXMLSchemaVersion() {
        return this.persistenceXMLSchemaVersion;
    }


    @Nullable
    public String getPersistenceProviderPackageName() {
        return this.persistenceProviderPackageName;
    }


    @Override
    @Nullable
    public ClassLoader getClassLoader() {
        return ClassUtils.getDefaultClassLoader();
    }

    /**
     * This implementation throws an UnsupportedOperationException.
     */
    @Override
    public void addTransformer(ClassTransformer classTransformer) {
//        throw new UnsupportedOperationException("addTransformer not supported");
    }

    /**
     * This implementation throws an UnsupportedOperationException.
     */
    @Override
    public ClassLoader getNewTempClassLoader() {
        throw new UnsupportedOperationException("getNewTempClassLoader not supported");
    }


    @Override
    public String toString() {
        return "PersistenceUnitInfo: name '" + this.persistenceUnitName +
               "', root URL [" + this.persistenceUnitRootUrl + "]";
    }
}
