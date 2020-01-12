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

import org.scleropages.core.util.RandomGenerator;
import org.scleropages.core.util.SecretKeys;
import org.scleropages.core.util.SecureRandomGenerator;
import org.scleropages.crud.GenericManager;
import org.scleropages.crud.orm.jpa.entity.EntityAware;
import org.scleropages.maldini.security.crypto.entity.KeyEntity;
import org.scleropages.maldini.security.crypto.entity.KeyEntityRepository;
import org.scleropages.maldini.security.crypto.model.Key;
import org.scleropages.maldini.security.crypto.model.KeyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.crypto.SecretKey;
import javax.validation.Valid;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.function.BiConsumer;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
@Service
@Validated
public class GenericKeyManager implements KeyManager, GenericManager<Key, Long, KeyMapper> {


    private KeyEntityRepository keyEntityRepository;

    private RandomGenerator randomGenerator = SecureRandomGenerator.DEFAULT_INSTANCE;


    @Override
    public SecretKey createRandomKey(String algorithm, int keySize) {
        return randomGenerator.consumeRandom(random -> SecretKeys.generateRandomKey(algorithm, keyGenerator -> {
            if (keySize != -1)
                keyGenerator.init(keySize, (SecureRandom) random);
            else
                keyGenerator.init((SecureRandom) random);
        }));
    }

    @Override
    public KeyPair createRandomKeyPair(String algorithm, int keySize) {
        return randomGenerator.consumeRandom(random -> SecretKeys.generateRandomKeyPair(algorithm, keyPairGenerator -> {
            if (keySize != -1)
                keyPairGenerator.initialize(keySize, (SecureRandom) random);
        }));
    }

    @Override
    public byte[] random(int length) {
        return randomGenerator.nextBytes(length);
    }

    @Override
    @Transactional
    public Long save(java.security.Key key) {
        Key save = new Key();
        mapLocalKey(key, save);
        return saveAndRead(save).getId();
    }

    @Override
    @Transactional
    public void save(KeyPair keyPair, BiConsumer<Long, Long> consumer) {

        PrivateKey privateKey = keyPair.getPrivate();
        Key privateKeyModel = new Key();
        mapLocalKey(privateKey, privateKeyModel);
        privateKeyModel.enable();

        PublicKey publicKey = keyPair.getPublic();
        Key publicKeyModel = new Key();
        mapLocalKey(publicKey, publicKeyModel);
        publicKeyModel.enable();

        KeyEntity savedPrivateKey = keyEntityRepository.save(getModelMapper().mapForSave(privateKeyModel));

        KeyEntity savedPublicKey = keyEntityRepository.save(getModelMapper().mapForSave(publicKeyModel));

        savedPrivateKey.setRefKey(savedPublicKey);
        savedPublicKey.setRefKey(savedPrivateKey);

        consumer.accept(savedPrivateKey.getId(), savedPublicKey.getId());
    }

    protected void mapLocalKey(java.security.Key source, Key target) {
        target.setAlgorithm(source.getAlgorithm());
        target.setFormat(source.getFormat());
        target.setEncoded(source.getEncoded());
    }

    @Override
    @Transactional
    public void save(@Valid Key model) {
        model.enable();
        keyEntityRepository.save(getModelMapper().mapForSave(model));
    }

    @Override
    @Transactional
    public Key saveAndRead(@Valid Key model) {
        model.enable();
        return getModelMapper().mapForRead(keyEntityRepository.save(getModelMapper().mapForSave(model)));
    }

    @Override
    @Transactional(readOnly = true)
    public Key findById(Long id) {
        return getModelMapper().mapForRead(keyEntityRepository.findById(id).get());
    }


    /**
     * not a open api. just used for manager implementation perform entity binding(such as jpa entity relational mapping.)
     *
     * @param id
     * @param entityAware
     */
    public void awareKeyEntity(Long id, KeyEntityAware entityAware) {
        entityAware.setEntity(keyEntityRepository.findById(id).get());
    }

    @Autowired
    public void setKeyEntityRepository(KeyEntityRepository keyEntityRepository) {
        this.keyEntityRepository = keyEntityRepository;
    }

    @Autowired
    public void setRandomGenerator(RandomGenerator randomGenerator) {
        this.randomGenerator = randomGenerator;
    }


    public interface KeyEntityAware extends EntityAware<KeyEntity> {
    }
}
