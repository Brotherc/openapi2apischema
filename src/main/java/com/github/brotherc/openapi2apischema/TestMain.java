package com.github.brotherc.openapi2apischema;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.brotherc.openapi2apischema.enums.OpenApiVersion;
import com.github.brotherc.openapi2apischema.model.ApiSchema;

import java.io.IOException;
import java.util.List;

public class TestMain {

    public static void main(String[] args) throws IOException {
        List<ApiSchema> apiSchemas = ApiSchemaGenerator.generateBySwaggerUrl(OpenApiVersion.V1, "https://pre-mobile.utyun.com/auth-enterprise/v2/api-docs", null);
        System.out.println(new ObjectMapper().writeValueAsString(apiSchemas));
    }

}
