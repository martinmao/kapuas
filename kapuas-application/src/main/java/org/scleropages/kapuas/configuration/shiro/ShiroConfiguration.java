package org.scleropages.kapuas.configuration.shiro;

import com.google.common.collect.Lists;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.pam.AtLeastOneSuccessfulStrategy;
import org.apache.shiro.authc.pam.AuthenticationStrategy;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.spring.web.config.ShiroFilterChainDefinition;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.apache.shiro.web.filter.mgt.DefaultFilter;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.scleropages.kapuas.security.authc.mgmt.GenericAuthenticationManager;
import org.scleropages.kapuas.security.authc.provider.AuthenticationDetailsProvider;
import org.scleropages.kapuas.security.authc.provider.Authenticator;
import org.scleropages.kapuas.security.authc.token.server.jwt.DefaultJwtTokenFactory;
import org.scleropages.kapuas.security.authc.token.server.jwt.JwtTokenFactory;
import org.scleropages.kapuas.security.provider.shiro.ApplicationFormAuthenticationFilter;
import org.scleropages.kapuas.security.provider.shiro.ShiroAuthenticator;
import org.scleropages.kapuas.security.provider.shiro.realm.DefaultTokenManager;
import org.scleropages.kapuas.security.provider.shiro.realm.DefaultTokenRealm;
import org.scleropages.kapuas.security.provider.shiro.realm.jwt.JwtTokenManager;
import org.scleropages.kapuas.security.provider.shiro.realm.jwt.JwtTokenRealm;
import org.scleropages.kapuas.session.provider.shiro.ApplicationShiroFilterFactoryBean;
import org.scleropages.kapuas.session.provider.shiro.ShiroSessionFilter;
import org.scleropages.kapuas.session.provider.shiro.cache.ShiroCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.event.ContextRefreshedEvent;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import java.util.List;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
@Configuration
@ConditionalOnProperty(name = "shiro.web.enabled", matchIfMissing = true)
@EnableCaching
@ImportResource("classpath:spring-security.xml")
public class ShiroConfiguration implements ApplicationListener<ContextRefreshedEvent> {


    private static final Logger logger = LoggerFactory.getLogger(ShiroConfiguration.class);


    public static final int ORDER_SECURITY_SHIRO_SESSION_FILTER = 10;

    public static final int ORDER_SECURITY_SHIRO_FILTER = ORDER_SECURITY_SHIRO_SESSION_FILTER + 100;


    @Value("#{ @environment['shiro.loginUrl'] ?: '/login' }")
    protected String loginUrl;

    @Value("#{ @environment['shiro.successUrl'] ?: '/home' }")
    protected String successUrl;

    @Value("#{ @environment['shiro.unauthorizedUrl'] ?: null }")
    protected String unauthorizedUrl;

    @Value("#{ @environment['jwt.signature.http-header-name'] ?: 'Authorization' }")
    private String encodedJwtTokenHeaderName;


    @Bean
    @ConditionalOnProperty(name = "shiro.web.pre-session-create.enabled", matchIfMissing = true)
    public FilterRegistrationBean shiroSessionFilter(SecurityManager securityManager) {
        ShiroSessionFilter shiroSessionFilter = new ShiroSessionFilter();
        shiroSessionFilter.setWebSecurityManager((WebSecurityManager) securityManager);
        return createDefaultFilterRegistrationBean(shiroSessionFilter, ORDER_SECURITY_SHIRO_SESSION_FILTER);
    }


    @Bean
    @ConditionalOnProperty(name = "shiro.web.pre-session-create.enabled", matchIfMissing = true)
    protected ShiroFilterFactoryBean shiroFilterFactoryBean(SecurityManager securityManager, ShiroFilterChainDefinition shiroFilterChainDefinition) {
        ApplicationShiroFilterFactoryBean filterFactoryBean = new ApplicationShiroFilterFactoryBean();
        filterFactoryBean.setLoginUrl(loginUrl);
        filterFactoryBean.setSuccessUrl(successUrl);
        filterFactoryBean.setUnauthorizedUrl(unauthorizedUrl);
        filterFactoryBean.setSecurityManager(securityManager);
        Filter associated = filterFactoryBean.getFilters().putIfAbsent(DefaultFilter.authc.name(), formAuthenticationFilter());
        if (null != associated) {
            logger.warn("detected autoconfigure conflict. authc has been assigned to other filter: {}", associated);
        }
        filterFactoryBean.setFilterChainDefinitionMap(shiroFilterChainDefinition.getFilterChainMap());
        return filterFactoryBean;
    }

    @Bean
    protected FormAuthenticationFilter formAuthenticationFilter() {
        return new ApplicationFormAuthenticationFilter();
    }

    @ConditionalOnProperty(name = "shiro.web.pre-session-create.enabled", matchIfMissing = true)
    @Bean(name = "filterShiroFilterRegistrationBean")
    public FilterRegistrationBean shiroFilter() throws Exception {
        return createDefaultFilterRegistrationBean((Filter) shiroFilterFactoryBean(null, null).getObject(), ORDER_SECURITY_SHIRO_FILTER);
    }

    @Bean
    public CacheManager shiroCacheManager(org.springframework.cache.CacheManager springCacheManager) {
        ShiroCacheManager shiroCacheManager = new ShiroCacheManager(springCacheManager);
        return shiroCacheManager;
    }

    @Bean
    public Realm defaultTokenRealm() {
        DefaultTokenRealm defaultTokenRealm = new DefaultTokenRealm();
        return defaultTokenRealm;
    }

    @Bean
    public Realm jwtTokenRealm() {
        JwtTokenRealm jwtTokenRealm = new JwtTokenRealm();
        return jwtTokenRealm;
    }

    /**
     * 修改shiro 默认策略，在尝试时如果抛出认证异常，传播出去，不进行其他realm尝试（如账号异常，密码过期等）
     *
     * @return
     * @see org.scleropages.kapuas.security.provider.shiro.AbstractShiroRealm
     */
    @Bean
    @ConditionalOnMissingBean
    protected AuthenticationStrategy authenticationStrategy() {
        return new AtLeastOneSuccessfulStrategy() {
            @Override
            public AuthenticationInfo afterAttempt(Realm realm, AuthenticationToken token, AuthenticationInfo singleRealmInfo, AuthenticationInfo aggregateInfo, Throwable t) throws AuthenticationException {
                if (null != t && t instanceof AuthenticationException)
                    throw (AuthenticationException) t;
                if (!logger.isDebugEnabled() && null != t) {
                    logger.warn("threw an exception during a multi-realm authentication attempt:", t);
                }
                return super.afterAttempt(realm, token, singleRealmInfo, aggregateInfo, t);
            }
        };
    }


    @Bean
    public DefaultTokenManager defaultTokenManager(GenericAuthenticationManager authenticationManager,
                                                   ObjectProvider<AuthenticationDetailsProvider> authenticationDetailsProviders) {
        DefaultTokenManager defaultAuthenticationTokenManager = new DefaultTokenManager(authenticationManager);
        List<AuthenticationDetailsProvider> applyAuthenticationDetailsProvider = Lists.newArrayList();

        authenticationDetailsProviders.orderedStream().forEach(item -> applyAuthenticationDetailsProvider.add(item));

        defaultAuthenticationTokenManager.setAuthenticationDetailsProviders(applyAuthenticationDetailsProvider);

        return defaultAuthenticationTokenManager;
    }

    @Bean
    public JwtTokenFactory jwtTokenFactory() {
        return new DefaultJwtTokenFactory();
    }

    @Bean
    public JwtTokenManager jwtTokenManager(JwtTokenFactory jwtTokenFactory) {
        JwtTokenManager jwtTokenManager = new JwtTokenManager(jwtTokenFactory);
        return jwtTokenManager;
    }

    @Bean
    public Authenticator shiroAuthenticator() {
        return new ShiroAuthenticator();
    }

    @Bean
    @ConditionalOnProperty(name = "shiro.annotations-processing.enabled", matchIfMissing = true)
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
        advisor.setSecurityManager(securityManager);
        return advisor;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        ApplicationContext applicationContext = event.getApplicationContext();

        DefaultTokenRealm defaulttokenRealm = applicationContext.getBean(DefaultTokenRealm.class);

        defaulttokenRealm.setAuthenticationTokenManager(applicationContext.getBean(DefaultTokenManager.class));


        JwtTokenRealm jwtTokenRealm = applicationContext.getBean(JwtTokenRealm.class);

        jwtTokenRealm.setAuthenticationTokenManager(applicationContext.getBean(JwtTokenManager.class));


        JwtTokenManager jwtTokenManager = applicationContext.getBean(JwtTokenManager.class);

        jwtTokenManager.setSignatureKeyProviders(Lists.newArrayList(applicationContext.getBeansOfType(JwtTokenFactory.SignatureKeyProvider.class).values()));


        applicationContext.getBean(ShiroAuthenticator.class).init();

        SecurityUtils.setSecurityManager(applicationContext.getBean(SecurityManager.class));
    }


    public static FilterRegistrationBean createFilterRegistrationBeanWithAllDispatcherTypes(Filter filter, int order) {
        return createFilterRegistrationBean(filter, order, DispatcherType.FORWARD, DispatcherType.INCLUDE, DispatcherType.ERROR);
    }

    public static FilterRegistrationBean createDefaultFilterRegistrationBean(Filter filter, int order) {
        return createFilterRegistrationBean(filter, order);
    }

    public static FilterRegistrationBean createFilterRegistrationBean(Filter filter, int order, DispatcherType... additional) {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setFilter(filter);
        filterRegistrationBean.setDispatcherTypes(DispatcherType.REQUEST, additional);
        filterRegistrationBean.setOrder(order);
        return filterRegistrationBean;
    }

}