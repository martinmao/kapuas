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
package org.scleropages.kapuas.security.provider.shiro.realm;

import org.apache.shiro.authc.UsernamePasswordToken;

/**
 *
 *
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public class ShiroUsernamePasswordToken extends UsernamePasswordToken {


    private org.scleropages.kapuas.security.authc.token.client.UsernamePasswordToken nativeToken;

    public ShiroUsernamePasswordToken() {
    }

    public ShiroUsernamePasswordToken(String username, char[] password) {
        super(username, password);
    }

    public ShiroUsernamePasswordToken(String username, String password) {
        super(username, password);
    }

    public ShiroUsernamePasswordToken(String username, char[] password, String host) {
        super(username, password, host);
    }

    public ShiroUsernamePasswordToken(String username, String password, String host) {
        super(username, password, host);
    }

    public ShiroUsernamePasswordToken(String username, char[] password, boolean rememberMe) {
        super(username, password, rememberMe);
    }

    public ShiroUsernamePasswordToken(String username, String password, boolean rememberMe) {
        super(username, password, rememberMe);
    }

    public ShiroUsernamePasswordToken(String username, char[] password, boolean rememberMe, String host) {
        super(username, password, rememberMe, host);
    }

    public ShiroUsernamePasswordToken(String username, String password, boolean rememberMe, String host) {
        super(username, password, rememberMe, host);
    }

    public org.scleropages.kapuas.security.authc.token.client.UsernamePasswordToken getNativeToken() {
        return nativeToken;
    }

    public void setNativeToken(org.scleropages.kapuas.security.authc.token.client.UsernamePasswordToken nativeToken) {
        this.nativeToken = nativeToken;
    }
}
