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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.SessionFactory;
import org.apache.shiro.session.mgt.SimpleSession;
import org.apache.shiro.session.mgt.ValidatingSession;
import org.apache.shiro.session.mgt.eis.CachingSessionDAO;
import org.scleropages.crud.FrameworkContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 该实现扩展 {@link CachingSessionDAO} 使其可以作为一级缓存降低远程io开销，当前实现不确保1-2级缓存一致性。
 * 仅用于降低完全依赖远程缓存造成的网络带宽瓶颈，所以为最大减少1级缓存中数据的中间状态持续时间，应将一级缓存策略在满足远程缓存的性能前提下尽量设置的短一些.
 * 此外，每次提供了touch的限流，同样减少每次请求都会去update远程状态，降低网络开销，在touch限流开启的情况下，本地会对每个key维护一个最后更新时间，如果请求间隔
 * 时间小于 minimumTouchTimeInMs，则不会进行remote update，但有一种情况例外，即更新了session attributes，会话属性一旦更新都会调用远程update.
 *
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public abstract class AbstractRemotingSessionDao extends CachingSessionDAO implements InitializingBean {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Value("#{ @environment['session.remote-session.timeout-minutes'] ?: 120 }")
    private long sessionTimeoutInMinutes = 120;

    @Value("#{ @environment['session.remote-session.use-local-cache-first'] ?: true }")
    private boolean useLocalCacheFirst;

    //touch flow-control
    @Value("#{ @environment['session.remote-session.touch-flow-control.enabled'] ?: false }")
    private boolean touchFlowControlEnabled;
    @Value("#{ @environment['session.remote-session.touch-flow-control.min-touch-time-ms'] ?: 60000 }")
    private long minimumTouchTimeInMs;//最小touch间隔

    @Value("#{ @environment['session.remote-session.touch-flow-control.max-monitor-size'] ?: 1000 }")
    private long touchMonitorSize;//监控列表最大大小

    //记录最后一次touch时间，减少touch次数
    private Cache<Serializable, Long> touchTimestamps;

    private SessionFactory sessionFactory;

    @Override
    protected void doUpdate(Session session) {
        if (!needUpdate(session))
            return;
        session.setTimeout(TimeUnit.MINUTES.toMillis(sessionTimeoutInMinutes));//session timeout会被shiro重置
        debugSession("session updating for: {}", session);
        Assert.state(doRemoteUpdate(wrap(session), sessionTimeoutInMinutes), "failure to do session update. may given session may not exists: " + session);
        if (touchFlowControlEnabled)
            touchTimestamps.put(session.getId(), System.currentTimeMillis());
    }


    /**
     * true if success return false may given session not exists
     *
     * @param session
     * @return
     */
    protected abstract boolean doRemoteUpdate(SimpleSessionAdapter session, long sessionTimeoutInMinutes);


    /**
     * return true if current session will updated.
     *
     * @param session
     * @return
     */
    protected boolean needUpdate(Session session) {
        if (!touchFlowControlEnabled)//touch flow control disabled
            return true;
        if (session instanceof ObservableSession && ((ObservableSession) session).attributeChanged()) {//attributes changed.
            ((ObservableSession) session).resetChangeState();
            return true;
        }
        //check time interval
        long now = System.currentTimeMillis();
        try {
            long previouslyTouch = touchTimestamps.get(session.getId(), () -> -1l);
            if (previouslyTouch != -1 && now - previouslyTouch < minimumTouchTimeInMs) {
                logger.debug("ignoring update session when touch frequency is less than minimum interval: now({}) - previouslyTouch({}) ={}< minimumTouchTimeInMs({}) "
                        , now, previouslyTouch, now - previouslyTouch, minimumTouchTimeInMs);
                return false;
            }
            if (session instanceof ObservableSession)
                ((ObservableSession) session).resetChangeState();
            return true;
        } catch (ExecutionException e) {
            throw new IllegalStateException(e);
        }
    }


    @Override
    protected void doDelete(Session session) {
        debugSession("session deleting for: {}", session);
        Assert.state(doRemoteDelete(session), "failure to do session delete. may given session may not exists: " + session);
        if (touchFlowControlEnabled)
            touchTimestamps.invalidate(session.getId());
    }

    /**
     * true if success return false may given session not exists
     *
     * @param session
     * @return
     */
    protected abstract boolean doRemoteDelete(Session session);


    @Override
    protected Serializable doCreate(Session session) {
        Serializable sessionId = generateSessionId(session);
        assignSessionId(session, sessionId);
        session.setTimeout(TimeUnit.MINUTES.toMillis(sessionTimeoutInMinutes));
        debugSession("session creating for: {}", session);
        boolean success = doRemoteCreate(sessionId, wrap(session), sessionTimeoutInMinutes);
        if (!success) {//检查可能的sessionId碰撞，重新生成
            logger.warn("sessionId already used.(Hash collision). if this message high frequency. check your sessionId generator.");
            return doCreate(session);
        }
        if (touchFlowControlEnabled)
            touchTimestamps.put(session.getId(), System.currentTimeMillis());
        return sessionId;
    }

    /**
     * true if success return false may given session already exists
     *
     * @param session
     * @return
     */
    protected abstract boolean doRemoteCreate(Serializable sessionId, SimpleSessionAdapter session, long sessionTimeoutInMinutes);


    @Override
    protected Session doReadSession(Serializable sessionId) {
        if (logger.isDebugEnabled()) {
            logger.debug("session reading for id: " + sessionId);
        }
        SimpleSessionAdapter _session = doRemoteRead(sessionId);
        if (null == _session)
            return null;
        Session session = unwrap(_session);
        cache(session, sessionId);//refresh level-1 cache
        debugSession("session read: {}", session);
        return session;
    }

    protected abstract SimpleSessionAdapter doRemoteRead(Serializable sessionId);


    protected void debugSession(String msg, Session session) {
        if (logger.isDebugEnabled()) {
            ToStringBuilder toStringBuilder = new ToStringBuilder(session, ToStringStyle.MULTI_LINE_STYLE)
                    .append("id", session.getId())
                    .append("host", session.getHost())
                    .append("lastAccess", session.getLastAccessTime())
                    .append("start", session.getStartTimestamp())
                    .append("timeOut", session.getTimeout());
            if (session.getAttributeKeys() != null) {
                session.getAttributeKeys().forEach(key -> {
                    try {
                        toStringBuilder.append("attr-" + key, FrameworkContext.getBean(ObjectMapper.class).writeValueAsString(session.getAttribute(key)));
                    } catch (JsonProcessingException e) {
                        logger.warn("failure to debug session: "+e.getMessage());
                    }
                });
            }
            logger.debug(msg, toStringBuilder.build());
        }
    }


    // Override CachingSessionDAO operations if useLocalCacheFirst=false

    public Session readSession(Serializable sessionId) throws UnknownSessionException {
        if (useLocalCacheFirst)
            return super.readSession(sessionId);
        else {
            Session s = doReadSession(sessionId);
            if (s == null) {
                throw new UnknownSessionException("There is no session with id [" + sessionId + "]");
            }
            return s;
        }
    }

    public Serializable create(Session session) {
        if (useLocalCacheFirst)
            return super.create(session);
        else {
            Serializable sessionId = doCreate(session);
            if (sessionId == null) {
                String msg = "sessionId returned from doCreate implementation is null.  Please verify the implementation.";
                throw new IllegalStateException(msg);
            }
            return sessionId;
        }
    }

    @Override
    public void delete(Session session) {
        if (useLocalCacheFirst)
            super.delete(session);
        else
            doDelete(session);
    }

    @Override
    public void update(Session session) throws UnknownSessionException {
        if (useLocalCacheFirst)
            super.update(session);
        else {
            doUpdate(session);
            if (session instanceof ValidatingSession) {
                if (!((ValidatingSession) session).isValid()) {
                    delete(session);
                }
            }
        }
    }


    protected SimpleSessionAdapter wrap(Session session) {
        return new SimpleSessionAdapter(session);
    }

    protected Session unwrap(SimpleSessionAdapter simpleSessionAdapter) {
        return simpleSessionAdapter.populateProperties(
                null != sessionFactory ?
                        sessionFactory instanceof ObservableSessionFactory ?
                                new ObservableSession(((ObservableSessionFactory) sessionFactory).getAttributeChangesConsumers()) : new SimpleSession() :
                        new SimpleSession());
    }


    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (touchFlowControlEnabled)
            touchTimestamps = CacheBuilder.newBuilder().maximumSize(touchMonitorSize).expireAfterWrite(minimumTouchTimeInMs, TimeUnit.MILLISECONDS).build();
    }

}
