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
package org.scleropages.kapuas.tenant.model;

import org.scleropages.kapuas.security.acl.AclPrincipal;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 描述租户应用实例中的一个角色
 *
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public class Role implements AclPrincipal {

    /**
     * 唯一标识
     */
    private Long id;
    /**
     * 角色名称
     */
    private String name;
    /**
     * 角色说明
     */
    private String description;
    /**
     * 数据访问策略
     */
    private AccessControlPolicy accessControlPolicy;
    /**
     * 所属的应用实例.
     */
    private TenantApp tenantApp;


    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public AccessControlPolicy getAccessControlPolicy() {
        return accessControlPolicy;
    }

    public TenantApp getTenantApp() {
        return tenantApp;
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

    public void setAccessControlPolicy(AccessControlPolicy accessControlPolicy) {
        this.accessControlPolicy = accessControlPolicy;
    }

    public void setTenantApp(TenantApp tenantApp) {
        this.tenantApp = tenantApp;
    }

    @Override
    public Serializable id() {
        return id;
    }

    @Override
    public String tag() {
        return name;
    }

    /**
     * 数据访问策略，系统基于该策略创建访问控制列表.
     */
    enum AccessControlPolicy {

        SELF(1, "自己", "只能访问自己和下属的数据"),
        GROUP(2, "所属部门", "能访问自己、下属、自己所属部门的数据"),
        GROUP_S(3, "所属部门及下级部门", "能访问自己、下属、自己所属部门及下级部门的数据"),
        ALL(4, "全公司", "能访问全公司的数据");

        private final int ordinal;
        private final String tag;
        private final String desc;

        AccessControlPolicy(int ordinal, String tag, String desc) {
            this.ordinal = ordinal;
            this.tag = tag;
            this.desc = desc;
        }

        public int getOrdinal() {
            return ordinal;
        }

        public String getTag() {
            return tag;
        }

        public String getDesc() {
            return desc;
        }


        private static final Map<String, AccessControlPolicy> nameMappings = new HashMap<>();
        private static final Map<Integer, AccessControlPolicy> ordinalMappings = new HashMap<>();

        static {
            for (AccessControlPolicy accessControlPolicy : AccessControlPolicy.values()) {
                nameMappings.put(accessControlPolicy.name(), accessControlPolicy);
                ordinalMappings.put(accessControlPolicy.getOrdinal(), accessControlPolicy);
            }
        }


        public static AccessControlPolicy getByName(String name) {
            return (name != null ? nameMappings.get(name) : null);
        }

        public static AccessControlPolicy getByOrdinal(int ordinal) {
            return ordinalMappings.get(ordinal);
        }
    }
}
