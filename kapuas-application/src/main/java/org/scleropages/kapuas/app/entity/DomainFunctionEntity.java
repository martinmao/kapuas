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
package org.scleropages.kapuas.app.entity;

import org.scleropages.crud.dao.orm.jpa.entity.EntityAware;
import org.scleropages.crud.dao.orm.jpa.entity.IdEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
@Entity
@Table(name = "app_domain_func", uniqueConstraints = @UniqueConstraint(columnNames = {"full_name", "app_api_id"}))
@SequenceGenerator(name = "app_domain_func_id", sequenceName = "seq_app_domain_func", allocationSize = IdEntity.SEQ_DEFAULT_ALLOCATION_SIZE, initialValue = IdEntity.SEQ_DEFAULT_INITIAL_VALUE)
public class DomainFunctionEntity extends IdEntity implements EntityAware<IdEntity> {

    private String name;
    private String fullName;
    private String tag;
    private String description;
    private String docUrl;
    private Boolean enabled;
    private ApiEntity apiEntity;
    private DomainEntity domainEntity;
    private String appId;

    @Column(name = "name_", nullable = false)
    public String getName() {
        return name;
    }

    @Column(name = "full_name", nullable = false)
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
    @JoinColumn(name = "app_api_id", nullable = false)
    public ApiEntity getApiEntity() {
        return apiEntity;
    }


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_domain_id", nullable = false)
    public DomainEntity getDomainEntity() {
        return domainEntity;
    }

    @Column(name = "app_id", nullable = false)
    public String getAppId() {
        return appId;
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

    public void setApiEntity(ApiEntity apiEntity) {
        this.apiEntity = apiEntity;
    }

    public void setDomainEntity(DomainEntity domainEntity) {
        this.domainEntity = domainEntity;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    @Override
    public void setEntity(IdEntity idEntity) {
        if (idEntity instanceof ApiEntity)
            setApiEntity((ApiEntity) idEntity);
        else if (idEntity instanceof DomainEntity)
            setDomainEntity((DomainEntity) idEntity);
        else
            throw new IllegalArgumentException("unsupported payload type: " + idEntity.getClass().getName());
    }
}
