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
package org.scleropages.kapuas.security.authc.provider;

import org.scleropages.core.util.GenericTypes;
import org.scleropages.kapuas.security.authc.token.client.EncodedToken;
import org.scleropages.kapuas.security.authc.token.client.jwt.JwtEncodedToken;
import org.scleropages.kapuas.security.authc.token.server.jwt.JwtToken;
import org.scleropages.kapuas.security.authc.token.server.jwt.JwtTokenFactory;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * 实现该接口以支持对特定认证授权主体（user，employee）以支持jwt认证功能
 *
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public interface JwtProvider<T> extends JwtTokenFactory.SignatureKeyProvider, EncodedTokenProvider {


    /**
     * implement this method to build a jwt token.<br>
     *
     * @param authenticated   current authenticated principal.
     * @param jwtTokenFactory used for creation jwt token encode/decode.
     * @param tokenBuilder    used for build jwt token creation.
     * @param requestContext  any request parameters
     * @return
     */
    JwtEncodedToken build(final Authenticated authenticated, final JwtTokenFactory jwtTokenFactory, final JwtToken.JwtTokenBuilder tokenBuilder, final Map<String, Object> requestContext);


    /**
     * return true if current jwt token support to resolve.<br>
     * NOTE1: 事先 {@link #build(Authenticated, JwtTokenFactory, JwtToken.JwtTokenBuilder, Map)} 将特定field填充入header，且判断应尽可能严谨，jwt 解析模块会依次调用该函数，可能导致判断规则先匹配而意外覆盖预期的解析器.<br>
     * NOTE2: jwt token header毫无私密性 (url safety base64 encode)，敏感信息不宜使用（例如使用user id风险性远高于随机产生的临时user auth_id，下次生成的token应刷新之前的auth_id ）
     *
     * @param jwtHeader
     * @return
     */
    boolean supportResolve(JwtTokenFactory.JwtHeader jwtHeader);

    /**
     * return current jwt token signature key.可以使用统一的固定的签名key，也可根据header不同属性返回不同key.
     *
     * @param jwtHeader
     * @return
     */
    byte[] resolveSignatureKey(JwtTokenFactory.JwtHeader jwtHeader);


    /**
     * 扩展实现不应该覆盖改规则，用于框架内部调用
     *
     * @param jwtHeader
     * @return
     */
    @Override
    default boolean support(JwtTokenFactory.JwtHeader jwtHeader) {
        return supportResolve(jwtHeader);
    }

    /**
     * 扩展实现不应该覆盖改规则，用于框架内部调用
     *
     * @param jwtHeader
     * @return
     */
    @Override
    default byte[] get(JwtTokenFactory.JwtHeader jwtHeader) {
        return resolveSignatureKey(jwtHeader);
    }


    default Class<T> getSource() {
        Class source = GenericTypes.getClassGenericType(getClass(), JwtProvider.class, 0);
        Assert.notNull(source, "implementation must defined Generic-type as source. or implementation getSource method.");
        return source;
    }

    @Override
    default Class<? extends EncodedToken> encodedTokenType() {
        return JwtEncodedToken.class;
    }
}
