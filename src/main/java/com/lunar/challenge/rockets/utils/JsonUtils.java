package com.lunar.challenge.rockets.utils;

import com.fasterxml.jackson.databind.JsonNode;

public final class JsonUtils {
    private JsonUtils() {
    }

    public static String getText(JsonNode node, String field, String defaultValue) {
        if (node == null) return defaultValue;
        JsonNode value = node.get(field);
        return value != null && !value.isNull() ? value.asText() : defaultValue;
    }

    public static int getInt(JsonNode node, String field, int defaultValue) {
        if (node == null) return defaultValue;
        JsonNode value = node.get(field);
        return value != null && value.isInt() ? value.asInt() : defaultValue;
    }
}