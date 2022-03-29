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
package org.scleropages.kapuas.tenant.user.model;

import org.scleropages.kapuas.tenant.model.Tenant;

/**
 * 描述租户的一个用户组
 *
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public class Group {

    /**
     * 唯一标识
     */
    private Long id;
    /**
     * 组名称
     */
    private String name;
    /**
     * 组描述
     */
    private String description;
    /**
     * 上级组
     */
    private Group parentGroup;
    /**
     * 所属租户
     */
    private Tenant tenant;


    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Group getParentGroup() {
        return parentGroup;
    }

    public Tenant getTenant() {
        return tenant;
    }


    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setParentGroup(Group parentGroup) {
        this.parentGroup = parentGroup;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }
}
