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
package org.scleropages.kapuas.security.provider.shiro.realm;

import com.google.common.collect.Maps;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.scleropages.kapuas.security.AuthenticationDetails;
import org.scleropages.kapuas.security.SecurityOption;
import org.scleropages.kapuas.security.authc.Authentication;
import org.scleropages.kapuas.security.authc.AuthenticationManager;
import org.scleropages.kapuas.security.authc.provider.Authenticating;
import org.scleropages.kapuas.security.authc.provider.AuthenticationDetailsProvider;
import org.scleropages.kapuas.security.authc.provider.NoAuthenticationDetailsProvider;
import org.scleropages.kapuas.security.provider.shiro.AuthenticationTokenManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;


/**
 * 基于 {@link AuthenticationManager} 的 {@link UsernamePasswordToken} token管理器，供shiro realm认证
 *
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public class DefaultTokenManager implements AuthenticationTokenManager<UsernamePasswordToken> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final AuthenticationManager authenticationManager;

    private Map<Integer, AuthenticationDetailsProvider> authenticationDetailsProviders;

    private AuthenticationDetailsProvider defaultAuthenticationDetailsProvider = new NoAuthenticationDetailsProvider();


    public DefaultTokenManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public Authenticating find(UsernamePasswordToken authenticationToken) {
        String username = authenticationToken.getUsername();
        Authentication entity = authenticationManager.getByPrincipal(username);
        if (entity == null)
            return null;

        Authenticating authenticating = new Authenticating(entity.getPrincipal(), entity.getCredentials(), entity.getSecureSalt(),
                entity.getEnabled(), entity.getExpired(), entity.getLocked(), entity.getCredentialsExpired(), null);


        Integer associatedType = entity.getAssociatedType();

        AuthenticationDetailsProvider provider = authenticationDetailsProviders.get(associatedType);

        Assert.notNull(provider, "authentication details provider not found by given assignedTo:" + associatedType);

        if (authenticationToken instanceof ShiroUsernamePasswordToken) {
            ShiroUsernamePasswordToken shiroUsernamePasswordToken = (ShiroUsernamePasswordToken) authenticationToken;
            Boolean autoLoadDetails = shiroUsernamePasswordToken.getNativeToken().getBooleanAuthenticationOption(SecurityOption.AUTHENTICATION_OPTION_AUTO_LOAD_DETAILS);
            if (!autoLoadDetails) {
                logger.debug("client authentication option[AUTHENTICATION_OPTION_AUTO_LOAD_DETAILS] is false. ignore authentication details.");
                try {
                    //details 必须提供，很多组件匹配执行可能都基于details类型的选择.即便不加载details也应该提供一个类型匹配的空对象实例.
                    authenticating.setDetails((AuthenticationDetails) provider.getDetailsType().newInstance());
                } catch (Exception e) {
                    throw new IllegalArgumentException("Implementations of AuthenticationDetails must have a default constructor.", e);
                }
                return authenticating;
            }
        }

        authenticating.setDetails(provider.getAuthenticationDetails(authenticating, entity.getAssociatedId()));

        return authenticating;
    }

    @Override
    public String getCredentialsEncoded() {
        return authenticationManager.getCredentialsEncoded();
    }

    @Override
    public String getCredentialsHashAlgorithmName() {
        return authenticationManager.getCredentialsHashAlgorithmName();
    }

    @Override
    public int getCredentialsHashIterations() {
        return authenticationManager.getCredentialsHashIterations();
    }

    @Override
    public boolean isCredentialsHashed() {
        return authenticationManager.isCredentialsHashed();
    }


    public void setDefaultAuthenticationDetailsProvider(AuthenticationDetailsProvider defaultAuthenticationDetailsProvider) {
        this.defaultAuthenticationDetailsProvider = defaultAuthenticationDetailsProvider;
    }

    protected AuthenticationDetailsProvider findAuthenticationDetailsProvider(Integer providerId) {
        AuthenticationDetailsProvider provider = authenticationDetailsProviders.get(providerId);
        return null != provider ? provider : defaultAuthenticationDetailsProvider;
    }

    public void setAuthenticationDetailsProviders(List<AuthenticationDetailsProvider> authenticationDetailsProviders) {
        Assert.notEmpty(authenticationDetailsProviders, "authenticationDetailsProviders must not empty.");

        this.authenticationDetailsProviders = Maps.newHashMap();
        for (AuthenticationDetailsProvider authenticationDetailsProvider : authenticationDetailsProviders) {
            Integer providerId = authenticationDetailsProvider.getProviderId();
            if (authenticationDetailsProviders
                    .contains(providerId))
                throw new BeanCreationException(
                        authenticationDetailsProvider.getProviderId()
                                + " already exists in authenticationDetailsProviders.");
            this.authenticationDetailsProviders.put(
                    providerId,
                    authenticationDetailsProvider);
        }
    }
}
