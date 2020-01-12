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
package org.scleropages.maldini.security.acl.web;

import org.scleropages.crud.web.GenericAction;
import org.scleropages.maldini.security.acl.AclManager;
import org.scleropages.maldini.security.acl.model.AclPrincipalModel;
import org.scleropages.maldini.security.acl.model.AclStrategy;
import org.scleropages.maldini.security.acl.model.PermissionModel;
import org.scleropages.maldini.security.acl.model.ResourceModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
@RestController
@RequestMapping("acl")
public class AclAction implements GenericAction {

    private AclManager aclManager;

    @PostMapping("strategy")
    public void createAclStrategy(@RequestBody AclStrategy aclStrategy) {
        aclManager.createAclStrategy(aclStrategy);
    }

    @GetMapping("strategy/{resource}")
    public Object getAclStrategy(@PathVariable String resource) {
        return aclManager.getAclStrategy(resource);
    }

    @PostMapping("principal")
    public void createPrincipal(@RequestBody AclPrincipalModel principalModel) {
        aclManager.createAclPrincipal(principalModel);
    }

    @GetMapping("principal")
    public Object queryPrincipal(HttpServletRequest request) {
        return aclManager.readAclPrincipals(buildSearchFilterFromRequest(request),buildPageableFromRequest(request));
    }

    @PostMapping
    public void createAcl(@RequestBody ResourceModel resourceModel) {
        aclManager.createAcl(resourceModel);
    }

    @GetMapping
    public Object readAcl(HttpServletRequest request, @RequestParam String type) {
        ResourceModel model = new ResourceModel();
        model.setType(type);
        return aclManager.readAcl(model, buildPageableFromRequest(request));
    }

    @GetMapping("item/{resourceType}/{resourceId}")
    public Object getAcl(@PathVariable String resourceType, @PathVariable String resourceId) {
        ResourceModel model = new ResourceModel();
        model.setType(resourceType);
        model.setId(resourceId);
        return aclManager.readAcl(model);
    }

    @PostMapping("entries/{resourceType}/{resourceId}")
    public void createAclEntry(@PathVariable String resourceType, @PathVariable String resourceId, @RequestBody @Valid CreateAclEntryRequest request) {
        ResourceModel resourceModel = new ResourceModel();
        resourceModel.setType(resourceType);
        resourceModel.setId(resourceId);
        AclPrincipalModel aclPrincipalModel = new AclPrincipalModel();
        aclPrincipalModel.setName(request.getPrincipal());

        String[] permissions = request.getPermission();
        int permissionsLength = null != permissions ? permissions.length : -1;
        PermissionModel[] permissionModels = null;
        if (permissionsLength > 0) {
            permissionModels = new PermissionModel[permissionsLength];
            for (int i = 0; i < permissionsLength; i++) {
                PermissionModel permissionModel = new PermissionModel();
                permissionModel.setName(permissions[i]);
                permissionModels[i] = permissionModel;
            }
        }
        aclManager.createAclEntry(resourceModel, aclPrincipalModel, permissionModels);
    }

    @GetMapping("entries/{resourceType}/{resourceId}")
    public Object readAclEntries(HttpServletRequest request, @PathVariable String resourceType, @PathVariable String resourceId, String principal) {
        ResourceModel resourceModel = new ResourceModel();
        resourceModel.setType(resourceType);
        resourceModel.setId(resourceId);
        return aclManager.readEntries(resourceModel, new AclPrincipalModel(principal), buildPageableFromRequest(request));
    }

    @GetMapping("principal_entries/{principal}/{resourceType}")
    public Object readPrincipalAclEntries(HttpServletRequest request,
                                          @PathVariable String principal, @PathVariable String resourceType,
                                          String resourceId, String permission) {
        ResourceModel resourceModel = new ResourceModel();
        resourceModel.setType(resourceType);
        resourceModel.setId(resourceId);
        return aclManager.readPrincipalEntries(new AclPrincipalModel(principal), resourceModel, new PermissionModel(permission), buildPageableFromRequest(request));
    }

    @Autowired
    public void setAclManager(AclManager aclManager) {
        this.aclManager = aclManager;
    }
}
