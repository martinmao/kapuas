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
package org.scleropages.maldini.security.authc.provider;

import org.scleropages.maldini.security.authc.mgmt.model.Authentication;
import org.scleropages.maldini.security.authc.token.client.AuthenticationToken;
import org.scleropages.maldini.security.authc.token.client.EncodedToken;

import java.util.Map;

/**
 * SPI interface. used for integration third-party security framework.
 *
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public interface Authenticator {


    /**
     * perform authenticate action(just authentication, No need to initialize session resources) by given authentication token.
     *
     * @param token
     * @param authentication
     */
    void authentication(AuthenticationToken token, Authentication authentication);

    /**
     * perform authenticate and login(init authentication resource such as session...) action by given client authentication token.
     *
     * @param authenticationToken
     */
    void login(AuthenticationToken authenticationToken);

    /**
     * perform logout (destroy authentication resource such as session...) action.
     */
    void logout();


    /**
     * perform authentication and client encoded token creation.
     *
     * @param authenticationToken
     * @param requestContext
     * @param encodedTokenType
     */
    EncodedToken createEncodedToken(AuthenticationToken authenticationToken, Map<String, Object> requestContext, Class<? extends EncodedToken> encodedTokenType);

}
