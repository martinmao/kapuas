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
package org.scleropages.kapuas.security.provider.shiro.realm.jwt;


import org.scleropages.kapuas.security.authc.provider.Authenticated;
import org.scleropages.kapuas.security.authc.provider.Authenticating;
import org.scleropages.kapuas.security.provider.shiro.AbstractShiroRealm;
import org.scleropages.kapuas.security.provider.shiro.AuthenticationTokenManager;

/**
 * 用于 jwt token 认证realm
 *
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public class JwtTokenRealm extends AbstractShiroRealm<ShiroJwtEncodedToken> {


    public JwtTokenRealm(AuthenticationTokenManager authenticationTokenManager) {
        super(authenticationTokenManager);
    }

    public JwtTokenRealm() {
    }

    /**
     * {@link io.jsonwebtoken.impl.DefaultJwtParser#parse(String)} already checked exp and nbf.
     * if use other implementations make sure validate exp and nbf here
     *
     * @param token
     * @param authenticating
     */
    @Override
    protected void postCheckAuthenticationToken(ShiroJwtEncodedToken token, Authenticating authenticating) {
        super.postCheckAuthenticationToken(token, authenticating);
    }

    @Override
    protected Authenticated createAuthenticated(ShiroJwtEncodedToken token, Authenticating authenticating) {
        return super.createAuthenticated(token, authenticating);
    }

    @Override
    protected boolean isPreAuthenticated(ShiroJwtEncodedToken token) {
        return true;
    }

    @Override
    protected void preCheckAuthenticationToken(ShiroJwtEncodedToken token) {
        //noting to do....
    }
}
