/**
 *
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*******************************************************************************
 *
 *
 * MODIFICATION DESCRIPTION
 *
 * Name                 Date                	     Description 
 * ============         =====================        ============
 * Martin Mao           Mar 21, 2015 11:48:44 PM     	     Created
 *
 *
 ********************************************************************************/

package org.scleropages.maldini.security.acl;

import java.io.Serializable;

/**
 * Represents a principal for access control list and access control entry.This
 * means the action was allowed to do somethings for specified resource(s).
 *
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public interface AclPrincipal extends Serializable {

    /**
     * return identifier of this principal.
     *
     * @return
     */
    Serializable id();

    /**
     * return tag used for display.
     *
     * @return
     */
    String tag();
}