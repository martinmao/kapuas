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

import org.scleropages.crud.types.EntryList;
import org.scleropages.kapuas.security.acl.Resource;
import org.scleropages.openapi.annotation.ApiIgnore;

import javax.validation.constraints.NotEmpty;
import java.beans.Transient;
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
    @ApiIgnore
    private Long typeId;

    private Map<String, Object> variables;
    private String bizPayload;
    private Map<String, Object> bizBody;


    //just used for frontend open-api
    private EntryList<String, Object> variableEntries;
    private EntryList<String, Object> bizBodyEntries;


    @NotEmpty(groups = {Create.class, Update.class, ReadAcl.class, ReadEntriesBySpecifyResource.class})
    @ApiIgnore({Update.class})
    public String getId() {
        return id;
    }

    @NotEmpty(groups = {Create.class})
    public String getTag() {
        return tag;
    }

    @NotEmpty(groups = {Create.class, Update.class, ReadAcl.class, ReadEntriesBySpecifyResource.class, ReadEntriesBySpecifyResourceType.class})
    @ApiIgnore({Update.class})
    public String getType() {
        return type;
    }

    @NotEmpty(groups = {Create.class})
    public String getOwner() {
        return owner;
    }

    public Long getTypeId() {
        return typeId;
    }

    @Transient
    public Map<String, Object> getVariables() {
        return variables;
    }


    public String getBizPayload() {
        return bizPayload;
    }

    @Transient
    public Map<String, Object> getBizBody() {
        return bizBody;
    }

    public EntryList<String, Object> getVariableEntries() {
        return variableEntries;
    }

    public EntryList<String, Object> getBizBodyEntries() {
        return bizBodyEntries;
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
        this.variableEntries = new EntryList().fromMap(variables);
    }

    public void setBizPayload(String bizPayload) {
        this.bizPayload = bizPayload;
    }

    public void setBizBody(Map<String, Object> bizBody) {
        this.bizBody = bizBody;
        this.bizBodyEntries = new EntryList().fromMap(bizBody);
    }

    public void setVariableEntries(EntryList<String, Object> variableEntries) {
        this.variableEntries = variableEntries;
        this.variables = variableEntries.toMap();
    }

    public void setBizBodyEntries(EntryList<String, Object> bizBodyEntries) {
        this.bizBodyEntries = bizBodyEntries;
        this.bizBody = bizBodyEntries.toMap();
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

    public interface Create {

    }

    public interface ReadAcl {

    }

    public interface ReadEntriesBySpecifyResource {

    }

    public interface ReadEntriesBySpecifyResourceType {

    }

    public interface Update {

    }
}
