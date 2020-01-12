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
package org.scleropages.maldini.app.jwt;

import org.scleropages.maldini.app.ApplicationManager;
import org.scleropages.maldini.app.model.Application;
import org.scleropages.maldini.security.acl.AclEntry;
import org.scleropages.maldini.security.acl.AclManager;
import org.scleropages.maldini.security.acl.model.AclPrincipalModel;
import org.scleropages.maldini.security.acl.model.PermissionModel;
import org.scleropages.maldini.security.acl.model.ResourceModel;
import org.scleropages.maldini.security.authc.provider.Authenticated;
import org.scleropages.maldini.security.authc.provider.JwtProvider;
import org.scleropages.maldini.security.authc.token.client.jwt.JwtEncodedToken;
import org.scleropages.maldini.security.authc.token.server.jwt.JwtToken;
import org.scleropages.maldini.security.authc.token.server.jwt.JwtTokenFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.Map;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
@Component
public class ApplicationJwtProvider implements JwtProvider<Application> {

    private static final String JWT_HEADER_APP_AUTH_ID = "aau";
    private static final String REQUEST_FUNCTION_NAME = "func";

    private static final String ACL_RESOURCE_TYPE_FUNCTION = "app_func";
    private static final String ACL_PERMISSION_NAME = "execute";


    @Value("#{ @environment['jwt.token.app_auth.expiration'] ?: 1800000 }")
    private long jwtTokenExpiration;
    @Value("#{ @environment['jwt.token.app_auth.issuer'] ?: 'app_auth' }")
    private String jwtTokenIssuer;
    @Value("#{ @environment['jwt.token.app_auth.not-before'] ?: 0 }")
    private long jwtTokenNotBefore;


    protected ApplicationManager applicationManager;

    protected AclManager aclManager;

    @Override
    public JwtEncodedToken build(Authenticated authenticated, JwtTokenFactory jwtTokenFactory, JwtToken.JwtTokenBuilder builder, final Map<String, String> requestContext) {
        assertAccessible(authenticated, requestContext);

        long now = System.currentTimeMillis();
        builder.withAudience(authenticated.host());
        builder.withExpiration(new Date(now + jwtTokenExpiration));
        builder.withIssuedAt(new Date(now));
        builder.withIssuer(jwtTokenIssuer);
        builder.withSubject(String.valueOf(authenticated.principal()));
        builder.withNotBefore(new Date(now + jwtTokenNotBefore));

        return null;
    }

    protected void assertAccessible(Authenticated authenticated, final Map<String, String> requestContext) {
        ResourceModel functionResource = new ResourceModel();
        functionResource.setId(requestContext.get(REQUEST_FUNCTION_NAME));
        functionResource.setType(ACL_RESOURCE_TYPE_FUNCTION);
        AclPrincipalModel principal = new AclPrincipalModel();
        principal.setName(String.valueOf(authenticated.principal()));
        Page<AclEntry> aclEntries = aclManager.readPrincipalEntries(principal, functionResource, new PermissionModel(ACL_PERMISSION_NAME), Pageable.unpaged());
        Assert.isTrue(!aclEntries.isEmpty(), "access denied.");
    }

    @Override
    public boolean supportResolve(JwtTokenFactory.JwtHeader jwtHeader) {
        return null != jwtHeader.get(JWT_HEADER_APP_AUTH_ID);
    }

    @Override
    public byte[] resolveSignatureKey(JwtTokenFactory.JwtHeader jwtHeader) {
        return new byte[0];
    }

    @Autowired
    public void setApplicationManager(ApplicationManager applicationManager) {
        this.applicationManager = applicationManager;
    }

    public void setAclManager(AclManager aclManager) {
        this.aclManager = aclManager;
    }
}
