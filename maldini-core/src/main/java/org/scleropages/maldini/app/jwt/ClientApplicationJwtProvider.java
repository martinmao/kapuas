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
package org.scleropages.maldini.app.jwt;

import org.scleropages.maldini.security.authc.token.server.jwt.JwtTokenFactory;

/**
 * 应用端验签密钥提供者
 *
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public class ClientApplicationJwtProvider implements JwtTokenFactory.SignatureKeyProvider {

    @Override
    public boolean support(JwtTokenFactory.JwtHeader jwtHeader) {
        return false;
    }

    @Override
    public byte[] get(JwtTokenFactory.JwtHeader jwtHeader) {
        return new byte[0];
    }
}
