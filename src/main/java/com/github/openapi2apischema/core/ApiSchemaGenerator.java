package com.github.openapi2apischema.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.openapi2apischema.core.enums.OpenApiVersion;
import com.github.openapi2apischema.core.model.*;
import com.github.openapi2apischema.core.parse.ApiParser;
import io.swagger.models.auth.AuthorizationValue;
import io.swagger.parser.Swagger20Parser;

import java.io.IOException;
import java.util.*;

public class ApiSchemaGenerator {

    public static final ObjectMapper objectMapper = new ObjectMapper();

    private ApiSchemaGenerator() {
    }

    public static List<ApiSchema> generateBySwaggerUrl(
            OpenApiVersion openApiVersion, String swaggerUrl, List<AuthorizationValue> auths) throws IOException {
        if (OpenApiVersion.V2.equals(openApiVersion)) {
            return ApiParser.parse(new Swagger20Parser().read(swaggerUrl, auths));
        }
        return Collections.emptyList();
    }

    public static List<ApiSchema> generateBySwaggerJson(OpenApiVersion openApiVersion, String swaggerJson) throws IOException {
        if (OpenApiVersion.V2.equals(openApiVersion)) {
            return ApiParser.parse(new Swagger20Parser().parse(swaggerJson));
        }
        return Collections.emptyList();
    }

}
