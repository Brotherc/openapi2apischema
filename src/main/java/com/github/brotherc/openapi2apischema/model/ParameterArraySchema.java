package com.github.brotherc.openapi2apischema.model;

public class ParameterArraySchema extends ParameterSchema {

    private ParameterSchema items;

    public ParameterSchema getItems() {
        return items;
    }

    public void setItems(ParameterSchema items) {
        this.items = items;
    }

    public static ParameterArraySchema of(ParameterSchema parameterSchema) {
        ParameterArraySchema parameterArraySchema = new ParameterArraySchema();
        parameterArraySchema.setName(parameterSchema.getName());
        parameterArraySchema.setIn(parameterSchema.getIn());
        parameterArraySchema.setDescription(parameterSchema.getDescription());
        parameterArraySchema.setRequired(parameterSchema.isRequired());
        parameterArraySchema.setType(parameterSchema.getType());
        return parameterArraySchema;
    }

}
