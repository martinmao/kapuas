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
 * 用户描述{@link Application} 对外提供的一组api接口供外部调用
 *
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public class Api {

    private Long id;
    private String apiId;
    private String tag;
    private String docUrl;
    private String description;
    private Long applicationId;

    @NotNull(groups = {UpdateModel.class})
    @Null(groups = {CreateModel.class})
    public Long getId() {
        return id;
    }

    @NotBlank(groups = {CreateModel.class})
    public String getApiId() {
        return apiId;
    }

    @NotBlank(groups = {CreateModel.class})
    public String getTag() {
        return tag;
    }

    @NotBlank(groups = {CreateModel.class})
    public String getDocUrl() {
        return docUrl;
    }

    @NotBlank(groups = {CreateModel.class})
    public String getDescription() {
        return description;
    }

    @Null(groups = {UpdateModel.class})
    @NotNull(groups = {CreateModel.class})
    public Long getApplicationId() {
        return applicationId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setApiId(String apiId) {
        this.apiId = apiId;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setDocUrl(String docUrl) {
        this.docUrl = docUrl;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public static interface CreateModel {
    }

    public static interface UpdateModel {
    }
}
