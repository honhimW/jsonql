package io.github.honhimw.jsonql.hibernate6;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.honhimw.jsonql.common.JsonUtils;
import io.github.honhimw.jsonql.common.Nodes;
import io.github.honhimw.jsonql.hibernate6.internal.JsonQLCompiler;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.spi.SharedSessionContractImplementor;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hon_him
 * @since 2024-07-02
 */

@Slf4j
public class JsonQLExecutor {

    private static final ObjectMapper MAPPER = JsonUtils.getObjectMapper();

    private final JsonQLCompiler compiler;

    public JsonQLExecutor(JsonQLCompiler compiler) {
        this.compiler = compiler;
    }

    public Object executeDml(JsonNode rootNode) {
        return compiler.getEm().unwrap(SharedSessionContractImplementor.class).doReturningWork(connection -> execute(rootNode, compiler, connection));
    }

    public Object executeDml(String jsonData) throws JsonProcessingException {
        JsonNode rootNode = MAPPER.readTree(jsonData);
        return executeDml(rootNode);
    }

    public static Object execute(String json, JsonQLCompiler compiler, Connection connection) throws JsonProcessingException, SQLException {
        JsonNode jsonNode = MAPPER.readTree(json);
        return execute(jsonNode, compiler, connection);
    }

    public static Object execute(JsonNode rootNode, JsonQLCompiler compiler, Connection connection) throws SQLException {
        String operation = rootNode.at(Nodes.OPERATION.path()).asText();
        return switch (operation.toLowerCase()) {
            case "insert" -> executeDmlInsert(compiler.compile(rootNode).get(0), connection);
            case "delete", "update", "logic_delete" -> {
                int effectMaxRows = rootNode.at(Nodes.EFFECT_MAX_ROWS.path()).asInt(1);
                yield executeDmlUpdate(compiler.compile(rootNode).get(0), effectMaxRows, connection);
            }
            case "select" -> executeDmlQuery(compiler.compile(rootNode), connection);
            default -> throw new IllegalArgumentException("Invalid operation: " + operation);
        };
    }

    public static Object executeDmlInsert(SQLHolder insert, Connection connection) throws SQLException {
        if (log.isDebugEnabled()) {
            log.debug(insert.toString());
        }
        String sql = insert.sql();
        List<Object> parameters = insert.parameters();

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < parameters.size(); i++) {
                statement.setObject(i + 1, parameters.get(i));
            }
            int count = statement.executeUpdate();
            if (0 < count) {
                ResultSet rs = statement.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 1;
            }
            return 0;
        }

    }

    public static Object executeDmlUpdate(SQLHolder update, int limit, Connection connection) throws SQLException {
        if (log.isDebugEnabled()) {
            log.debug(update.toString());
        }
        String sql = update.sql();
        List<Object> parameters = update.parameters();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < parameters.size(); i++) {
                statement.setObject(i + 1, parameters.get(i));
            }
            int count = statement.executeUpdate();
            if (limit > 0) {
                if (count <= limit) {
                    return count;
                } else {
                    throw new IllegalStateException("effect rows[%d] greater than limit[%d], rollback!".formatted(count, limit));
                }
            } else if (limit == -1) {
                return count;
            } else if (limit == 0) {
                connection.rollback();
                return 0;
            } else {
                throw new IllegalArgumentException("Value of `effectMaxRows` must between -1 and 2^31.");
            }
        }
    }

    public static Object executeDmlQuery(List<SQLHolder> sqlHolders, Connection connection) throws SQLException {
        if (log.isDebugEnabled()) {
            for (SQLHolder sqlHolder : sqlHolders) {
                log.debug(sqlHolder.toString());
            }
        }
        boolean needPageSelect = sqlHolders.size() > 1;
        List<List<Map<String, Object>>> resultList = new ArrayList<>();
        for (SQLHolder sqlHolder : sqlHolders) {
            String sql = sqlHolder.sql();
            List<Object> parameters = sqlHolder.parameters();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                ParameterMetaData parameterMetaData = statement.getParameterMetaData();
                int parameterCount = parameterMetaData.getParameterCount();
                for (int i = 0; i < parameterCount; i++) {
                    statement.setObject(i + 1, parameters.get(i));
                }
                resultList.add(JDBCUtils.extractResult(statement.executeQuery()));
            }
        }

        List<Map<String, Object>> mapList = resultList.get(0);
        if (needPageSelect) {
            List<Map<String, Object>> pageList = resultList.get(1);
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("total", pageList.get(0).values().stream().findFirst().orElse(null));
            resultMap.put("list", mapList);
            return resultMap;
        }
        return mapList;

    }

}
