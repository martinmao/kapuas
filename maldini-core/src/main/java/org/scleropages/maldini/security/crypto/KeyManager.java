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
package org.scleropages.maldini.security.crypto;

import org.scleropages.maldini.security.crypto.model.Key;

import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.util.function.BiConsumer;

/**
 * Manager used for java(jca,jce) security keys management.
 *
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public interface KeyManager {

    /**
     * create and save a random {@link SecretKey} by given algorithm and key size.
     *
     * @param algorithm
     * @param keySize -1 use default key size by given algorithm.
     * @return saved id.
     */
    Long createRandomKey(String algorithm, int keySize);

    /**
     * create and save a random {@link KeyPair} by given algorithm and key size.
     *
     * @param algorithm
     * @param keySize -1 use default key size by given algorithm
     * @return consumer parameter first is private key id, and second is public key id.
     */
    void createRandomKeyPair(String algorithm, int keySize, BiConsumer<Long, Long> consumer);

    /**
     * return a random bytes by given bytes length.The {@link org.scleropages.core.util.RandomGenerator} is current implementation used.
     *
     * @param length
     * @return
     */
    byte[] random(int length);

    /**
     * find key by given id.
     *
     * @param id
     * @return
     */
    Key findById(Long id);

}
