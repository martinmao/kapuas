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
package org.scleropages.kapuas.security.authc.model;

import org.scleropages.kapuas.security.authc.Authentication;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public class AuthenticationModel implements Authentication {


    private Long id;
    private String principal;
    private String credentials;
    private Integer associatedType;
    private String associatedId;

    private Boolean enabled;
    private Boolean expired;
    private Boolean locked;
    private Boolean credentialsExpired;

    private byte[] secureSalt;

    public AuthenticationModel() {
    }

    public AuthenticationModel(String principal, String credentials) {
        this.principal = principal;
        this.credentials = credentials;
    }

    @NotNull(groups = {Update.class})
    @Null(groups = {Create.class})
    public Long getId() {
        return id;
    }

    @NotEmpty(groups = {Create.class})
    @Null(groups = {Update.class})
    public String getPrincipal() {
        return principal;
    }

    @NotEmpty(groups = {Create.class})
    @Null(groups = {Update.class})
    public String getCredentials() {
        return credentials;
    }

    @Null(groups = {Create.class, Update.class})
    public byte[] getSecureSalt() {
        return secureSalt;
    }

    public Integer getAssociatedType() {
        return associatedType;
    }

    public String getAssociatedId() {
        return associatedId;
    }

    @Null(groups = {Create.class, Update.class})
    public Boolean getEnabled() {
        return enabled;
    }

    @Null(groups = {Create.class, Update.class})
    public Boolean getExpired() {
        return expired;
    }

    @Null(groups = {Create.class, Update.class})
    public Boolean getLocked() {
        return locked;
    }

    @Null(groups = {Create.class, Update.class})
    public Boolean getCredentialsExpired() {
        return credentialsExpired;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
        if(enabled){
            setExpired(false);
            setLocked(false);
            setCredentialsExpired(false);
        }
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

    public static interface Create {
    }

    public static interface Update {
    }
}
