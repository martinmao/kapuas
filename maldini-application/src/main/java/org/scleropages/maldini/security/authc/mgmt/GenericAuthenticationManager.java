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
import org.scleropages.crud.dao.orm.jpa.entity.EntityAware;
import org.scleropages.crud.exception.BizError;
import org.scleropages.maldini.security.authc.AuthenticationManager;
import org.scleropages.maldini.security.authc.mgmt.entity.AuthenticationEntity;
import org.scleropages.maldini.security.authc.mgmt.entity.AuthenticationEntityRepository;
import org.scleropages.maldini.security.authc.mgmt.model.Authentication;
import org.scleropages.maldini.security.authc.mgmt.model.AuthenticationMapper;
import org.scleropages.maldini.security.authc.provider.Authenticator;
import org.scleropages.maldini.security.authc.token.client.AuthenticationToken;
import org.scleropages.maldini.security.authc.token.client.EncodedToken;
import org.scleropages.maldini.security.authc.token.client.UsernamePasswordToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
@Service
@Validated
@BizError("10")
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
    @BizError("01")
    public void authentication(AuthenticationToken token) {
        authenticator.authentication(token, getByPrincipal(String.valueOf(token.getPrincipal())));
    }

    @Override
    @BizError("02")
    public EncodedToken createEncodedToken(AuthenticationToken authenticationToken, Map<String, Object> requestContext, Class<? extends EncodedToken> encodedTokenType) {
        return authenticator.createEncodedToken(authenticationToken, requestContext, encodedTokenType);
    }

    @Override
    @BizError("03")
    public void login(AuthenticationToken authenticationToken) {
        authenticator.login(authenticationToken);
    }

    @Override
    @BizError("04")
    public void logout() {
        authenticator.logout();
    }


    @Override
    @BizError("05")
    public Authentication randomAuthentication(int numberOfPrincipalBytes, int numberOfCredentialsBytes, String encoded) {
        String principal = Digests.encode(encoded, randomGenerator.nextBytes(numberOfPrincipalBytes));
        String credentials = Digests.encode(encoded, randomGenerator.nextBytes(numberOfCredentialsBytes));
        return new Authentication(principal, credentials);
    }

    @Override
    @Transactional
    @Validated(Authentication.CreateModel.class)
    @BizError("06")
    public void create(Authentication authentication) {
        AuthenticationEntity authenticationEntity = getModelMapper().mapForSave(authentication);
        hashPassword(authenticationEntity);
        authenticationEntityRepository.save(authenticationEntity);
    }

    @Override
    @Transactional
    @BizError("07")
    public void delete(Long id) {
        authenticationEntityRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    @BizError("08")
    public Authentication getByPrincipal(String principal) {
        return getModelMapper().mapForRead(authenticationEntityRepository.getByPrincipal(principal));
    }

    @Override
    @Validated(Authentication.UpdateModel.class)
    @Transactional
    @BizError("09")
    public void save(Authentication model) {
        AuthenticationEntity authenticationEntity = getModelMapper().mapForSave(model);
        hashPassword(authenticationEntity);
        authenticationEntityRepository.save(authenticationEntity);
    }

    @Override
    @Transactional(readOnly = true)
    @BizError("10")
    public Authentication getById(Long id) {
        return getModelMapper().mapForRead(authenticationEntityRepository.findById(id).get());
    }

    /**
     * not a open api. just used for manager implementation perform payload binding(such as jpa payload relational mapping.)
     *
     * @param id
     * @param entityAware
     */
    public void awareAuthenticationEntity(Long id, AuthenticationEntityAware entityAware) {
        entityAware.setEntity(authenticationEntityRepository.findById(id).get());
    }

    @Override
    @Transactional
    @BizError("11")
    public void updateCredentials(Long id, String oldCredentials, String newCredentials) {
        authenticationEntityRepository.findById(id).ifPresent(authenticationEntity -> {
            authenticator.authentication(new UsernamePasswordToken(authenticationEntity.getPrincipal(), oldCredentials), getModelMapper().mapForRead(authenticationEntity));
            authenticationEntity.setCredentials(newCredentials);
            hashPassword(authenticationEntity);
        });
    }

    @Override
    @Transactional
    @BizError("12")
    public Authentication resetCredentials(Long id) {
        Authentication authentication = new Authentication();
        authenticationEntityRepository.findById(id).ifPresent(authenticationEntity -> {
            String reset = Digests.encode(credentialsEncoded, randomGenerator.nextBytes());
            authentication.setCredentials(reset);
            authentication.setPrincipal(authenticationEntity.getPrincipal());
            authentication.setId(authenticationEntity.getId());
            authenticationEntity.setCredentials(reset);
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

    @Override
    public RandomGenerator getRandomGenerator() {
        return randomGenerator;
    }

    @Override
    public String getCredentialsEncoded() {
        return credentialsEncoded;
    }


    @Override
    public boolean isCredentialsHashed() {
        return credentialsHashed;
    }

    @Override
    public String getCredentialsHashAlgorithmName() {
        return credentialsHashAlgorithmName;
    }


    @Override
    public int getCredentialsHashIterations() {
        return credentialsHashIterations;
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

    interface AuthenticationEntityAware extends EntityAware<AuthenticationEntity> {
    }

}
