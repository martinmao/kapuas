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
package org.scleropages.maldini.security.authc.provider;


import org.scleropages.maldini.AuthenticationDetails;

import java.io.Serializable;
import java.util.Date;

/**
 * SPI辅助类
 * 加载必要信息交给realm进行认证，以及认证成功后的必要参数设置
 *
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public class Authenticating implements Serializable{

    /**
     * The principals identifying the account
     */
    private final Object principal;
    /**
     * The credentials verifying the account principals.
     */
    private final Object credentials;
    /**
     * Any salt used in hashing the credentials.
     */
    private final byte[] credentialsSalt;

    /*!!!used for principal or credentials check before realm authentication.*/
    private final boolean enabled;
    private final boolean expired;
    private final boolean locked;
    private final boolean credentialsExpired;

    /*required settings if realm authentication if success.*/
    private AuthenticationDetails details;
    /*其他自定义参数*/
    private Object context;


    /**
     * 加载必要信息交给realm进行认证，以及认证成功后的必要参数设置
     *
     * @param principal          账号主体
     * @param credentials        用于验证账号主体(证书或密码)
     * @param credentialsSalt    credentials 盐值(hashes处理时加入的盐值)，如果没有加盐处理则返回空
     * @param enabled            账号主体是否可用
     * @param expired            账号主体是否过期
     * @param locked             账号主体是否锁定
     * @param credentialsExpired credentials是否过期
     * @param details            账号主体详情
     */
    public Authenticating(Object principal, Object credentials, byte[] credentialsSalt, boolean enabled, boolean expired, boolean locked, boolean credentialsExpired, AuthenticationDetails details) {
        this.principal = principal;
        this.credentials = credentials;
        this.credentialsExpired = credentialsExpired;
        this.enabled = enabled;
        this.expired = expired;
        this.locked = locked;
        this.credentialsSalt = credentialsSalt;
        this.details = details;
    }

    /**
     * 加载必要信息交给realm进行认证，以及认证成功后的必要参数设置（principal状态均可用前提下使用）
     *
     * @param principal       账号主体
     * @param credentials     用于验证账号主体(证书或密码)
     * @param credentialsSalt credentials 盐值(hashes处理时加入的盐值)，如果没有加盐处理则返回空
     * @param details         账号主体详情
     */
    public Authenticating(String principal, Object credentials, byte[] credentialsSalt, AuthenticationDetails details) {
        this(principal, credentials, credentialsSalt, true, false, false, false, details);
    }


    public boolean isSamePrincipalFromToken() {
        return principal == null;
    }

    public boolean isCredentialsHashed() {
        return credentialsSalt != null;
    }

    public Object getPrincipal() {
        return principal;
    }

    public byte[] getCredentialsSalt() {
        return credentialsSalt;
    }

    public Object getCredentials() {
        return credentials;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isExpired() {
        return expired;
    }

    public boolean isLocked() {
        return locked;
    }

    public boolean isCredentialsExpired() {
        return credentialsExpired;
    }

    public AuthenticationDetails getDetails() {
        return details;
    }

    public void setDetails(AuthenticationDetails details) {
        this.details = details;
    }

    public void setContext(Object context) {
        this.context = context;
    }

    public Object getContext() {
        return context;
    }

    public Authenticated done(Object principal, String host, Serializable realm) {
        return new Authenticated() {
            @Override
            public Object principal() {
                return principal;
            }

            @Override
            public String host() {
                return host;
            }

            @Override
            public Date time() {
                return new Date();
            }

            @Override
            public AuthenticationDetails details() {
                return details;
            }

            @Override
            public Serializable realm() {
                return realm;
            }
        };
    }

}
