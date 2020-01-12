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
package org.scleropages.maldini.security.provider.shiro;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.crypto.hash.Md2Hash;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.apache.shiro.crypto.hash.Sha1Hash;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.crypto.hash.Sha384Hash;
import org.apache.shiro.crypto.hash.Sha512Hash;
import org.scleropages.maldini.security.authc.provider.Authenticating;

/**
 * 凭证管理器，根据token提供必要信息供realm进行认证
 *
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public interface AuthenticationTokenManager<T extends AuthenticationToken> {


    String SHA1 = Sha1Hash.ALGORITHM_NAME;
    String SHA256 = Sha256Hash.ALGORITHM_NAME;
    String SHA384 = Sha384Hash.ALGORITHM_NAME;
    String SHA512 = Sha512Hash.ALGORITHM_NAME;
    String MD2 = Md2Hash.ALGORITHM_NAME;
    String MD5 = Md5Hash.ALGORITHM_NAME;

    /**
     * 支持的hash算法列表
     */
    String[] SUPPORTED_HASH_ALGORITHM_NAME = new String[]{SHA1, SHA256, SHA384, SHA512, MD2, MD5};

    String BASE64 = "BASE64";
    String HEX = "HEX";

    /**
     * 支持的二进制编码表示格式
     */
    String[] SUPPORTED_BINARY_ENCODED = new String[]{BASE64, HEX};


    /**
     * 加载必要信息交给realm进行认证，以及认证成功后的必要参数设置
     *
     * @param authenticationToken
     * @return
     */
    Authenticating find(T authenticationToken);


    /**
     * credentials hashes bytes stored encoded
     *
     * @return {@link #SUPPORTED_BINARY_ENCODED}
     */
    default String getCredentialsEncoded() {
        return HEX;
    }

    /**
     * credentials hashes algorithm name( see supported algorithms {@link #SUPPORTED_HASH_ALGORITHM_NAME} )
     *
     * @return {@link #SUPPORTED_HASH_ALGORITHM_NAME}
     */
    default String getCredentialsHashAlgorithmName() {
        return SHA1;
    }


    /**
     * the number of times credentials will be hashed before comparing
     * to the credentials stored.
     *
     * @return
     */
    default int getCredentialsHashIterations() {
        return 1;
    }

    /**
     * return true if credentials hashes before stored.
     *
     * @return
     */
    default boolean isCredentialsHashed() {
        return getCredentialsHashAlgorithmName() != null;
    }


}
