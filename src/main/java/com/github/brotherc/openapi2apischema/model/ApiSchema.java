package com.github.brotherc.openapi2apischema.model;

import java.util.List;

public class ApiSchema {

    private String method;

    private String basePath;

    private String path;

    private String name;

    private String code;

    private String cnName;

    private String description;

    private List<String> tags;

    private List<ParameterSchema> parameters;

    private ParameterSchema responses;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCnName() {
        return cnName;
    }

    public void setCnName(String cnName) {
        this.cnName = cnName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<ParameterSchema> getParameters() {
        return parameters;
    }

    public void setParameters(List<ParameterSchema> parameters) {
        this.parameters = parameters;
    }

    public ParameterSchema getResponses() {
        return responses;
    }

    public void setResponses(ParameterSchema responses) {
        this.responses = responses;
    }

}
