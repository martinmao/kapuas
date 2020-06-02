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

import io.swagger.v3.oas.models.media.Schema;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public interface SchemaResolver {

    /**
     * return true if given class can resolved.
     * @param source
     * @return
     */
    boolean support(Class source);

    /**
     * resolved given class as {@link Schema}
     * @param source
     * @return
     */
    Schema resolve(Class source);
}