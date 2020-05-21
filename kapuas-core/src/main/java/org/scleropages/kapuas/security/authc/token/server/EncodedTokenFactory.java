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
package org.scleropages.kapuas.security.authc.token.server;

import org.scleropages.kapuas.security.authc.token.client.EncodedToken;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public interface EncodedTokenFactory<T> {

    /**
     * use specify signature-key to decode encoded-token.
     *
     * @param signatureKey
     * @param encodedToken
     * @return
     */
    T decode(final byte[] signatureKey, final EncodedToken encodedToken);

    /**
     * use specify signature-key to encode given token object.
     *
     * @param tokenObject
     * @param signatureKey
     * @return
     */
    EncodedToken encode(byte[] signatureKey, T tokenObject);

}
