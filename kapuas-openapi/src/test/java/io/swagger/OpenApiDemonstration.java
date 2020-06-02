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
package io.swagger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.integration.GenericOpenApiContextBuilder;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.OpenApiContextLocator;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.integration.api.ObjectMapperProcessor;
import io.swagger.v3.oas.integration.api.OpenAPIConfiguration;
import io.swagger.v3.oas.integration.api.OpenApiContext;
import io.swagger.v3.oas.integration.api.OpenApiReader;
import io.swagger.v3.oas.integration.api.OpenApiScanner;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Schema;
import org.apache.commons.lang3.ArrayUtils;
import org.scleropages.kapuas.ExampleApi;
import org.scleropages.kapuas.ExampleRequest;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public class OpenApiDemonstration implements OpenApiScanner, OpenApiReader, ObjectMapperProcessor, ModelConverter {

    public static void main(String[] args) throws OpenApiConfigurationException, JsonProcessingException, NoSuchMethodException {
        OpenAPI oas = new OpenAPI();
        Info info = new Info()
                .title("Swagger Sample App - independent config exposed by dedicated servlet")
                .description("This is a sample server Petstore server.  You can find out more about Swagger " +
                        "at [http://swagger.io](http://swagger.io) or on [irc.freenode.net, #swagger](http://swagger.io/irc/).  For this sample, " +
                        "you can use the api key `special-key` to test the authorization filters.")
                .termsOfService("http://swagger.io/terms/")
                .contact(new Contact()
                        .email("apiteam@swagger.io"))
                .license(new License()
                        .name("Apache 2.0")
                        .url("http://www.apache.org/licenses/LICENSE-2.0.html"));
        info.version("1.0.0");
        oas.info(info);

        PathItem order = new PathItem();
        Operation createOrder = new Operation();
        createOrder.setDescription("11111");
        createOrder.setTags(Lists.newArrayList("1", "2", "3"));
        order.post(createOrder);
        order.get(new Operation());
        oas.path("/order", order);

        SwaggerConfiguration oasConfig = new SwaggerConfiguration()
                .openAPI(oas)
                .resourcePackages(Stream.of("io.swagger").collect(Collectors.toSet()));

        oasConfig.setScannerClass(OpenApiDemonstration.class.getName());//use GenericOpenApiScanner as default.

        oasConfig.setReaderClass(OpenApiDemonstration.class.getName());// default read OpenApi from SwaggerConfiguration

        oasConfig.setObjectMapperProcessorClass(OpenApiDemonstration.class.getName());

        oasConfig.setModelConverterClassess(Sets.newHashSet(OpenApiDemonstration.class.getName()));

        GenericOpenApiContextBuilder builder = new GenericOpenApiContextBuilder().openApiConfiguration(oasConfig);

        OpenApiContext openApiContext = builder.buildContext(true);

        System.out.println(Yaml.pretty(OpenApiContextLocator.getInstance().getOpenApiContext(openApiContext.getId()).read()));


        Method example = ExampleApi.class.getMethod("example", ExampleRequest.class);
        MergedAnnotations from = MergedAnnotations.from(example);
        MergedAnnotation<RequestMapping> requestMappingMergedAnnotation = from.get(RequestMapping.class);
        System.out.println(ArrayUtils.toString(requestMappingMergedAnnotation.getValue("value").get()));
    }

    private OpenAPIConfiguration openApiConfiguration;


    @Override
    public void setConfiguration(OpenAPIConfiguration openApiConfiguration) {
        if (null == this.openApiConfiguration)
            this.openApiConfiguration = openApiConfiguration;
    }


    /**
     * 定制化 {@link ObjectMapper}
     *
     * @param mapper
     */
    @Override
    public void processJsonObjectMapper(ObjectMapper mapper) {
        System.out.println("customizing object mapper...");
    }

    /**
     * {@link OpenApiScanner} 扫描符合条件的类
     *
     * @return
     */
    @Override
    public Set<Class<?>> classes() {
        System.out.println("scan classes...");
        return Sets.newHashSet(ExampleApi.class);
    }

    /**
     * {@link OpenApiScanner} 扫描符合条件的资源
     *
     * @return
     */
    @Override
    public Map<String, Object> resources() {
        System.out.println("scan resources...");
        return null;
    }

    /**
     * {@link OpenApiReader} 从扫描到的类以及资源构建 {@link OpenAPI}
     *
     * @param classes
     * @param resources
     * @return
     */
    @Override
    public OpenAPI read(Set<Class<?>> classes, Map<String, Object> resources) {
        System.out.println("read...");
        OpenAPI openApi = openApiConfiguration.getOpenAPI();
        return openApi;
    }


    @Override
    @Deprecated
    public void processYamlObjectMapper(ObjectMapper mapper) {
        processJsonObjectMapper(mapper);
    }

    @Override
    public Schema resolve(AnnotatedType type, ModelConverterContext context, Iterator<ModelConverter> chain) {
        return null;
    }
}
