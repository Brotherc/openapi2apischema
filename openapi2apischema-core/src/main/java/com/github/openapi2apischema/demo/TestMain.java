package com.github.openapi2apischema.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.openapi2apischema.core.ApiSchemaGenerator;
import com.github.openapi2apischema.core.enums.OpenApiVersion;
import com.github.openapi2apischema.core.model.ApiSchema;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class TestMain {

    // 替换为实际的swagger接口地址
    private static final List<String> swaggerUrl = Arrays.asList(
            "https://pre-mobile.utyun.com/auth-enterprise/v2/api-docs"
    );

    public static void main(String[] args) throws IOException {
        for (String url : swaggerUrl) {
            List<ApiSchema> apiSchemas = ApiSchemaGenerator.generateBySwaggerUrl(OpenApiVersion.V2, url, null);
            System.out.println(new ObjectMapper().writeValueAsString(apiSchemas));
        }
    }

}
