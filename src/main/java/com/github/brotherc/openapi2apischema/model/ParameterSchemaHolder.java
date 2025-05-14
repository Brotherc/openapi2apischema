package com.github.brotherc.openapi2apischema.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.List;

public class ParameterSchemaHolder {

    private ParameterSchema parameterSchema;
    private JsonNode displaySchema;

    private List<ParameterSchema> parameterSchemaList;
    private ArrayNode displaySchemaList;

    public ParameterSchemaHolder() {
    }

    public ParameterSchema getParameterSchema() {
        return parameterSchema;
    }

    public ParameterSchemaHolder(ParameterSchema parameterSchema, JsonNode displaySchema) {
        this.parameterSchema = parameterSchema;
        this.displaySchema = displaySchema;
    }

    public void setParameterSchema(ParameterSchema parameterSchema) {
        this.parameterSchema = parameterSchema;
    }

    public JsonNode getDisplaySchema() {
        return displaySchema;
    }

    public void setDisplaySchema(JsonNode displaySchema) {
        this.displaySchema = displaySchema;
    }

    public List<ParameterSchema> getParameterSchemaList() {
        return parameterSchemaList;
    }

    public void setParameterSchemaList(List<ParameterSchema> parameterSchemaList) {
        this.parameterSchemaList = parameterSchemaList;
    }

    public ArrayNode getDisplaySchemaList() {
        return displaySchemaList;
    }

    public void setDisplaySchemaList(ArrayNode displaySchemaList) {
        this.displaySchemaList = displaySchemaList;
    }

}
