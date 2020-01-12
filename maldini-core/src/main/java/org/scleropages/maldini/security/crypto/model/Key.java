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
package org.scleropages.maldini.security.crypto.model;

import org.scleropages.crud.types.Available;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Null;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public class Key implements Available {

    public static final int KEY_TYPE = 0;
    public static final int KEY_PAIR_TYPE = 1;
    public static final int KEY_STORE_TYPE = 2;

    private Long id;

    private String algorithm;

    private String format;

    private byte[] encoded;

    private String keyPassword;

    private Boolean enabled;

    @Null
    public Long getId() {
        return id;
    }

    @NotBlank
    public String getAlgorithm() {
        return algorithm;
    }

    @Null
    public String getFormat() {
        return format;
    }

    @Null
    public byte[] getEncoded() {
        return encoded;
    }

    public String getKeyPassword() {
        return keyPassword;
    }

    @Null
    public Boolean getEnabled() {
        return enabled;
    }


    public void setId(Long id) {
        this.id = id;
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

    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void enable() {
        this.enabled = true;
    }

    @Override
    public void disable() {
        this.enabled = false;
    }

    @Override
    public boolean isAvailable() {
        return enabled;
    }
}
