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
package org.scleropages.maldini.security.authc.token.server.jwt;


import org.scleropages.core.util.RandomGenerator;
import org.scleropages.maldini.security.authc.token.client.EncodedToken;
import org.scleropages.maldini.security.authc.token.server.EncodedTokenFactory;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public interface JwtTokenFactory extends EncodedTokenFactory<JwtToken> {


    public static final String ALGORITHM_NONE = "NONE";
    public static final String ALGORITHM_HS256 = "HS256";
    public static final String ALGORITHM_HS384 = "HS384";
    public static final String ALGORITHM_HS512 = "HS512";
    public static final String ALGORITHM_RS256 = "RS256";
    public static final String ALGORITHM_RS384 = "RS384";
    public static final String ALGORITHM_RS512 = "RS512";
    public static final String ALGORITHM_ES256 = "ES256";
    public static final String ALGORITHM_ES384 = "ES384";
    public static final String ALGORITHM_ES512 = "ES512";
    public static final String ALGORITHM_PS256 = "PS256";
    public static final String ALGORITHM_PS384 = "PS384";
    public static final String ALGORITHM_PS512 = "PS512";


    RandomGenerator getRandomGenerator();


    /**
     * used for read jwt headers.
     */
    interface JwtHeader {
        /**
         * return value from header by given header name.
         *
         * @param name
         * @return
         */
        Object get(String name);
    }

    /**
     * Implements this interface lookup signature-key from encoded jwt token.
     */
    interface SignatureKeyProvider {

        boolean support(JwtHeader jwtHeader);

        byte[] get(JwtHeader jwtHeader);
    }


    @Override
    default EncodedToken encode(byte[] signatureKey, JwtToken tokenObject) {
        return encode(ALGORITHM_HS256, signatureKey, tokenObject);
    }


    EncodedToken encode(String algorithm, byte[] signatureKey, JwtToken tokenObject);

    JwtToken decode(EncodedToken encodedJwt, SignatureKeyProvider... signatureKeyProviders);
}
