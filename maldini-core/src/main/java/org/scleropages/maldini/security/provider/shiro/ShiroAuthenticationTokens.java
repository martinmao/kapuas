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

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.scleropages.maldini.security.authc.token.client.StatelessUsernamePasswordToken;
import org.scleropages.maldini.security.provider.shiro.realm.ShiroStatelessUsernamePasswordToken;

/**
 * Utility class used for convert client token(org.scleropages.maldini.security.authc.token.client) as shiro token.
 *
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public abstract class ShiroAuthenticationTokens {


    public static AuthenticationToken asShiroToken(org.scleropages.maldini.security.authc.token.client.AuthenticationToken source) {
        if (source instanceof StatelessUsernamePasswordToken)
            return toShiroStatelessUsernamePasswordToken((StatelessUsernamePasswordToken) source);
        if (source instanceof org.scleropages.maldini.security.authc.token.client.UsernamePasswordToken)
            return toShiroUsernamePasswordToken((org.scleropages.maldini.security.authc.token.client.UsernamePasswordToken) source);

        throw new IllegalStateException("Unsupported token: " + source);
    }


    private static UsernamePasswordToken toShiroUsernamePasswordToken(org.scleropages.maldini.security.authc.token.client.UsernamePasswordToken source) {
        return new UsernamePasswordToken(source.getUsername(), source.getPassword(), source.isRememberMe(), source.host());
    }

    private static ShiroStatelessUsernamePasswordToken toShiroStatelessUsernamePasswordToken(StatelessUsernamePasswordToken source) {
        return new ShiroStatelessUsernamePasswordToken(source.getUsername(), source.getPassword(), source.isRememberMe(), source.host());
    }
}
