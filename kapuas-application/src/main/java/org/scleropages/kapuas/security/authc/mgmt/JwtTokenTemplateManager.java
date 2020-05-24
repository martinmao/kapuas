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
package org.scleropages.kapuas.security.authc.mgmt;

import org.scleropages.core.util.Signatures;
import org.scleropages.crud.GenericManager;
import org.scleropages.kapuas.security.authc.mgmt.entity.JwtTokenTemplateEntity;
import org.scleropages.kapuas.security.authc.mgmt.entity.JwtTokenTemplateEntityRepository;
import org.scleropages.kapuas.security.authc.mgmt.model.JwtTokenTemplate;
import org.scleropages.kapuas.security.authc.mgmt.model.JwtTokenTemplateMapper;
import org.scleropages.kapuas.security.crypto.CryptographyManager;
import org.scleropages.kapuas.security.crypto.entity.CryptographyEntity;
import org.scleropages.kapuas.security.crypto.entity.KeyEntity;
import org.scleropages.kapuas.security.crypto.model.Key;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.Map;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
@Service
@Validated
public class JwtTokenTemplateManager implements GenericManager<JwtTokenTemplate, Long, JwtTokenTemplateMapper> {

    private JwtTokenTemplateEntityRepository jwtTokenTemplateEntityRepository;

    private CryptographyManager cryptographyManager;

    @Transactional
    @Validated({JwtTokenTemplate.CreateModel.class})
    public void save(@Valid JwtTokenTemplate jwtTokenTemplate) {
        JwtTokenTemplateEntity entity = getModelMapper().mapForSave(jwtTokenTemplate);
        cryptographyManager.awareKeyEntity(jwtTokenTemplate.getCryptographyId(), entity);
        CryptographyEntity cryptographyEntity = entity.getCryptography();
        Assert.isTrue(Signatures.isSignatureAlgorithm(cryptographyEntity.getAlgorithm()), "not a valid signature algorithm.");
        entity.setAlgorithm(cryptographyEntity.getAlgorithm());
        KeyEntity signKeyEntity = cryptographyEntity.getKey();
        entity.setSignKeyEncoded(signKeyEntity.getEncoded());
        if (signKeyEntity.getKeyType().equals(Key.KEY_PAIR_TYPE_PRIVATE)) {
            KeyEntity verifyKeyEntity = signKeyEntity.getRefKey();
            entity.setVerifyKeyEncoded(verifyKeyEntity.getEncoded());
        }
        if (!StringUtils.hasText(entity.getAssociatedId())) {
            entity.setAssociatedId(cryptographyEntity.getAssociatedId());
        }
        if (null == entity.getAssociatedType()) {
            entity.setAssociatedType(cryptographyEntity.getAssociatedType());
        }
        jwtTokenTemplateEntityRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public JwtTokenTemplate getByAssociatedTypeAndAssociatedId(Integer associatedType, String associatedId) {
        Assert.notNull(associatedType,"associatedType is required.");
        Assert.hasText(associatedId,"associatedId is required.");
        return getModelMapper().mapForRead(jwtTokenTemplateEntityRepository.getByAssociatedTypeAndAssociatedId(associatedType, associatedId));
    }

    @Transactional(readOnly = true)
    public JwtTokenTemplate getVerifyKeyEncodedAndAlgorithmByAssociatedTypeAndAssociatedId(Integer associatedType, String associatedId) {
        Assert.notNull(associatedType,"associatedType is required.");
        Assert.hasText(associatedId,"associatedId is required.");
        Map<String, Object> data = jwtTokenTemplateEntityRepository.getVerifyKeyEncodedAndAlgorithmByAssociatedTypeAndAssociatedId(associatedType, associatedId);
        Assert.notNull(data, "no jwt token template found.");
        JwtTokenTemplate jwtTokenTemplate = new JwtTokenTemplate();
        jwtTokenTemplate.setAlgorithm((String) data.get("alg_"));
        jwtTokenTemplate.setVerifyKeyEncoded((byte[]) data.get("verify_key_encoded"));
        jwtTokenTemplate.setAssociatedType(associatedType);
        jwtTokenTemplate.setAssociatedId(associatedId);
        return jwtTokenTemplate;
    }

    @Transactional(readOnly = true)
    public JwtTokenTemplate getById(Long id) {
        return getModelMapper().mapForRead(jwtTokenTemplateEntityRepository.get(id).get());
    }


    @Autowired
    public void setJwtTokenTemplateEntityRepository(JwtTokenTemplateEntityRepository jwtTokenTemplateEntityRepository) {
        this.jwtTokenTemplateEntityRepository = jwtTokenTemplateEntityRepository;
    }

    @Autowired
    public void setCryptographyManager(CryptographyManager cryptographyManager) {
        this.cryptographyManager = cryptographyManager;
    }
}
