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

import org.scleropages.kapuas.security.authc.provider.Authenticating;
import org.scleropages.kapuas.security.authc.token.server.jwt.JwtToken;
import org.scleropages.kapuas.security.authc.token.server.jwt.JwtTokenFactory;
import org.scleropages.kapuas.security.provider.shiro.AuthenticationTokenManager;

import java.util.List;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public class JwtTokenManager implements AuthenticationTokenManager<ShiroJwtEncodedToken> {


    private JwtTokenFactory.SignatureKeyProvider[] signatureKeyProviders;

    private final JwtTokenFactory jwtTokenFactory;

    public JwtTokenManager(JwtTokenFactory jwtTokenFactory) {
        this.jwtTokenFactory = jwtTokenFactory;
    }

    @Override
    public Authenticating find(ShiroJwtEncodedToken shiroJwtToken) {
        JwtToken jwtToken = jwtTokenFactory.decode(shiroJwtToken.getJwtEncodedToken(), signatureKeyProviders);
        Authenticating authenticating = new Authenticating(String.valueOf(jwtToken.getSubject()), "PROTECTED", null, null);
        authenticating.setContext(jwtToken);
        return authenticating;
    }

    public void setSignatureKeyProviders(List<JwtTokenFactory.SignatureKeyProvider> signatureKeyProviders) {
        this.signatureKeyProviders = signatureKeyProviders.toArray(new JwtTokenFactory.SignatureKeyProvider[signatureKeyProviders.size()]);
    }
}
