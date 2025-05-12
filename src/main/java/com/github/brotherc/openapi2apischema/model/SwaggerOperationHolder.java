package com.github.brotherc.openapi2apischema.model;

import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;

public class SwaggerOperationHolder {

    private String path;

    private HttpMethod httpMethod;

    private Operation operation;

    public SwaggerOperationHolder() {
    }

    public SwaggerOperationHolder(String path, HttpMethod httpMethod, Operation operation) {
        this.path = path;
        this.httpMethod = httpMethod;
        this.operation = operation;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

}
