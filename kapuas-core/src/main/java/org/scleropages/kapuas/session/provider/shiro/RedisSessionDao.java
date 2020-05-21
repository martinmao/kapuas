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
import org.scleropages.connector.redis.RedisClient;
import org.scleropages.connector.redis.RedisKey;
import org.scleropages.serialize.LookupSerializerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public class RedisSessionDao extends AbstractRemotingSessionDao implements InitializingBean {

    private final RedisClient redisClient;

    @Value("#{ @environment['session.remote-session.redis.key-namespace'] ?: 'shiro' }")
    private String sessionKeyCatalog;

    @Value("#{ @environment['session.remote-session.redis.key-type'] ?: 'simple_session' }")
    private String sessionKeyType;

    @Value("#{ @environment['session.remote-session.redis.slot-key'] ?: null }")
    private String slotKey;

    @Value("#{ @environment['session.remote-session.redis.serialize'] ?: null }")
    private String serialize;

    private LookupSerializerFactory serializerFactory;


    public RedisSessionDao(RedisClient redisClient) {
        this.redisClient = redisClient;
    }


    /**
     * 执行序列化实现切换
     *
     * @param redisOp
     * @param <T>
     * @return
     */
    protected <T> T execute(Function<RedisClient, T> redisOp) {
        if (StringUtils.hasText(serialize) && null != serializerFactory) {
            serializerFactory.setCurrentLookupKey(serialize);
            try {
                return redisOp.apply(redisClient);
            } finally {
                serializerFactory.resetCurrentLookupKey();
            }
        }
        return redisOp.apply(redisClient);
    }


    @Override
    protected boolean doRemoteUpdate(SimpleSessionAdapter session, long sessionTimeoutInMinutes) {
        return execute(redis ->
                redisClient.objectValueOperations().setIfPresent(fromSessionId(session.getId()), session, sessionTimeoutInMinutes, TimeUnit.MINUTES));
    }


    @Override
    protected boolean doRemoteDelete(Session session) {
        return execute(redis -> redis.objectValueTemplate().delete(fromSession(session)));
    }


    @Override
    protected boolean doRemoteCreate(Serializable sessionId, SimpleSessionAdapter session, long sessionTimeoutInMinutes) {
        return execute(redis -> redis.objectValueOperations().setIfAbsent(fromSessionId(sessionId), session, sessionTimeoutInMinutes, TimeUnit.MINUTES));
    }


    @Override
    protected SimpleSessionAdapter doRemoteRead(Serializable sessionId) {
        return execute(redis -> (SimpleSessionAdapter) redis.objectValueOperations().get(fromSessionId(sessionId)));
    }

    protected RedisKey fromSession(Session session) {
        return fromSessionId(session.getId());
    }

    protected RedisKey fromSessionId(Serializable sessionId) {
        RedisKey.RedisKeyBuilder redisKeyBuilder = RedisKey.RedisKeyBuilder.fromCatalogAndType(sessionKeyCatalog, sessionKeyType).withId(String.valueOf(sessionId));
        if (StringUtils.hasText(slotKey)) {
            redisKeyBuilder.withSlotKey(slotKey);
        }
        return redisKeyBuilder.build();
    }


    @Override
    public Collection<Session> getActiveSessions() {
        return super.getActiveSessions();
    }

    public void setSerializerFactory(LookupSerializerFactory serializerFactory) {
        this.serializerFactory = serializerFactory;
    }
}
