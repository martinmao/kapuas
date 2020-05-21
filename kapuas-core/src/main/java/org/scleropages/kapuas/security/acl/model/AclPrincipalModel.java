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
package org.scleropages.kapuas.security.acl.model;

import org.scleropages.kapuas.security.acl.AclPrincipal;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public class AclPrincipalModel implements AclPrincipal {

    private Long id;

    private String name;

    private String tag;

    public AclPrincipalModel(String name) {
        this.name = name;
    }

    public AclPrincipalModel() {
    }

    public Long getId() {
        return id;
    }

    @NotBlank(groups = {CreateModel.class, CreateAclModel.class})
    public String getName() {
        return name;
    }


    @NotBlank(groups = CreateModel.class)
    public String getTag() {
        return tag;
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

    @Override
    public Serializable id() {
        return name;
    }

    @Override
    public String tag() {
        return tag;
    }

    public interface CreateModel {

    }

    public interface CreateAclModel {

    }
}
