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

package org.scleropages.maldini.security;


import org.scleropages.maldini.security.authc.provider.Authenticated;

import java.io.Serializable;
import java.security.Principal;
import java.util.Date;

/**
 * Interface defining the minimum security information.
 *
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public interface SecurityContext extends Serializable, Principal {

    /**
     * return true if current principal already authenticated.
     *
     * @return
     */
    boolean isAuthenticated();

    /**
     * return true if current principal authenticated by remember-me service(if enable remember-me feature).
     *
     * @return
     */
    boolean isRemembered();


    /**
     * return principal if current principal already authenticated.
     *
     * @return
     */
    Object getPrincipal();

    /**
     * return additional info by current principal if authenticated.
     *
     * @return
     */
    AuthenticationDetails getDetails();

    /**
     * return authenticated time.
     *
     * @return
     */
    Date getAuthenticatedTime();

    /**
     * return host of remote.
     *
     * @return
     */
    String getHost();

    /**
     * Return authenticated information.
     * @return
     */
    Authenticated getAuthenticated();
}
