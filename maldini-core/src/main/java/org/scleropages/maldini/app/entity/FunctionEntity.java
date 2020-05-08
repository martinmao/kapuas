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
package org.scleropages.maldini.app.entity;

import org.scleropages.crud.dao.orm.jpa.entity.EntityAware;
import org.scleropages.crud.dao.orm.jpa.entity.IdEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
@Entity
@Table(name = "app_func")
@SequenceGenerator(name = "app_func_id", sequenceName = "seq_app_func", allocationSize = IdEntity.SEQ_DEFAULT_ALLOCATION_SIZE, initialValue = IdEntity.SEQ_DEFAULT_INITIAL_VALUE)
public class FunctionEntity extends IdEntity implements EntityAware<IdEntity> {

    private String name;
    private String fullName;
    private String tag;
    private String description;
    private String docUrl;
    private Boolean enabled;
    private ApplicationEntity applicationEntity;
    private PackageEntity packageEntity;

    @Column(name = "name_", nullable = false)
    public String getName() {
        return name;
    }

    @Column(name = "full_name", nullable = false, unique = true)
    public String getFullName() {
        return fullName;
    }

    @Column(name = "tag_", nullable = false)
    public String getTag() {
        return tag;
    }

    @Column(name = "desc_", nullable = false)
    public String getDescription() {
        return description;
    }

    @Column(name = "doc_url", nullable = false)
    public String getDocUrl() {
        return docUrl;
    }

    @Column(name = "enabled_", nullable = false)
    public Boolean getEnabled() {
        return enabled;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_info_id", nullable = false)
    public ApplicationEntity getApplicationEntity() {
        return applicationEntity;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_pkg_id", nullable = false)
    public PackageEntity getPackageEntity() {
        return packageEntity;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDocUrl(String docUrl) {
        this.docUrl = docUrl;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public void setApplicationEntity(ApplicationEntity applicationEntity) {
        this.applicationEntity = applicationEntity;
    }

    public void setPackageEntity(PackageEntity packageEntity) {
        this.packageEntity = packageEntity;
    }

    @Override
    public void setEntity(IdEntity idEntity) {
        if (idEntity instanceof ApplicationEntity)
            setApplicationEntity((ApplicationEntity) idEntity);
        else if (idEntity instanceof PackageEntity)
            setPackageEntity((PackageEntity) idEntity);
        else
            throw new IllegalArgumentException("unsupported payload type: " + idEntity.getClass().getName());
    }
}
