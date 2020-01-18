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

import org.scleropages.core.util.Signatures;
import org.scleropages.crud.GenericManager;
import org.scleropages.maldini.security.authc.mgmt.entity.JwtTokenTemplateEntity;
import org.scleropages.maldini.security.authc.mgmt.entity.JwtTokenTemplateEntityRepository;
import org.scleropages.maldini.security.authc.mgmt.model.JwtTokenTemplate;
import org.scleropages.maldini.security.authc.mgmt.model.JwtTokenTemplateMapper;
import org.scleropages.maldini.security.crypto.CryptographyManager;
import org.scleropages.maldini.security.crypto.entity.CryptographyEntity;
import org.scleropages.maldini.security.crypto.entity.KeyEntity;
import org.scleropages.maldini.security.crypto.model.Key;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;

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
    public JwtTokenTemplate find(String associatedId, Integer associatedType) {
        return getModelMapper().mapForRead(jwtTokenTemplateEntityRepository.findByAssociatedIdAndAssociatedType(associatedId, associatedType));
    }

    @Override
    @Transactional(readOnly = true)
    public JwtTokenTemplate findById(Long id) {
        return jwtTokenTemplateEntityRepository.findModelById(id);
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
