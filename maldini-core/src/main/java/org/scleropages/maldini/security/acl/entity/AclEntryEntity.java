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
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
@Entity
@Table(name = "sec_acl_entries",
        uniqueConstraints = @UniqueConstraint(columnNames = {"sec_acl_id", "sec_acl_principal_id", "sec_acl_permission_id"}),
        indexes = {@Index(columnList = "principal_,resource_type_id,resource_id")})
@SequenceGenerator(name = "sec_acl_entries_id", sequenceName = "seq_sec_acl_entries", allocationSize = IdEntity.SEQ_DEFAULT_ALLOCATION_SIZE, initialValue = IdEntity.SEQ_DEFAULT_INITIAL_VALUE)
public class AclEntryEntity extends AbstractAclEntryEntity {

    private String permissionName;

    private PermissionEntity permission;

    @Column(name = "permission_", nullable = false)
    public String getPermissionName() {
        return permissionName;
    }


    @ManyToOne(fetch = FetchType.LAZY, targetEntity = AclEntity.class)
    @JoinColumn(name = "sec_acl_id", nullable = false)
    public AclEntity getAcl() {
        return (AclEntity) super.getAcl();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sec_acl_permission_id", nullable = false)
    public PermissionEntity getPermission() {
        return permission;
    }


    public void setPermissionName(String permissionName) {
        this.permissionName = permissionName;
    }

    public void setPermission(PermissionEntity permission) {
        this.permission = permission;
    }
}
