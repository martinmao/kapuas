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

import org.scleropages.kapuas.security.acl.Permission;

import java.io.Serializable;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public class PermissionModel implements Permission {

    private String id;

    private String tag;

    private String name;

    private String extension;


    public PermissionModel() {
    }

    public PermissionModel(String name) {
        this.name = name;
    }


    public String getId() {
        return id;
    }

    public String getTag() {
        return tag;
    }

    public String getName() {
        return name;
    }

    public String getExtension() {
        return extension;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    @Override
    public Serializable id() {
        return id;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String tag() {
        return tag;
    }
}
