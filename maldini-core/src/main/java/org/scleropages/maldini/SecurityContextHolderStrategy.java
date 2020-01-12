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
 * Martin Mao           Apr 1, 2015 1:37:19 AM     	     Created
 *
 *
 ********************************************************************************/

package org.scleropages.maldini;

/**
 * Determine how to lookup {@link SecurityContext}
 *
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public interface SecurityContextHolderStrategy {

    void clearContext();

    SecurityContext getContext();

    void setContext(SecurityContext context);

    SecurityContext createEmptyContext();
}
