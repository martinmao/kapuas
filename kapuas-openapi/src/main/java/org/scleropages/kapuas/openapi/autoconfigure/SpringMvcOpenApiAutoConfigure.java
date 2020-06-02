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
package org.scleropages.kapuas.openapi.autoconfigure;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.scleropages.kapuas.openapi.OpenApiContextBuilder;
import org.scleropages.kapuas.openapi.provider.swagger.BeanComponentApiScanner;
import org.scleropages.kapuas.openapi.provider.swagger.SpringMvcOpenApiReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
@Configuration
@ConditionalOnProperty(name = "openapi.base-packages")
@ComponentScan(basePackages = {"org.scleropages.kapuas.openapi.web"})
public class SpringMvcOpenApiAutoConfigure implements ApplicationListener<ContextRefreshedEvent>, WebMvcConfigurer {

    @Value("#{ @environment['openapi.base-packages'] ?: 'no_package_provided' }")
    private String basePackages;

    @Value("#{ @environment['openapi.build-on-startup'] ?: true }")
    private boolean buildOpenApiOnStartup;

    @Bean
    @ConditionalOnMissingBean
    public BeanComponentApiScanner beanComponentApiScanner() {
        BeanComponentApiScanner beanComponentApiScanner = new BeanComponentApiScanner();
        beanComponentApiScanner.setIncludeFilters(Lists.newArrayList(new AnnotationTypeFilter(Controller.class)));
        return beanComponentApiScanner;
    }

    @Bean
    @ConditionalOnMissingBean
    public SpringMvcOpenApiReader springMvcOpenApiReader() {
        SpringMvcOpenApiReader springMvcOpenApiReader = new SpringMvcOpenApiReader();
        return springMvcOpenApiReader;
    }

    @Bean
    @ConditionalOnMissingBean
    public OpenApiContextBuilder openApiContextBuilder() {
        OpenApiContextBuilder builder = new OpenApiContextBuilder(beanComponentApiScanner(), springMvcOpenApiReader(), StringUtils.split(basePackages, ","));
        return builder;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (buildOpenApiOnStartup)
            event.getApplicationContext().getBean(OpenApiContextBuilder.class).build();
    }


}
