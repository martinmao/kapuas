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

import org.springframework.util.Assert;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public abstract class AbstractAuthenticationToken implements AuthenticationToken {


    public static final String HOST_UNKNOWN = "UNKNOWN";

    private final boolean rememberMe;
    private final String host;


    public AbstractAuthenticationToken(boolean rememberMe, String host) {
        Assert.hasText(host, "token source host must not empty.");
        this.rememberMe = rememberMe;
        this.host = host;
    }

    public AbstractAuthenticationToken(String host) {
        this(false, host);
    }

    public AbstractAuthenticationToken() {
        this(false, HOST_UNKNOWN);
    }

    @Override
    public boolean isRememberMe() {
        return rememberMe;
    }

    @Override
    public String host() {
        return host;
    }

}
