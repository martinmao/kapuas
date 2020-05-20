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

import org.apache.shiro.authc.UsernamePasswordToken;
import org.scleropages.maldini.security.provider.shiro.AbstractShiroRealm;
import org.scleropages.maldini.security.provider.shiro.AuthenticationTokenManager;

/**
 * 调用中央认证服务(提供在 xxx-application 模块)，面向前端应用提供全局认证功能.
 *
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public class CentralAuthenticationRealm extends AbstractShiroRealm<UsernamePasswordToken> {

    public CentralAuthenticationRealm() {
    }

    public CentralAuthenticationRealm(AuthenticationTokenManager authenticationTokenManager) {
        super(authenticationTokenManager);
    }

    /**
     * directly return true indicate already authenticated by central certification service.
     *
     * @param token
     * @return
     */
    @Override
    protected boolean isPreAuthenticated(UsernamePasswordToken token) {
        return true;
    }

    @Override
    protected void preCheckAuthenticationToken(UsernamePasswordToken token) {
        //noting to do....
    }
}
