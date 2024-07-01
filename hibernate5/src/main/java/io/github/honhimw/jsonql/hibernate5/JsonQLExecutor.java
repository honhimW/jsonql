package io.github.honhimw.jsonql.hibernate5;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.honhimw.jsonql.common.JsonQLException;
import io.github.honhimw.jsonql.common.JsonUtils;
import io.github.honhimw.jsonql.hibernate5.internal.JsonQLCompiler;
import io.github.honhimw.jsonql.hibernate5.meta.SQLHolder;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.slf4j.MDC;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hon_him
 * @since 2024-01-31
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

    public static Object execute(String json, JsonQLCompiler compiler, Connection connection) throws JsonProcessingException {
        JsonNode jsonNode = MAPPER.readTree(json);
        return execute(jsonNode, compiler, connection);
    }

    public static Object execute(JsonNode rootNode, JsonQLCompiler compiler, Connection connection) {
        String operation = rootNode.get("operation").asText();
        return switch (operation.toLowerCase()) {
            case "insert" -> executeDmlInsert(compiler.compile(rootNode).get(0), connection);
            case "delete", "update", "logic_delete" -> {
                int effectMaxRows = rootNode.at("/effectMaxRows").asInt(1);
                yield executeDmlUpdate(compiler.compile(rootNode).get(0), effectMaxRows, connection);
            }
            case "select" -> executeDmlQuery(compiler.compile(rootNode), connection);
            default -> throw new IllegalArgumentException("Invalid operation: " + operation);
        };
    }

    /**
     * 执行dml query操作
     *
     * @return 影响行数
     */
    public static Object executeDmlInsert(SQLHolder insert, Connection connection) {
        String sql = insert.sql();
        List<Object> parameters = insert.parameters();

        log.info("执行sql插入语句：{},参数：{}", sql, JsonUtils.toJson(parameters));
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
        } catch (SQLException e) {
            String traceId = MDC.get("traceId");
            log.warn("%s 服务异常 %s".formatted(traceId, e.getMessage()), e);
            throw new JsonQLException("服务异常，请联系管理员，异常编码：%s".formatted(traceId));
        }

    }

    public static Object executeDmlUpdate(SQLHolder update, int limit, Connection connection) {
        String sql = update.sql();
        List<Object> parameters = update.parameters();

        log.info("执行sql更新语句：{},参数：{}", sql, JsonUtils.toJson(parameters));
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < parameters.size(); i++) {
                statement.setObject(i + 1, parameters.get(i));
            }
            int count = statement.executeUpdate();
            if (limit > 0) {
                if (count <= limit) {
                    return count;
                } else {
                    throw new IllegalStateException("当前更新语句影响行数[%d]大于允许影响的最大行数[%d], 拒绝提交事务!".formatted(count, limit));
                }
            } else if (limit == -1) {
                return count;
            } else if (limit == 0) {
                connection.rollback();
                return 0;
            } else {
                throw new IllegalArgumentException("Value of `effectMaxRows` must between -1 and 2^31.");
            }
        } catch (SQLException e) {
            String traceId = MDC.get("traceId");
            log.warn("%s 服务异常 %s".formatted(traceId, e.getMessage()), e);
            throw new JsonQLException("服务异常，请联系管理员，异常编码：%s".formatted(traceId));
        }
    }

    public static Object executeDmlQuery(List<SQLHolder> sqlHolders, Connection connection) {
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
                log.info("执行sql查询语句：{}, 参数：{}", sql, JsonUtils.toJson(parameters));
                resultList.add(JDBCUtils.extractResult(statement.executeQuery()));
            } catch (SQLException e) {
                String traceId = MDC.get("traceId");
                log.warn("%s 服务异常 %s".formatted(traceId, e.getMessage()), e);
                throw new JsonQLException("服务异常，请联系管理员，异常编码：%s".formatted(traceId));
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
