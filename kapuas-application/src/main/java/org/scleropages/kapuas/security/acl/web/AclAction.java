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
package org.scleropages.kapuas.security.acl.web;

import org.apache.commons.collections.MapUtils;
import org.scleropages.crud.dao.orm.SearchFilter;
import org.scleropages.crud.types.NamedPrimitive;
import org.scleropages.crud.web.GenericAction;

import org.scleropages.kapuas.security.acl.Acl;
import org.scleropages.kapuas.security.acl.AclEntry;
import org.scleropages.kapuas.security.acl.AclManager;
import org.scleropages.kapuas.security.acl.AclPrincipal;
import org.scleropages.kapuas.security.acl.model.AclEntryModel;
import org.scleropages.kapuas.security.acl.model.AclModel;
import org.scleropages.kapuas.security.acl.model.AclPrincipalModel;
import org.scleropages.kapuas.security.acl.model.AclStrategy;
import org.scleropages.kapuas.security.acl.model.PermissionModel;
import org.scleropages.kapuas.security.acl.model.ResourceModel;
import org.scleropages.openapi.annotation.ApiIgnore;
import org.scleropages.openapi.annotation.ApiModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
@RestController
@RequestMapping("acl")
public class AclAction implements GenericAction {

    private AclManager aclManager;

    @PostMapping("strategy")
    public void createAclStrategy(@ApiIgnore(AclStrategy.Create.class) @RequestBody AclStrategy aclStrategy) {
        aclManager.createAclStrategy(aclStrategy);
    }

    @GetMapping("strategy/{resource}")
    @ApiIgnore(AclStrategy.Read.class)
    public AclStrategy getAclStrategy(@PathVariable String resource) {
        return aclManager.getAclStrategy(resource);
    }

    @GetMapping("strategy")
    public String[] getAllAclStrategies() {
        return aclManager.getAllAclStrategyResourceTypes();
    }

    @PostMapping("principal")
    public void createPrincipal(@ApiIgnore(AclPrincipalModel.Create.class) @RequestBody AclPrincipalModel principalModel) {
        aclManager.createAclPrincipal(principalModel);
    }

    @GetMapping("principal")
    @ApiModel(AclPrincipalModel.class)
    public Page<AclPrincipal> findPrincipal(HttpServletRequest request) {
        return aclManager.findAclPrincipals(buildSearchFilterFromRequest(request), buildPageableFromRequest(request));
    }

    @PostMapping("resource")
    public void createAcl(@ApiIgnore(ResourceModel.Create.class) @RequestBody ResourceModel resourceModel) {
        aclManager.createAcl(resourceModel);
    }

    @GetMapping("resource/{resourceType}")
    @ApiModel(AclModel.class)
    @ApiIgnore({AclModel.PageItem.class})
    public Page<Acl> findAcl(HttpServletRequest request, @PathVariable String resourceType, @RequestParam(required = false) String variables) {
        ResourceModel model = new ResourceModel();
        model.setType(resourceType);
        Map<String, Object> variablesSearchParams = StringUtils.hasText(variables) ? buildObjectFromJsonPayload(variables, Map.class) : MapUtils.EMPTY_MAP;
        return aclManager.findAcl(model, buildPageableFromRequest(request), SearchFilter.SearchFilterBuilder.build(variablesSearchParams));
    }

    @GetMapping("resource/{resourceType}/{resourceId}")
    @ApiModel(AclModel.class)
    @ApiIgnore({AclModel.Info.class})
    public Acl getAcl(@PathVariable String resourceType, @PathVariable String resourceId) {
        ResourceModel model = new ResourceModel();
        model.setType(resourceType);
        model.setId(resourceId);
        return aclManager.getAcl(model);
    }


    @PutMapping("resource/{resourceType}/{resourceId}")
    public void updateAcl(@PathVariable String resourceType, @PathVariable String resourceId, @ApiIgnore(ResourceModel.Update.class) @RequestBody ResourceModel resourceModel) {
        resourceModel.setType(resourceType);
        resourceModel.setId(resourceId);
        aclManager.updateAcl(resourceModel);
    }

    @DeleteMapping("resource/{resourceType}/{resourceId}")
    public void deleteAcl(@PathVariable String resourceType, @PathVariable String resourceId) {
        ResourceModel model = new ResourceModel();
        model.setType(resourceType);
        model.setId(resourceId);
        aclManager.deleteAcl(model);
    }

    @GetMapping("resource/payload/{resourceType}")
    public List<String> findAllAclBizPayload(@PathVariable String resourceType, @RequestParam(name = "aclId") Long... aclIds) {
        ResourceModel resourceModel = new ResourceModel();
        resourceModel.setType(resourceType);
        return aclManager.findAllAclBizPayload(resourceModel, aclIds);
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


    @DeleteMapping("entries/{resourceType}/{resourceId}/{principalName}")
    public void deleteAclEntry(@PathVariable String resourceType, @PathVariable String resourceId, @PathVariable String principalName, @RequestParam(name = "permission", required = false) String... permissionNames) {
        ResourceModel model = new ResourceModel();
        model.setType(resourceType);
        model.setId(resourceId);

        AclPrincipalModel aclPrincipalModel = new AclPrincipalModel(principalName);

        PermissionModel[] permissionModels = null != permissionNames ? new PermissionModel[permissionNames.length] : null;
        if (null != permissionModels)
            for (int i = 0; i < permissionNames.length; i++) {
                permissionModels[i] = new PermissionModel(permissionNames[i]);
            }
        aclManager.deleteAclEntry(model, aclPrincipalModel, permissionModels);
    }

    @GetMapping("entries/{resourceType}/{resourceId}")
    @ApiModel(AclEntryModel.class)
    public Page<AclEntry> findAclEntries(HttpServletRequest request, @PathVariable String resourceType, @PathVariable String resourceId, String principal) {
        ResourceModel resourceModel = new ResourceModel();
        resourceModel.setType(resourceType);
        resourceModel.setId(resourceId);
        return aclManager.findEntries(resourceModel, new AclPrincipalModel(principal), buildPageableFromRequest(request));
    }

    @GetMapping("principal_entries/{principal}/{resourceType}")
    @ApiModel(AclEntryModel.class)
    public Page<AclEntry> findPrincipalAclEntries(HttpServletRequest request,
                                                  @PathVariable String principal, @PathVariable String resourceType,
                                                  String resourceId, String permission, String variables) {
        ResourceModel resourceModel = new ResourceModel();
        resourceModel.setType(resourceType);
        resourceModel.setId(resourceId);
        Map<String, Object> variablesSearchParams = StringUtils.hasText(variables) ? buildObjectFromJsonPayload(variables, Map.class) : MapUtils.EMPTY_MAP;

        return aclManager.findPrincipalEntries(new AclPrincipalModel(principal), resourceModel, new PermissionModel(permission), buildPageableFromRequest(request), SearchFilter.SearchFilterBuilder.build(variablesSearchParams));
    }

    @GetMapping("accessible/{principal}/{resourceType}/{resourceId}")
    public NamedPrimitive<Boolean> isAccessible(@PathVariable String principal, @PathVariable String resourceType, @PathVariable String resourceId, @RequestParam(name = "permission", required = false) String permission) {
        ResourceModel resourceModel = new ResourceModel();
        resourceModel.setType(resourceType);
        resourceModel.setId(resourceId);
        boolean accessible = aclManager.isAccessible(resourceModel, new AclPrincipalModel(principal), new PermissionModel(permission));
        return new NamedPrimitive<>("accessible", accessible);
    }

    @Autowired
    public void setAclManager(AclManager aclManager) {
        this.aclManager = aclManager;
    }
}
