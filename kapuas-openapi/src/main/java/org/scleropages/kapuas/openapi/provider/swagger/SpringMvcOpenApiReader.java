/**
 * Copyright 2001-2005 The Apache Software Foundation.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.scleropages.kapuas.openapi.provider.swagger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.PathParameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.scleropages.kapuas.openapi.OpenApi;
import org.scleropages.kapuas.openapi.annotation.ApiIgnore;
import org.scleropages.kapuas.openapi.provider.OpenApiReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.annotation.SynthesizingMethodParameter;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 基于spring mvc 的 {@link OpenApiReader}实现（解析并生成 {@link OpenAPI}定义）.
 * 设计策略上，暴露全部注解了 {@link org.springframework.stereotype.Controller} 作为 {@link PathItem}.（前后端分离的开发模式中几乎所有接口都对外暴露).
 *
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public class SpringMvcOpenApiReader implements OpenApiReader {

    private static final String ANNOTATION_METHOD_NAME_REQUEST_MAPPING_PATH = "path";

    private static final String ANNOTATION_METHOD_NAME_REQUEST_MAPPING_METHOD = "method";

    private static final String OPENAPI_RENDER_FORMAT_YAML = "yaml";

    private static final String OPENAPI_RENDER_FORMAT_JSON = "json";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final PrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
    private final PrettyPrinter nonePrettyPrinter = new MinimalPrettyPrinter();

    @Value("${openapi.title:[set your title via 'openapi.title']}")
    private String title;
    @Value("${openapi.desc:[set your description via 'openapi.desc']}")
    private String desc;
    @Value("${openapi.version:[set your version via 'openapi.version']}")
    private String version;
    @Value("${openapi.terms_of_service_url:[set your terms of service url via 'openapi.terms_of_service_url']}")
    private String termsOfServiceUrl;
    @Value("${openapi.contact_name:[set your contact name via 'openapi.contact_name']}")
    private String contactName;
    @Value("${openapi.contact_url:[set your contact url via 'openapi.contact_url']}")
    private String contactUrl;
    @Value("${openapi.contact_email:[set your contact email via 'openapi.contact_email']}")
    private String contactEmail;
    @Value("${openapi.license:[set your license via 'openapi.license']}")
    private String license;
    @Value("${openapi.license_url:[set your license url via 'openapi.license_url']}")
    private String licenseUrl;


    @Value("#{ @environment['openapi.render.format'] ?: 'yaml' }")
    private String openApiRenderFormat;

    @Value("#{ @environment['openapi.render.pretty'] ?: false }")
    private boolean openApiRenderPretty;

    @Value("#{ @environment['openapi.consume.media-type'] ?: 'application/json' }")
    private String defaultConsumeMediaType;

    private OpenAPI openAPI;

    private Map<Method, Operation> operations = Maps.newHashMap();

    private Map<String, Method> operationIdToMethod = Maps.newHashMap();

    @Override
    public OpenApi read(String basePackage, Set<Class<?>> classes) {
        this.openAPI = new OpenAPI();
        this.openAPI.setInfo(createApiInfo(basePackage));
        this.openAPI.setPaths(new Paths());
        classes.forEach(clazz -> {
            if (logger.isDebugEnabled())
                logger.debug("resolving class: {}", clazz.getSimpleName());
            if (!readableClass(clazz)) {
                logger.debug("ignore class: {} to resolve.", clazz.getSimpleName());
                return;
            }
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (!readableMethod(method)) {
                    logger.debug("ignore method: {} to resolve.", method.getName());
                    break;
                }
                resolveControllerMethod(clazz, method);
            }
        });
        resolveSchemas();
        return new OpenApi() {
            @Override
            public String openapi() {
                return openAPI.getOpenapi();
            }

            @Override
            public String id() {
                return basePackage;
            }

            @Override
            public Object nativeOpenApi() {
                return openAPI;
            }

            @Override
            public String render() {
                if (OPENAPI_RENDER_FORMAT_YAML.equalsIgnoreCase(openApiRenderFormat)) {
                    try {
                        return Yaml.mapper().writer(openApiRenderPretty ? prettyPrinter : nonePrettyPrinter).writeValueAsString(openAPI);
                    } catch (JsonProcessingException e) {
                        throw new IllegalStateException("failure to render open api for: " + id(), e);
                    }
                } else if (OPENAPI_RENDER_FORMAT_JSON.equalsIgnoreCase(openApiRenderFormat)) {
                    try {
                        return Json.mapper().writer(openApiRenderPretty ? prettyPrinter : nonePrettyPrinter).writeValueAsString(openAPI);
                    } catch (JsonProcessingException e) {
                        throw new IllegalStateException("failure to render open api for: " + id(), e);
                    }
                } else {
                    return openAPI.toString();
                }
            }
        };
    }

    protected void resolveSchemas() {
        List<Schema> allSchemas = SchemaUtil.getAllSchemas();
        Components components = new Components();
        allSchemas.forEach(schema -> components.addSchemas(schema.getName(), schema));
        openAPI.setComponents(components);
    }


    /**
     * 解析controller method 并将解析到的内容直接应用的 {@link OpenAPI} 上下文，对子类提供部分扩展点在特定OAI对象创建后的后置处理(postXXXXCreation).
     *
     * @param controllerClass
     * @param controllerMethod
     */
    protected void resolveControllerMethod(final Class controllerClass, final Method controllerMethod) {

        MergedAnnotation<RequestMapping> baseMapping = MergedAnnotations.from(controllerClass).get(RequestMapping.class);
        MergedAnnotation<RequestMapping> methodMapping = MergedAnnotations.from(controllerMethod).get(RequestMapping.class);

        ResolveContext resolveContext = new ResolveContext(baseMapping, methodMapping);

        //compute and merge request path(controller path * method path)
        String[] paths = computePath(resolveContext);

        for (String path : paths) {// foreach path create path item....
            openAPI.getPaths().computeIfAbsent(path, s -> {
                PathItem pathItem = new PathItem();
                try {
                    postPathItemCreation(pathItem);
                    Operation operation = createOperation(controllerMethod);
                    bindOperationToPathItem(pathItem, operation, methodMapping);
                    resolveControllerMethodArguments(controllerMethod, resolveContext);
                } catch (Exception e) {
                    throw new IllegalStateException("failure to build operation for path: " + pathItem, e);
                }
                return pathItem;
            });
        }
        if (logger.isDebugEnabled()) {
            logger.debug("successfully resolve {}.{} with paths:{}", controllerClass.getSimpleName(), controllerMethod.getName(), ArrayUtils.toString(paths));
        }
    }


    protected void resolveControllerMethodArguments(final Method controllerMethod, ResolveContext resolveContext) {
        MethodParameter[] methodParameters = getMethodParameters(controllerMethod);
        for (int i = 0; i < methodParameters.length; i++) {
            resolveControllerMethodParameter(controllerMethod, methodParameters[i], resolveContext, i);
        }
    }

    private MethodParameter[] getMethodParameters(final Method controllerMethod) {
        Method bridgedMethod = BridgeMethodResolver.findBridgedMethod(controllerMethod);
        List<MethodParameter> methodParameters = Lists.newArrayList();
        int count = bridgedMethod.getParameterCount();
        for (int i = 0; i < count; i++) {
            MethodParameter methodParameter = new SynthesizingMethodParameter(controllerMethod, i);
            if (null != methodParameter.getParameterAnnotation(PathVariable.class)) {
                methodParameters.add(methodParameter);
            } else if (null != methodParameter.getParameterAnnotation(RequestParam.class)) {
                methodParameters.add(methodParameter);
            } else if (null != methodParameter.getParameterAnnotation(RequestBody.class)) {
                methodParameters.add(methodParameter);
            }
        }
        methodParameters.add(new SynthesizingMethodParameter(controllerMethod, -1));//方法返回值
        return methodParameters.toArray(new MethodParameter[methodParameters.size()]);
    }

    protected void resolveControllerMethodParameter(final Method controllerMethod, final MethodParameter methodParameter, ResolveContext resolveContext, int index) {
        Operation operation = operations.get(controllerMethod);
        PathVariable pathVariable = methodParameter.getParameterAnnotation(PathVariable.class);
        if (null != pathVariable) {
            operation.addParametersItem(createPathParameter(pathVariable, methodParameter, resolveContext, index));
            return;
        }
        RequestParam requestParam = methodParameter.getParameterAnnotation(RequestParam.class);
        if (null != requestParam) {
            operation.addParametersItem(createQueryParameter(requestParam, methodParameter, resolveContext));
            return;
        }
        RequestBody requestBody = methodParameter.getParameterAnnotation(RequestBody.class);
        if (null != requestBody) {
            operation.setRequestBody(createRequestBody(requestBody, methodParameter, resolveContext));
            return;
        }
        if (methodParameter.getParameterIndex() == -1) {
            operation.setResponses(createApiResponses(methodParameter, resolveContext));
        }
    }

    protected ApiResponses createApiResponses(MethodParameter methodParameter, ResolveContext resolveContext) {
        ApiResponses apiResponses = new ApiResponses();
        ApiResponse apiResponse = new ApiResponse();
        Content content = new Content();
        apiResponse.setContent(content);
        if (!Objects.equals(methodParameter.getParameterType().getName(), "void")) {
            processContent(resolveContext.methodMapping.getStringArray("produces"), content, SchemaUtil.createSchema(methodParameter));
        }
        apiResponse.setDescription("successfully.");
        apiResponses.addApiResponse("200", apiResponse);
        return apiResponses;
    }

    protected io.swagger.v3.oas.models.parameters.RequestBody createRequestBody(RequestBody requestBody, MethodParameter methodParameter, ResolveContext resolveContext) {
        io.swagger.v3.oas.models.parameters.RequestBody swaggerBody = new io.swagger.v3.oas.models.parameters.RequestBody();
        swaggerBody.setRequired(requestBody.required());
        Content content = new Content();
        swaggerBody.setContent(content);
        Schema schema = SchemaUtil.createSchema(methodParameter);
        processContent(resolveContext.methodMapping.getStringArray("consumes"), content, schema);
        return swaggerBody;
    }

    protected void processContent(String[] mediaTypes, Content content, Schema schema) {
        if (ArrayUtils.isEmpty(mediaTypes)) {
            mediaTypes = new String[]{defaultConsumeMediaType};
        }
        for (String consume : mediaTypes) {
            MediaType mediaType = new MediaType();
            mediaType.setSchema(schema);
            content.addMediaType(consume, mediaType);
        }
    }

    protected Parameter createQueryParameter(RequestParam requestParam, MethodParameter methodParameter, ResolveContext resolveContext) {
        QueryParameter parameter = new QueryParameter();
        String parameterName = MergedAnnotations.from(requestParam).get(RequestParam.class).getString("name");
        postParameterCreationInternal(parameterName, methodParameter, parameter, requestParam.required(), resolveContext);
        return parameter;
    }


    protected Parameter createPathParameter(PathVariable pathVariable, MethodParameter methodParameter, ResolveContext resolveContext, int index) {
        PathParameter parameter = new PathParameter();
        String parameterName = MergedAnnotations.from(pathVariable).get(PathVariable.class).getString("name");
        if (StringUtils.isBlank(parameterName)) {
            String[] path = resolveContext.methodMapping.getStringArray("path");
            if (ArrayUtils.isNotEmpty(path)) {
                String[] paths = StringUtils.substringsBetween(path[0], "{", "}");
                if (null != paths && paths.length >= index) {
                    parameterName = paths[index];
                }
            }
        }
        postParameterCreationInternal(parameterName, methodParameter, parameter, pathVariable.required(), resolveContext);
        return parameter;
    }

    protected final void postParameterCreationInternal(String parameterName, MethodParameter methodParameter, Parameter parameter, boolean required, ResolveContext resolveContext) {
        if (StringUtils.isBlank(parameterName))
            parameterName = methodParameter.getParameterName();
        if (StringUtils.isBlank(parameterName))
            parameterName = "arg" + methodParameter.getParameterIndex();
        parameter.setName(parameterName);
        parameter.setRequired(required);
        Schema schema = SchemaUtil.createSchema(methodParameter);
        parameter.setSchema(schema);
        postParameterCreation(methodParameter, parameter, schema);
    }

    protected final Operation createOperation(final Method controllerMethod) {
        return operations.computeIfAbsent(controllerMethod, method -> {
            Operation operation = new Operation();
            operation.setParameters(Lists.newArrayList());
            operation.setTags(Lists.newArrayList());
            String qualifiedMethodName = ClassUtils.getQualifiedMethodName(controllerMethod);
            if (operationIdToMethod.containsKey(qualifiedMethodName))
                qualifiedMethodName += controllerMethod.getParameterCount();
            if (operationIdToMethod.containsKey(qualifiedMethodName)) {
                qualifiedMethodName += ArrayUtils.toString(controllerMethod.getParameterTypes());
            }
            operation.setOperationId(qualifiedMethodName);
            operationIdToMethod.putIfAbsent(qualifiedMethodName, controllerMethod);
            postOperationCreation(operation);
            return operation;
        });
    }

    protected final void bindOperationToPathItem(PathItem pathItem, Operation operation, MergedAnnotation<RequestMapping> methodMapping) {
        RequestMethod[] httpMethods = methodMapping.getEnumArray(ANNOTATION_METHOD_NAME_REQUEST_MAPPING_METHOD, RequestMethod.class);
        for (RequestMethod httpMethod : httpMethods) {
            switch (httpMethod) {
                case GET:
                    pathItem.setGet(operation);
                    break;
                case POST:
                    pathItem.setPost(operation);
                    break;
                case PUT:
                    pathItem.setPut(operation);
                    break;
                case DELETE:
                    pathItem.setDelete(operation);
                    break;
                case PATCH:
                    pathItem.setPatch(operation);
                    break;
                case HEAD:
                    pathItem.setHead(operation);
                    break;
                case TRACE:
                    pathItem.setTrace(operation);
                    break;
                case OPTIONS:
                    pathItem.setOptions(operation);
                    break;
            }
        }
    }

    protected String[] computePath(ResolveContext resolveContext) {
        String[] basePaths = resolveContext.baseMapping.getStringArray(ANNOTATION_METHOD_NAME_REQUEST_MAPPING_PATH);
        String[] methodPaths = resolveContext.methodMapping.getStringArray(ANNOTATION_METHOD_NAME_REQUEST_MAPPING_PATH);
        String[] paths = new String[basePaths.length * methodPaths.length];
        int i = 0;
        for (String basePath : basePaths) {
            if (basePath.charAt(0) != '/') {
                basePath = '/' + basePath;
            }
            if (basePath.charAt(basePath.length() - 1) != '/') {
                basePath += '/';
            }
            for (String methodPath : methodPaths) {
                methodPath = StringUtils.removeEnd(StringUtils.removeStart(methodPath, "/"), "/");
                paths[i] = basePath + methodPath;
                i++;
            }
        }
        return paths;
    }


    protected boolean readableClass(Class clazz) {
        if (null != AnnotationUtils.findAnnotation(clazz, Hidden.class))
            return false;
        if (null != AnnotationUtils.findAnnotation(clazz, ApiIgnore.class))
            return false;
        return true;
    }

    protected boolean readableMethod(Method method) {
        if (null != AnnotationUtils.findAnnotation(method, Hidden.class))
            return false;
        ApiIgnore methodIgnore = AnnotationUtils.findAnnotation(method, ApiIgnore.class);
        if (null != methodIgnore && ArrayUtils.isEmpty(methodIgnore.returnValue())) {//方法返回值限定
            return false;
        }
        if (null == AnnotationUtils.findAnnotation(method, RequestMapping.class))
            return false;
        return true;
    }

    protected Info createApiInfo(String basePackage) {
        Info info = new Info();
        info.setTitle(this.title);
        String packageVersion = StringUtils.substringAfterLast(basePackage, ".");
        if (packageVersion.length() > 2
                && (packageVersion.indexOf(0) == 'v' || packageVersion.indexOf(0) == 'V')
                && NumberUtils.isCreatable(String.valueOf(packageVersion.indexOf(1)))) {
            info.setVersion(packageVersion);
        } else {
            info.setVersion(version);
        }
        info.setDescription(this.desc);
        info.setTermsOfService(this.termsOfServiceUrl);
        Contact contact = new Contact();
        contact.setName(this.contactName);
        contact.setEmail(this.contactEmail);
        contact.setUrl(this.contactUrl);
        info.setContact(contact);
        License license = new License();
        license.setName(this.license);
        license.setUrl(this.licenseUrl);
        info.setLicense(license);
        return info;
    }

    /*******************************************sub class callback methods.************************************************************/


    protected void postPathItemCreation(PathItem pathItem) {

    }

    protected void postOperationCreation(Operation operation) {

    }

    protected void postParameterCreation(MethodParameter methodParameter, Parameter parameter, Schema schema) {

    }


    private class ResolveContext {
        private final MergedAnnotation<RequestMapping> baseMapping;
        private final MergedAnnotation<RequestMapping> methodMapping;

        public ResolveContext(MergedAnnotation<RequestMapping> baseMapping, MergedAnnotation<RequestMapping> methodMapping) {
            this.baseMapping = baseMapping;
            this.methodMapping = methodMapping;
        }
    }


    public void setDefaultConsumeMediaType(String defaultConsumeMediaType) {
        this.defaultConsumeMediaType = defaultConsumeMediaType;
    }
}
