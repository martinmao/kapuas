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


import com.google.common.collect.Maps;

import java.util.Date;
import java.util.Map;

/**
 * represent a jwt(JSON Web Tokens) https://jwt.io/
 *
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public abstract class JwtToken {

    /**
     * JWT {@code Issuer} claims parameter name: <code>"iss"</code>
     */
    public static final String ISSUER = "iss";

    /**
     * JWT {@code Subject} claims parameter name: <code>"sub"</code>
     */
    public static final String SUBJECT = "sub";

    /**
     * JWT {@code Audience} claims parameter name: <code>"aud"</code>
     */
    public static final String AUDIENCE = "aud";

    /**
     * JWT {@code Expiration} claims parameter name: <code>"exp"</code>
     */
    public static final String EXPIRATION = "exp";

    /**
     * JWT {@code Not Before} claims parameter name: <code>"nbf"</code>
     */
    public static final String NOT_BEFORE = "nbf";

    /**
     * JWT {@code Issued At} claims parameter name: <code>"iat"</code>
     */
    public static final String ISSUED_AT = "iat";

    /**
     * JWT {@code JWT ID} claims parameter name: <code>"jti"</code>
     */
    public static final String JWT_ID = "jti";


    // customized header's and field definitions...
    public static final String JWT_HEADERS = "hdr";


    public class JwtTokenBuilder {


        private final Map<String, Object> tokenMap = Maps.newHashMap();

        /**
         * with JWT {@code JWT ID} parameter name: <code>"jti"</code>
         *
         * @param map
         * @param jwtId
         * @return
         */
        public JwtTokenBuilder withJwtId(String jwtId) {
            tokenMap.put(JWT_ID, jwtId);
            return this;
        }

        /**
         * with JWT {@code Issuer} parameter name: <code>"iss"</code>
         *
         * @param map
         * @param issuer
         * @return
         */
        public JwtTokenBuilder withIssuer(String issuer) {
            tokenMap.put(ISSUER, issuer);
            return this;
        }

        /**
         * with JWT {@code Subject} parameter name: <code>"sub"</code>
         *
         * @param map
         * @param subject
         * @return
         */
        public JwtTokenBuilder withSubject(String subject) {
            tokenMap.put(SUBJECT, subject);
            return this;
        }

        /**
         * with JWT {@code Audience} parameter name: <code>"aud"</code>
         *
         * @param map
         * @param audience
         * @return
         */
        public JwtTokenBuilder withAudience(String audience) {
            tokenMap.put(AUDIENCE, audience);
            return this;
        }

        /**
         * with JWT {@code Expiration} parameter name: <code>"exp"</code>
         *
         * @param map
         * @param expiration
         * @return
         */
        public JwtTokenBuilder withExpiration(Date expiration) {
            // The JWT RFC *mandates* NumericDate values are represented as seconds.
            // Because Because java.util.Date requires milliseconds, we need to multiply by 1000:
            tokenMap.put(EXPIRATION, expiration.getTime() / 1000);
            return this;
        }

        /**
         * with JWT {@code Not Before} parameter name: <code>"nbf"</code>
         *
         * @param map
         * @param notBefore
         * @return
         */
        public JwtTokenBuilder withNotBefore(Date notBefore) {
            // The JWT RFC *mandates* NumericDate values are represented as seconds.
            // Because Because java.util.Date requires milliseconds, we need to multiply by 1000:
            tokenMap.put(NOT_BEFORE, notBefore.getTime() / 1000);
            return this;
        }

        /**
         * with JWT {@code Issued At} parameter name: <code>"iat"</code>
         *
         * @param map
         * @param issuedAt
         * @return
         */
        public JwtTokenBuilder withIssuedAt(Date issuedAt) {
            // The JWT RFC *mandates* NumericDate values are represented as seconds.
            // Because Because java.util.Date requires milliseconds, we need to multiply by 1000:
            tokenMap.put(ISSUED_AT, issuedAt.getTime() / 1000);
            return this;
        }

        /**
         * with JWT {@code headers} for this token.
         *
         * @param map
         * @param header
         * @return
         */
        public JwtTokenBuilder withHeaders(Map<String, Object> header) {
            tokenMap.put(JWT_HEADERS, header);
            return this;
        }

        public JwtToken build() {
            return new JwtToken() {
                @Override
                public Map<String, Object> getHeader() {
                    return (Map<String, Object>) tokenMap.get(JWT_HEADERS);
                }

                @Override
                public String getIssuer() {
                    return (String) tokenMap.get(ISSUER);
                }

                @Override
                public String getSubject() {
                    return (String) tokenMap.get(ISSUER);
                }

                @Override
                public String getAudience() {
                    return (String) tokenMap.get(ISSUER);
                }

                @Override
                public String getId() {
                    return (String) tokenMap.get(ISSUER);
                }

                @Override
                public Date getExpiration() {
                    return (Date) tokenMap.get(ISSUER);
                }

                @Override
                public Date getNotBefore() {
                    return (Date) tokenMap.get(ISSUER);
                }

                @Override
                public Date getIssuedAt() {
                    return (Date) tokenMap.get(ISSUER);
                }

                @Override
                public <T> T get(String propertyName, Class<T> requiredType) {
                    return (T) tokenMap.get(propertyName);
                }

                @Override
                public <T> T getNativeToken() {
                    return (T) tokenMap;
                }
            };
        }

    }


    /**
     * create a builder to easy way build jwt token.
     *
     * @return
     */
    public JwtTokenBuilder newBuilder() {
        return new JwtTokenBuilder();
    }


    /**
     * 头信息
     *
     * @return
     */
    public abstract Map<String, Object> getHeader();

    /**
     * 签发者
     *
     * @return
     */
    public abstract String getIssuer();

    /**
     * 主题
     *
     * @return
     */
    public abstract String getSubject();

    /**
     * 接收方
     *
     * @return
     */
    public abstract String getAudience();

    /**
     * id标识
     *
     * @return
     */
    public abstract String getId();

    /**
     * 过期时间
     *
     * @return
     */
    public abstract Date getExpiration();

    /**
     * 生效时间
     *
     * @return
     */
    public abstract Date getNotBefore();

    /**
     * 签发时间
     *
     * @return
     */
    public abstract Date getIssuedAt();

    /**
     * 自定义属性
     *
     * @param propertyName
     * @param requiredType
     * @param <T>
     * @return
     */
    public abstract <T> T get(String propertyName, Class<T> requiredType);

    /**
     * 获取实现
     *
     * @param <T>
     * @return
     */
    public abstract <T> T getNativeToken();


    @Override
    public String toString() {
        return getNativeToken().toString();
    }
}
