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
package org.scleropages.maldini.security.acl.entity;

import org.scleropages.crud.orm.jpa.entity.IdEntity;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
@MappedSuperclass
public abstract class AbstractAclEntity extends IdEntity {

    private String resourceId;
    private String resourceType;
    private String resourceTag;
    private Long resourceTypeId;

    private AclPrincipalEntity owner;

    @Column(name = "resource_id", nullable = false)
    public String getResourceId() {
        return resourceId;
    }

    @Column(name = "resource_type", nullable = false)
    public String getResourceType() {
        return resourceType;
    }

    @Column(name = "resource_tag", nullable = false)
    public String getResourceTag() {
        return resourceTag;
    }

    @Column(name = "resource_type_id", nullable = false)
    public Long getResourceTypeId() {
        return resourceTypeId;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sec_acl_principal_id", nullable = false)
    public AclPrincipalEntity getOwner() {
        return owner;
    }


    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public void setResourceTag(String resourceTag) {
        this.resourceTag = resourceTag;
    }

    public void setResourceTypeId(Long resourceTypeId) {
        this.resourceTypeId = resourceTypeId;
    }

    public void setOwner(AclPrincipalEntity owner) {
        this.owner = owner;
    }
}
