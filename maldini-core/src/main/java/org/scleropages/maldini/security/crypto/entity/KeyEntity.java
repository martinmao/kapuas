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
package org.scleropages.maldini.security.crypto.entity;

import org.scleropages.crud.orm.jpa.entity.IdEntity;

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
@Table(name = "sec_key")
@SequenceGenerator(name = "sec_key_id", sequenceName = "seq_sec_key", allocationSize = IdEntity.SEQ_DEFAULT_ALLOCATION_SIZE, initialValue = IdEntity.SEQ_DEFAULT_INITIAL_VALUE)
public class KeyEntity extends IdEntity {

    private String algorithm;

    private String format;

    private byte[] encoded;

    private String keyPassword;//just used for keyStore

    private Boolean enabled;

    private Integer keySize;

    private Integer keyType;

    private KeyEntity refKey;//just used for keypair

    @Column(name = "alg_", nullable = false)
    public String getAlgorithm() {
        return algorithm;
    }

    @Column(name = "format_", nullable = false)
    public String getFormat() {
        return format;
    }

    @Column(name = "encoded_", nullable = false, length = 2048)
    public byte[] getEncoded() {
        return encoded;
    }

    @Column(name = "enabled_", nullable = false)
    public Boolean getEnabled() {
        return enabled;
    }

    @Column(name = "key_password")
    public String getKeyPassword() {
        return keyPassword;
    }

    @Column(name = "key_size", nullable = false)
    public Integer getKeySize() {
        return keySize;
    }

    @Column(name = "key_type", nullable = false)
    public Integer getKeyType() {
        return keyType;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ref_sec_key_id")
    public KeyEntity getRefKey() {
        return refKey;
    }


    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public void setEncoded(byte[] encoded) {
        this.encoded = encoded;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }

    public void setKeySize(Integer keySize) {
        this.keySize = keySize;
    }

    public void setKeyType(Integer keyType) {
        this.keyType = keyType;
    }

    public void setRefKey(KeyEntity refKey) {
        this.refKey = refKey;
    }
}
