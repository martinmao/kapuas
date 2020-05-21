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

import org.apache.commons.collections.CollectionUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.HostAuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.scleropages.kapuas.security.AuthenticationDetails;
import org.scleropages.kapuas.security.SecurityBizException;
import org.scleropages.kapuas.security.authc.provider.Authenticated;
import org.scleropages.kapuas.security.authc.provider.Authenticating;
import org.scleropages.kapuas.security.authc.token.client.AbstractAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ResolvableType;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * abstract shiro realm，将realm与认证token绑定机制执行认证，允许多个realm配置（针对不同token类型，如jwt，oauth，三方认证系统），但要求每一种realm只会对一种token提供认证.
 * 该类设计原则上需在认证前对账号状态进行检查 {@link #postCheckAuthenticationToken(AuthenticationToken, Authenticating)}.
 * <p>
 * <p>
 * 但shiro默认实现思路（多realm情况下，单realm不存在该问题），realm中的 {@link #doGetAuthenticationInfo(AuthenticationToken)}抛出异常无效，例如：账号不存在，或账号异常(禁用，密码过期等)无法传播抛出的异常到前端
 * ({@link org.apache.shiro.authc.pam.ModularRealmAuthenticator#doMultiRealmAuthentication(Collection, AuthenticationToken)}中会捕获异常尝试下一个realm，这样异常无法反馈给客户端)
 * <p>
 * 当前实现需配合自定义 {@link org.apache.shiro.authc.pam.AuthenticationStrategy#afterAttempt(Realm, AuthenticationToken, AuthenticationInfo, AuthenticationInfo, Throwable)}
 * 遇到realm抛出的 {@link AuthenticationException}类型异常. 直接传播（throw）出去，不继续尝试下一个realm. 通过自定义{@link org.apache.shiro.authc.pam.AuthenticationStrategy}实现.
 * <p>
 * 当前实现不适用与非集中式账户系统(db->ldap->三方账户系统，需轮流realm检查匹配.)，可以使用shiro 默认的策略
 *
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public abstract class AbstractShiroRealm<T extends AuthenticationToken> extends AuthorizingRealm {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private AuthenticationTokenManager authenticationTokenManager;

    private List<AuthorizationManager> authorizationManagers = Collections.emptyList();

    private Class supportTokenType;

    @Override
    protected void onInit() {
        //do somethings
        super.onInit();
    }

    public AbstractShiroRealm() {
        try {
            this.supportTokenType = ResolvableType.forClass(AbstractShiroRealm.class, getClass()).getGeneric(0).resolve();
        } catch (Exception e) {
            logger.warn("Can't determine generic type of support token. if you see this message make sure your realm already implements supports(AuthenticationToken token) method.");
        }
    }

    public AbstractShiroRealm(AuthenticationTokenManager authenticationTokenManager) {
        this();
        setAuthenticationTokenManager(authenticationTokenManager);
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {

        Authenticated authenticated = (Authenticated) principals.getPrimaryPrincipal();
        SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
        authorizationManagers.stream().forEachOrdered(authorizationManager -> {
            if (authorizationManager.supports(authenticated)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Using... {} to populate AuthorizationInfo", authorizationManager.getClass().getSimpleName());
                }
                authorizationManager.populateAuthorizationInfo(simpleAuthorizationInfo, authenticated, authenticated.details());
            }
        });
        if (logger.isDebugEnabled() &&
                (CollectionUtils.isNotEmpty(simpleAuthorizationInfo.getRoles()) || CollectionUtils.isNotEmpty(simpleAuthorizationInfo.getStringPermissions()))) {
            logger.info("successfully populate [{}] authorization info: with roles:{} and permissions: {}.",
                    authenticated.principal(), simpleAuthorizationInfo.getRoles(), simpleAuthorizationInfo.getStringPermissions());
        } else if (logger.isDebugEnabled()) {
            logger.warn("no authorization info found or no matches AuthorizationManager found.");
        }
        return simpleAuthorizationInfo;
    }


    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        if (logger.isDebugEnabled()) {
            logger.debug("accept a authentication request for token [{}]", token);
        }

        T actualToken = (T) token;
        preCheckAuthenticationToken(actualToken);
        Authenticating authenticating = authenticationTokenManager.find(actualToken);
        postCheckAuthenticationToken(actualToken, authenticating);


        SimpleAuthenticationInfo simpleAuthenticationInfo = new SimpleAuthenticationInfo(createAuthenticated(actualToken, authenticating), authenticating.getCredentials(),
                authenticating.isCredentialsHashed() ?
                        ByteSource.Util.bytes(authenticating.getCredentialsSalt()) : null, getName());
        return simpleAuthenticationInfo;
    }

    /*!!覆盖默认实现方法*/

    /**
     * 覆盖shiro默认实现，对于远程session存储，反序列化后为一个全新的对象
     *
     * @param principals
     * @return
     */
    @Override
    protected Object getAuthorizationCacheKey(PrincipalCollection principals) {
        Authenticated authentication = (Authenticated) principals.getPrimaryPrincipal();
        return String.valueOf(authentication.principal()) + authentication.time();
    }


    /**
     * 覆盖默认实现，对于已认证过的（如三方认证）token，则不进行 credentials 匹配检查
     *
     * @param token
     * @param info
     * @throws AuthenticationException
     */
    @Override
    protected void assertCredentialsMatch(AuthenticationToken token, AuthenticationInfo info)
            throws AuthenticationException {
        if (!isPreAuthenticated((T) token))
            super.assertCredentialsMatch(token, info);
    }


    /*!!子类扩展点*/

    /**
     * check {@link AuthenticationToken} before authentication action.
     *
     * @param token
     */
    protected void preCheckAuthenticationToken(final T token) {
        Object principal = token.getPrincipal();
        Assert.notNull(principal, "principal must not be null.");

        if (principal instanceof String) {
            Assert.hasText(principal.toString(), "principal must not be empty text.");
        }
    }


    /**
     * return true if current token is already authenticated by other way(such as authenticated by third-party oauth server).
     * the realm will not check credentials matches(check only the token is exists in {@link #postCheckAuthenticationToken(AuthenticationToken, Authenticating)}).
     *
     * @return
     */
    protected boolean isPreAuthenticated(T token) {
        return false;
    }

    /**
     * check {@link AuthenticationToken} after authentication action.
     *
     * @param token
     */
    protected void postCheckAuthenticationToken(final T token, final Authenticating authenticating) {
        if (null == authenticating) {
            logger.warn("associated authenticating not found by given token: {}", token);
            onAuthenticatingNotExists(token);
        }
        if (null == authenticating.getDetails()) {
            if (logger.isDebugEnabled())
                logger.warn("associated authenticating details not provided by given token: {}", token);
        }
        if (authenticating.isExpired())
            throw new AuthenticationException("principal expired.");
        if (authenticating.isLocked())
            throw new AuthenticationException("principal locked");
        if (authenticating.isCredentialsExpired())
            throw new AuthenticationException("credentials Expired.");
        if (!authenticating.isEnabled())
            throw new AuthenticationException("principal disabled.");
    }


    /**
     * token本地无法找到，为安全起见，authenticating不存在时返回Bad Credentials，子类可覆写该方法返回其他错误讯息
     *
     * @param token
     */
    protected void onAuthenticatingNotExists(final T token) {
        throw new AuthenticationException(SecurityBizException.AUTHC_FAILURE_MESSAGE);
    }

    /**
     * token关联的 {@link AuthenticationDetails} 返回null触发
     *
     * @param token
     */
    protected void onAuthenticatingDetailsNotExists(final T token) {

    }


    protected Authenticated createAuthenticated(final T token, final Authenticating authenticating) {
        Object principal = authenticating.isSamePrincipalFromToken() ? token.getPrincipal() : authenticating.getPrincipal();
        String host = token instanceof HostAuthenticationToken ? ((HostAuthenticationToken) token).getHost() : AbstractAuthenticationToken.HOST_UNKNOWN;
        return authenticating.done(principal, host, getClass());
    }

    @Override
    public boolean supports(AuthenticationToken token) {
        return ClassUtils.isAssignableValue(supportTokenType, token);
    }

    public void setAuthenticationTokenManager(AuthenticationTokenManager authenticationTokenManager) {
        this.authenticationTokenManager = authenticationTokenManager;
        if (this.authenticationTokenManager.isCredentialsHashed()) {
            HashedCredentialsMatcher hashedCredentialsMatcher = new HashedCredentialsMatcher(this.authenticationTokenManager.getCredentialsHashAlgorithmName());
            hashedCredentialsMatcher.setHashIterations(this.authenticationTokenManager.getCredentialsHashIterations());
            if (this.authenticationTokenManager.getCredentialsEncoded().equals(AuthenticationTokenManager.HEX))
                hashedCredentialsMatcher.setStoredCredentialsHexEncoded(true);
            setCredentialsMatcher(hashedCredentialsMatcher);
        }
    }

    public void setAuthorizationManagers(List<AuthorizationManager> authorizationManagers) {
        this.authorizationManagers = authorizationManagers;
    }
}
