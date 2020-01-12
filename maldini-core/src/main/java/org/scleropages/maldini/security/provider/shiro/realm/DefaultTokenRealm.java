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
package org.scleropages.maldini.security.provider.shiro.realm;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.util.ByteSource;
import org.scleropages.maldini.security.authc.mgmt.Authenticator;
import org.scleropages.maldini.security.authc.mgmt.model.Authentication;
import org.scleropages.maldini.security.authc.token.client.AuthenticationToken;
import org.scleropages.maldini.security.provider.shiro.AbstractShiroRealm;
import org.scleropages.maldini.security.provider.shiro.AuthenticationTokenManager;
import org.scleropages.maldini.security.provider.shiro.ShiroAuthenticationTokens;
import org.springframework.util.Assert;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public class DefaultTokenRealm extends AbstractShiroRealm<UsernamePasswordToken> implements Authenticator {


    public DefaultTokenRealm(AuthenticationTokenManager authenticationTokenManager) {
        super(authenticationTokenManager);
    }

    public DefaultTokenRealm() {
    }

    @Override
    public void authentication(AuthenticationToken token, Authentication authentication) {
        Assert.notNull(token, "bad credentials.");
        Assert.notNull(authentication, "bad credentials.");
        SimpleAuthenticationInfo simpleAuthenticationInfo = new SimpleAuthenticationInfo(authentication.getPrincipal(), authentication.getCredentials(),
                ArrayUtils.isNotEmpty(authentication.getSecureSalt()) ?
                        ByteSource.Util.bytes(authentication.getSecureSalt()) : null, getName());
        assertCredentialsMatch(ShiroAuthenticationTokens.asShiroToken(token), simpleAuthenticationInfo);
    }

    @Override
    public void login(AuthenticationToken authenticationToken) {
        SecurityUtils.getSubject().login(ShiroAuthenticationTokens.asShiroToken(authenticationToken));
    }

    @Override
    public void logout() {
        SecurityUtils.getSubject().logout();
    }
}
