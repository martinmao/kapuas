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
package org.scleropages.kapuas.openapi;

import org.scleropages.kapuas.openapi.provider.GenericOpenApiContext;
import org.scleropages.kapuas.openapi.provider.OpenApiReader;
import org.scleropages.kapuas.openapi.provider.OpenApiScanner;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public class OpenApiContextBuilder {

    private final OpenApiScanner openApiScanner;
    private final OpenApiReader openApiReader;
    private final String[] basePackages;

    private AtomicBoolean buildFlag = new AtomicBoolean(false);

    private OpenApiContext openApiContext;


    public OpenApiContextBuilder(OpenApiScanner openApiScanner, OpenApiReader openApiReader, String[] basePackages) {
        this.openApiScanner = openApiScanner;
        this.openApiReader = openApiReader;
        this.basePackages = basePackages;
    }

    public OpenApiContext build() {
        if (buildFlag.compareAndSet(false, true)) {
            GenericOpenApiContext openApiContext = new GenericOpenApiContext();
            for (String basePackage : basePackages) {
                Set<Class<?>> scanClasses = openApiScanner.scan(basePackage);
                OpenApi read = openApiReader.read(basePackage, scanClasses);
                openApiContext.register(basePackage, read);
            }
            this.openApiContext = openApiContext;
            OpenApiContextHolder.setOpenApiContext(openApiContext);
        }
        return openApiContext;
    }
}
