package io.github.honhimw.jsonql.hibernate5.supports;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.honhimw.jsonql.common.JsonUtils;
import io.github.honhimw.jsonql.hibernate5.JsonQLExecutor;
import io.github.honhimw.jsonql.hibernate5.internal.JsonQLCompiler;
import io.github.honhimw.jsonql.hibernate5.meta.MockTableMetaCache;
import io.github.honhimw.jsonql.hibernate5.meta.SQLHolder;
import io.github.honhimw.jsonql.hibernate5.meta.TableMetaCache;
import lombok.Getter;
import org.hibernate.mapping.Table;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hon_him
 * @since 2024-07-02
 */

@Getter
public class JsonQL implements AutoCloseable {

    @Override
    public void close() throws Exception {
        jsonQLContext.close();
    }

    private final JsonQLContext jsonQLContext;

    private final ObjectMapper mapper;

    private final TableMetaCache tableMetaCache;

    private final JsonQLCompiler compiler;

    private final JsonQLExecutor executor;

    private JsonQL(Builder builder) {
        jsonQLContext = JsonQLContext.builder()
            .driverClassName(builder.driverClassName)
            .url(builder.url)
            .username(builder.username)
            .password(builder.password)
            .driverProperties(builder.driverProperties)
            .mapper(JsonUtils.getObjectMapper())
            .build();

        if (builder.tableMetaCache != null) {
            tableMetaCache = builder.tableMetaCache;
        } else if (builder.tables != null) {
            Map<String, Table> tableMap = new HashMap<>();
            for (String tableName : builder.tables) {
                Table table = jsonQLContext.getMetadataExtractor().getTable(tableName);
                tableMap.put(tableName, table);
            }
            tableMetaCache = new MockTableMetaCache(tableMap);
        } else {
            tableMetaCache = new MockTableMetaCache(new HashMap<>());
        }

        compiler = new JsonQLCompiler(jsonQLContext.getEm(), tableMetaCache);
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
        private List<String> tables;
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

        public Builder tableMetaCache(TableMetaCache val) {
            tableMetaCache = val;
            return this;
        }

        public Builder tables(String... tables) {
            this.tables = Arrays.asList(tables);
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
         * Returns a {@code JsonQL} built from the parameters previously set.
         *
         * @return a {@code JsonQL} built with parameters of this {@code JsonQL.Builder}
         */
        public JsonQL build() {
            return new JsonQL(this);
        }
    }
}
