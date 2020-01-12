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
package org.scleropages.maldini.security.acl.model;

import javax.validation.constraints.NotEmpty;

/**
 * Represents how acl struct.Used field (expression) to defined.<br>
 * <p>
 * There are support tow defining format:
 * <pre>
 * example permission simple format: publish=发布,subscribe=订阅
 *
 * example permission support-inherit format: administration=管理>write=写>read=读>execute=执行
 * </pre>
 *
 * <B>
 * NOTE: not support Mixed mode for permission separator( '>' or ',' Only one you can be selected).
 * </B>
 *
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public class AclStrategy {

    public static final String EXP_FORMAT_PERMISSION_SEPARATOR = ",";
    public static final String EXP_FORMAT_PERMISSION_INHERIT_SEPARATOR = ">";
    public static final String EXP_FORMAT_PERMISSION_NAME_TAG_SEPARATOR = "=";

    private String resource;

    /**
     * example permission simple format: publish=发布,subscribe=订阅
     * example permission support-inherit format: administration=管理>write=写>read=读>execute=执行
     */
    private String expression;

    private PermissionModel[] permissions;

    public AclStrategy() {
    }

    public AclStrategy(String resource, PermissionModel[] permissions) {
        this.resource = resource;
        this.permissions = permissions;
    }

    @NotEmpty(groups = CreateModel.class)
    public String getResource() {
        return resource;
    }

    public String getExpression() {
        return expression;
    }


    public void setResource(String resource) {
        this.resource = resource;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public PermissionModel[] getPermissions() {
        return permissions;
    }

    public interface CreateModel {

    }
}
