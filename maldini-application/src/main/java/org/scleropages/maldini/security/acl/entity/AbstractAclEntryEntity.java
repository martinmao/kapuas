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

import org.scleropages.crud.dao.orm.jpa.entity.IdEntity;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
@MappedSuperclass
public class AbstractAclEntryEntity extends IdEntity {

    /**
     * FOLLOW COLUMN NAME MUST NEVER CHANGED.BECAUSE REPOSITORY USED HARD CODE WRITTEN.
     */
    protected static final String PRINCIPAL_COLUMN = "principal_";

    private String aclPrincipalName;
    private Long resourceTypeId;
    private String resourceId;

    private AclPrincipalEntity grant;
    private AbstractAclEntity acl;


    @Column(name = PRINCIPAL_COLUMN, nullable = false)
    public String getAclPrincipalName() {
        return aclPrincipalName;
    }

    @Column(name = "resource_type_id", nullable = false)
    public Long getResourceTypeId() {
        return resourceTypeId;
    }

    @Column(name = "resource_id", nullable = false)
    public String getResourceId() {
        return resourceId;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sec_acl_principal_id", nullable = false)
    public AclPrincipalEntity getGrant() {
        return grant;
    }

    @Transient
    public AbstractAclEntity getAcl() {
        return acl;
    }

    public void setAclPrincipalName(String aclPrincipalName) {
        this.aclPrincipalName = aclPrincipalName;
    }

    public void setResourceTypeId(Long resourceTypeId) {
        this.resourceTypeId = resourceTypeId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public void setGrant(AclPrincipalEntity grant) {
        this.grant = grant;
    }

    public void setAcl(AbstractAclEntity acl) {
        this.acl = acl;
    }
}
