package com.github.brotherc.openapi2apischema.model;

public class ParameterArraySchema extends ParameterSchema {

    private ParameterSchema items;

    public ParameterSchema getItems() {
        return items;
    }

    public void setItems(ParameterSchema items) {
        this.items = items;
    }

}
