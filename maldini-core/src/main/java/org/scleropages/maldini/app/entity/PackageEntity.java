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

import org.scleropages.crud.dao.orm.jpa.entity.IdEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
@Entity
@Table(name = "app_pkg")
@SequenceGenerator(name = "app_pkg_id", sequenceName = "seq_app_pkg", allocationSize = IdEntity.SEQ_DEFAULT_ALLOCATION_SIZE, initialValue = IdEntity.SEQ_DEFAULT_INITIAL_VALUE)
public class PackageEntity extends IdEntity {

    private String namespace;
    private String tag;
    private String description;
    private String docUrl;
    private Boolean enabled;

    @Column(name = "ns_", nullable = false, unique = true)
    public String getNamespace() {
        return namespace;
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

    public void setNamespace(String namespace) {
        this.namespace = namespace;
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
}
