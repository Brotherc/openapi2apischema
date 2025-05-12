package com.github.brotherc.openapi2apischema.enums;

import io.swagger.models.properties.*;

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
    BINARY("string", "string", "binary"),
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

    public static ParameterType getParameterType(Property property) {
        if (property instanceof ArrayProperty) {
            return ARRAY;
        } else if (property instanceof BinaryProperty) {
            return BINARY;
        } else if (property instanceof BooleanProperty || property instanceof BooleanValueProperty) {
            return BOOLEAN;
        } else if (property instanceof ByteArrayProperty) {
            return BYTE;
        } else if (property instanceof ComposedProperty || property instanceof MapProperty ||
                property instanceof ObjectProperty || property instanceof RefProperty) {
            return OBJECT;
        } else if (property instanceof DateProperty) {
            return DATE;
        } else if (property instanceof DateTimeProperty) {
            return DATETIME;
        } else if (property instanceof FloatProperty) {
            return FLOAT;
        } else if (property instanceof DoubleProperty) {
            return DOUBLE;
        } else if (property instanceof DecimalProperty) {
            return NUMBER;
        } else if (property instanceof PasswordProperty || property instanceof StringProperty ||
                property instanceof UUIDProperty) {
            return STRING;
        } else if (property instanceof FileProperty) {
            return FILE;
        } else if (property instanceof IntegerProperty) {
            return INTEGER;
        } else if (property instanceof LongProperty) {
            return LONG;
        } else {
            return null;
        }
    }

    public static boolean isStructDataType(ParameterType parameterType) {
        return structDataType.contains(parameterType);
    }

}
