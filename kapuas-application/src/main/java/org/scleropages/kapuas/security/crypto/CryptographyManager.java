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

import org.scleropages.core.util.Ciphers;
import org.scleropages.core.util.SecretKeys;
import org.scleropages.core.util.Signatures;
import org.scleropages.crud.GenericManager;
import org.scleropages.crud.dao.orm.jpa.entity.EntityAware;
import org.scleropages.crud.exception.BizError;
import org.scleropages.kapuas.security.crypto.entity.CryptographyEntity;
import org.scleropages.kapuas.security.crypto.entity.CryptographyEntityRepository;
import org.scleropages.kapuas.security.crypto.entity.KeyEntity;
import org.scleropages.kapuas.security.crypto.model.Cryptography;
import org.scleropages.kapuas.security.crypto.model.CryptographyMapper;
import org.scleropages.kapuas.security.crypto.model.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.security.InvalidKeyException;
import java.security.SignatureException;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
@Service
@Validated
@BizError("30")
public class CryptographyManager implements GenericManager<Cryptography, Long, CryptographyMapper> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private GenericKeyManager keyManager;

    private CryptographyEntityRepository cryptographyEntityRepository;


    @Transactional
    @Validated({Cryptography.CreateModel.class})
    @BizError("01")
    public void save(@Valid Cryptography model) {
        CryptographyEntity cryptographyEntity = getModelMapper().mapForSave(model);
        String keyAlg = model.getKeyAlgorithm();
        int keySize = model.getKeySize();
        if (SecretKeys.isKeyAlgorithm(keyAlg)) {
            keyManager.awareKeyEntity(keyManager.createRandomKey(keyAlg, keySize), cryptographyEntity);
        } else if (SecretKeys.isKeyPairAlgorithm(keyAlg)) {
            keyManager.createRandomKeyPair(keyAlg, keySize, (privateId, publicId) -> keyManager.awareKeyEntity(privateId, cryptographyEntity));
        } else
            throw new IllegalArgumentException("un-support key algorithm.");
        if (Signatures.isSignatureAlgorithm(cryptographyEntity.getAlgorithm())) {
            validationSignature(cryptographyEntity);
        } else if (Ciphers.isCipherAlgorithm(cryptographyEntity.getAlgorithm())) {
            validationCipher(cryptographyEntity);
        } else
            throw new IllegalArgumentException("un-support algorithm.");
        cryptographyEntityRepository.save(cryptographyEntity);
    }

    protected void validationSignature(CryptographyEntity cryptographyEntity) {
        Signatures.doInSign(cryptographyEntity.getAlgorithm(), signatureProvider -> {
            byte[] random = keyManager.random(64);
            try {
                KeyEntity keyEntity = cryptographyEntity.getKey();
                java.security.Key signKey;
                java.security.Key verifyKey;
                if (keyEntity.getKeyType().equals(Key.KEY_TYPE)) {
                    signKey = SecretKeys.generateKey(keyEntity.getAlgorithm(), keyEntity.getEncoded());
                    verifyKey = signKey;
                } else if (keyEntity.getKeyType().equals(Key.KEY_PAIR_TYPE_PRIVATE)) {
                    signKey = SecretKeys.generatePKCS8PrivateKey(keyEntity.getAlgorithm(), keyEntity.getEncoded());
                    KeyEntity refKey = keyEntity.getRefKey();
                    verifyKey = SecretKeys.generateX509PublicKey(refKey.getAlgorithm(), refKey.getEncoded());
                } else
                    throw new IllegalArgumentException("un-support key type.");
                signatureProvider.initSign(signKey);
                signatureProvider.update(random);
                byte[] signResult = signatureProvider.sign();
                signatureProvider.initVerify(verifyKey);
                signatureProvider.update(random);
                Assert.isTrue(signatureProvider.isValid(signResult), "validation signature failure: not match.");
            } catch (SignatureException | InvalidKeyException e) {
                throw new IllegalArgumentException("validation signature failure.", e);
            }
        });
    }

    protected void validationCipher(CryptographyEntity cryptographyEntity) {

    }


    @Transactional(readOnly = true)
    @BizError("02")
    public Cryptography getById(Long id) {
        return getModelMapper().mapForReadWithKeys(cryptographyEntityRepository.findById(id).get(), true);
    }

    @Transactional(readOnly = true)
    @BizError("03")
    public Cryptography findOne(Integer associatedType, String associatedId, String name) {
        return getModelMapper().mapForReadWithKeys(cryptographyEntityRepository.findByAssociatedIdAndNameAndAssociatedType(associatedId, name, associatedType).get(), true);
    }

    @Transactional(readOnly = true)
    @BizError("04")
    public Page<Cryptography> findPage(Integer associatedType, Pageable pageable) {
        return cryptographyEntityRepository.findAllByAssociatedType
                (associatedType, pageable).map(cryptographyEntity -> getModelMapper().mapForRead(cryptographyEntity));
    }

    @Transactional(readOnly = true)
    @BizError("05")
    public Page<Cryptography> findPage(Integer associatedType, String associatedId, Pageable pageable) {
        return cryptographyEntityRepository.findAllByAssociatedTypeAndAssociatedId
                (associatedType, associatedId, pageable).map(cryptographyEntity -> getModelMapper().mapForRead(cryptographyEntity));
    }


    @Autowired
    public void setCryptographyEntityRepository(CryptographyEntityRepository cryptographyEntityRepository) {
        this.cryptographyEntityRepository = cryptographyEntityRepository;
    }

    @Autowired
    public void setKeyManager(GenericKeyManager keyManager) {
        this.keyManager = keyManager;
    }


    /**
     * not a open api. just used for manager implementation perform payload binding(such as jpa payload relational mapping.)
     *
     * @param id
     * @param entityAware
     */
    public void awareKeyEntity(Long id, CryptographyEntityAware entityAware) {
        entityAware.setEntity(cryptographyEntityRepository.findById(id).get());
    }

    public interface CryptographyEntityAware extends EntityAware<CryptographyEntity> {
    }
}
