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
package org.scleropages.maldini.security.acl.model;

import org.scleropages.maldini.security.acl.Acl;
import org.scleropages.maldini.security.acl.AclEntry;
import org.scleropages.maldini.security.acl.AclPrincipal;
import org.scleropages.maldini.security.acl.Permission;
import org.scleropages.maldini.security.acl.Resource;

import java.io.Serializable;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public class AclEntryModel implements AclEntry {

    private Long id;
    private Acl acl;
    private AclPrincipal grant;
    private Permission permission;
    private Resource resource;

    private final String aclPrincipalName;
    private final String permissionName;
    private final String resourceType;
    private final String resourceId;


    public AclEntryModel(Long id, String aclPrincipalName, String permissionName, String resourceType, String resourceId) {
        this.id = id;
        this.aclPrincipalName = aclPrincipalName;
        this.permissionName = permissionName;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public AclEntryModel() {
        this(null, null, null, null, null);
    }

    public Long getId() {
        return id;
    }

    public Acl getAcl() {
        return acl;
    }

    public AclPrincipal getGrant() {
        return grant;
    }

    public Permission getPermission() {
        return permission;
    }

    public Resource getResource() {
        return resource;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setAcl(Acl acl) {
        this.acl = acl;
    }

    public void setGrant(AclPrincipal grant) {
        this.grant = grant;
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }


    public String getAclPrincipalName() {
        return aclPrincipalName;
    }

    public String getPermissionName() {
        return permissionName;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }

    @Override
    public Serializable id() {
        return id;
    }

    @Override
    public Acl acl() {
        if (acl == null) {
            AclModel aclModel = new AclModel();
            aclModel.setResourceId(resourceId);
            aclModel.setResourceType(resourceType);
            acl = aclModel;
        }
        return acl;
    }

    @Override
    public AclPrincipal grant() {
        if (null == grant) {
            AclPrincipalModel aclPrincipalModel = new AclPrincipalModel();
            aclPrincipalModel.setName(aclPrincipalName);
            grant = aclPrincipalModel;
        }
        return grant;
    }

    @Override
    public Permission permission() {
        if (null == permission) {
            PermissionModel permissionModel = new PermissionModel();
            permissionModel.setName(permissionName);
            permission = permissionModel;
        }
        return permission;
    }

    @Override
    public Resource resource() {
        return resource;
    }
}
