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

import org.scleropages.crud.types.Available;
import org.scleropages.kapuas.security.AuthenticationDetails;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.io.Serializable;

/**
 * 用于描述一个应用，技术上application作为一个单独的进程单元，提供一组接口供外部使用，或本身
 * 作为消费者对其他application有功能访问的需求.
 *
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public class Application implements AuthenticationDetails {

    private Long id;
    private String name;
    private String tag;
    private String description;
    private String contact;
    private String apiGateway;
    private String contactNumber;
    private Boolean enabled;
    private String appId;
    private String appSecret;

    @NotNull(groups = {UpdateModel.class})
    @Null(groups = {CreateModel.class})
    public Long getId() {
        return id;
    }

    @NotBlank(groups = {CreateModel.class})
    public String getName() {
        return name;
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
    public String getContact() {
        return contact;
    }

    @NotBlank(groups = {CreateModel.class})
    public String getApiGateway() {
        return apiGateway;
    }

    @NotBlank(groups = {CreateModel.class})
    public String getContactNumber() {
        return contactNumber;
    }

    @Null
    public Boolean getEnabled() {
        return enabled;
    }

    @Null
    public String getAppId() {
        return appId;
    }

    @Null
    public String getAppSecret() {
        return appSecret;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public void setApiGateway(String apiGateway) {
        this.apiGateway = apiGateway;
    }


    @Override
    public String tag() {
        return name;
    }

    @Override
    public Serializable identifier() {
        return id;
    }

    @Override
    public Serializable type() {
        return getClass();
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    public static interface CreateModel {
    }

    public static interface UpdateModel {
    }
}
