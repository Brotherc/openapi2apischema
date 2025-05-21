package com.github.openapi2apischema.core.enums;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum ParameterType {

    INTEGER("integer", "integer", "int32"),
    LONG("long", "integer", "int64"),
    FLOAT("float", "number", "float"),
    DOUBLE("double", "number", "double"),
    NUMBER("decimal", "number", ""),
    STRING("string", "string", ""),
    BYTE("byte", "string", "byte"),
    BINARY("file", "string", "binary"),
    BOOLEAN("boolean", "boolean", ""),
    DATE("date", "string", "date"),
    DATETIME("dateTime", "string", "date-time"),
    PASSWORD("string", "string", "password"),
    EMAIL("string", "string", "email"),
    UUID("string", "string", "uuid"),
    OBJECT("object", "object", ""),
    ARRAY("array", "array", ""),
    FILE("file", "file", ""),
    ;

    private static final Set<ParameterType> structDataType = new HashSet<>(
            Arrays.asList(OBJECT, ARRAY)
    );

    private final String displayName;
    private final String type;
    private final String format;

    ParameterType(String displayName, String type, String format) {
        this.displayName = displayName;
        this.type = type;
        this.format = format;
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

    public static boolean isStructDataType(ParameterType parameterType) {
        return structDataType.contains(parameterType);
    }

}
