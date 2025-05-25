package com.github.openapi2apischema.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.openapi2apischema.core.enums.OpenApiVersion;
import com.github.openapi2apischema.core.model.*;
import com.github.openapi2apischema.core.parse.ApiParser;
import com.github.openapi2apischema.core.parse.v3.ApiV3Parser;
import io.swagger.models.auth.AuthorizationValue;
import io.swagger.parser.Swagger20Parser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;

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
        } else if (OpenApiVersion.V3.equals(openApiVersion)) {
            OpenAPI openAPI = new OpenAPIV3Parser().read(swaggerUrl, null, null);
            return ApiV3Parser.parse(openAPI);
        }
        return Collections.emptyList();
    }

    public static List<ApiSchema> generateBySwaggerJson(OpenApiVersion openApiVersion, String swaggerJson) throws IOException {
        if (OpenApiVersion.V2.equals(openApiVersion)) {
            return ApiParser.parse(new Swagger20Parser().parse(swaggerJson));
        } else if (OpenApiVersion.V3.equals(openApiVersion)) {

        }
        return Collections.emptyList();
    }

}
