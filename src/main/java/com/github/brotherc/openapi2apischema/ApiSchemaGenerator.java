package com.github.brotherc.openapi2apischema;

import com.github.brotherc.openapi2apischema.enums.OpenApiVersion;
import com.github.brotherc.openapi2apischema.enums.ParameterType;
import com.github.brotherc.openapi2apischema.model.*;
import io.swagger.models.*;
import io.swagger.models.auth.AuthorizationValue;
import io.swagger.models.parameters.AbstractSerializableParameter;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.RefParameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.parser.Swagger20Parser;
import io.swagger.parser.SwaggerParser;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ApiSchemaGenerator {

    private ApiSchemaGenerator() {
    }

    public static List<ApiSchema> generateBySwaggerUrl(
            OpenApiVersion openApiVersion, String swaggerUrl, List<AuthorizationValue> auths) throws IOException {
        if (OpenApiVersion.V1.equals(openApiVersion)) {
            return parse(new SwaggerParser().read(swaggerUrl));
        } else if (OpenApiVersion.V2.equals(openApiVersion)) {
            return parse20(new Swagger20Parser().read(swaggerUrl, auths));
        }
        return Collections.emptyList();
    }

    public static List<ApiSchema> generateBySwaggerJson(OpenApiVersion openApiVersion, String swaggerJson) throws IOException {
        if (OpenApiVersion.V1.equals(openApiVersion)) {
            return parse(new SwaggerParser().parse(swaggerJson));
        } else if (OpenApiVersion.V2.equals(openApiVersion)) {
            return parse20(new Swagger20Parser().parse(swaggerJson));
        }
        return Collections.emptyList();
    }

    private static List<ApiSchema> parse(Swagger swagger) {
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

                String code = String.join("_", path, httpMethod.name());
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
            apiSchema.setCode(code);
            apiSchema.setName(code.replace("_", "."));

            Operation operation = operationHolder.getOperation();
            apiSchema.setCnName(operation.getSummary());
            apiSchema.setDescription(operation.getDescription());
            apiSchema.setTags(operation.getTags());

            apiSchema.setParameters(parseParameters(operation.getParameters(), swagger));
            apiSchema.setResponses(parseResponses(operation.getResponses(), swagger));

            return apiSchema;
        }).collect(Collectors.toList());
    }

    private static List<ParameterSchema> parseParameters(List<Parameter> parameterList, Swagger swagger) {
        if (parameterList == null || parameterList.isEmpty()) {
            return Collections.emptyList();
        }

        return parameterList.stream().map(parameter -> transformParameter(parameter, swagger)).collect(Collectors.toList());
    }

    private static ParameterSchema transformParameter(Parameter parameter, Swagger swagger) {
        String type = "";
        String format = "";

        if (parameter instanceof AbstractSerializableParameter) {
            AbstractSerializableParameter serializableParameter = (AbstractSerializableParameter) parameter;
            type = serializableParameter.getType();
            format = serializableParameter.getFormat();
            if (ArrayProperty.TYPE.equals(type)) {
                ParameterArraySchema parameterArraySchema = new ParameterArraySchema();
                ParameterType parameterType = ParameterType.getParameterType(serializableParameter.getItems());
                parameterArraySchema.setName(parameter.getName());
                parameterArraySchema.setIn(parameter.getIn());
                parameterArraySchema.setDescription(parameter.getDescription());
                parameterArraySchema.setRequired(parameter.getRequired());
                parameterArraySchema.setType(ParameterType.ARRAY.getDisplayName());
                if (!ParameterType.isStructDataType(parameterType)) {
                    ParameterSchema parameterSchema = new ParameterSchema();
                    parameterSchema.setType(parameterType.getDisplayName());
                    parameterArraySchema.setItems(parameterSchema);
                } else {
                    // TODO
                    System.out.println("AbstractSerializableParameter数组中是结构体");
                }
                return parameterArraySchema;
            } else {
                ParameterSchema parameterSchema = new ParameterSchema();
                parameterSchema.setName(parameter.getName());
                parameterSchema.setIn(parameter.getIn());
                parameterSchema.setDescription(parameter.getDescription());
                parameterSchema.setRequired(parameter.getRequired());
                String typeName = Optional.ofNullable(ParameterType.getParameterType(type, format))
                        .map(ParameterType::getDisplayName)
                        .orElse("");
                parameterSchema.setType(typeName);
                return parameterSchema;
            }
        } else if (parameter instanceof RefParameter) {
            RefParameter refParameter = (RefParameter) parameter;
            type = ObjectProperty.TYPE;
            // TODO
            System.out.println("RefParameter");
        } else if (parameter instanceof BodyParameter) {
            BodyParameter bodyParameter = (BodyParameter) parameter;
            Model schema = bodyParameter.getSchema();
            Map<String, Model> definitions = swagger.getDefinitions();
            if (schema instanceof RefModel) {
                RefModel refModel = (RefModel) schema;
                Model model = definitions.get(refModel.getSimpleRef());
                ParameterObjectSchema parameterObjectSchema = new ParameterObjectSchema();
                parameterObjectSchema.setName(parameter.getName());
                parameterObjectSchema.setIn(parameter.getIn());
                parameterObjectSchema.setDescription(parameter.getDescription());
                parameterObjectSchema.setRequired(parameter.getRequired());
                parameterObjectSchema.setType(ParameterType.OBJECT.getDisplayName());
                if (model.getProperties() != null && !model.getProperties().isEmpty()) {
                    parameterObjectSchema.setProperties(
                            model.getProperties().entrySet().stream()
                                    .map(entry -> parseProperties(entry.getKey(), entry.getValue(), swagger))
                                    .collect(Collectors.toList())
                    );
                }
                return parameterObjectSchema;
            } else if (schema instanceof ArrayModel) {
                // TODO
                System.out.println("BodyParameter ArrayModel");
            } else if (schema instanceof ModelImpl) {
                // TODO
                System.out.println("BodyParameter ModelImpl");
            } else if (schema instanceof BooleanValueModel) {
                // TODO
                System.out.println("BodyParameter BooleanValueModel");
            } else if (schema instanceof ComposedModel) {
                // TODO
                System.out.println("BodyParameter ComposedModel");
            }
        }

        return new ParameterSchema();
    }

    private static ParameterSchema parseProperties(String name, Property property, Swagger swagger) {
        ParameterSchema parameterSchema = new ParameterSchema();
        ParameterType parameterType = ParameterType.getParameterType(property);
        parameterSchema.setName(name);
        parameterSchema.setType(parameterType.getDisplayName());
        parameterSchema.setRequired(property.getRequired());
        parameterSchema.setDescription(property.getDescription());
        if (ParameterType.isStructDataType(parameterType)) {
            if (property instanceof ArrayProperty) {
                ParameterArraySchema parameterArraySchema = ParameterArraySchema.of(parameterSchema);
                parameterArraySchema.setItems(parseProperties(null, ((ArrayProperty) property).getItems(), swagger));
                return parameterArraySchema;
            } else if (property instanceof RefProperty || property instanceof ObjectProperty) {
                Map<String, Property> properties;
                if (property instanceof RefProperty) {
                    Map<String, Model> definitions = swagger.getDefinitions();
                    Model model = definitions.get(((RefProperty) property).getSimpleRef());
                    properties = model.getProperties();
                } else {
                    properties = ((ObjectProperty) property).getProperties();
                }

                ParameterObjectSchema parameterObjectSchema = ParameterObjectSchema.of(parameterSchema);
                if (properties != null && !properties.isEmpty()) {
                    parameterObjectSchema.setProperties(
                            properties.entrySet().stream()
                                    .map(entry -> parseProperties(entry.getKey(), entry.getValue(), swagger))
                                    .collect(Collectors.toList())
                    );
                }
                return parameterObjectSchema;
            }
        }
        return parameterSchema;
    }

    private static ParameterSchema parseResponses(Map<String, Response> responses, Swagger swagger) {
        return null;
    }

    private static List<ApiSchema> parse20(Swagger read) {
        return null;
    }

}
