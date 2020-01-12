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
package org.scleropages.maldini.app.entity;

import org.scleropages.crud.orm.jpa.entity.IdEntity;
import org.scleropages.maldini.security.crypto.GenericKeyManager;
import org.scleropages.maldini.security.crypto.entity.KeyEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
@Entity
@Table(name = "app_secret")
@SequenceGenerator(name = "app_secret_id", sequenceName = "seq_app_secret", allocationSize = IdEntity.SEQ_DEFAULT_ALLOCATION_SIZE, initialValue = IdEntity.SEQ_DEFAULT_INITIAL_VALUE)
public class SecretEntity extends IdEntity implements GenericKeyManager.KeyEntityAware {

    private String secretId;

    private String name;

    private String description;

    private KeyEntity key;

    private ApplicationEntity application;

    private Boolean enabled;

    private Integer keyType;

    private String algorithm;

    private Integer keySize;

    @Column(name = "secret_id", nullable = false)
    public String getSecretId() {
        return secretId;
    }

    @Column(name = "name_", nullable = false)
    public String getName() {
        return name;
    }

    @Column(name = "desc_")
    public String getDescription() {
        return description;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sec_key_id", nullable = false)
    public KeyEntity getKey() {
        return key;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_info_id", nullable = false)
    public ApplicationEntity getApplication() {
        return application;
    }

    @Column(name = "enabled_", nullable = false)
    public Boolean getEnabled() {
        return enabled;
    }

    @Column(name = "key_type",nullable = false)
    public Integer getKeyType() {
        return keyType;
    }

    @Column(name = "alg_",nullable = false)
    public String getAlgorithm() {
        return algorithm;
    }

    @Column(name = "key_size",nullable = false)
    public Integer getKeySize() {
        return keySize;
    }

    public void setSecretId(String secretId) {
        this.secretId = secretId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setKey(KeyEntity key) {
        this.key = key;
    }

    public void setApplication(ApplicationEntity application) {
        this.application = application;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public void setKeyType(Integer keyType) {
        this.keyType = keyType;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public void setKeySize(Integer keySize) {
        this.keySize = keySize;
    }

    @Override
    public void setEntity(KeyEntity keyEntity) {
        setKey(keyEntity);
    }
}
