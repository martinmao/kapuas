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
package org.scleropages.kapuas.security.crypto.model;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.util.List;


/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public class Cryptography {

    private Long id;

    private Integer associatedType;

    private String associatedId;

    private String name;

    private String algorithm;

    private String mode;

    private String padding;

    private Integer blockSize;

    private Integer initializationVectorSize;

    private String description;

    private String keyAlgorithm;

    private Integer keySize;

    private List<Key> keys;

    @Null(groups = {Create.class})
    public Long getId() {
        return id;
    }

    @NotNull(groups = {Create.class})
    public Integer getAssociatedType() {
        return associatedType;
    }

    @NotEmpty(groups = {Create.class})
    public String getAssociatedId() {
        return associatedId;
    }

    @NotEmpty(groups = {Create.class})
    public String getName() {
        return name;
    }

    @NotEmpty(groups = {Create.class})
    public String getAlgorithm() {
        return algorithm;
    }


    public String getMode() {
        return mode;
    }

    public String getPadding() {
        return padding;
    }

    public Integer getBlockSize() {
        return blockSize;
    }

    public Integer getInitializationVectorSize() {
        return initializationVectorSize;
    }

    @NotEmpty(groups = {Create.class})
    public String getDescription() {
        return description;
    }

    @NotEmpty(groups = {Create.class})
    public String getKeyAlgorithm() {
        return keyAlgorithm;
    }

    @NotNull(groups = {Create.class})
    public Integer getKeySize() {
        return keySize;
    }

    @Null
    public List<Key> getKeys() {
        return keys;
    }

    public void setId(Long id) {
        this.id = id;
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

    public void setKeySize(Integer keySize) {
        this.keySize = keySize;
    }

    public void setKeyAlgorithm(String keyAlgorithm) {
        this.keyAlgorithm = keyAlgorithm;
    }

    public void setKeys(List<Key> keys) {
        this.keys = keys;
    }

    public interface Create {

    }
}
