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
package org.scleropages.maldini.security.authc.token.client;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public class UsernamePasswordToken extends AbstractAuthenticationToken implements AuthenticationToken {

    private final String username;
    private final char[] password;

    public UsernamePasswordToken(final String username, final String password, boolean rememberMe, String host) {
        super(rememberMe, host);
        this.username = username;
        this.password = password.toCharArray();
    }

    public UsernamePasswordToken(final String username, final String password, String host) {
        super(false, host);
        this.username = username;
        this.password = password.toCharArray();
    }

    public UsernamePasswordToken(final String username, final String password) {
        this(username, password, false, HOST_UNKNOWN);
    }


    @Override
    public Object getPrincipal() {
        return username;
    }

    @Override
    public Object getCredentials() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public char[] getPassword() {
        return password;
    }

}
