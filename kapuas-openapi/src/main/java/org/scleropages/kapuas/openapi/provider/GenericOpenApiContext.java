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
package org.scleropages.kapuas.openapi.provider;

import com.google.common.collect.Maps;
import org.scleropages.kapuas.openapi.OpenApi;
import org.scleropages.kapuas.openapi.OpenApiContext;

import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public class GenericOpenApiContext implements OpenApiContext {

    private final Map<String, OpenApi> openApis = Maps.newConcurrentMap();

    public void register(String id, OpenApi openApi) {
        openApis.putIfAbsent(id, openApi);
    }

    @Override
    public OpenApi openApi(String id) {
        return openApis.get(id);
    }

    @Override
    public Set<String> ids() {
        return openApis.keySet();
    }
}
