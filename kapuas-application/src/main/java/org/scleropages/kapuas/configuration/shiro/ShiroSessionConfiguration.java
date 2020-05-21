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
package org.scleropages.kapuas.configuration.shiro;

import com.esotericsoftware.kryo.Kryo;
import com.google.common.collect.Sets;
import org.apache.shiro.mgt.SubjectFactory;
import org.apache.shiro.session.mgt.SessionFactory;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.web.servlet.Cookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.apache.shiro.web.util.SavedRequest;
import org.objenesis.strategy.SerializingInstantiatorStrategy;
import org.scleropages.connector.redis.RedisClient;
import org.scleropages.kapuas.session.provider.shiro.ObservableSession;
import org.scleropages.kapuas.session.provider.shiro.ObservableSessionFactory;
import org.scleropages.kapuas.session.provider.shiro.ProxyCookie;
import org.scleropages.kapuas.session.provider.shiro.RedisSessionDao;
import org.scleropages.kapuas.session.provider.shiro.SimpleSessionAdapter;
import org.scleropages.kapuas.session.provider.shiro.StatelessTokenWebSubjectFactory;
import org.scleropages.serialize.LookupSerializerFactory;
import org.scleropages.serialize.SerialIdRegistry;
import org.scleropages.serialize.SerializerFactory;
import org.scleropages.serialize.kryo.PooledKryoSerializerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
@Configuration
@ConditionalOnProperty(name = "session.native-session-manager.enabled")
public class ShiroSessionConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(ShiroSessionConfiguration.class);

    private static final int SIMPLE_SESSION_SERIALIZE_ID = 3313;


    /*native session settings*/

    @Value("#{ @environment['session.sessionManager.deleteInvalidSessions'] ?: true }")
    protected boolean sessionManagerDeleteInvalidSessions;

    @Value("#{ @environment['session.sessionManager.sessionIdCookieEnabled'] ?: true }")
    protected boolean sessionIdCookieEnabled;

    @Value("#{ @environment['session.sessionManager.sessionIdUrlRewritingEnabled'] ?: true }")
    protected boolean sessionIdUrlRewritingEnabled;

    /*native session cookie settings*/
    @Value("#{ @environment['session.native-session-manager.cookie-name'] ?: T(org.apache.shiro.web.servlet.ShiroHttpSession).DEFAULT_SESSION_ID_NAME }")
    protected String sessionIdCookieName;

    @Value("#{ @environment['session.native-session-manager.cookie-maxAge'] ?: T(org.apache.shiro.web.servlet.SimpleCookie).DEFAULT_MAX_AGE }")
    protected int sessionIdCookieMaxAge;

    @Value("#{ @environment['session.native-session-manager.cookie-domain'] ?: null }")
    protected String sessionIdCookieDomain;

    @Value("#{ @environment['session.native-session-manager.cookie-path'] ?: null }")
    protected String sessionIdCookiePath;

    @Value("#{ @environment['session.native-session-manager.cookie-secure'] ?: true }")
    protected boolean sessionIdCookieSecure;

    @Value("#{ @environment['session.native-session-manager.cookie-http-only'] ?: true }")
    protected boolean sessionIdCookieHttpOnly;

    @Value("#{ @environment['session.native-session-manager.cookie-less.http-header-name'] ?: null }")
    protected String sessionIdHeaderName;

    @Value("#{ @environment['session.native-session-manager.cookie-less.request-parameter-name'] ?: null }")
    protected String sessionIdParameterName;

    /*native remember me cookie settings*/
    @Value("#{ @environment['session.remember-me-manager.cookie-name'] ?: T(org.apache.shiro.web.mgt.CookieRememberMeManager).DEFAULT_REMEMBER_ME_COOKIE_NAME }")
    protected String rememberMeCookieName;

    @Value("#{ @environment['session.remember-me-manager.cookie-maxAge'] ?: T(org.apache.shiro.web.servlet.Cookie).ONE_YEAR }")
    protected int rememberMeCookieMaxAge;

    @Value("#{ @environment['session.remember-me-manager.cookie-domain'] ?: null }")
    protected String rememberMeCookieDomain;

    @Value("#{ @environment['session.remember-me-manager.cookie-path'] ?: null }")
    protected String rememberMeCookiePath;

    @Value("#{ @environment['session.remember-me-manager.cookie-secure'] ?: true }")
    protected boolean rememberMeCookieSecure;

    @Value("#{ @environment['session.remember-me-manager.cookie-http-only'] ?: true }")
    protected boolean rememberMeCookieHttpOnly;

    @Value("#{ @environment['session.remember-me-manager.cookie-less.http-header-name'] ?: null }")
    protected String rememberMeHeaderName;

    @Value("#{ @environment['session.remember-me-manager.cookie-less.request-parameter-name'] ?: null }")
    protected String rememberMeParameterName;


    /*remote session registry settings*/
    @Value("#{ @environment['session.remote-session.enabled'] ?: false }")
    protected boolean remoteSessionEnabled;


    @Bean
    @ConditionalOnMissingBean
    public SessionFactory observableSessionFactory(ObjectProvider<ObservableSession.SessionAttributeChangeConsumer> sessionAttributeChangeConsumers) {
        ObservableSessionFactory observableSessionFactory = new ObservableSessionFactory();

        Set<ObservableSession.SessionAttributeChangeConsumer> consumers = Sets.newHashSet();
        sessionAttributeChangeConsumers.orderedStream().forEach(sessionAttributeChangeConsumer -> consumers.add(sessionAttributeChangeConsumer));
        observableSessionFactory.setAttributeChangesConsumers(consumers);

        return observableSessionFactory;
    }

    @Bean
    @ConditionalOnMissingBean
    public SessionManager sessionManager(SessionDAO sessionDAO, SessionFactory sessionFactory) {
        logger.info("Using native session manager...");
        DefaultWebSessionManager webSessionManager = new DefaultWebSessionManager();
        webSessionManager.setSessionIdCookieEnabled(sessionIdCookieEnabled);
        webSessionManager.setSessionIdUrlRewritingEnabled(sessionIdUrlRewritingEnabled);
        webSessionManager.setSessionIdCookie(sessionCookieTemplate());

        webSessionManager.setSessionFactory(sessionFactory);
        webSessionManager.setSessionDAO(sessionDAO);
        webSessionManager.setDeleteInvalidSessions(sessionManagerDeleteInvalidSessions);
        return webSessionManager;
    }

    @Bean
    @ConditionalOnMissingBean
    public SubjectFactory subjectFactory() {
        return new StatelessTokenWebSubjectFactory();
    }

    @Bean
    @ConditionalOnMissingBean
    public Cookie sessionCookieTemplate() {
        return buildCookie(newProxyCookie(sessionIdCookieName,sessionIdHeaderName,sessionIdParameterName), sessionIdCookieMaxAge, sessionIdCookiePath, sessionIdCookieDomain, sessionIdCookieSecure, sessionIdCookieHttpOnly);
    }

    @Bean
    @ConditionalOnMissingBean
    protected Cookie rememberMeCookieTemplate() {
        return buildCookie(newProxyCookie(rememberMeCookieName,rememberMeHeaderName,rememberMeParameterName), rememberMeCookieMaxAge, rememberMeCookiePath, rememberMeCookieDomain, rememberMeCookieSecure, rememberMeCookieHttpOnly);
    }

    protected Cookie newProxyCookie(String cookieName,String headerName,String parameterName){
        return new ProxyCookie(cookieName, new ProxyCookie.CookieProxy() {
            @Override
            public String readCookieValue(HttpServletRequest request, HttpServletResponse response) {
                String sessionId = null;
                if (StringUtils.hasText(headerName))
                    sessionId = request.getHeader(headerName);
                if (!StringUtils.hasText(sessionId) && StringUtils.hasText(parameterName)) {
                    sessionId = request.getParameter(parameterName);
                }
                return sessionId;
            }
            @Override
            public void saveCookie(Cookie cookie, HttpServletRequest request, HttpServletResponse response) {

            }

            @Override
            public void removeCookie(Cookie cookie, HttpServletRequest request, HttpServletResponse response) {

            }
        });
    }

    protected Cookie buildCookie(Cookie cookie,int maxAge, String path, String domain, boolean secure, boolean httpOnly) {
        cookie.setHttpOnly(httpOnly);
        cookie.setMaxAge(maxAge);
        cookie.setPath(path);
        cookie.setDomain(domain);
        cookie.setSecure(secure);
        return cookie;
    }

    @Bean
    @ConditionalOnMissingBean
    public SessionDAO shiroSessionDao(@Autowired(required = false) List<SerializerFactory<InputStream, OutputStream>> serializerFactories, @Autowired(required = false) RedisClient redisClient, SessionFactory sessionFactory) {
        if (!remoteSessionEnabled) {
            logger.info("Using local session registry...");
            return new EnterpriseCacheSessionDAO();
        }
        logger.info("Using remote session registry...");
        RedisSessionDao redisSessionDao = new RedisSessionDao(redisClient);
        redisSessionDao.setSessionFactory(sessionFactory);
        for (SerializerFactory serializerFactory : serializerFactories) {
            try {
                if (serializerFactory instanceof LookupSerializerFactory) {
                    redisSessionDao.setSerializerFactory((LookupSerializerFactory) serializerFactory);
                } else if (serializerFactory.supportClassRegistry()) {
                    SerialIdRegistry classRegistry = serializerFactory.getClassRegistry();
                    classRegistry.register(SimpleSessionAdapter.class, SIMPLE_SESSION_SERIALIZE_ID);
                    classRegistry.register(SavedRequest.class, SIMPLE_SESSION_SERIALIZE_ID + 2);
                    classRegistry.register(SimplePrincipalCollection.class, SIMPLE_SESSION_SERIALIZE_ID + 3);
                }
            } catch (Exception e) {
                logger.warn("failure to register classes to serializer provider: " + serializerFactory.getClass().getName() + ".");
            }
        }
        return redisSessionDao;
    }

    @Bean
    @ConditionalOnMissingBean
    public PooledKryoSerializerFactory.KryoCustomizer instantiatorCustomizer() {
        return kryo -> {
            //SHIRO 待序列化组件(如savedRequest等)定义了非空参构造函数，为简化开发，
            //增加部分实例化风险以及牺牲部分性能开启二级实例化策略（https://github.com/EsotericSoftware/kryo#instantiatorstrategy）
            //即构造函数调用失败采用jdk序列化实例化策略，避免开发自行定义register(OBJ.class, new FieldSerializer(kryo, OBJ.class),9527)
            kryo.setInstantiatorStrategy(new Kryo.DefaultInstantiatorStrategy(new SerializingInstantiatorStrategy()));
//            kryo.register(Authenticated.class, new FieldSerializer(kryo, Authenticated.class) {
//                @Override
//                protected Authenticated create(Kryo kryo, Input input, Class type) {
//                    return new Authenticated("IF YOU SEE THIS MESSAGE MAY CAUSE A BUG!!!!");
//                }
//            });
        };
    }

}
