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
package org.scleropages.kapuas.session.provider.shiro;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.SimpleSession;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * {@link SimpleSession} 自实现的JDK 序列化方法，将所有字段定义为transient,会导致其他系列化工具无法工作，该适配器用于包装 {@link SimpleSession}，
 * 子类可扩展该类使其兼容各种序列化方案
 *
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public class SimpleSessionAdapter implements Serializable{


    private static final long serialVersionUID = 1l;


    private Serializable id;
    private Date startTimestamp;
    private Date stopTimestamp;
    private Date lastAccessTime;
    private long timeout;
    private boolean expired;
    private String host;
    private Map<Object, Object> attributes;


    /**
     * just used for serialize provider
     */
    public SimpleSessionAdapter() {
    }

    public SimpleSessionAdapter(Session session) {
        if (!(session instanceof SimpleSession))
            throw new IllegalArgumentException("given session not supported: " + session);
        SimpleSession simpleSession = (SimpleSession) session;
        this.id = simpleSession.getId();
        this.startTimestamp = simpleSession.getStartTimestamp();
        this.stopTimestamp = simpleSession.getStopTimestamp();
        this.lastAccessTime = simpleSession.getLastAccessTime();
        this.timeout = simpleSession.getTimeout();
        this.expired = simpleSession.isExpired();
        this.host = simpleSession.getHost();
        this.attributes = simpleSession.getAttributes();
    }


    public Session populateProperties(SimpleSession simpleSession) {
        //clear status.
        simpleSession.setTimeout(0);
        simpleSession.setStartTimestamp(null);
        simpleSession.setLastAccessTime(null);

        //populates
        simpleSession.setLastAccessTime(getLastAccessTime());
        simpleSession.setStartTimestamp(getStartTimestamp());
        simpleSession.setTimeout(getTimeout());
        simpleSession.setId(getId());
        simpleSession.setExpired(isExpired());
        simpleSession.setHost(getHost());
        simpleSession.setStopTimestamp(getStopTimestamp());
        simpleSession.setAttributes(getAttributes());

        return simpleSession;
    }

    public Serializable getId() {
        return id;
    }

    public void setId(Serializable id) {
        this.id = id;
    }

    public Date getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(Date startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public Date getStopTimestamp() {
        return stopTimestamp;
    }

    public void setStopTimestamp(Date stopTimestamp) {
        this.stopTimestamp = stopTimestamp;
    }

    public Date getLastAccessTime() {
        return lastAccessTime;
    }

    public void setLastAccessTime(Date lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Map<Object, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<Object, Object> attributes) {
        this.attributes = attributes;
    }
}
