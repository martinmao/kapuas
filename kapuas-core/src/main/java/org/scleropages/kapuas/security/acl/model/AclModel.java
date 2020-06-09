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
package org.scleropages.kapuas.security.acl.model;

import org.scleropages.crud.types.EntryList;
import org.scleropages.kapuas.openapi.annotation.ApiIgnore;
import org.scleropages.kapuas.openapi.annotation.ApiModel;
import org.scleropages.kapuas.security.acl.Acl;
import org.scleropages.kapuas.security.acl.AclEntry;
import org.scleropages.kapuas.security.acl.AclPrincipal;
import org.scleropages.kapuas.security.acl.Resource;

import java.beans.Transient;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public class AclModel implements Acl {

    private Long id;
    private String resourceId;
    private String resourceType;
    private Resource resource;
    private List<AclPrincipal> owners;
    private String tag;
    private List<AclEntry> aclEntries;
    private Map<String, Object> variables;
    private EntryList<String, Object> variableEntries;


    @Transient
    public Long getId() {
        return id;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getResourceType() {
        return resourceType;
    }

    @ApiModel(ResourceModel.class)
    @ApiIgnore({Page.class,Info.class})
    public Resource getResource() {
        return resource;
    }

    @ApiModel(AclPrincipalModel.class)
    @ApiIgnore({Page.class})
    public List<AclPrincipal> getOwners() {
        return owners;
    }

    public String getTag() {
        return tag;
    }

    @ApiModel(AclEntryModel.class)
    @ApiIgnore({Page.class, Info.class})
    public List<AclEntry> getAclEntries() {
        return aclEntries;
    }

    @Transient
    public Map<String, Object> getVariables() {
        return variables;
    }

    @ApiIgnore({Page.class})
    public EntryList<String, Object> getVariableEntries() {
        return variableEntries;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public void setOwners(List<AclPrincipal> owners) {
        this.owners = owners;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setAclEntries(List<AclEntry> aclEntries) {
        this.aclEntries = aclEntries;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
        this.variableEntries = new EntryList().fromMap(variables);
    }

    public void setVariableEntries(EntryList<String, Object> variableEntries) {
        this.variableEntries = variableEntries;
        this.variables = variableEntries.toMap();
    }

    @Override
    public Serializable id() {
        return id;
    }

    @Override
    public Resource resource() {
        if (null == resource) {
            ResourceModel model = new ResourceModel();
            model.setId(resourceId);
            model.setTag(tag);
            model.setType(resourceType);
            resource = model;
        }
        return resource;
    }

    @Override
    public List<AclPrincipal> owners() {
        return owners;
    }

    @Override
    public String tag() {
        return tag;
    }

    @Override
    public List<? extends AclEntry> entries() {
        return aclEntries;
    }

    public static interface Page {
    }

    public static interface Info {

    }
}
