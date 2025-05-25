package com.github.openapi2apischema.core.enums;

import io.swagger.models.properties.ByteArrayProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.Property;
import io.swagger.v3.oas.models.media.ByteArraySchema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum ParameterType {

    INTEGER("integer", "integer", "int32", ""),
    LONG("long", "integer", "int64", ""),
    FLOAT("float", "number", "float", ""),
    DOUBLE("double", "number", "double", ""),
    NUMBER("decimal", "number", "", ""),
    STRING("string", "string", "", ""),
    BYTE("byte", "string", "byte", ""),
    BYTEARRAY("byte[]", "string", "byte", "ByteArray"),
    BINARY("file", "string", "binary", ""),
    BOOLEAN("boolean", "boolean", "", ""),
    DATE("date", "string", "date", ""),
    DATETIME("dateTime", "string", "date-time", ""),
    PASSWORD("string", "string", "password", ""),
    EMAIL("string", "string", "email", ""),
    UUID("string", "string", "uuid", ""),
    OBJECT("object", "object", "", ""),
    MAP("map", "object", "", "Map"),
    ARRAY("array", "array", "", ""),
    FILE("file", "file", "", ""),
    ;

    private static final Set<ParameterType> structDataType = new HashSet<>(
            Arrays.asList(OBJECT, ARRAY)
    );

    private final String displayName;
    private final String type;
    private final String format;
    private final String name;

    ParameterType(String displayName, String type, String format, String name) {
        this.displayName = displayName;
        this.type = type;
        this.format = format;
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getType() {
        return type;
    }

    public String getFormat() {
        return format;
    }

    public String getName() {
        return name;
    }

    public static ParameterType getParameterType(String type, String format) {
        if (format == null) {
            format = "";
        }
        for (ParameterType parameterType : values()) {
            if (parameterType.getType().equals(type) && parameterType.getFormat().equals(format)) {
                return parameterType;
            }
        }
        return null;
    }

    public static ParameterType getParameterType(String type, String format, Property property) {
        if (format == null) {
            format = "";
        }
        String name = "";
        if (property instanceof ByteArrayProperty) {
            name = "ByteArray";
        }
        if (property instanceof MapProperty) {
            name = "Map";
        }
        for (ParameterType parameterType : values()) {
            if (parameterType.getType().equals(type) && parameterType.getFormat().equals(format) && parameterType.getName().equals(name)) {
                return parameterType;
            }
        }
        return null;
    }

    public static ParameterType getParameterType(String type, String format, Schema schema) {
        if (format == null) {
            format = "";
        }
        String name = "";
        if (schema instanceof ByteArraySchema) {
            name = "ByteArray";
        }
        if (schema instanceof MapSchema) {
            name = "Map";
        }
        for (ParameterType parameterType : values()) {
            if (parameterType.getType().equals(type) && parameterType.getFormat().equals(format) && parameterType.getName().equals(name)) {
                return parameterType;
            }
        }
        return null;
    }

    public static boolean isStructDataType(ParameterType parameterType) {
        return structDataType.contains(parameterType);
    }

}
