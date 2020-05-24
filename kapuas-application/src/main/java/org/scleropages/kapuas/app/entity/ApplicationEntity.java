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

import org.scleropages.crud.dao.orm.jpa.entity.IdEntity;
import org.scleropages.crud.types.Available;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
@Entity
@Table(name = "app_info")
@SequenceGenerator(name = "app_info_id", sequenceName = "seq_app_info", allocationSize = IdEntity.SEQ_DEFAULT_ALLOCATION_SIZE, initialValue = IdEntity.SEQ_DEFAULT_INITIAL_VALUE)
public class ApplicationEntity extends IdEntity implements Available {

    private String appId;
    private String name;
    private String tag;
    private String apiGateway;
    private String contact;
    private String contactNumber;
    private String description;
    private Boolean enabled;

    @Column(name = "app_id", nullable = false, unique = true)
    public String getAppId() {
        return appId;
    }

    @Column(name = "name_", nullable = false, unique = true)
    public String getName() {
        return name;
    }

    @Column(name = "tag_", nullable = false)
    public String getTag() {
        return tag;
    }

    @Column(name = "contact_", nullable = false)
    public String getContact() {
        return contact;
    }

    @Column(name = "api_gateway", nullable = false)
    public String getApiGateway() {
        return apiGateway;
    }

    @Column(name = "contact_number", nullable = false)
    public String getContactNumber() {
        return contactNumber;
    }

    @Column(name = "desc_", nullable = false)
    public String getDescription() {
        return description;
    }

    @Column(name = "enabled_", nullable = false)
    public Boolean getEnabled() {
        return enabled;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public void setApiGateway(String apiGateway) {
        this.apiGateway = apiGateway;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void enable() {
        setEnabled(true);
    }

    @Override
    public void disable() {
        setEnabled(false);
    }

    @Override
    public boolean availableState() {
        return getEnabled();
    }
}
