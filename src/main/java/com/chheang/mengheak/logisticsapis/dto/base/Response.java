package com.chheang.mengheak.logisticsapis.dto.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;

/**
 * Single envelope used by every endpoint, success or failure.
 */
@JsonPropertyOrder({"code", "message", "description", "data", "timestamp"})
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
public class Response {

    private final String code;
    private final String message;
    private final String description;
    private final Object data;
    private final Long timestamp;

    private Response(String code, String message, String description, Object data) {
        this.code = code;
        this.message = message;
        this.description = description;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    public static Response success(String message, String description) {
        return new Response("200", message, description, null);
    }

    public static Response success(String code, String message, String description) {
        return new Response(code, message, description, null);
    }

    public static Response success(String code, String message, String description, Object data) {
        return new Response(code, message, description, data);
    }

    public static Response created(String description, Object data) {
        return new Response("201", "success", description, data);
    }

    public static Response ok(String description, Object data) {
        return new Response("200", "success", description, data);
    }

    public static Response error(String code, String message, String description) {
        return new Response(code, message, description, null);
    }

    public static Response error(String code, String message, String description, Object data) {
        return new Response(code, message, description, data);
    }

    public static Response badRequest(String description) {
        return new Response("400", "fail", description, null);
    }
}
