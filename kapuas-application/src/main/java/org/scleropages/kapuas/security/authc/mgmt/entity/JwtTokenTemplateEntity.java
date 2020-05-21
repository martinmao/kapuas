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
package org.scleropages.kapuas.security.authc.mgmt.entity;

import org.scleropages.crud.dao.orm.jpa.entity.IdEntity;
import org.scleropages.kapuas.security.crypto.CryptographyManager;
import org.scleropages.kapuas.security.crypto.entity.CryptographyEntity;

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
@Table(name = "sec_jwtt", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"associated_type", "associated_id"})})
@SequenceGenerator(name = "sec_jwtt_id", sequenceName = "seq_sec_jwtt", allocationSize = IdEntity.SEQ_DEFAULT_ALLOCATION_SIZE, initialValue = IdEntity.SEQ_DEFAULT_INITIAL_VALUE)
public class JwtTokenTemplateEntity extends IdEntity implements CryptographyManager.CryptographyEntityAware {

    private String algorithm;
    private String subject;
    private String issuer;
    private Integer associatedType;
    private String associatedId;
    private byte[] signKeyEncoded;
    private byte[] verifyKeyEncoded;
    private CryptographyEntity cryptography;

    @Column(name = "alg_", nullable = false)
    public String getAlgorithm() {
        return algorithm;
    }

    @Column(name = "subject_", nullable = false)
    public String getSubject() {
        return subject;
    }

    @Column(name = "issuer_", nullable = false)
    public String getIssuer() {
        return issuer;
    }

    @Column(name = "associated_type", nullable = false)
    public Integer getAssociatedType() {
        return associatedType;
    }

    @Column(name = "associated_id", nullable = false)
    public String getAssociatedId() {
        return associatedId;
    }

    @Column(name = "sign_key_encoded", nullable = false, length = 2048)
    public byte[] getSignKeyEncoded() {
        return signKeyEncoded;
    }

    @Column(name = "verify_key_encoded")
    public byte[] getVerifyKeyEncoded() {
        return verifyKeyEncoded;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sign_key_id", nullable = false)
    public CryptographyEntity getCryptography() {
        return cryptography;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public void setAssociatedType(Integer associatedType) {
        this.associatedType = associatedType;
    }

    public void setAssociatedId(String associatedId) {
        this.associatedId = associatedId;
    }

    public void setSignKeyEncoded(byte[] signKeyEncoded) {
        this.signKeyEncoded = signKeyEncoded;
    }

    public void setVerifyKeyEncoded(byte[] verifyKeyEncoded) {
        this.verifyKeyEncoded = verifyKeyEncoded;
    }

    public void setCryptography(CryptographyEntity cryptography) {
        this.cryptography = cryptography;
    }

    @Override
    public void setEntity(CryptographyEntity cryptographyEntity) {
        setCryptography(cryptographyEntity);
    }
}
