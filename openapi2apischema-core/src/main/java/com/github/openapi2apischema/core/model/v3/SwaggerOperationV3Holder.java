package com.github.openapi2apischema.core.model.v3;

import io.swagger.models.HttpMethod;
import io.swagger.v3.oas.models.Operation;

public class SwaggerOperationV3Holder {

    private String path;

    private HttpMethod httpMethod;

    private Operation operation;

    public SwaggerOperationV3Holder() {
    }

    public SwaggerOperationV3Holder(String path, HttpMethod httpMethod, Operation operation) {
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
