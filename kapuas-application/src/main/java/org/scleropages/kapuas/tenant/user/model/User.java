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

import org.scleropages.kapuas.security.acl.AclPrincipal;
import org.scleropages.kapuas.tenant.model.Tenant;

import java.io.Serializable;
import java.util.List;

/**
 * 描述租户的一个用户
 *
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public class User implements AclPrincipal {
    
    /**
     * 唯一标识
     */
    private Long id;
    /**
     * 姓名
     */
    private String name;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 联系电话
     */
    private String tel;
    /**
     * 所属组列表
     */
    private List<Group> groups;
    /**
     * 主管列表
     */
    private List<User> managers;
    /**
     * 所属租户
     */
    private Tenant tenant;

    @Override
    public Serializable id() {
        return id;
    }

    @Override
    public String tag() {
        return name;
    }
}
