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
import org.scleropages.maldini.security.acl.Resource;

import java.io.Serializable;
import java.util.List;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public class AclModel implements Acl {

    private Serializable id;
    private String resourceId;
    private String resourceType;
    private Resource resource;
    private List<AclPrincipal> owners;
    private String tag;
    private List<AclEntry> entries;


    public Serializable getId() {
        return id;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public Resource getResource() {
        return resource;
    }

    public List<AclPrincipal> getOwners() {
        return owners;
    }

    public String getTag() {
        return tag;
    }

    public List<AclEntry> getEntries() {
        return entries;
    }


    public void setId(Serializable id) {
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

    public void setEntries(List<AclEntry> entries) {
        this.entries = entries;
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
        return entries;
    }
}
