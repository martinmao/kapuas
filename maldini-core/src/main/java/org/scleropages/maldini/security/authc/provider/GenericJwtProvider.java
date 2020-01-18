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

import java.util.Date;
import java.util.Map;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public abstract class GenericJwtProvider<T> implements JwtProvider {


    private static final String JWT_HEADER_AUTH_TYPE = "aut";
    private static final String JWT_HEADER_AUTH_ID = "aui";

    @Value("#{ @environment['jwt.token.default.expiration'] ?: 1800000 }")
    private long jwtTokenExpiration;
    @Value("#{ @environment['jwt.token.default.not-before'] ?: 0 }")
    private long jwtTokenNotBefore;


    private JwtTokenTemplateManager tokenTemplateManager;

    abstract protected String jwtAssociatedId(Authenticated authenticated);

    abstract protected Integer jwtAssociatedType(Authenticated authenticated);

    protected void postJwtTokenBuild(JwtToken.JwtTokenBuilder tokenBuilder) {

    }

    @Override
    public JwtEncodedToken build(Authenticated authenticated, JwtTokenFactory jwtTokenFactory, JwtToken.JwtTokenBuilder tokenBuilder, Map requestContext) {
        long now = System.currentTimeMillis();
        JwtTokenTemplate jwtTokenTemplate = tokenTemplateManager.find(jwtAssociatedId(authenticated), jwtAssociatedType(authenticated));
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
        postJwtTokenBuild(tokenBuilder);
        return jwtTokenFactory.encode(jwtTokenTemplate.getAlgorithm(), jwtTokenTemplate.getSignKeyEncoded(), tokenBuilder.build());
    }

    @Override
    public byte[] resolveSignatureKey(JwtTokenFactory.JwtHeader jwtHeader) {
        Integer jwtAssociatedType = Integer.parseInt(String.valueOf(jwtHeader.get(JWT_HEADER_AUTH_TYPE)));
        String jwtAssociatedId = String.valueOf(jwtHeader.get(JWT_HEADER_AUTH_ID));
        JwtTokenTemplate jwtTokenTemplate = tokenTemplateManager.find(jwtAssociatedId, jwtAssociatedType);
        if (null != jwtTokenTemplate) {
            return jwtTokenTemplate.getVerifyKeyEncoded()
                    != null ? jwtTokenTemplate.getVerifyKeyEncoded() : jwtTokenTemplate.getSignKeyEncoded();
        }
        return new byte[0];
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
