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
package org.scleropages.maldini.session.provider.shiro.cache;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Set;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public class ShiroJCache<K, V> implements Cache<K, V> {


    private final javax.cache.Cache nativeCache;

    public ShiroJCache(org.springframework.cache.Cache springCache) {
        Assert.notNull(springCache, "springCache must not be null");
        Object nativeCache = springCache.getNativeCache();
        if (nativeCache instanceof javax.cache.Cache)
            this.nativeCache = (javax.cache.Cache) springCache.getNativeCache();
        else
            throw new IllegalStateException("currently used cache provider [" + nativeCache.getClass().getName() + "] not is javax.cache.Cache implementation.");
    }

    @Override
    public V get(K k) throws CacheException {
        Object v = nativeCache.get(k);
        return v != null ? (V) v : null;
    }

    @Override
    public V put(K k, V v) throws CacheException {
        V associated = get(k);
        nativeCache.put(k, v);
        return associated;
    }

    @Override
    public V remove(K k) throws CacheException {
        V associated = get(k);
        nativeCache.remove(k);
        return associated;
    }

    @Override
    public void clear() throws CacheException {
        nativeCache.clear();
    }

    /**
     * no callable found
     *
     * @return
     */
    @Override
    public int size() {
        int i = 0;
        while (nativeCache.iterator().hasNext())
            i++;
        return i;
    }

    /**
     * no callable found
     *
     * @return
     */
    @Override
    public Set<K> keys() {
        Set<K> keys = Sets.newHashSet();
        nativeCache.iterator().forEachRemaining(o -> keys.add((K) o));
        return keys;
    }

    /**
     * called by org.apache.shiro.session.mgt.eis.CachingSessionDAO#getActiveSessions()
     *
     * @return
     */
    @Override
    public Collection<V> values() {
        Collection<V> values = Lists.newArrayList();
        nativeCache.iterator().forEachRemaining(o -> values.add((V) o));
        return values;
    }
}
