package com.github.openapi2apischema.core.parse.v3;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.openapi2apischema.core.ApiSchemaGenerator;
import com.github.openapi2apischema.core.constant.ApiSchemaConstant;
import com.github.openapi2apischema.core.enums.ParameterType;
import com.github.openapi2apischema.core.model.*;
import com.github.openapi2apischema.core.model.v3.SwaggerOperationV3Holder;
import io.swagger.models.HttpMethod;
import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.CookieParameter;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.servers.Server;
import org.apache.commons.lang3.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class ApiV3Parser {

    private ApiV3Parser() {
    }

    public static List<ApiSchema> parse(OpenAPI openAPI) {
        Paths paths = openAPI.getPaths();

        if (paths == null || paths.isEmpty()) {
            return Collections.emptyList();
        }

        StringBuilder basePath = new StringBuilder();
        List<Server> servers = openAPI.getServers();
        if (servers != null && !servers.isEmpty()) {
            Server server = servers.get(0);
            try {
                basePath.append(new URL(server.getUrl()).getPath());
            } catch (MalformedURLException ignored) {
            }
        }

        Map<String, SwaggerOperationV3Holder> operationMapping = new HashMap<>();

        for (Map.Entry<String, PathItem> entry : paths.entrySet()) {
            PathItem pathItem = entry.getValue();
            HttpMethod httpMethod;
            Operation operation;
            if (pathItem.getPost() != null) {
                httpMethod = HttpMethod.POST;
                operation = pathItem.getPost();
            } else if (pathItem.getGet() != null) {
                httpMethod = HttpMethod.GET;
                operation = pathItem.getGet();
            } else if (pathItem.getPut() != null) {
                httpMethod = HttpMethod.PUT;
                operation = pathItem.getPut();
            } else if (pathItem.getDelete() != null) {
                httpMethod = HttpMethod.DELETE;
                operation = pathItem.getDelete();
            } else if (pathItem.getPatch() != null) {
                httpMethod = HttpMethod.PATCH;
                operation = pathItem.getPatch();
            } else if (pathItem.getHead() != null) {
                httpMethod = HttpMethod.HEAD;
                operation = pathItem.getHead();
            } else {
                continue;
            }

            String path = entry.getKey();
            path = path.replace("/", "_");
            if (path.startsWith("_")) {
                path = path.substring(1);
            }

            String code = String.join("_", path, httpMethod.name().toLowerCase());
            SwaggerOperationV3Holder operationHolder = new SwaggerOperationV3Holder(entry.getKey(), httpMethod, operation);
            operationMapping.put(code, operationHolder);
        }

        return operationMapping.entrySet().stream().map(entry -> {
            ApiSchema apiSchema = new ApiSchema();
            apiSchema.setBasePath(basePath.toString());

            SwaggerOperationV3Holder operationHolder = entry.getValue();
            apiSchema.setMethod(operationHolder.getHttpMethod().name());
            apiSchema.setPath(operationHolder.getPath());

            String code = entry.getKey();
            if (!StringUtils.isBlank(basePath.toString())) {
                String basePathTmp = basePath.toString();
                basePathTmp = basePathTmp.replace("/", "_");
                if (basePathTmp.startsWith("_")) {
                    basePathTmp = basePathTmp.substring(1);
                }
                code = String.join("_", basePathTmp, code);
            }
            code = code.replace("{", "").replace("}", "");
            apiSchema.setCode(code);
            apiSchema.setName(code.replace("_", "."));

            Operation operation = operationHolder.getOperation();
            apiSchema.setCnName(operation.getSummary());
            apiSchema.setDescription(operation.getDescription());
            apiSchema.setTags(operation.getTags());
            List<String> consumes = Optional.ofNullable(operation.getRequestBody())
                    .map(RequestBody::getContent)
                    .map(LinkedHashMap::keySet)
                    .map(ArrayList::new)
                    .orElse(null);
            apiSchema.setConsumes(consumes);

            ParameterSchemaHolder parameterSchemaHolder = parseParameters(operation, openAPI);
            apiSchema.setParameters(parameterSchemaHolder.getParameterSchemaList());
            apiSchema.setDisplayParameters(parameterSchemaHolder.getDisplaySchemaList());

            parameterSchemaHolder = parseResponses(operation.getResponses(), openAPI);
            apiSchema.setResponses(parameterSchemaHolder.getParameterSchema());
            apiSchema.setDisplayResponses(parameterSchemaHolder.getDisplaySchema());

            return apiSchema;
        }).collect(Collectors.toList());
    }

    private static ParameterSchemaHolder parseParameters(Operation operation, OpenAPI openAPI) {
        List<Parameter> parameters = operation.getParameters();
        RequestBody requestBody = operation.getRequestBody();

        if (parameters == null && requestBody == null) {
            return new ParameterSchemaHolder();
        }

        List<ParameterSchema> parameterSchemaList = new ArrayList<>();
        ArrayNode displaySchemaList = ApiSchemaGenerator.objectMapper.createArrayNode();
        if (parameters != null && !parameters.isEmpty()) {
            for (Parameter parameter : parameters) {
                ParameterSchemaHolder parameterSchemaHolder = transformParameter(parameter);
                if (parameterSchemaHolder.getParameterSchema() != null) {
                    parameterSchemaList.add(parameterSchemaHolder.getParameterSchema());
                }
                if (parameterSchemaHolder.getDisplaySchema() != null) {
                    displaySchemaList.add(parameterSchemaHolder.getDisplaySchema());
                }
            }
        }

        if (requestBody != null) {
            ParameterSchemaHolder parameterSchemaHolder = transformBody(requestBody, openAPI);
            if (parameterSchemaHolder.getParameterSchema() != null) {
                parameterSchemaList.add(parameterSchemaHolder.getParameterSchema());
            }
            if (parameterSchemaHolder.getDisplaySchema() != null) {
                displaySchemaList.add(parameterSchemaHolder.getDisplaySchema());
            }
        }
        ParameterSchemaHolder parameterSchemaHolder = new ParameterSchemaHolder();
        parameterSchemaHolder.setParameterSchemaList(parameterSchemaList);
        parameterSchemaHolder.setDisplaySchemaList(displaySchemaList);

        return parameterSchemaHolder;
    }

    private static ParameterSchemaHolder transformParameter(Parameter parameter) {
        ParameterSchema parameterSchema;
        ObjectNode displaySchema;

        Schema schema = parameter.getSchema();
        if (schema instanceof ArraySchema) {
            ParameterArraySchema parameterArraySchema = new ParameterArraySchema();
            ParameterType parameterType = ParameterType.getParameterType(
                    schema.getItems().getType(), schema.getItems().getFormat(), schema.getItems());
            parameterArraySchema.setName(parameter.getName());
            parameterArraySchema.setIn(parameter.getIn());
            parameterArraySchema.setDescription(parameter.getDescription());
            parameterArraySchema.setRequired(parameter.getRequired());
            parameterArraySchema.setType(ParameterType.ARRAY.getDisplayName());
            parameterArraySchema.setExample(parameter.getExample());

            displaySchema = parameterArraySchema.toJsonNode();

            if (!ParameterType.isStructDataType(parameterType)) {
                ParameterSchema itemsSchema = new ParameterSchema();
                String typeDisplayName = parameterType != null ? parameterType.getDisplayName() : null;
                itemsSchema.setType(typeDisplayName);
                parameterArraySchema.setItems(itemsSchema);
                displaySchema.put(ApiSchemaConstant.TYPE, parameterArraySchema.getType() + "[" + typeDisplayName + "]");
            } else {
                // TODO
                System.out.println("AbstractSerializableParameter数组中是结构体");
            }
            parameterSchema = parameterArraySchema;
        } else {
            parameterSchema = new ParameterSchema();
            parameterSchema.setName(parameter.getName());
            parameterSchema.setIn(parameter.getIn());
            parameterSchema.setDescription(parameter.getDescription());
            parameterSchema.setRequired(parameter.getRequired());
            parameterSchema.setExample(parameter.getExample());
            String typeName = Optional.ofNullable(ParameterType.getParameterType(schema.getType(), schema.getFormat(), schema))
                    .map(ParameterType::getDisplayName)
                    .orElse("");
            parameterSchema.setType(typeName);

            displaySchema = parameterSchema.toJsonNode();
        }

        ParameterSchemaHolder parameterSchemaHolder = new ParameterSchemaHolder();
        parameterSchemaHolder.setParameterSchema(parameterSchema);
        // 如果参数类型是header或cookie，不进行展示
        if (!(parameter instanceof HeaderParameter) && !(parameter instanceof CookieParameter)) {
            parameterSchemaHolder.setDisplaySchema(displaySchema);
        }
        parameterSchemaHolder.setDisplaySchema(displaySchema);
        return parameterSchemaHolder;
    }

    private static ParameterSchemaHolder transformBody(RequestBody requestBody, OpenAPI openAPI) {
        Optional<MediaType> optional = requestBody.getContent().values().stream().findFirst();

        if (optional.isPresent()) {
            Schema schema = optional.get().getSchema();

            String name;
            if (schema instanceof ObjectSchema || schema.get$ref() != null) {
                name = "obj";
            } else if (schema instanceof ArraySchema) {
                name = "arr";
            } else {
                name = "param";
            }

            ParameterSchemaHolder parameterSchemaHolder = parseSchema(openAPI, name, schema, requestBody.getRequired(), new HashMap<>());
            ParameterSchema parameterSchema = parameterSchemaHolder.getParameterSchema();
            parameterSchema.setIn("body");
            ObjectNode displaySchema = (ObjectNode) parameterSchemaHolder.getDisplaySchema();
            displaySchema.put(ApiSchemaConstant.IN, "body");

            return parameterSchemaHolder;
        }
        return new ParameterSchemaHolder();
    }

    private static ParameterSchemaHolder parseSchema(
            OpenAPI openAPI, String name, Schema schema, boolean required, Map<String, ParameterSchema> parsedObjectSchema) {
        ParameterSchema parameterSchema = new ParameterSchema();
        parameterSchema.setName(name);
        String typeName = Optional.ofNullable(ParameterType.getParameterType(schema.getType(), schema.getFormat(), schema))
                .map(ParameterType::getDisplayName)
                .orElse("");
        parameterSchema.setType(typeName);
        parameterSchema.setRequired(required);
        parameterSchema.setDescription(schema.getDescription());
        parameterSchema.setExample(schema.getExample());

        ObjectNode displaySchema = parameterSchema.toJsonNode();

        List<String> requiredList = Optional.ofNullable(schema.getRequired()).orElse(new ArrayList<>());
        if (schema instanceof ArraySchema) {
            ParameterArraySchema parameterArraySchema = ParameterArraySchema.of(parameterSchema);
            ParameterSchemaHolder parameterSchemaHolder = parseSchema(openAPI, null, schema.getItems(), false, parsedObjectSchema);
            parameterArraySchema.setItems(parameterSchemaHolder.getParameterSchema());
            parameterSchema = parameterArraySchema;

            String type = parameterSchemaHolder.getParameterSchema().getType();
            if (parameterSchemaHolder.getParameterSchema() instanceof ParameterArraySchema) {
                displaySchema.set(ApiSchemaConstant.CHILDREN, parameterSchemaHolder.getDisplaySchema());
            } else if (parameterSchemaHolder.getParameterSchema() instanceof ParameterObjectSchema) {
                displaySchema.put(ApiSchemaConstant.TYPE, parameterArraySchema.getType() + "[" + type + "]");
                displaySchema.set(ApiSchemaConstant.CHILDREN, parameterSchemaHolder.getDisplaySchema().get(ApiSchemaConstant.CHILDREN));
            } else {
                displaySchema.put(ApiSchemaConstant.TYPE, parameterArraySchema.getType() + "[" + type + "]");
            }

        } else if (schema.get$ref() != null || schema instanceof ObjectSchema) {
            ParameterObjectSchema parameterObjectSchema = ParameterObjectSchema.of(parameterSchema);

            Map<String, Schema> properties;
            String refFullName = schema.get$ref();
            if (refFullName != null) {
                Map<String, Schema> componentsSchemas = openAPI.getComponents().getSchemas();
                String[] refFullNameArr = refFullName.split("/");
                String refName = refFullNameArr[refFullNameArr.length - 1];
                Schema actualSchema = componentsSchemas.get(refName);
                properties = actualSchema.getProperties();

                if (parsedObjectSchema.containsKey(refFullName)) {
                    parameterObjectSchema.setType(ApiSchemaConstant.CIRCULAR_REF_TYPE);
                    parsedObjectSchema.get(refFullName).setType(ApiSchemaConstant.CIRCULAR_REF_TYPE);
                    ParameterSchemaHolder parameterSchemaHolder = new ParameterSchemaHolder();
                    parameterSchemaHolder.setParameterSchema(parameterObjectSchema);
                    parameterSchemaHolder.setDisplaySchema(parameterObjectSchema.toJsonNode());
                    return parameterSchemaHolder;
                } else {
                    typeName = Optional.ofNullable(ParameterType.getParameterType(actualSchema.getType(), actualSchema.getFormat(), schema))
                            .map(ParameterType::getDisplayName)
                            .orElse("");
                    parameterObjectSchema.setType(typeName);
                    parsedObjectSchema.put(refFullName, parameterObjectSchema);
                }
            } else {
                properties = schema.getProperties();
            }

            if (properties != null && !properties.isEmpty()) {
                List<ParameterSchema> parameterSchemaList = new ArrayList<>();
                ArrayNode displaySchemaList = ApiSchemaGenerator.objectMapper.createArrayNode();
                for (Map.Entry<String, Schema> entry : properties.entrySet()) {
                    ParameterSchemaHolder parameterSchemaHolder = parseSchema(
                            openAPI, entry.getKey(), entry.getValue(), requiredList.contains(entry.getKey()), parsedObjectSchema);
                    parameterSchemaList.add(parameterSchemaHolder.getParameterSchema());
                    displaySchemaList.add(parameterSchemaHolder.getDisplaySchema());
                }
                parameterObjectSchema.setProperties(parameterSchemaList);
                displaySchema.set(ApiSchemaConstant.CHILDREN, displaySchemaList);
            }
            if (refFullName != null && parsedObjectSchema.containsKey(refFullName)) {
                parsedObjectSchema.remove(refFullName);
                displaySchema.put(ApiSchemaConstant.TYPE, parameterObjectSchema.getType());
            }
            parameterSchema = parameterObjectSchema;
        }

        ParameterSchemaHolder parameterSchemaHolder = new ParameterSchemaHolder();
        parameterSchemaHolder.setParameterSchema(parameterSchema);
        parameterSchemaHolder.setDisplaySchema(displaySchema);
        return parameterSchemaHolder;
    }

    private static ParameterSchemaHolder parseResponses(ApiResponses responses, OpenAPI openAPI) {
        if (responses == null || responses.isEmpty()) {
            return new ParameterSchemaHolder();
        }

        Optional<MediaType> optional = Optional.ofNullable(responses.get("200"))
                .map(ApiResponse::getContent).map(LinkedHashMap::values).flatMap(o -> o.stream().findFirst());

        if (!optional.isPresent()) {
            return new ParameterSchemaHolder();
        }

        Schema schema = optional.get().getSchema();
        String name;
        if (schema instanceof ObjectSchema || schema.get$ref() != null) {
            name = "obj";
        } else if (schema instanceof ArraySchema) {
            name = "arr";
        } else {
            name = "param";
        }

        ParameterSchemaHolder parameterSchemaHolder = parseSchema(openAPI, name, schema, false, new HashMap<>());

        if (parameterSchemaHolder.getDisplaySchema() != null) {
            ArrayNode arrayNode = ApiSchemaGenerator.objectMapper.createArrayNode();
            arrayNode.add(parameterSchemaHolder.getDisplaySchema());
            parameterSchemaHolder.setDisplaySchema(arrayNode);
        }

        return parameterSchemaHolder;
    }

}
