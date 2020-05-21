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
package org.scleropages.kapuas.session.provider.shiro;

import org.apache.shiro.subject.ExecutionException;
import org.apache.shiro.web.filter.mgt.FilterChainResolver;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.apache.shiro.web.servlet.AbstractShiroFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * 进入filter 处理链前，就已经初始化好 subject session...<br>
 * 该filter 不进行subject 创建，直接进入处理链
 *
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public class ShiroProcessingFilter extends AbstractShiroFilter {

	protected ShiroProcessingFilter(WebSecurityManager webSecurityManager, FilterChainResolver resolver) {
		super();
		if (webSecurityManager == null) {
			throw new IllegalArgumentException("WebSecurityManager property cannot be null.");
		}
		setSecurityManager(webSecurityManager);
		if (resolver != null) {
			setFilterChainResolver(resolver);
		}
	}

	@Override
	/**
	 * ShiroSessionFilter
	 * 修改默认实现，此处不进行subject创建（直接返回），由{@link ShiroSessionFilter} 实现创建
	 */
	protected void doFilterInternal(ServletRequest servletRequest, ServletResponse servletResponse,
			final FilterChain chain) throws ServletException, IOException {
		Throwable t = null;

		try {
			final ServletRequest request = prepareServletRequest(servletRequest, servletResponse, chain);
			final ServletResponse response = prepareServletResponse(request, servletResponse, chain);

			// SecurityUtils.getSubject().execute(new Callable() {
			// public Object call() throws Exception {
			// updateSessionLastAccessTime(request, response);
			// executeChain(request, response, chain);
			// return null;
			// }
			// });

			// noinspection unchecked
			updateSessionLastAccessTime(request, response);
			executeChain(request, response, chain);

		} catch (ExecutionException ex) {
			t = ex.getCause();
		} catch (Throwable throwable) {
			t = throwable;
		}

		if (t != null) {
			if (t instanceof ServletException) {
				throw (ServletException) t;
			}
			if (t instanceof IOException) {
				throw (IOException) t;
			}
			// otherwise it's not one of the two exceptions expected by the
			// filter method signature - wrap it in one:
			String msg = "Filtered request failed.";
			throw new ServletException(msg, t);
		}
	}
}
