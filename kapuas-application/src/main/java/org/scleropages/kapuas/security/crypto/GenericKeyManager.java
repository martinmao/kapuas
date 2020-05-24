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
package org.scleropages.kapuas.security.crypto;

import org.scleropages.core.util.RandomGenerator;
import org.scleropages.core.util.SecretKeys;
import org.scleropages.core.util.SecureRandomGenerator;
import org.scleropages.crud.GenericManager;
import org.scleropages.crud.dao.orm.jpa.entity.EntityAware;
import org.scleropages.crud.exception.BizError;
import org.scleropages.kapuas.security.crypto.entity.KeyEntity;
import org.scleropages.kapuas.security.crypto.entity.KeyEntityRepository;
import org.scleropages.kapuas.security.crypto.model.Key;
import org.scleropages.kapuas.security.crypto.model.KeyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.util.function.BiConsumer;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
@Service
@Validated
@BizError("30")
public class GenericKeyManager implements KeyManager, GenericManager<Key, Long, KeyMapper> {


    private KeyEntityRepository keyEntityRepository;

    private RandomGenerator randomGenerator = SecureRandomGenerator.DEFAULT_INSTANCE;


    @Override
    @Transactional
    @BizError("01")
    public Long createRandomKey(String algorithm, int keySize) {
        SecretKey created = randomGenerator.consumeRandom(random -> SecretKeys.generateRandomKey(algorithm, keyGenerator -> {
            if (keySize != -1)
                keyGenerator.init(keySize, (SecureRandom) random);
            else
                keyGenerator.init((SecureRandom) random);
        }));
        KeyEntity keyEntity = new KeyEntity();
        keyEntity.setKeyType(Key.KEY_TYPE);
        mapKeyEntity(keyEntity, keySize, created);
        return keyEntityRepository.save(keyEntity).getId();
    }

    @Override
    @Transactional
    @BizError("02")
    public void createRandomKeyPair(String algorithm, int keySize, BiConsumer<Long, Long> consumer) {
        KeyPair created = randomGenerator.consumeRandom(random -> SecretKeys.generateRandomKeyPair(algorithm, keyPairGenerator -> {
            if (keySize != -1)
                keyPairGenerator.initialize(keySize, (SecureRandom) random);
        }));
        KeyEntity privateKeyEntity = new KeyEntity();
        privateKeyEntity.setKeyType(Key.KEY_PAIR_TYPE_PRIVATE);

        KeyEntity publicKeyEntity = new KeyEntity();
        publicKeyEntity.setKeyType(Key.KEY_PAIR_TYPE_PUBLIC);
        mapKeyEntity(privateKeyEntity, keySize, created.getPrivate());
        mapKeyEntity(publicKeyEntity, keySize, created.getPublic());

        privateKeyEntity.setRefKey(publicKeyEntity);
        publicKeyEntity.setRefKey(privateKeyEntity);
        consumer.accept(keyEntityRepository.save(privateKeyEntity).getId(), keyEntityRepository.save(publicKeyEntity).getId());
    }

    @Transactional
    @BizError("03")
    public void resetKeyEncoded(Long id) {
        keyEntityRepository.findById(id).ifPresent(keyEntity -> {
            if (keyEntity.getKeyType() == Key.KEY_TYPE) {
                SecretKey created = randomGenerator.consumeRandom(random -> SecretKeys.generateRandomKey(keyEntity.getAlgorithm(), keyGenerator -> {
                    if (keyEntity.getKeySize() != -1)
                        keyGenerator.init(keyEntity.getKeySize(), (SecureRandom) random);
                    else
                        keyGenerator.init((SecureRandom) random);
                }));
                keyEntity.setEncoded(created.getEncoded());
            } else if (keyEntity.getKeyType() == Key.KEY_PAIR_TYPE_PRIVATE) {
                KeyPair created = randomGenerator.consumeRandom(random -> SecretKeys.generateRandomKeyPair(keyEntity.getAlgorithm(), keyPairGenerator -> {
                    if (keyEntity.getKeySize() != -1)
                        keyPairGenerator.initialize(keyEntity.getKeySize(), (SecureRandom) random);
                }));
                keyEntity.setEncoded(created.getPrivate().getEncoded());
                keyEntity.getRefKey().setEncoded(created.getPublic().getEncoded());
            } else
                throw new IllegalArgumentException("unsupported key type for key encoded resetting: " + keyEntity.getKeyType());
        });
    }


    @Override
    public byte[] random(int length) {
        return randomGenerator.nextBytes(length);
    }

    @Transactional
    @BizError("04")
    public void save(Key model) {
        model.setEnabled(true);
        keyEntityRepository.save(getModelMapper().mapForSave(model));
    }

    @Override
    @Transactional(readOnly = true)
    @BizError("05")
    public Key getById(Long id) {
        return getModelMapper().mapForRead(keyEntityRepository.findById(id).get());
    }


    /**
     * not a open api. just used for manager implementation perform payload binding(such as jpa payload relational mapping.)
     *
     * @param id
     * @param entityAware
     */
    public void awareKeyEntity(Long id, KeyEntityAware entityAware) {
        entityAware.setEntity(keyEntityRepository.findById(id).get());
    }


    protected void mapKeyEntity(KeyEntity keyEntity, int keySize, java.security.Key jcaKey) {
        keyEntity.setAlgorithm(jcaKey.getAlgorithm());
        keyEntity.setFormat(jcaKey.getFormat());
        keyEntity.setKeySize(keySize);
        keyEntity.setEncoded(jcaKey.getEncoded());
        keyEntity.setEnabled(true);
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
