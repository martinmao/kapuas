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
package org.scleropages.maldini.session.provider.shiro;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.servlet.Cookie;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;

/**
 * 对于 web-cookie 不支持应用，提供该类进行适配，通过将一个 {@link CookieProxy} 执行相关cookie操作
 *
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public class ProxyCookie extends SimpleCookie {

    private final CookieProxy cookieProxy;

    public interface CookieProxy {

        /**
         * <p>
         * {@link org.apache.shiro.web.session.mgt.DefaultWebSessionManager#storeSessionId(Serializable, HttpServletRequest, HttpServletResponse)}以及
         * {@link org.apache.shiro.web.mgt.CookieRememberMeManager#rememberSerializedIdentity(Subject, byte[])}
         * 为防止cookie在服务端泄露均使用硬编码方式强制使用{@link SimpleCookie}写cookie进客户端，目前扩展该方法没有实际效果，
         * 除非扩展DefaultWebSessionManager,CookieRememberMeManager 来使用 {@link CookieProxy}代替 {@link SimpleCookie}。改动较大不利于后期版本升级
         *</p>
         * <note>其中客户端sessionId cookie较容易获取，直接通过getSession().getId()解决，但rememberMe cookie除非扩展CookieRememberMeManager</note>
         * @param cookie
         * @param request
         * @param response
         */
        default void saveCookie(Cookie cookie, HttpServletRequest request, HttpServletResponse response) {
        }

        /**
         * 客户端移除cookie处理可能性较多，可通过专门接口，由客户端主动调用移除
         *
         * @param cookie
         * @param request
         * @param response
         */
        default void removeCookie(Cookie cookie, HttpServletRequest request, HttpServletResponse response) {
        }

        String readCookieValue(HttpServletRequest request, HttpServletResponse response);

    }

    public ProxyCookie(String name, CookieProxy cookieProxy) {
        super(name);
        this.cookieProxy = cookieProxy;
    }

    public ProxyCookie(Cookie cookie, CookieProxy cookieProxy) {
        super(cookie);
        this.cookieProxy = cookieProxy;
    }

    public ProxyCookie(CookieProxy cookieProxy) {
        this.cookieProxy = cookieProxy;
    }

    @Override
    public void saveTo(HttpServletRequest request, HttpServletResponse response) {
        super.saveTo(request, response);
    }

    @Override
    public void removeFrom(HttpServletRequest request, HttpServletResponse response) {
        super.removeFrom(request, response);
    }

    @Override
    public String readValue(HttpServletRequest request, HttpServletResponse response) {
        String cookieValue = cookieProxy.readCookieValue(request, response);
        return StringUtils.hasText(cookieValue) ? cookieValue : super.readValue(request, response);
    }
}
