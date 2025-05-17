package com.github.openapi2apischema.core.model;

import java.util.ArrayList;
import java.util.List;

public class ParameterObjectSchema extends ParameterSchema {

    private List<ParameterSchema> properties = new ArrayList<>();

    public List<ParameterSchema> getProperties() {
        return properties;
    }

    public void setProperties(List<ParameterSchema> properties) {
        this.properties = properties;
    }

    public static ParameterObjectSchema of(ParameterSchema parameterSchema) {
        ParameterObjectSchema parameterObjectSchema = new ParameterObjectSchema();
        parameterObjectSchema.setName(parameterSchema.getName());
        parameterObjectSchema.setIn(parameterSchema.getIn());
        parameterObjectSchema.setDescription(parameterSchema.getDescription());
        parameterObjectSchema.setRequired(parameterSchema.isRequired());
        parameterObjectSchema.setType(parameterSchema.getType());
        return parameterObjectSchema;
    }

}
