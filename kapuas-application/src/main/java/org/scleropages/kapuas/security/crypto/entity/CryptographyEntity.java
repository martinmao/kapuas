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
package org.scleropages.kapuas.security.crypto.entity;

import org.scleropages.crud.dao.orm.jpa.entity.IdEntity;
import org.scleropages.kapuas.security.crypto.GenericKeyManager;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
@Entity
@Table(name = "sec_cryptography", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"associated_id", "name_", "associated_type"})
})
@SequenceGenerator(name = "sec_cryptography_id", sequenceName = "seq_sec_cryptography", allocationSize = IdEntity.SEQ_DEFAULT_ALLOCATION_SIZE, initialValue = IdEntity.SEQ_DEFAULT_INITIAL_VALUE)
public class CryptographyEntity extends IdEntity implements GenericKeyManager.KeyEntityAware {

    private Integer associatedType;

    private String associatedId;

    private String name;

    private String algorithm;

    private String mode;

    private String padding;

    private Integer blockSize;

    private Integer initializationVectorSize;

    private String description;

    private KeyEntity key;

    @Column(name = "associated_type", nullable = false)
    public Integer getAssociatedType() {
        return associatedType;
    }

    @Column(name = "associated_id", nullable = false)
    public String getAssociatedId() {
        return associatedId;
    }

    @Column(name = "name_", nullable = false)
    public String getName() {
        return name;
    }

    @Column(name = "alg_", nullable = false)
    public String getAlgorithm() {
        return algorithm;
    }

    @Column(name = "mode_")
    public String getMode() {
        return mode;
    }

    @Column(name = "padding_")
    public String getPadding() {
        return padding;
    }

    @Column(name = "block_size")
    public Integer getBlockSize() {
        return blockSize;
    }

    @Column(name = "iv_size")
    public Integer getInitializationVectorSize() {
        return initializationVectorSize;
    }

    @Column(name = "desc_", nullable = false)
    public String getDescription() {
        return description;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sec_key_id", nullable = false)
    public KeyEntity getKey() {
        return key;
    }

    public void setAssociatedType(Integer associatedType) {
        this.associatedType = associatedType;
    }

    public void setAssociatedId(String associatedId) {
        this.associatedId = associatedId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setPadding(String padding) {
        this.padding = padding;
    }

    public void setBlockSize(Integer blockSize) {
        this.blockSize = blockSize;
    }

    public void setInitializationVectorSize(Integer initializationVectorSize) {
        this.initializationVectorSize = initializationVectorSize;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setKey(KeyEntity key) {
        this.key = key;
    }

    @Override
    public void setEntity(KeyEntity keyEntity) {
        setKey(keyEntity);
    }
}
