package io.github.honhimw.jsonql.common;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.FastDateFormat;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author hon_him
 * @since 2024-06-28
 */

public class JsonUtils {

    /**
     * RFC-3339 date format pattern.
     */
    public static final String RFC_3339 = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    /**
     * RFC-3339 date format formatter.
     */
    public static final DateTimeFormatter RFC_3339_FORMATTER = new DateTimeFormatterBuilder()
        .appendPattern("yyyy-MM-dd")
        .appendLiteral('T')
        .appendPattern("HH:mm:ss")
        .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
        .appendLiteral('Z')
        .toFormatter();

    private static final ObjectMapper MAPPER = newDefault();

    private JsonUtils() {
    }

    public static ObjectMapper getObjectMapper() {
        return MAPPER;
    }

    public static ObjectMapper newDefault() {
        JsonMapper.Builder builder = defaultBuilder();
        return builder.build();
    }

    public static JsonMapper.Builder defaultBuilder() {
        JsonMapper.Builder builder = JsonMapper.builder();

        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(RFC_3339_FORMATTER));
        javaTimeModule.addSerializer(LocalDateTime.class,
            new LocalDateTimeSerializer(RFC_3339_FORMATTER));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(RFC_3339_FORMATTER));
        javaTimeModule.addDeserializer(LocalDateTime.class,
            new LocalDateTimeDeserializer(RFC_3339_FORMATTER));
        javaTimeModule.addSerializer(Date.class, new JsonSerializer<>() {
            @Override
            public void serialize(Date date, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
                throws IOException {
                String formattedDate = FastDateFormat.getInstance(RFC_3339).format(date);
                jsonGenerator.writeString(formattedDate);
            }
        });
        javaTimeModule.addDeserializer(Date.class, new JsonDeserializer<>() {
            @Override
            public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
                throws IOException {
                String date = jsonParser.getText();
                try {
                    return FastDateFormat.getInstance(RFC_3339).parse(date);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        SimpleModule longAsStringModule = new SimpleModule();
        longAsStringModule.addSerializer(Long.class, ToStringSerializer.instance);
        longAsStringModule.addSerializer(Long.TYPE, ToStringSerializer.instance);

        builder
            .addModules(javaTimeModule, longAsStringModule)
            .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        ;

        return builder;
    }

    public static String toJson(Object object) {

        try {
            return MAPPER.writeValueAsString(object);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String toPrettyJson(Object object) {

        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T fromJson(String jsonString, Class<T> clazz) {
        if (StringUtils.isEmpty(jsonString)) {
            return null;
        }
        try {
            return MAPPER.readValue(jsonString, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T fromJson(String jsonString, JavaType javaType) {
        if (StringUtils.isEmpty(jsonString)) {
            return null;
        }

        try {
            return MAPPER.readValue(jsonString, javaType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T fromJson(String jsonData, TypeReference<T> typeReference) {
        if (StringUtils.isBlank(jsonData)) {
            return null;
        }
        try {
            return MAPPER.readValue(jsonData, typeReference);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T fromJson(String jsonData, Type type) {
        if (StringUtils.isBlank(jsonData)) {
            return null;
        }
        try {
            return MAPPER.readValue(jsonData, MAPPER.getTypeFactory().constructType(type));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("rawtypes")
    public static JavaType buildMapType(Class<? extends Map> mapClass, Class<?> keyClass, Class<?> valueClass) {
        return MAPPER.getTypeFactory().constructMapType(mapClass, keyClass, valueClass);
    }

    public static Map<String, Object> fromJson2Map(String json) {
        JavaType javaType = buildMapType(LinkedHashMap.class, String.class, Object.class);
        return fromJson(json, javaType);
    }

    public static void update(String jsonString, Object object) {
        try {
            MAPPER.readerForUpdating(object).readValue(jsonString);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T readValue(Class<T> type, Object value) {
        try {
            return MAPPER.readerFor(type)
                .readValue(String.format("\"%s\"", value));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonNode readTree(String json) {
        try {
            return MAPPER.readTree(json);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static <T> T readValueAs(JsonNode jsonNode, TypeReference<T> type) {
        try {
            return jsonNode.traverse(MAPPER).readValueAs(type);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static <T> T readValueAs(JsonNode jsonNode, Class<T> type) {
        try {
            return jsonNode.traverse(MAPPER).readValueAs(type);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static boolean isMissingOrNull(JsonNode node) {
        return node.isMissingNode() || node.isNull();
    }

    public static JsonNode toNode(Object node) {
        return MAPPER.valueToTree(node);
    }
    
}

