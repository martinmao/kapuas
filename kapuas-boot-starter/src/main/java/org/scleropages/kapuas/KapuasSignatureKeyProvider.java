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
package org.scleropages.kapuas;

import com.google.common.collect.Maps;
import org.scleropages.kapuas.bizmodel.security.JwtTokenTemplateModel;
import org.scleropages.kapuas.security.authc.token.server.jwt.JwtTokenFactory;
import org.scleropages.kapuas.spring.boot.autoconfigure.KapuasProperties;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public class KapuasSignatureKeyProvider implements JwtTokenFactory.SignatureKeyProvider {

    private static final String METHOD_PATH_GET_VERIFY_KEY_ENCODED = "METHOD_PATH_GET_VERIFY_KEY_ENCODED";

    private static final int ORG_SCLEROPAGES_KAPUAS_APP_APPLICATION_MANAGER_AUTHENTICATION_DETAILS_PROVIDER_ID = 9527;

    private static final Map<String, String> PATH_MAPPING = Maps.newHashMap();

    static {
        PATH_MAPPING.put(METHOD_PATH_GET_VERIFY_KEY_ENCODED, "jwt/{associatedType}/{associatedId}");
    }

    private String gateway;

    private String appId;

    private final RestTemplate restTemplate;

    public KapuasSignatureKeyProvider(KapuasProperties kapuasProperties, RestTemplate restTemplate) {
        this.gateway = kapuasProperties.getGateway();
        this.appId = kapuasProperties.getAppId();
        this.restTemplate = restTemplate;
        Assert.isTrue(gateway != null && appId != null && restTemplate != null, "failure to initializing KapuasSignatureKeyProvider. gateway or appId or restTemplate is required.");
    }

    @Override
    public boolean support(JwtTokenFactory.JwtHeader jwtHeader) {
        return true;
    }

    @Override
    public byte[] get(JwtTokenFactory.JwtHeader jwtHeader) {
        JwtTokenTemplateModel token = restTemplate.getForObject(computeRequestUrl(METHOD_PATH_GET_VERIFY_KEY_ENCODED), JwtTokenTemplateModel.class, ORG_SCLEROPAGES_KAPUAS_APP_APPLICATION_MANAGER_AUTHENTICATION_DETAILS_PROVIDER_ID, appId);
        return token.getVerifyKeyEncoded();
    }

    protected String computeRequestUrl(String method) {
        return gateway + "/" + PATH_MAPPING.get(method);
    }
}
