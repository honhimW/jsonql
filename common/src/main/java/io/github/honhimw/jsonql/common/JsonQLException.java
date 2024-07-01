package io.github.honhimw.jsonql.common;

/**
 * @author hon_him
 * @since 2024-06-28
 */

public class JsonQLException extends RuntimeException{
    public JsonQLException() {
    }

    public JsonQLException(String message) {
        super(message);
    }

    public JsonQLException(String message, Throwable cause) {
        super(message, cause);
    }

    public JsonQLException(Throwable cause) {
        super(cause);
    }

    public JsonQLException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
