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
package org.scleropages.kapuas.bizmodel.security;

/**
 *
 *
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public class JwtTokenTemplateModel {

    private String subject;
    private String issuer;
    private Long expiration;
    private Integer associatedType;
    private String associatedId;
    private String algorithm;
    private byte[] signKeyEncoded;
    private byte[] verifyKeyEncoded;


    public String getSubject() {
        return subject;
    }

    public String getIssuer() {
        return issuer;
    }

    public Long getExpiration() {
        return expiration;
    }

    public Integer getAssociatedType() {
        return associatedType;
    }

    public String getAssociatedId() {
        return associatedId;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public byte[] getSignKeyEncoded() {
        return signKeyEncoded;
    }

    public byte[] getVerifyKeyEncoded() {
        return verifyKeyEncoded;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public void setExpiration(Long expiration) {
        this.expiration = expiration;
    }

    public void setAssociatedType(Integer associatedType) {
        this.associatedType = associatedType;
    }

    public void setAssociatedId(String associatedId) {
        this.associatedId = associatedId;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public void setSignKeyEncoded(byte[] signKeyEncoded) {
        this.signKeyEncoded = signKeyEncoded;
    }

    public void setVerifyKeyEncoded(byte[] verifyKeyEncoded) {
        this.verifyKeyEncoded = verifyKeyEncoded;
    }
}
