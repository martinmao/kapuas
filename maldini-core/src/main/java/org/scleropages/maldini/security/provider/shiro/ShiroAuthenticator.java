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
package org.scleropages.maldini.security.provider.shiro;

import com.google.common.collect.Maps;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.realm.AuthenticatingRealm;
import org.apache.shiro.util.ByteSource;
import org.scleropages.maldini.security.SecurityBizException;
import org.scleropages.maldini.security.SecurityContext;
import org.scleropages.maldini.security.SecurityContextHolder;
import org.scleropages.maldini.security.authc.Authentication;
import org.scleropages.maldini.security.authc.provider.Authenticator;
import org.scleropages.maldini.security.authc.provider.JwtProvider;
import org.scleropages.maldini.security.authc.token.client.AuthenticationToken;
import org.scleropages.maldini.security.authc.token.client.EncodedToken;
import org.scleropages.maldini.security.authc.token.client.jwt.JwtEncodedToken;
import org.scleropages.maldini.security.authc.token.server.jwt.JwtToken;
import org.scleropages.maldini.security.authc.token.server.jwt.JwtTokenFactory;
import org.scleropages.maldini.security.provider.shiro.realm.DefaultTokenRealm;
import org.scleropages.maldini.session.StatelessToken;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.util.Map;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public class ShiroAuthenticator implements Authenticator, ApplicationContextAware {

    private Map<Class, JwtProvider> jwtProviders;

    private JwtTokenFactory jwtTokenFactory;

    private ApplicationContext applicationContext;

    private AuthenticatingRealm authenticatingRealm;

    @Override
    public void authentication(AuthenticationToken token, Authentication authentication) {
        if (null == authentication)
            throw new SecurityBizException(SecurityBizException.AUTHC_FAILURE_MESSAGE);
        SimpleAuthenticationInfo simpleAuthenticationInfo = new SimpleAuthenticationInfo(
                authentication.getPrincipal(),
                authentication.getCredentials(),
                ByteSource.Util.bytes(authentication.getSecureSalt()),
                getClass().getName());

        boolean matcherResult = authenticatingRealm.getCredentialsMatcher().doCredentialsMatch(ShiroAuthenticationTokens.asShiroToken(token), simpleAuthenticationInfo);
        if (!matcherResult)
            throw new SecurityBizException(SecurityBizException.AUTHC_FAILURE_MESSAGE);
    }

    @Override
    public void login(AuthenticationToken authenticationToken) {
        SecurityUtils.getSubject().login(ShiroAuthenticationTokens.asShiroToken(authenticationToken));
    }

    @Override
    public void logout() {
        SecurityUtils.getSubject().logout();
    }

    @Override
    public EncodedToken createEncodedToken(AuthenticationToken authenticationToken, Map<String, Object> requestContext, Class<? extends EncodedToken> encodedTokenType) {
        if (ClassUtils.isAssignable(JwtEncodedToken.class, encodedTokenType)) {
            return createJwtToken(authenticationToken, requestContext);
        } else {
            throw new IllegalArgumentException("not support encoded token type.");
        }
    }

    public EncodedToken createJwtToken(AuthenticationToken authenticationToken, Map<String, Object> requestContext) {
        Assert.isInstanceOf(StatelessToken.class, authenticationToken, "given authentication token must stateless token.");
        login(authenticationToken);
        SecurityContext securityContext = SecurityContextHolder.getContext();
        JwtProvider jwtProvider = jwtProviders.get(securityContext.getDetails().getClass());
        Assert.notNull(jwtProvider, "no jwt provider found by given authentication token.");
        return jwtProvider.build(securityContext.getAuthenticated(), jwtTokenFactory, JwtToken.newBuilder(), requestContext);
    }

    public void init() {
        Map<String, JwtProvider> beansOfType = applicationContext.getBeansOfType(JwtProvider.class);
        this.jwtProviders = Maps.newHashMap();
        beansOfType.forEach((beanName, jwtProvider) -> jwtProviders.put(jwtProvider.getSource(), jwtProvider));
        this.jwtTokenFactory = applicationContext.getBean(JwtTokenFactory.class);
        this.authenticatingRealm = applicationContext.getBean(DefaultTokenRealm.class);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
