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
package org.scleropages.kapuas.security.acl;

import java.io.Serializable;

/**
 * Represents an access control list (Acl) entry for specify resource.
 *
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public interface AclEntry {

    /**
     * Return identity of this acl entry.
     *
     * @return
     */
    Serializable id();


    /**
     * Return associated {@link Acl} of this entry.
     *
     * @return
     */
    Acl acl();


    /**
     * Return {@link AclPrincipal} (who) of this entry was granted.
     *
     * @return
     */
    AclPrincipal grant();

    /**
     * Return {@link Permission} (action) of this entry was granted.
     *
     * @return
     */
    Permission permission();

    /**
     * Return associated {@link Resource} of this entry.
     *
     * @return
     */
    Resource resource();
}
