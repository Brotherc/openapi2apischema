package com.github.openapi2apischema.core.parse;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.openapi2apischema.core.ApiSchemaGenerator;
import com.github.openapi2apischema.core.constant.ApiSchemaConstant;
import com.github.openapi2apischema.core.enums.ParameterType;
import com.github.openapi2apischema.core.model.*;
import io.swagger.models.*;
import io.swagger.models.parameters.*;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.utils.PropertyModelConverter;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class ApiParser {

    private ApiParser() {
    }

    public static List<ApiSchema> parse(Swagger swagger) {
        Map<String, Path> paths = swagger.getPaths();

        if (paths == null || paths.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, SwaggerOperationHolder> operationMapping = new HashMap<>();

        for (Map.Entry<String, Path> entry : paths.entrySet()) {
            String path = entry.getKey();
            path = path.replace("/", "_");
            if (path.startsWith("_")) {
                path = path.substring(1);
            }

            for (Map.Entry<HttpMethod, Operation> operationEntry : entry.getValue().getOperationMap().entrySet()) {
                HttpMethod httpMethod = operationEntry.getKey();
                Operation operation = operationEntry.getValue();

                String code = String.join("_", path, httpMethod.name().toLowerCase());
                SwaggerOperationHolder operationHolder = new SwaggerOperationHolder(entry.getKey(), httpMethod, operation);
                operationMapping.put(code, operationHolder);
            }
        }

        return operationMapping.entrySet().stream().map(entry -> {
            ApiSchema apiSchema = new ApiSchema();
            apiSchema.setBasePath(swagger.getBasePath());

            SwaggerOperationHolder operationHolder = entry.getValue();
            apiSchema.setMethod(operationHolder.getHttpMethod().name());
            apiSchema.setPath(operationHolder.getPath());

            String code = entry.getKey();
            if (!StringUtils.isBlank(swagger.getBasePath())) {
                String basePath = swagger.getBasePath();
                basePath = basePath.replace("/", "_");
                if (basePath.startsWith("_")) {
                    basePath = basePath.substring(1);
                }
                code = String.join("_", basePath, code);
            }
            code = code.replace("{", "").replace("}", "");
            apiSchema.setCode(code);
            apiSchema.setName(code.replace("_", "."));

            Operation operation = operationHolder.getOperation();
            apiSchema.setCnName(operation.getSummary());
            apiSchema.setDescription(operation.getDescription());
            apiSchema.setTags(operation.getTags());
            apiSchema.setConsumes(operation.getConsumes());

            ParameterSchemaHolder parameterSchemaHolder = parseParameters(operation.getParameters(), swagger);
            apiSchema.setParameters(parameterSchemaHolder.getParameterSchemaList());
            apiSchema.setDisplayParameters(parameterSchemaHolder.getDisplaySchemaList());

            parameterSchemaHolder = parseResponses(operation.getResponses(), swagger);
            apiSchema.setResponses(parameterSchemaHolder.getParameterSchema());
            apiSchema.setDisplayResponses(parameterSchemaHolder.getDisplaySchema());

            return apiSchema;
        }).collect(Collectors.toList());
    }

    private static ParameterSchemaHolder parseParameters(List<Parameter> parameterList, Swagger swagger) {
        if (parameterList == null || parameterList.isEmpty()) {
            return new ParameterSchemaHolder();
        }

        List<ParameterSchema> parameterSchemaList = new ArrayList<>();
        ArrayNode displaySchemaList = ApiSchemaGenerator.objectMapper.createArrayNode();
        for (Parameter parameter : parameterList) {
            ParameterSchemaHolder parameterSchemaHolder = transformParameter(parameter, swagger);
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

    private static ParameterSchemaHolder transformParameter(Parameter parameter, Swagger swagger) {
        ParameterSchema parameterSchema = null;
        ObjectNode displaySchema = null;

        if (parameter instanceof AbstractSerializableParameter) {
            AbstractSerializableParameter serializableParameter = (AbstractSerializableParameter) parameter;
            String type = serializableParameter.getType();
            String format = serializableParameter.getFormat();
            if (ArrayProperty.TYPE.equals(type)) {
                ParameterArraySchema parameterArraySchema = new ParameterArraySchema();
                ParameterType parameterType = ParameterType.getParameterType(
                        serializableParameter.getItems().getType(), serializableParameter.getItems().getFormat());
                parameterArraySchema.setName(parameter.getName());
                parameterArraySchema.setIn(parameter.getIn());
                parameterArraySchema.setDescription(parameter.getDescription());
                parameterArraySchema.setRequired(parameter.getRequired());
                parameterArraySchema.setType(ParameterType.ARRAY.getDisplayName());
                parameterArraySchema.setExample(serializableParameter.getExample());

                displaySchema = parameterArraySchema.toJsonNode();

                if (!ParameterType.isStructDataType(parameterType)) {
                    ParameterSchema itemsSchema = new ParameterSchema();
                    String typeDisplayName = parameterType != null ? parameterType.getDisplayName() : "";
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
                parameterSchema.setExample(serializableParameter.getExample());
                String typeName = Optional.ofNullable(ParameterType.getParameterType(type, format))
                        .map(ParameterType::getDisplayName)
                        .orElse("");
                parameterSchema.setType(typeName);

                displaySchema = parameterSchema.toJsonNode();
            }
        } else if (parameter instanceof RefParameter) {
            RefParameter refParameter = (RefParameter) parameter;
            String type = ObjectProperty.TYPE;
            // TODO
            System.out.println("RefParameter");
        } else if (parameter instanceof BodyParameter) {
            BodyParameter bodyParameter = (BodyParameter) parameter;
            Model schema = bodyParameter.getSchema();
            if (schema instanceof RefModel || schema instanceof ArrayModel || schema instanceof ModelImpl) {
                Property property = new PropertyModelConverter().modelToProperty(schema);
                ParameterSchemaHolder parameterSchemaHolder = parseProperties(swagger, parameter.getName(), property, new HashMap<>());
                parameterSchema = parameterSchemaHolder.getParameterSchema();
                parameterSchema.setIn(parameter.getIn());
                parameterSchema.setDescription(parameter.getDescription());
                parameterSchema.setRequired(parameter.getRequired());
                displaySchema = (ObjectNode) parameterSchemaHolder.getDisplaySchema();
                displaySchema.put(ApiSchemaConstant.IN, parameter.getIn());
                displaySchema.put(ApiSchemaConstant.DESCRIPTION, parameter.getDescription());
                displaySchema.put(ApiSchemaConstant.REQUIRED, parameter.getRequired());
            } else if (schema instanceof BooleanValueModel) {
                // TODO
                System.out.println("BodyParameter BooleanValueModel");
            } else if (schema instanceof ComposedModel) {
                // TODO
                System.out.println("BodyParameter ComposedModel");
            }
        }

        ParameterSchemaHolder parameterSchemaHolder = new ParameterSchemaHolder();
        parameterSchemaHolder.setParameterSchema(parameterSchema);
        // 如果参数类型是header或cookie，不进行展示
        if (!(parameter instanceof HeaderParameter) && !(parameter instanceof CookieParameter)) {
            parameterSchemaHolder.setDisplaySchema(displaySchema);
        }
        return parameterSchemaHolder;
    }

    private static ParameterSchemaHolder parseProperties(
            Swagger swagger, String name, Property property, Map<String, ParameterSchema> parsedRefProperty) {
        ParameterSchema parameterSchema = new ParameterSchema();
        parameterSchema.setName(name);
        String typeName = Optional.ofNullable(ParameterType.getParameterType(property.getType(), property.getFormat(), property))
                .map(ParameterType::getDisplayName)
                .orElse("");
        parameterSchema.setType(typeName);
        parameterSchema.setRequired(property.getRequired());
        parameterSchema.setDescription(property.getDescription());
        parameterSchema.setExample(property.getExample());

        ObjectNode displaySchema = parameterSchema.toJsonNode();

        if (property instanceof ArrayProperty) {
            ParameterArraySchema parameterArraySchema = ParameterArraySchema.of(parameterSchema);
            ParameterSchemaHolder parameterSchemaHolder = parseProperties(swagger, null, ((ArrayProperty) property).getItems(), parsedRefProperty);
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

        } else if (property instanceof RefProperty || property instanceof ObjectProperty) {
            ParameterObjectSchema parameterObjectSchema = ParameterObjectSchema.of(parameterSchema);

            Map<String, Property> properties;
            String simpleRef = null;
            if (property instanceof RefProperty) {
                Map<String, Model> definitions = swagger.getDefinitions();
                Model model = definitions.get(((RefProperty) property).getSimpleRef());
                // 防止关联的引用名称中包含/，导致无法根据名称正确获取引用对象，所以当取不到引用对象时重新解析名称之后再获取一次
                if (model == null) {
                    model = definitions.get(((RefProperty) property).get$ref().replace("#/definitions/", ""));
                }

                properties = model.getProperties();

                simpleRef = ((RefProperty) property).getSimpleRef();
                if (parsedRefProperty.containsKey(simpleRef)) {
                    parameterObjectSchema.setType(ApiSchemaConstant.CIRCULAR_REF_TYPE);
                    parsedRefProperty.get(simpleRef).setType(ApiSchemaConstant.CIRCULAR_REF_TYPE);
                    ParameterSchemaHolder parameterSchemaHolder = new ParameterSchemaHolder();
                    parameterSchemaHolder.setParameterSchema(parameterObjectSchema);
                    parameterSchemaHolder.setDisplaySchema(parameterObjectSchema.toJsonNode());
                    return parameterSchemaHolder;
                } else {
                    Property p = new PropertyModelConverter().modelToProperty(model);
                    typeName = Optional.ofNullable(ParameterType.getParameterType(p.getType(), p.getFormat(), p))
                            .map(ParameterType::getDisplayName)
                            .orElse("");
                    parameterObjectSchema.setType(typeName);
                    parsedRefProperty.put(simpleRef, parameterObjectSchema);
                }
            } else {
                properties = ((ObjectProperty) property).getProperties();
            }

            if (properties != null && !properties.isEmpty()) {
                List<ParameterSchema> parameterSchemaList = new ArrayList<>();
                ArrayNode displaySchemaList = ApiSchemaGenerator.objectMapper.createArrayNode();
                for (Map.Entry<String, Property> entry : properties.entrySet()) {
                    ParameterSchemaHolder parameterSchemaHolder = parseProperties(swagger, entry.getKey(), entry.getValue(), parsedRefProperty);
                    parameterSchemaList.add(parameterSchemaHolder.getParameterSchema());
                    displaySchemaList.add(parameterSchemaHolder.getDisplaySchema());
                }
                parameterObjectSchema.setProperties(parameterSchemaList);
                displaySchema.set(ApiSchemaConstant.CHILDREN, displaySchemaList);
            }
            if (simpleRef != null && parsedRefProperty.containsKey(simpleRef)) {
                parsedRefProperty.remove(simpleRef);
                // 当出现循环依赖的对象结构时，将用于展示的类型重新设置为依赖的对象类型
                // 因为parameterObjectSchema在处理循环依赖过程中已经重新设置过了，所以displaySchema直接取parameterObjectSchema的类型即可
                displaySchema.put(ApiSchemaConstant.TYPE, parameterObjectSchema.getType());
            }
            parameterSchema = parameterObjectSchema;
        }

        ParameterSchemaHolder parameterSchemaHolder = new ParameterSchemaHolder();
        parameterSchemaHolder.setParameterSchema(parameterSchema);
        parameterSchemaHolder.setDisplaySchema(displaySchema);
        return parameterSchemaHolder;
    }

    private static ParameterSchemaHolder parseResponses(Map<String, Response> responses, Swagger swagger) {
        if (responses == null || responses.isEmpty()) {
            return new ParameterSchemaHolder();
        }

        Model responseSchema = Optional.ofNullable(responses.get("200")).map(Response::getResponseSchema).orElse(null);
        if (responseSchema == null) {
            return new ParameterSchemaHolder();
        }
        Property property = new PropertyModelConverter().modelToProperty(responseSchema);
        ParameterSchemaHolder parameterSchemaHolder = parseProperties(swagger, property.getName(), property, new HashMap<>());

        if (parameterSchemaHolder.getDisplaySchema() != null) {
            ArrayNode arrayNode = ApiSchemaGenerator.objectMapper.createArrayNode();
            arrayNode.add(parameterSchemaHolder.getDisplaySchema());
            parameterSchemaHolder.setDisplaySchema(arrayNode);
        }

        return parameterSchemaHolder;
    }

}
