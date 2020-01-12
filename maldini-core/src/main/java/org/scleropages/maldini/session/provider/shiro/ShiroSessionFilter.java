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
package org.scleropages.maldini.session.provider.shiro;

import org.apache.shiro.subject.ExecutionException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.apache.shiro.web.servlet.OncePerRequestFilter;
import org.apache.shiro.web.subject.WebSubject;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * 进入 shiro filter 处理链前，就已经初始化好(确保定义shirofilter之前的filter可以使用shiro session) subject session...<br>
 * 
 * 该filter只执行创建
 *
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public class ShiroSessionFilter extends OncePerRequestFilter {

	private WebSecurityManager webSecurityManager;

	@Override
	protected void doFilterInternal(final ServletRequest request, final ServletResponse response,
			final FilterChain chain) throws ServletException, IOException {
		Throwable t = null;
		try {
			Subject subject = new WebSubject.Builder(webSecurityManager, request, response).buildWebSubject();
			/*
			 * Associates the specified Callable with this Subject instance and
			 * then executes it on the currently running thread
			 */
			subject.execute(new Callable<Object>() {
				public Object call() throws Exception {
					chain.doFilter(request, response);
					return null;
				}
			});
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

	public void setWebSecurityManager(WebSecurityManager webSecurityManager) {
		this.webSecurityManager = webSecurityManager;
	}
}
