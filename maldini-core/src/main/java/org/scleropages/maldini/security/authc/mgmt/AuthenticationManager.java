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
package org.scleropages.maldini.security.authc.mgmt;

import org.scleropages.core.util.RandomGenerator;
import org.scleropages.maldini.security.authc.mgmt.model.Authentication;
import org.scleropages.maldini.security.authc.token.client.AuthenticationToken;

import javax.validation.Valid;

/**
 * Manager used for security principal management.
 *
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public interface AuthenticationManager {

    /**
     * perform authenticate action (just authentication, never to initialize session resources) by given authentication token.
     *
     * @param token
     */
    void authentication(AuthenticationToken token);

    /**
     * perform login(authentication and initialize resource such as session...) action by given authentication token.
     *
     * @param authenticationToken
     */
    void login(AuthenticationToken authenticationToken);

    /**
     * perform logout (destroy resources such as session...) action.
     */
    void logout();

    /**
     * generate a random authentication(principal and credentials.) the random value's length will generated by given number of bytes.
     *
     * @param numberOfPrincipalBytes
     * @param numberOfCredentialsBytes
     * @param encoded
     * @return
     */
    Authentication randomAuthentication(int numberOfPrincipalBytes, int numberOfCredentialsBytes, String encoded);

    /**
     * save a authentication.
     *
     * @param authentication
     */
    void save(@Valid Authentication authentication);

    /**
     * delete a authentication by given id.
     *
     * @param id
     */
    void delete(Long id);

    /**
     * find a unique authentication by given principal.
     *
     * @param principal
     * @return
     */
    Authentication findOne(String principal);

    /**
     * find a authentication by given id.
     *
     * @param id
     * @return
     */
    Authentication findById(Long id);

    /**
     * update exists credentials. oldCredentials must matches current principal credentials.
     *
     * @param id
     * @param oldCredentials
     * @param newCredentials
     */
    void updateCredentials(Long id, String oldCredentials, String newCredentials);

    /**
     * reset a principal credentials as a random.
     *
     * @param id
     * @return
     */
    Authentication resetCredentials(Long id);

    /**
     * Return  {@link RandomGenerator} using by this authentication manager.
     *
     * @return
     */
    RandomGenerator getRandomGenerator();
}