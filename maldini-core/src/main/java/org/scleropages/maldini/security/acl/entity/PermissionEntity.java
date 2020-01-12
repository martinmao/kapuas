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

import org.apache.commons.lang3.StringUtils;
import org.scleropages.crud.orm.jpa.entity.IdEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Objects;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
@Entity
@Table(name = "sec_acl_permission")
@SequenceGenerator(name = "sec_acl_permission_id", sequenceName = "seq_sec_acl_permission", allocationSize = IdEntity.SEQ_DEFAULT_ALLOCATION_SIZE, initialValue = IdEntity.SEQ_DEFAULT_INITIAL_VALUE)
public class PermissionEntity extends IdEntity {

    public static final String NOT_SUPPORT = "N/A";

    public static final String PERMISSION_EXTENSION_SEPARATOR = ",";

    private String resourceType;
    private Long resourceTypeId;

    //!!!! name tag 在创建权限模型时如果未设置，即当前资源选择粗粒度权acl模型，acl可直接关联 principal，即不存在permission层
    private String name = NOT_SUPPORT;
    private String extension;
    private String tag = "不支持";

    @Column(name = "resource_type", nullable = false)
    public String getResourceType() {
        return resourceType;
    }

    @Column(name = "resource_type_id", nullable = false)
    public Long getResourceTypeId() {
        return resourceTypeId;
    }


    @Column(name = "extends_")
    public String getExtension() {
        return extension;
    }

    @Column(name = "name_", nullable = false)
    public String getName() {
        return name;
    }

    @Column(name = "tag_", nullable = false)
    public String getTag() {
        return tag;
    }


    /**
     * return true if current permission coarse-grained model.
     *
     * @return
     */
    @Transient
    public boolean isNotSupport() {
        return Objects.equals(name, NOT_SUPPORT);
    }

    /**
     * return true if current permission inherit include given entity.
     *
     * @return
     */
    @Transient
    public boolean isInheritInclude(PermissionEntity entity) {
        return StringUtils.contains(getExtension(), entity.getName());
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public void setResourceTypeId(Long resourceTypeId) {
        this.resourceTypeId = resourceTypeId;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
