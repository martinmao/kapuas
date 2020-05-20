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

package org.scleropages.maldini.session;

import org.scleropages.maldini.session.provider.shiro.ShiroSessionHolderStrategy;

import java.io.Serializable;

/**
 * 
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 * 
 */
public abstract class SessionHolder {

	private static SessionHolderStrategy strategy = new ShiroSessionHolderStrategy();

	public static void clearSession() {
		strategy.clearSession();
	}

	public static Session getSession() {
		return strategy.getSession();
	}

	public static Object getAttribute(Object key) {
		return getSession().getAttribute(key);
	}

	public static void setAttribute(Object key, Object value) {
		getSession().setAttribute(key, value);
	}

	public static SessionHolderStrategy getSessionHolderStrategy() {
		return strategy;
	}

	public static Serializable getId() {
		return getSession().getId();
	}

}
