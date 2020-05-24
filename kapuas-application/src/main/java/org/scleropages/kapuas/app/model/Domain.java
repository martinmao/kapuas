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
package org.scleropages.kapuas.app.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

/**
 * 用于描述领域，从业务角度将一组业务功能 {@link DomainFunction} 聚合在一起作为一种能力对外提供服务
 *
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public class Domain {

    private Long id;
    private String name;
    private String namespace;
    private String tag;
    private String description;
    private String docUrl;
    private Boolean enabled;
    private Long parentId;

    @NotNull(groups = {UpdateModel.class})
    @Null(groups = {CreateModel.class})
    public Long getId() {
        return id;
    }

    @NotBlank(groups = {CreateModel.class})
    public String getName() {
        return name;
    }

    @Null(groups = {CreateModel.class})
    @Null(groups = {UpdateModel.class})
    public String getNamespace() {
        return namespace;
    }

    @NotBlank(groups = {CreateModel.class})
    public String getTag() {
        return tag;
    }

    @NotBlank(groups = {CreateModel.class})
    public String getDescription() {
        return description;
    }

    @NotBlank(groups = {CreateModel.class})
    public String getDocUrl() {
        return docUrl;
    }

    @Null
    public Boolean getEnabled() {
        return enabled;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
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

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public static interface CreateModel {
    }

    public static interface UpdateModel {
    }
}
