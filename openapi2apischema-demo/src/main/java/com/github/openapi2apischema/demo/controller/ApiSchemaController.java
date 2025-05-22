package com.github.openapi2apischema.demo.controller;

import com.github.openapi2apischema.core.ApiSchemaGenerator;
import com.github.openapi2apischema.core.enums.OpenApiVersion;
import com.github.openapi2apischema.core.model.ApiSchema;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/apiSchema")
public class ApiSchemaController {

    private Map<String, List<ApiSchema>> apiMapping;

    @PostConstruct
    public void init() throws IOException {
        // 替换为实际的swagger接口地址
        List<ApiSchema> apiSchemas = ApiSchemaGenerator.generateBySwaggerUrl(
                OpenApiVersion.V2, "https://xxx/v2/api-docs", null);
        if (!CollectionUtils.isEmpty(apiSchemas)) {
            apiMapping = apiSchemas.stream().collect(Collectors.groupingBy(o -> o.getTags().get(0)));
        } else {
            apiMapping = new HashMap<>();
        }
    }

    @GetMapping("/tags")
    public List<Map<String, Object>> getTags() {
        return apiMapping.keySet().stream().map(o -> {
            Map<String, Object> map = new HashMap<>();
            map.put("value", o);
            map.put("label", o);
            return map;
        }).collect(Collectors.toList());
    }

    @GetMapping("/apiListByTag")
    public List<Map<String, Object>> getApiListByTag(@RequestParam String tag) {
        return apiMapping.get(tag).stream().map(o -> {
            Map<String, Object> map = new HashMap<>();
            map.put("title", o.getName());
            map.put("subTitle", o.getCnName());
            map.put("code", o.getCode());
            return map;
        }).collect(Collectors.toList());
    }

    @GetMapping("/apiAchemaByCode")
    public ApiSchema getApiAchemaByCode(@RequestParam String tag, @RequestParam String code) {
        return Optional.ofNullable(apiMapping.get(tag))
                .map(list -> list.stream().filter(o -> o.getCode().equals(code)).findFirst())
                .map(Optional::get)
                .orElse(null);
    }

}
