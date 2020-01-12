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

import org.apache.shiro.SecurityUtils;
import org.scleropages.maldini.session.Session;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

/**
 * 
 *
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public class ShiroSession implements Session {

	public static final ShiroSession DEFAULT_SHIRO_SESSION = new ShiroSession();
	
	private ShiroSession(){
		
	}

	protected org.apache.shiro.session.Session getShiroSession() {
		org.apache.shiro.session.Session session= SecurityUtils.getSubject().getSession();

		return session;
	}

	@Override
	public Serializable getId() {
		return getShiroSession().getId();
	}

	@Override
	public Date getStartTimestamp() {
		return getShiroSession().getStartTimestamp();
	}

	@Override
	public Date getLastAccessTime() {
		return getShiroSession().getLastAccessTime();
	}

	@Override
	public long getTimeout() {
		return getShiroSession().getTimeout();
	}

	@Override
	public void setTimeout(long maxIdleTimeInMillis) {
		getShiroSession().setTimeout(maxIdleTimeInMillis);
	}

	@Override
	public String getHost() {
		return getShiroSession().getHost();
	}

	@Override
	public void touch() {
		getShiroSession().touch();
	}

	@Override
	public void stop() {
		getShiroSession().stop();
	}

	@Override
	public Collection<Object> getAttributeKeys() {
		return getShiroSession().getAttributeKeys();
	}

	@Override
	public Object getAttribute(Object key) {
		return getShiroSession().getAttribute(key);
	}

	@Override
	public void setAttribute(Object key, Object value) {
		getShiroSession().setAttribute(key, value);
	}

	@Override
	public Object removeAttribute(Object key) {
		return getShiroSession().removeAttribute(key);
	}

}
