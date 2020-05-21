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
package org.scleropages.kapuas.configuration.shiro;

import org.apache.commons.collections.MapUtils;
import org.apache.shiro.config.Ini;
import org.apache.shiro.spring.web.config.ShiroFilterChainDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Map.Entry;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public class UrlShiroFilterChainDefinition implements ShiroFilterChainDefinition {

    private static final String DEFAULT_PERMISSION_IDENTIFIER = "P_ACCESS";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private String permissionIdentifier = DEFAULT_PERMISSION_IDENTIFIER;

    private String configureFilterChainDefinitions;

    private Map<String, Object> internalQueryParams = MapUtils.EMPTY_MAP;


    @Override
    public Map<String, String> getFilterChainMap() {
        logger.debug("Loading shiro filter chain definitions...");
        Ini ini = new Ini();
        ini.load(configureFilterChainDefinitions);
        Ini.Section section = ini.getSection(Ini.DEFAULT_SECTION_NAME);

        logger.debug("Successfully loaded configure filter chain definitions.");

        loadInternalFilterChainDefinitions(section);

        logger.info("Successfully loaded all filter chain definitions as follow: ");

        if (logger.isInfoEnabled()) {
            for (Entry<String, String> sectionEntry : section.entrySet()) {
                logger.info(sectionEntry.getKey() + " = " + sectionEntry.getValue());
            }
        }
        return section;
    }


    protected void loadInternalFilterChainDefinitions(Ini.Section section) {

    }

    public void setConfigureFilterChainDefinitions(String configureFilterChainDefinitions) {
        this.configureFilterChainDefinitions = configureFilterChainDefinitions;
    }


    public void setPermissionIdentifier(String permissionIdentifier) {
        this.permissionIdentifier = permissionIdentifier;
    }

    public void setInternalQueryParams(Map<String, Object> internalQueryParams) {
        this.internalQueryParams = internalQueryParams;
    }
}
