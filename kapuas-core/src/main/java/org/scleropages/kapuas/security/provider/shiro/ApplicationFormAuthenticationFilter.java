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
package org.scleropages.kapuas.security.provider.shiro;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * <pre>
 * 扩展shiro默认实现，增加特性如下：
 * 登录请求刷新之前session，提高安全性，即确保登录前后使用不同的sessionId
 * </pre>
 *
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public class ApplicationFormAuthenticationFilter extends FormAuthenticationFilter {

    protected final Logger logger = LoggerFactory.getLogger(getClass());


    @Value("#{ @environment['shiro.authc.session-refresh-enabled'] ?: true }")
    protected boolean refreshSessionOnLogin;

    @Override
    protected boolean executeLogin(ServletRequest request, ServletResponse response) throws Exception {
        AuthenticationToken token = createToken(request, response);
        if (token == null) {
            String msg = "createToken method implementation returned null. A valid non-null AuthenticationToken " +
                    "must be created in order to execute a login attempt.";
            throw new IllegalStateException(msg);
        }
        try {
            Subject subject = getSubject(request, response);
            refreshSessionIfNecessary(subject);
            subject.login(token);
            return onLoginSuccess(token, subject, request, response);
        } catch (AuthenticationException e) {
            return onLoginFailure(token, e, request, response);
        }
    }


    protected void refreshSessionIfNecessary(Subject subject) {
        if (!refreshSessionOnLogin)
            return;
        Session previouslySession = subject.getSession();
        Collection<Object> attributeKeys = previouslySession.getAttributeKeys();
        logger.debug("refreshing... session[{}]", previouslySession.getId());
        if (null != attributeKeys && attributeKeys.size() > 0) {
            logger.debug("detected previously session[{}] has {} attributes will migrating....", previouslySession.getId(), attributeKeys.size());
            Map<Object, Object> previouslySessionAttributes = new HashMap<>(attributeKeys.size());
            attributeKeys.forEach(key -> {
                previouslySessionAttributes.put(key, previouslySession.getAttribute(key));
            });
            subject.logout();
            Session currentlySession = subject.getSession();
            previouslySessionAttributes.forEach((key, value) -> currentlySession.setAttribute(key, value));
            logger.debug("successfully migrated session[{}] attributes from previously session[{}] with {} attributes",
                    currentlySession.getId(), previouslySession.getId(), previouslySessionAttributes.size());
        } else {
            subject.logout();
        }
    }
}
