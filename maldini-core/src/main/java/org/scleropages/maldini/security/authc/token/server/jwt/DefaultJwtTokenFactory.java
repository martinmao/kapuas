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

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SigningKeyResolverAdapter;
import org.scleropages.core.util.Digests;
import org.scleropages.core.util.RandomGenerator;
import org.scleropages.core.util.SecureRandomGenerator;
import org.scleropages.maldini.security.authc.token.client.EncodedToken;
import org.scleropages.maldini.security.authc.token.client.jwt.JwtEncodedToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.Map;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public class DefaultJwtTokenFactory implements JwtTokenFactory {

    protected RandomGenerator randomGenerator = SecureRandomGenerator.DEFAULT_INSTANCE;


    @Override
    public JwtToken decode(EncodedToken encodedJwt, SignatureKeyProvider... signatureKeyProvider) {
        Jws<Claims> claimsJws = Jwts.parser().setSigningKeyResolver(new SigningKeyResolverAdapter() {
            @Override
            public byte[] resolveSigningKeyBytes(JwsHeader header, Claims claims) {
                for (int i = 0; i < signatureKeyProvider.length; i++) {
                    if (signatureKeyProvider[i].support(name -> header.get(name))) {
                        return signatureKeyProvider[i].get(name -> header.get(name));
                    }
                }
                throw new IllegalArgumentException("unsupported encoded jwt token: " + encodedJwt.getEncoded());
            }
        }).parseClaimsJws(String.valueOf(encodedJwt.getEncoded()));
        return createJwtToken(claimsJws, encodedJwt);
    }

    @Override
    public JwtToken decode(byte[] signatureKey, EncodedToken encodedJwt) {
        Jws<Claims> claimsJws = Jwts.parser().setSigningKey(signatureKey).parseClaimsJws(String.valueOf(encodedJwt.getEncoded()));
        return createJwtToken(claimsJws, encodedJwt);
    }

    protected JwtToken createJwtToken(Jws<Claims> claimsJws, EncodedToken encodedJwt) {
        final Claims body = claimsJws.getBody();
        return new JwtToken() {
            @Override
            public Map<String, Object> getHeader() {
                return claimsJws.getHeader();
            }

            @Override
            public String getIssuer() {
                return body.getIssuer();
            }

            @Override
            public String getSubject() {
                return body.getSubject();
            }

            @Override
            public String getAudience() {
                return body.getAudience();
            }

            @Override
            public String getId() {
                return body.getId();
            }

            @Override
            public Date getExpiration() {
                return body.getExpiration();
            }

            @Override
            public Date getNotBefore() {
                return body.getNotBefore();
            }

            @Override
            public Date getIssuedAt() {
                return body.getIssuedAt();
            }

            @Override
            public Object get(String propertyName, Class requiredType) {
                return body.get(propertyName, requiredType);
            }

            @Override
            public Object getNativeToken() {
                return body;
            }
        };
    }

    @Override
    public JwtEncodedToken encode(String algorithm, byte[] signatureKey, JwtToken tokenObject) {

        Object nativeToken = tokenObject.getNativeToken();
        Assert.state(nativeToken instanceof Map, "Internal error: encoded jwt native token not an instanceof of java.util.Map implementation.");
        Map<String, Object> jwtMapToken = (Map<String, Object>) nativeToken;
        JwtBuilder builder = Jwts.builder();

        Object nativeHeader = jwtMapToken.get(JwtToken.JWT_HEADERS);

        if (null != nativeHeader && nativeHeader instanceof Map) {
            builder.setHeader((Map<String, Object>) nativeHeader);
            jwtMapToken.remove(JwtToken.JWT_HEADERS);
        }

        builder.setClaims(jwtMapToken);

        populateNotProvidedAsDefaultValue(tokenObject, builder);

        builder.signWith(SignatureAlgorithm.forName(algorithm), signatureKey);
        return new JwtEncodedToken(builder.compact());
    }


    protected void populateNotProvidedAsDefaultValue(JwtToken tokenObject, JwtBuilder builder) {
        long now = System.currentTimeMillis();

        if (tokenObject.getId() == null) {// if no id provided. default generate random 16 bytes encoded hex.
            builder.setId(Digests.encodeHex(getRandomGenerator().nextBytes()));
        }
        if (tokenObject.getIssuedAt() == null) {// if no issued at at provided. use current date time.
            builder.setIssuedAt(new Date(now / 1000));
        }
        if (tokenObject.getNotBefore() == null) {// if no not before provided. use current date time.
            builder.setIssuedAt(new Date(now / 1000));
        }
    }


    @Override
    public RandomGenerator getRandomGenerator() {
        return randomGenerator;
    }

    @Autowired
    public void setRandomGenerator(RandomGenerator randomGenerator) {
        this.randomGenerator = randomGenerator;
    }
}
