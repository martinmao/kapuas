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

import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.SimpleSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

/**
 * 可监控的session，一组 {@link BiConsumer} 订阅 session attribute changes事件，session变化仅会通知1次，无论后续是否再发送变化,除非调用reset
 * <p>
 * NOTE:该类并非用于完整监控session属性变化，目前用于远程session序列化，在session属性未发生变化时，减少update次数.降低io开销
 * 需注意线程安全，父类 {@link SimpleSession} 并非线程安全实现
 *
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public class ObservableSession extends SimpleSession {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObservableSession.class);


    public interface SessionAttributeChangeConsumer extends BiConsumer<Session, Object> {
    }


    private transient AtomicBoolean attributeChanges = new AtomicBoolean(false);

    private final transient Set<SessionAttributeChangeConsumer> attributeChangesConsumers;


    public ObservableSession() {
        attributeChangesConsumers = null;
    }

    public ObservableSession(String host, Set<SessionAttributeChangeConsumer> attributeChangesConsumers) {
        super(host);
        this.attributeChangesConsumers = attributeChangesConsumers;
    }

    public ObservableSession(Set<SessionAttributeChangeConsumer> attributeChangesConsumers) {
        this(null, attributeChangesConsumers);
    }

    @Override
    public void setAttribute(Object key, Object value) {
        super.setAttribute(key, value);
        notifyIfNecessary("updated", key);
    }

    @Override
    public Object removeAttribute(Object key) {
        Object object = super.removeAttribute(key);
        notifyIfNecessary("removed", key);
        return object;
    }

    protected void notifyIfNecessary(String action, Object key) {
        if (attributeChanges.compareAndSet(false, true)) {
            LOGGER.debug("performing notify session-attributes {} for session: {}. with key: {}.", action, getId(), key);
            if (null != attributeChangesConsumers)
                attributeChangesConsumers.forEach(sessionConsumer -> sessionConsumer.accept(this, key));
        }
    }

    public void forceChange() {
        notifyIfNecessary("force-changed", null);
    }

    public boolean resetChangeState() {
        if (attributeChanges.compareAndSet(true, false)) {
            LOGGER.debug("performing reset session-attributes changes for session: {}.", getId());
            return true;
        }
        return false;
    }

    boolean attributeChanged() {
        return attributeChanges.get();
    }
}
