package com.github.openapi2apischema.core.model;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.openapi2apischema.core.ApiSchemaGenerator;

public class ParameterSchema {

    private String name;

    private String in;

    private String description;

    private Boolean required;

    private String type;

    private Object example;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIn() {
        return in;
    }

    public void setIn(String in) {
        this.in = in;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean isRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getExample() {
        return example;
    }

    public void setExample(Object example) {
        this.example = example;
    }

    public ObjectNode toJsonNode() {
        ObjectNode objectNode = ApiSchemaGenerator.objectMapper.createObjectNode();
        if (name != null) {
            objectNode.put("name", name);
        }
        if (in != null) {
            objectNode.put("in", in);
        }
        if (description != null) {
            objectNode.put("description", description);
        }
        if (required != null) {
            objectNode.put("required", required);
        }
        if (type != null) {
            objectNode.put("type", type);
        }
        if (example != null) {
            objectNode.putPOJO("example", example);
        }
        return objectNode;
    }

}
