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

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import org.apache.commons.beanutils.BeanUtils;
import org.scleropages.kapuas.openapi.provider.swagger.ResolveContext;
import org.scleropages.kapuas.openapi.provider.swagger.SchemaResolver;
import org.scleropages.kapuas.openapi.provider.swagger.SchemaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.data.domain.Page;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * support for spring data {@link Page} as schema. it's will also resolve {@link Page#getContent()} as schema.
 *
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public class PageSchemaResolver implements SchemaResolver {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final AtomicBoolean pageSchemaCreated = new AtomicBoolean(false);


    @Override
    public boolean supportInternal(Class javaType, Optional<MethodParameter> methodParameter, Optional<Field> field, ResolveContext resolveContext) {
        return ClassUtils.isAssignable(Page.class, javaType);
    }

    @Override
    public Schema resolveInternal(Class javaType, Optional<MethodParameter> methodParameter, Optional<Field> field, ResolveContext resolveContext) {
        if (pageSchemaCreated.compareAndSet(false, true)) {
            SchemaUtil.createSchema(javaType, true, resolveContext);
        }
        Schema copedSchema = new Schema();
        try {
            BeanUtils.copyProperties(copedSchema, resolveContext.getSwaggerOpenApi().getSchema(Page.class, Page.class));
        } catch (Exception e) {
            if (logger.isDebugEnabled())
                logger.warn("failure to copy schema of: " + copedSchema.getClass().getName(), e);
        }
        ArraySchema contentArray = new ArraySchema();
        copedSchema.addProperties("content", contentArray);
        Class<?> contentType = null;
        MethodParameter mp = methodParameter.isPresent() ? methodParameter.get() : null;
        Field fd = field.isPresent() ? field.get() : null;
        if (methodParameter.isPresent() && (!field.isPresent())) {
            contentType = ResolvableType.forMethodParameter(mp).resolveGeneric(0);
        }
        if (field.isPresent()) {
            contentType = ResolvableType.forField(fd).resolveGeneric(0);
        }
        Schema contentSchema = SchemaUtil.createSchema(contentType, mp, fd, resolveContext);
//        copedSchema.setName(Page.class.getName() + "_" + contentType.getSimpleName());
        contentArray.setItems(contentSchema);
//        copedSchema.addProperties("content", contentSchema);
        return copedSchema;
    }

    @Override
    public void reset() {
        pageSchemaCreated.set(false);
    }
}
