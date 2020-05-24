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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.scleropages.crud.dao.orm.jpa.entity.IdEntity;
import org.scleropages.crud.types.Available;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
@Entity
@Table(name = "sec_authc")
@SequenceGenerator(name = "authc_id", sequenceName = "seq_sec_authc", allocationSize = IdEntity.SEQ_DEFAULT_ALLOCATION_SIZE, initialValue = IdEntity.SEQ_DEFAULT_INITIAL_VALUE)
public class AuthenticationEntity extends IdEntity implements Available {

    /* authentication basic info */
    private String principal; // required; unique
    private String credentials; // required
    private Integer associatedType; // required
    private String associatedId; //optional if associated from here.
    /* authentication status */
    private Boolean enabled;// required
    private Boolean expired;// required
    private Boolean locked;// required
    private Boolean credentialsExpired;// required
    /* hash the plain-text password with the random salt bytes*/
    private byte[] secureSalt;// optional

    // ===================helper methods=====================//

    public AuthenticationEntity() {
    }

    public AuthenticationEntity(String principal, String credentials, Integer associatedType) {
        this();
        this.principal = principal;
        this.credentials = credentials;
        this.associatedType = associatedType;
    }

    public void activate() {
        this.enabled = true;
        this.expired = false;
        this.locked = false;
        this.credentialsExpired = false;
    }

    // ===================getters=====================//

    @Column(name = "principal_", nullable = false, unique = true)
    public String getPrincipal() {
        return principal;
    }


    @Column(name = "credentials_", nullable = false)
    public String getCredentials() {
        return credentials;
    }


    @Column(name = "associated_type", nullable = false)
    public Integer getAssociatedType() {
        return associatedType;
    }

    @Column(name = "associated_id")
    public String getAssociatedId() {
        return associatedId;
    }

    @Column(name = "enabled_", nullable = false)
    public Boolean getEnabled() {
        return enabled;
    }

    @Column(name = "expired_", nullable = false)
    public Boolean getExpired() {
        return expired;
    }

    @Column(name = "locked_", nullable = false)
    public Boolean getLocked() {
        return locked;
    }

    @Column(name = "credentials_expired", nullable = false)
    public Boolean getCredentialsExpired() {
        return credentialsExpired;
    }

    @Column(name = "secure_salt")
    public byte[] getSecureSalt() {
        return secureSalt;
    }

    // ===================setters=====================//
    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public void setCredentials(String credentials) {
        this.credentials = credentials;
    }

    public void setAssociatedType(Integer associatedType) {
        this.associatedType = associatedType;
    }

    public void setAssociatedId(String associatedId) {
        this.associatedId = associatedId;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public void setExpired(Boolean expired) {
        this.expired = expired;
    }

    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

    public void setCredentialsExpired(Boolean credentialsExpired) {
        this.credentialsExpired = credentialsExpired;
    }

    public void setSecureSalt(byte[] secureSalt) {
        this.secureSalt = secureSalt;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AuthenticationEntity)) {
            return false;
        }

        final AuthenticationEntity token = (AuthenticationEntity) o;

        return !(principal != null ? !principal.equals(token.getPrincipal()) : token.getPrincipal() != null);

    }

    public int hashCode() {
        return (principal != null ? principal.hashCode() : 0);
    }

    public String toString() {
        ToStringBuilder sb = new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("principal", this.principal).append("credentials", "[*******]").append("enabled", this.enabled)
                .append("accountExpired", this.expired).append("credentialsExpired", this.credentialsExpired)
                .append("accountLocked", this.locked);
        return sb.toString();
    }

    @Override
    public void enable() {
        setEnabled(true);
    }

    @Override
    public void disable() {
        setEnabled(false);
    }

    @Override
    public boolean availableState() {
        return getEnabled();
    }
}
