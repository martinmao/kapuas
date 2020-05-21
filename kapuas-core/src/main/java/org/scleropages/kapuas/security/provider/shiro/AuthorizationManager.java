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
package org.scleropages.kapuas.security.provider.shiro;

import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.scleropages.core.util.GenericTypes;
import org.scleropages.kapuas.security.authc.provider.Authenticated;
import org.springframework.util.ClassUtils;

/**
 * 授权管理器，实现类需根据认证主体加载主体的权限信息供shiro进行鉴权. shiro realm 根据实现类的{@link #supports(Authenticated)}返回结果来执行
 * {@link #populateAuthorizationInfo(SimpleAuthorizationInfo, Authenticated, Object)}完成必要的权限信息加载.这些权限信息会被shiro缓存起来，
 * 如果权限数据量不大以及确实必须的情况下才应提供该实现(例如菜单，url等通用权限的加载)，而具体业务权限否则应通过 {@link org.scleropages.kapuas.security.acl.AclManager} 来进行鉴权.
 *
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public interface AuthorizationManager<T> {


    /**
     * Load current principal permissions and populate to {@link org.apache.shiro.authc.SimpleAuthenticationInfo}
     *
     * @param info
     * @param authentication
     * @param details
     */
    void populateAuthorizationInfo(SimpleAuthorizationInfo info, Authenticated authentication, T details);

    /**
     * Return true if this implementation support current principal. by default use generic type matches.
     *
     * @param authenticated
     * @return
     */
    default boolean supports(Authenticated authenticated) {
        Class genericType = GenericTypes.getClassGenericType(getClass(), AuthorizationManager.class, 0);
        return ClassUtils.isAssignable(genericType, authenticated.details().getClass());
    }
}
