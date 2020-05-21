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
package org.scleropages.kapuas.app.jwt;

import org.scleropages.kapuas.app.ApplicationManager;
import org.scleropages.kapuas.app.DomainManager;
import org.scleropages.kapuas.app.model.Application;
import org.scleropages.kapuas.security.acl.AclManager;
import org.scleropages.kapuas.security.acl.model.AclPrincipalModel;
import org.scleropages.kapuas.security.acl.model.PermissionModel;
import org.scleropages.kapuas.security.acl.model.ResourceModel;
import org.scleropages.kapuas.security.authc.mgmt.AbstractJwtTokenTemplateProvider;
import org.scleropages.kapuas.security.authc.provider.Authenticated;
import org.scleropages.kapuas.security.authc.token.server.jwt.JwtToken;
import org.scleropages.kapuas.security.authc.token.server.jwt.JwtTokenFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
@Component
public class ApplicationJwtProvider extends AbstractJwtTokenTemplateProvider<Application> {

    private static final String REQUEST_CONTEXT_PARAMETER_FUNCTION_NAME = "app_func";

    @Value("#{ @environment['jwt.token.app_auth.acl_resource'] ?: 'domain_function_access' }")
    private String applicationJwtAclResourceType;
    @Value("#{ @environment['jwt.token.app_auth.acl_permission'] ?: null }")
    private String applicationJwtAclPermission;

    private AclManager aclManager;

    private DomainManager domainManager;

    private ApplicationManager applicationManager;

    @Override
    protected void preJwtTokenBuild(Authenticated authenticated, JwtTokenFactory jwtTokenFactory, JwtToken.JwtTokenBuilder tokenBuilder, Map<String, Object> requestContext) {
        assertAccessible(authenticated, requestContext);
    }

    @Override
    protected void postJwtTokenBuild(Authenticated authenticated, JwtTokenFactory jwtTokenFactory, JwtToken.JwtTokenBuilder tokenBuilder, Map<String, Object> requestContext) {
        tokenBuilder.withSubject(requestContext.get(REQUEST_CONTEXT_PARAMETER_FUNCTION_NAME).toString());
        tokenBuilder.withAudience(authenticated.principal().toString());
        tokenBuilder.set("clt", authenticated.host());
    }


    @Override
    protected Integer jwtAssociatedType(Authenticated authenticated, Map<String, Object> requestContext) {
        return applicationManager.getProviderId();
    }

    @Override
    protected String jwtAssociatedId(Authenticated authenticated, Map<String, Object> requestContext) {
        return domainManager.getAppIdByFunctionFullName(requestContext.get(REQUEST_CONTEXT_PARAMETER_FUNCTION_NAME).toString());
    }

    protected void assertAccessible(Authenticated authenticated, final Map<String, Object> requestContext) {
        ResourceModel functionResource = new ResourceModel();
        functionResource.setId(getRequiredFunctionName(requestContext));
        functionResource.setType(applicationJwtAclResourceType);
        AclPrincipalModel principal = new AclPrincipalModel();
        principal.setName(String.valueOf(authenticated.principal()));
        aclManager.accessible(functionResource, principal, new PermissionModel(applicationJwtAclPermission));
    }

    protected String getRequiredFunctionName(final Map<String, Object> requestContext) {
        Object requestFunction = requestContext.get(REQUEST_CONTEXT_PARAMETER_FUNCTION_NAME);
        Assert.notNull(requestFunction, "param " + REQUEST_CONTEXT_PARAMETER_FUNCTION_NAME + " required.");
        return String.valueOf(requestFunction);
    }

    @Autowired
    public void setAclManager(AclManager aclManager) {
        this.aclManager = aclManager;
    }

    @Autowired
    public void setDomainManager(DomainManager domainManager) {
        this.domainManager = domainManager;
    }

    @Autowired
    public void setApplicationManager(ApplicationManager applicationManager) {
        this.applicationManager = applicationManager;
    }
}
