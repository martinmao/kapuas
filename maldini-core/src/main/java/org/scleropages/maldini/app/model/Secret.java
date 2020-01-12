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
package org.scleropages.maldini.app.model;

import org.scleropages.crud.types.Available;
import org.scleropages.maldini.security.crypto.model.Key;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.util.List;

import static org.scleropages.maldini.security.crypto.model.Key.*;


/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public class Secret implements Available {

    private Long id;

    private String secretId;

    private String name;

    private String description;

    private Boolean enabled;

    private String algorithm;

    private Integer keySize;

    private Integer keyType;

    private List<Key> keys;

    @Null(groups = {CreateModel.class})
    @Null(groups = {AlgorithmSpecificCreateModel.class})
    public Long getId() {
        return id;
    }

    @Null
    @Null(groups = {AlgorithmSpecificCreateModel.class})
    public String getSecretId() {
        return secretId;
    }

    @NotEmpty(groups = {CreateModel.class})
    @NotEmpty(groups = {AlgorithmSpecificCreateModel.class})
    public String getName() {
        return name;
    }

    @NotEmpty(groups = {CreateModel.class})
    @NotEmpty(groups = {AlgorithmSpecificCreateModel.class})
    public String getDescription() {
        return description;
    }

    @Null
    public Boolean getEnabled() {
        return enabled;
    }

    @NotEmpty(groups = {CreateModel.class})
    @Null(groups = {AlgorithmSpecificCreateModel.class})
    public String getAlgorithm() {
        return algorithm;
    }

    @NotNull(groups = {CreateModel.class})
    @NotNull(groups = {AlgorithmSpecificCreateModel.class})
    public Integer getKeySize() {
        return keySize;
    }

    @NotNull(groups = {CreateModel.class})
    @Min(value = KEY_TYPE, groups = {CreateModel.class})
    @Max(value = KEY_STORE_TYPE, groups = {CreateModel.class})
    @Null(groups = {AlgorithmSpecificCreateModel.class})
    public Integer getKeyType() {
        return keyType;
    }


    @Null
    public List<Key> getKeys() {
        return keys;
    }


    public void setId(Long id) {
        this.id = id;
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

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public void setKeySize(Integer keySize) {
        this.keySize = keySize;
    }

    public void setKeyType(Integer keyType) {
        this.keyType = keyType;
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

    public boolean isKeyPairType() {
        return keyType == KEY_PAIR_TYPE;
    }

    public boolean isKeyType() {
        return keyType == KEY_TYPE;
    }

    public boolean isKeyStoreType() {
        return keyType == KEY_STORE_TYPE;
    }

    public void setKeys(List<Key> keys) {
        this.keys = keys;
    }

    public interface CreateModel {

    }

    public interface AlgorithmSpecificCreateModel {

    }
}
