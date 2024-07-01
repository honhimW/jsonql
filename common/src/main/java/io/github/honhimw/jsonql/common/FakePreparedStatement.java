package io.github.honhimw.jsonql.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author hon_him
 * @since 2024-04-08
 */
public class FakePreparedStatement implements PreparedStatement {

    private List<Object> params;

    public FakePreparedStatement() {
        this.params = initList(2);
    }

    public List<Object> getParams() {
        return params.subList(1, params.size());
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        throw new UnsupportedOperationException("not support");
    }

    @Override
    public int executeUpdate() throws SQLException {
        throw new UnsupportedOperationException("not support");
    }

    @Override
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        setParam(parameterIndex, null);
    }

    @Override
    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        setParam(parameterIndex, x);
    }

    @Override
    public void setByte(int parameterIndex, byte x) throws SQLException {
        setParam(parameterIndex, x);
    }

    @Override
    public void setShort(int parameterIndex, short x) throws SQLException {
        setParam(parameterIndex, x);
    }

    @Override
    public void setInt(int parameterIndex, int x) throws SQLException {
        setParam(parameterIndex, x);
    }

    @Override
    public void setLong(int parameterIndex, long x) throws SQLException {
        setParam(parameterIndex, x);
    }

    @Override
    public void setFloat(int parameterIndex, float x) throws SQLException {
        setParam(parameterIndex, x);
    }

    @Override
    public void setDouble(int parameterIndex, double x) throws SQLException {
        setParam(parameterIndex, x);
    }

    @Override
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        setParam(parameterIndex, x);
    }

    @Override
    public void setString(int parameterIndex, String x) throws SQLException {
        setParam(parameterIndex, x);
    }

    @Override
    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        setParam(parameterIndex, x);
    }

    @Override
    public void setDate(int parameterIndex, Date x) throws SQLException {
        setParam(parameterIndex, x);
    }

    @Override
    public void setTime(int parameterIndex, Time x) throws SQLException {
        setParam(parameterIndex, x);
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        setParam(parameterIndex, x);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        setParam(parameterIndex, x);
    }

    @Override
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        setParam(parameterIndex, x);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
        setParam(parameterIndex, x);
    }

    @Override
    public void clearParameters() throws SQLException {
        params.clear();
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        setParam(parameterIndex, x);
    }

    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException {
        setParam(parameterIndex, x);
    }

    @Override
    public boolean execute() throws SQLException {
        throw new UnsupportedOperationException("not support");
    }

    @Override
    public void addBatch() throws SQLException {
        throw new UnsupportedOperationException("not support");
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        char[] chars = new char[length];
        try {
            reader.read(chars);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        setParam(parameterIndex, chars);
    }

    @Override
    public void setRef(int parameterIndex, Ref x) throws SQLException {
        setParam(parameterIndex, x);
    }

    @Override
    public void setBlob(int parameterIndex, Blob x) throws SQLException {
        setParam(parameterIndex, x);
    }

    @Override
    public void setClob(int parameterIndex, Clob x) throws SQLException {
        setParam(parameterIndex, x);
    }

    @Override
    public void setArray(int parameterIndex, Array x) throws SQLException {
        setParam(parameterIndex, x);
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        throw new UnsupportedOperationException("not support");
    }

    @Override
    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
        setParam(parameterIndex, x);
    }

    @Override
    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        setParam(parameterIndex, x);
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        setParam(parameterIndex, x);
    }

    @Override
    public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
        setParam(parameterIndex, null);
    }

    @Override
    public void setURL(int parameterIndex, URL x) throws SQLException {
        setParam(parameterIndex, x);
    }

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        throw new UnsupportedOperationException("not support");
    }

    @Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        setParam(parameterIndex, x);
    }

    @Override
    public void setNString(int parameterIndex, String value) throws SQLException {
        setParam(parameterIndex, value);
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
        throw new UnsupportedOperationException("not support");
    }

    @Override
    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        setParam(parameterIndex, value);
    }

    @Override
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        throw new UnsupportedOperationException("not support");
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        throw new UnsupportedOperationException("not support");
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        throw new UnsupportedOperationException("not support");
    }

    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        setParam(parameterIndex, xmlObject);
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
        setParam(parameterIndex, x);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
        throw new UnsupportedOperationException("not support");
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
        throw new UnsupportedOperationException("not support");
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        throw new UnsupportedOperationException("not support");
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
        throw new UnsupportedOperationException("not support");
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
        setParam(parameterIndex, x);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        setParam(parameterIndex, reader);
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        setParam(parameterIndex, value);
    }

    @Override
    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        setParam(parameterIndex, reader);
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        setParam(parameterIndex, inputStream);
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        setParam(parameterIndex, reader);
    }

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        throw new UnsupportedOperationException("not support");
    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
        throw new UnsupportedOperationException("not support");
    }

    @Override
    public void close() {

    }

    @Override
    public int getMaxFieldSize() throws SQLException {
        return params.size();
    }

    @Override
    public void setMaxFieldSize(int max) throws SQLException {
    }

    @Override
    public int getMaxRows() throws SQLException {
        return -1;
    }

    @Override
    public void setMaxRows(int max) throws SQLException {

    }

    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {

    }

    @Override
    public int getQueryTimeout() throws SQLException {
        return 0;
    }

    @Override
    public void setQueryTimeout(int seconds) throws SQLException {

    }

    @Override
    public void cancel() throws SQLException {

    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return null;
    }

    @Override
    public void clearWarnings() throws SQLException {

    }

    @Override
    public void setCursorName(String name) throws SQLException {

    }

    @Override
    public boolean execute(String sql) throws SQLException {
        throw new UnsupportedOperationException("not support");
    }

    @Override
    public ResultSet getResultSet() throws SQLException {
        throw new UnsupportedOperationException("not support");
    }

    @Override
    public int getUpdateCount() throws SQLException {
        throw new UnsupportedOperationException("not support");
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        throw new UnsupportedOperationException("not support");
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {

    }

    @Override
    public int getFetchDirection() throws SQLException {
        return 0;
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {

    }

    @Override
    public int getFetchSize() throws SQLException {
        return 0;
    }

    @Override
    public int getResultSetConcurrency() throws SQLException {
        return 0;
    }

    @Override
    public int getResultSetType() throws SQLException {
        return 0;
    }

    @Override
    public void addBatch(String sql) throws SQLException {
        throw new UnsupportedOperationException("not support");
    }

    @Override
    public void clearBatch() throws SQLException {

    }

    @Override
    public int[] executeBatch() throws SQLException {
        throw new UnsupportedOperationException("not support");
    }

    @Override
    public Connection getConnection() throws SQLException {
        throw new UnsupportedOperationException("not support");
    }

    @Override
    public boolean getMoreResults(int current) throws SQLException {
        throw new UnsupportedOperationException("not support");
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        throw new UnsupportedOperationException("not support");
    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        throw new UnsupportedOperationException("not support");
    }

    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        throw new UnsupportedOperationException("not support");
    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        throw new UnsupportedOperationException("not support");
    }

    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        throw new UnsupportedOperationException("not support");
    }

    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        throw new UnsupportedOperationException("not support");
    }

    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException {
        throw new UnsupportedOperationException("not support");
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        throw new UnsupportedOperationException("not support");
    }

    @Override
    public boolean isClosed() throws SQLException {
        return false;
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {

    }

    @Override
    public boolean isPoolable() throws SQLException {
        return false;
    }

    @Override
    public void closeOnCompletion() throws SQLException {

    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new UnsupportedOperationException("not support");
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        throw new UnsupportedOperationException("not support");
    }

    private void setParam(int index, Object value) {
        if (index > params.size() - 1) {
            List<Object> tmp = initList(index + 1);
            for (int i = 0; i < params.size(); i++) {
                tmp.set(i, params.get(i));
            }
            params = tmp;
        }
        params.set(index, value);
    }

    private List<Object> initList(int size) {
        List<Object> tmp = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            tmp.add(i, null);
        }
        return tmp;
    }

}
