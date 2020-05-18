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

import org.scleropages.crud.exception.BizStateViolationException;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public class SecurityBizException extends BizStateViolationException {


    public static final String AUTHC_FAILURE = "AUTHC_FAILURE";

    public static final String AUTHC_FAILURE_MESSAGE = "Bad Credentials.";

    public static final String AUTHZ_FAILURE = "AUTHZ_FAILURE";

    public static final String AUTHZ_FAILURE_MESSAGE = "Access Denied.";


    public SecurityBizException(String message) {
        super(message);
    }

    public SecurityBizException(Throwable cause) {
        super(cause);
    }

    public SecurityBizException(String message, Throwable cause) {
        super(message, cause);
    }

    public SecurityBizException(String code, String message) {
        super(code, message);
    }

    public SecurityBizException(String code, String message, Throwable cause) {
        super(code, message, cause);
    }
}
