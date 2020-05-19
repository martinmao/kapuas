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
package org.scleropages.maldini.security.authc.provider;

import com.google.common.collect.Maps;
import org.scleropages.maldini.security.authc.mgmt.JwtTokenTemplateManager;
import org.scleropages.maldini.security.authc.mgmt.model.JwtTokenTemplate;
import org.scleropages.maldini.security.authc.token.client.jwt.JwtEncodedToken;
import org.scleropages.maldini.security.authc.token.server.jwt.JwtToken;
import org.scleropages.maldini.security.authc.token.server.jwt.JwtTokenFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.Map;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public abstract class AbstractJwtTokenTemplateProvider<T> implements JwtProvider<T> {

    private static final String JWT_HEADER_AUTH_TYPE = "aut";//auth type .
    private static final String JWT_HEADER_AUTH_ID = "aui";//auth id.

    @Value("#{ @environment['jwt.token.template.expiration'] ?: 1800000 }")
    private long jwtTokenExpiration;
    @Value("#{ @environment['jwt.token.template.not-before'] ?: 0 }")
    private long jwtTokenNotBefore;

    private JwtTokenTemplateManager tokenTemplateManager;

    abstract protected String jwtAssociatedId(Authenticated authenticated, Map<String, Object> requestContext);

    abstract protected Integer jwtAssociatedType(Authenticated authenticated, Map<String, Object> requestContext);


    @Override
    public JwtEncodedToken build(Authenticated authenticated, JwtTokenFactory jwtTokenFactory, JwtToken.JwtTokenBuilder tokenBuilder, Map<String, Object> requestContext) {
        preJwtTokenBuild(authenticated, jwtTokenFactory, tokenBuilder, requestContext);
        long now = System.currentTimeMillis();
        JwtTokenTemplate jwtTokenTemplate = tokenTemplateManager.getByAssociatedTypeAndAssociatedId(jwtAssociatedType(authenticated, requestContext), jwtAssociatedId(authenticated, requestContext));
        Assert.notNull(jwtTokenTemplate, "no jwt token template found.");
        Map<String, Object> jwtHeader = Maps.newHashMap();
        jwtHeader.put(JWT_HEADER_AUTH_TYPE, jwtTokenTemplate.getAssociatedType());
        jwtHeader.put(JWT_HEADER_AUTH_ID, jwtTokenTemplate.getAssociatedId());
        tokenBuilder.withHeaders(jwtHeader);
        tokenBuilder.withSubject(jwtTokenTemplate.getSubject());
        tokenBuilder.withIssuer(jwtTokenTemplate.getIssuer());
        tokenBuilder.withAudience(authenticated.host());
        tokenBuilder.withIssuedAt(new Date(now));
        tokenBuilder.withExpiration(new Date(now + jwtTokenExpiration));
        tokenBuilder.withNotBefore(new Date(now + jwtTokenNotBefore));
        postJwtTokenBuild(authenticated, jwtTokenFactory, tokenBuilder, requestContext);
        return jwtTokenFactory.encode(jwtTokenTemplate.getAlgorithm(), jwtTokenTemplate.getSignKeyEncoded(), tokenBuilder.build());
    }

    @Override
    public byte[] resolveSignatureKey(JwtTokenFactory.JwtHeader jwtHeader) {
        Integer jwtAssociatedType;
        try {
            jwtAssociatedType = Integer.parseInt(String.valueOf(jwtHeader.get(JWT_HEADER_AUTH_TYPE)));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(JWT_HEADER_AUTH_TYPE + " not a valid number.");
        }
        String jwtAssociatedId = String.valueOf(jwtHeader.get(JWT_HEADER_AUTH_ID));
        JwtTokenTemplate jwtTokenTemplate = tokenTemplateManager.getByAssociatedTypeAndAssociatedId(jwtAssociatedType, jwtAssociatedId);
        Assert.notNull(jwtTokenTemplate, "no jwt token template found.");
        return jwtTokenTemplate.getVerifyKeyEncoded()
                != null ? jwtTokenTemplate.getVerifyKeyEncoded() : jwtTokenTemplate.getSignKeyEncoded();
    }


    protected void preJwtTokenBuild(Authenticated authenticated, JwtTokenFactory jwtTokenFactory, JwtToken.JwtTokenBuilder tokenBuilder, Map<String, Object> requestContext) {

    }

    protected void postJwtTokenBuild(Authenticated authenticated, JwtTokenFactory jwtTokenFactory, JwtToken.JwtTokenBuilder tokenBuilder, Map<String, Object> requestContext) {

    }

    @Override
    public boolean supportResolve(JwtTokenFactory.JwtHeader jwtHeader) {
        return null != jwtHeader.get(JWT_HEADER_AUTH_TYPE) && null != jwtHeader.get(JWT_HEADER_AUTH_ID);
    }

    @Autowired
    public void setTokenTemplateManager(JwtTokenTemplateManager tokenTemplateManager) {
        this.tokenTemplateManager = tokenTemplateManager;
    }
}
