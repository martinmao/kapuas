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

import org.scleropages.core.util.Digests;
import org.scleropages.core.util.RandomGenerator;
import org.scleropages.core.util.SecureRandomGenerator;
import org.scleropages.crud.GenericManager;
import org.scleropages.crud.orm.jpa.entity.EntityAware;
import org.scleropages.maldini.security.authc.AuthenticationManager;
import org.scleropages.maldini.security.authc.mgmt.entity.AuthenticationEntity;
import org.scleropages.maldini.security.authc.mgmt.entity.AuthenticationEntityRepository;
import org.scleropages.maldini.security.authc.mgmt.model.Authentication;
import org.scleropages.maldini.security.authc.mgmt.model.AuthenticationMapper;
import org.scleropages.maldini.security.authc.token.client.AuthenticationToken;
import org.scleropages.maldini.security.authc.token.client.UsernamePasswordToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
@Service
@Validated
public class GenericAuthenticationManager implements AuthenticationManager, GenericManager<Authentication, Long, AuthenticationMapper> {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Value("#{ @environment['authentication.credentials-hashed'] ?: true }")
    private boolean credentialsHashed = true;

    @Value("#{ @environment['authentication.credentials-hash-algorithm-name'] ?: 'SHA-1' }")
    private String credentialsHashAlgorithmName;

    @Value("#{ @environment['authentication.credentials-encoded'] ?: 'HEX' }")
    private String credentialsEncoded;

    @Value("#{ @environment['authentication.credentials-hash-iterations'] ?: 1 }")
    private int credentialsHashIterations = 1;

    private AuthenticationEntityRepository authenticationEntityRepository;

    private RandomGenerator randomGenerator = SecureRandomGenerator.DEFAULT_INSTANCE;

    private Authenticator authenticator;


    @Override
    public void authentication(AuthenticationToken token) {
        authenticator.authentication(token, findOne(String.valueOf(token.getPrincipal())));
    }

    @Override
    public void login(AuthenticationToken authenticationToken) {
        authenticator.login(authenticationToken);
    }

    @Override
    public void logout() {
        authenticator.logout();
    }


    @Override
    public Authentication randomAuthentication(int numberOfPrincipalBytes, int numberOfCredentialsBytes, String encoded) {
        String principal = Digests.encode(encoded, randomGenerator.nextBytes(numberOfPrincipalBytes));
        String credentials = Digests.encode(encoded, randomGenerator.nextBytes(numberOfCredentialsBytes));
        return new Authentication(principal, credentials);
    }

    @Override
    @Transactional
    public void save(Authentication authentication) {
        AuthenticationEntity authenticationEntity = getModelMapper().mapForSave(authentication);
        hashPassword(authenticationEntity);
        authenticationEntityRepository.save(authenticationEntity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        authenticationEntityRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Authentication findOne(String principal) {
        return getModelMapper().mapForRead(authenticationEntityRepository.findByPrincipal(principal));
    }

    @Override
    @Transactional(readOnly = true)
    public Authentication findById(Long id) {
        return getModelMapper().mapForRead(authenticationEntityRepository.findById(id).get());
    }

    /**
     * not a open api. just used for manager implementation perform entity binding(such as jpa entity relational mapping.)
     *
     * @param id
     * @param entityAware
     */
    public void awareAuthenticationEntity(Long id, AuthenticationEntityAware entityAware) {
        entityAware.setEntity(authenticationEntityRepository.findById(id).get());
    }

    @Override
    @Transactional
    public void updateCredentials(Long id, String oldCredentials, String newCredentials) {
        authenticationEntityRepository.findById(id).ifPresent(authenticationEntity -> {
            authenticator.authentication(new UsernamePasswordToken(authenticationEntity.getPrincipal(), oldCredentials), getModelMapper().mapForRead(authenticationEntity));
            authenticationEntity.setCredentials(newCredentials);
            hashPassword(authenticationEntity);
        });
    }

    @Override
    @Transactional
    public Authentication resetCredentials(Long id) {
        Authentication authentication = new Authentication();
        authenticationEntityRepository.findById(id).ifPresent(authenticationEntity -> {
            authenticationEntity.setCredentials(Digests.encode(credentialsEncoded, randomGenerator.nextBytes()));
            hashPassword(authenticationEntity);
        });
        return authentication;
    }

    protected void hashPassword(AuthenticationEntity entity) {
        if (!isCredentialsHashed()) {
            return;
        }
        byte[] saltBytes = randomGenerator.nextBytes();

        entity.setCredentials(Digests.encode(credentialsEncoded,
                Digests.plainTextToHashDigest(credentialsHashAlgorithmName, entity.getCredentials(), saltBytes, getCredentialsHashIterations())));
        entity.setSecureSalt(saltBytes);
    }

    @Autowired
    public void setAuthenticationEntityRepository(AuthenticationEntityRepository authenticationEntityRepository) {
        this.authenticationEntityRepository = authenticationEntityRepository;
    }

    @Autowired(required = false)
    public void setRandomGenerator(RandomGenerator randomGenerator) {
        this.randomGenerator = randomGenerator;
    }


    @Autowired
    public void setAuthenticator(Authenticator authenticator) {
        this.authenticator = authenticator;
    }

    @Override
    public RandomGenerator getRandomGenerator() {
        return randomGenerator;
    }

    public String getCredentialsEncoded() {
        return credentialsEncoded;
    }


    public boolean isCredentialsHashed() {
        return credentialsHashed;
    }

    public void setCredentialsHashed(boolean credentialsHashed) {
        this.credentialsHashed = credentialsHashed;
    }


    public String getCredentialsHashAlgorithmName() {
        return credentialsHashAlgorithmName;
    }

    public void setCredentialsHashAlgorithmName(String credentialsHashAlgorithmName) {
        this.credentialsHashAlgorithmName = credentialsHashAlgorithmName;
    }


    public int getCredentialsHashIterations() {
        return credentialsHashIterations;
    }

    public void setCredentialsHashIterations(int credentialsHashIterations) {
        this.credentialsHashIterations = credentialsHashIterations;
    }

    public void setCredentialsEncoded(String credentialsEncoded) {
        this.credentialsEncoded = credentialsEncoded;
    }


    interface AuthenticationEntityAware extends EntityAware<AuthenticationEntity> {
    }

}
