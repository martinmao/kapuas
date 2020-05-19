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
package org.scleropages.maldini.security.provider.shiro;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authz.AuthorizationException;
import org.scleropages.crud.exception.BizExceptionTranslator;
import org.scleropages.maldini.security.SecurityBizException;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
@Component
public class SecurityBizExceptionTranslator implements BizExceptionTranslator<SecurityBizException> {
    @Override
    public SecurityBizException translation(MethodInvocation invocation, Exception e) {
        SecurityBizException securityBizException = e instanceof SecurityBizException ? (SecurityBizException) e : new SecurityBizException(e.getMessage(), e);
        if (e instanceof AuthenticationException) {
            securityBizException.setConstraintViolations(new String[]{SecurityBizException.AUTHC_FAILURE});
        } else if (e instanceof AuthorizationException) {
            securityBizException.setConstraintViolations(new String[]{SecurityBizException.AUTHZ_FAILURE});
        } else {
            if(Objects.equals(securityBizException.getMessage(),SecurityBizException.AUTHC_FAILURE_MESSAGE))
                securityBizException.setConstraintViolations(new String[]{SecurityBizException.AUTHC_FAILURE});
            else if(Objects.equals(securityBizException.getMessage(),SecurityBizException.AUTHZ_FAILURE_MESSAGE))
                securityBizException.setConstraintViolations(new String[]{SecurityBizException.AUTHZ_FAILURE});
        }
        return securityBizException;
    }

    @Override
    public boolean support(Exception e) {
        if (e instanceof AuthenticationException)
            return true;
        if (e instanceof AuthorizationException)
            return true;
        if (e instanceof SecurityBizException)
            return true;
        return false;
    }
}
