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
package org.scleropages.kapuas.spring.boot.autoconfigure;

import org.scleropages.kapuas.KapuasSignatureKeyProvider;
import org.scleropages.kapuas.security.authc.token.server.jwt.JwtTokenFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
@Configuration
@ConditionalOnClass(JwtTokenFactory.SignatureKeyProvider.class)
@AutoConfigureAfter(RestTemplateAutoConfiguration.class)
@EnableConfigurationProperties({KapuasProperties.class})
public class KapuasAutoConfigure {

    @Bean
    @ConditionalOnMissingBean
    public KapuasSignatureKeyProvider kapuasSignatureKeyProvider(KapuasProperties kapuasProperties, RestTemplate restTemplate) {
        return new KapuasSignatureKeyProvider(kapuasProperties, restTemplate);
    }
}
