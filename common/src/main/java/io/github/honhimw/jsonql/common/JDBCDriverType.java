package io.github.honhimw.jsonql.common;

import lombok.Getter;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * @author hon_him
 * @since 2024-01-31
 */
@Getter
public enum JDBCDriverType {

    MYSQL("mysql", "com.mysql.cj.jdbc.Driver", "jdbc:%s://%s:%d/", "SELECT 1"),
    PG("postgresql", "org.postgresql.Driver", "jdbc:%s://%s:%d/", "SELECT 1"),
    ORACLE("oracle:thin", "oracle.jdbc.OracleDriver", "jdbc:%s://%s:%d/", "SELECT 'Hello' from DUAL"),
    H2("h2:tcp", "org.h2.Driver", "jdbc:%s://%s:%d/", "SELECT 1"),
    SQLITE("sqlite", "org.sqlite.JDBC", "jdbc:%s://%s:%d", null),
    SQL_SERVER("sqlserver", "com.microsoft.sqlserver.jdbc.SQLServerDriver", "jdbc:%s://%s:%d", "SELECT 1"),
    ;

    private final String protocol;
    private final String driver;
    private final String urlFormatter;
    private final String validationQuery;

    JDBCDriverType(String protocol, String driver, String urlFormatter, String validationQuery) {
        this.protocol = protocol;
        this.driver = driver;
        this.urlFormatter = urlFormatter;
        this.validationQuery = validationQuery;
    }

    public boolean validatable() {
        return validationQuery != null;
    }

    public boolean validation(Connection connection) {
        if (validatable()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(validationQuery)) {
                return preparedStatement.execute();
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

}
