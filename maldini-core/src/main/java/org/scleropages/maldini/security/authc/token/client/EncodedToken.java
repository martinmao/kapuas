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

import java.io.Serializable;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public class EncodedToken extends AbstractAuthenticationToken {

    private final Serializable encoded;

    public EncodedToken(Serializable encoded) {
        super();
        this.encoded = encoded;
    }

    public EncodedToken(boolean rememberMe, String host, Serializable encoded) {
        super(rememberMe, host);
        this.encoded = encoded;
    }

    @Override
    public Object getPrincipal() {
        throw new IllegalStateException("not decoded");
    }

    @Override
    public Object getCredentials() {
        throw new IllegalStateException("not decoded");
    }


    @Override
    public boolean isRememberMe() {
        throw new IllegalStateException("not decoded");
    }

    @Override
    public String host() {
        return super.host();
    }

    public Serializable getEncoded() {
        return encoded;
    }
}
