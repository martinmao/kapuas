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
package org.scleropages.kapuas.security.acl.model;

import org.scleropages.kapuas.security.acl.Resource;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.Map;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public class ResourceModel implements Resource {

    private String id;
    private String tag;
    private String type;
    private String owner;
    private Long typeId;

    private Map<String, Object> variables;
    private String bizPayload;
    private Map<String,Object> bizBody;


    @NotEmpty(groups = {CreateModel.class})
    @NotEmpty(groups = {ReadAclModel.class})
    @NotEmpty(groups = {ReadEntriesBySpecifyResource.class})
    @NotEmpty(groups = {UpdateModel.class})
    public String getId() {
        return id;
    }

    @NotEmpty(groups = {CreateModel.class})
    public String getTag() {
        return tag;
    }

    @NotEmpty(groups = {CreateModel.class})
    @NotEmpty(groups = {ReadAclModel.class})
    @NotEmpty(groups = {ReadEntriesBySpecifyResource.class})
    @NotEmpty(groups = {ReadEntriesBySpecifyResourceType.class})
    @NotEmpty(groups = {UpdateModel.class})
    public String getType() {
        return type;
    }

    @NotEmpty(groups = {CreateModel.class})
    public String getOwner() {
        return owner;
    }

    public Long getTypeId() {
        return typeId;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public String getBizPayload() {
        return bizPayload;
    }

    public Map<String, Object> getBizBody() {
        return bizBody;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }

    public void setBizPayload(String bizPayload) {
        this.bizPayload = bizPayload;
    }

    public void setBizBody(Map<String, Object> bizBody) {
        this.bizBody = bizBody;
    }

    @Override
    public Serializable id() {
        return id;
    }

    @Override
    public String tag() {
        return tag;
    }

    @Override
    public Serializable type() {
        return type;
    }

    public interface CreateModel {

    }

    public interface ReadAclModel {

    }

    public interface ReadEntriesBySpecifyResource {

    }

    public interface ReadEntriesBySpecifyResourceType {

    }

    public interface UpdateModel {

    }
}
