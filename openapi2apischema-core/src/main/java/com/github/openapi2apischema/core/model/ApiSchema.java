package com.github.openapi2apischema.core.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.List;

public class ApiSchema {

    private String method;

    private String basePath;

    private String path;

    private String name;

    private String code;

    private String cnName;

    private String description;

    private List<String> consumes;

    private List<String> tags;

    private List<ParameterSchema> parameters;

    private ArrayNode displayParameters;

    private ParameterSchema responses;

    private JsonNode displayResponses;

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

    public List<String> getConsumes() {
        return consumes;
    }

    public void setConsumes(List<String> consumes) {
        this.consumes = consumes;
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

    public ArrayNode getDisplayParameters() {
        return displayParameters;
    }

    public void setDisplayParameters(ArrayNode displayParameters) {
        this.displayParameters = displayParameters;
    }

    public ParameterSchema getResponses() {
        return responses;
    }

    public void setResponses(ParameterSchema responses) {
        this.responses = responses;
    }

    public JsonNode getDisplayResponses() {
        return displayResponses;
    }

    public void setDisplayResponses(JsonNode displayResponses) {
        this.displayResponses = displayResponses;
    }

}
