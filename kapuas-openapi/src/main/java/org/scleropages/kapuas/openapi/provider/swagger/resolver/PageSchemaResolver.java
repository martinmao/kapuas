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
package org.scleropages.kapuas.openapi.provider.swagger.resolver;

import io.swagger.v3.oas.models.media.Schema;
import org.scleropages.kapuas.openapi.provider.swagger.SchemaResolver;
import org.scleropages.kapuas.openapi.provider.swagger.SchemaUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.util.ClassUtils;

import java.beans.PropertyDescriptor;
import java.util.stream.Stream;

/**
 * support for spring data {@link Page} as schema.
 *
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public class PageSchemaResolver implements SchemaResolver {


    @Override
    public boolean support(Class source) {
        return ClassUtils.isAssignable(Page.class, source);
    }

    @Override
    public Schema resolve(Class source) {
        return null;
    }

    public static void main(String[] args) {
        PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(Page.class);
        Stream.of(propertyDescriptors).forEach(propertyDescriptor -> System.out.println(propertyDescriptor));

    }
}
