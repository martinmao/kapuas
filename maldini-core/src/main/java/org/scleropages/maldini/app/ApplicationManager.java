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
package org.scleropages.maldini.app;

import org.scleropages.crud.GenericManager;
import org.scleropages.crud.orm.SearchFilter;
import org.scleropages.crud.orm.jpa.entity.EntityAware;
import org.scleropages.maldini.AuthenticationDetails;
import org.scleropages.maldini.app.entity.ApplicationEntity;
import org.scleropages.maldini.app.entity.ApplicationEntityRepository;
import org.scleropages.maldini.app.model.Application;
import org.scleropages.maldini.app.model.ApplicationMapper;
import org.scleropages.maldini.security.authc.AuthenticationManager;
import org.scleropages.maldini.security.authc.mgmt.model.Authentication;
import org.scleropages.maldini.security.authc.provider.Authenticating;
import org.scleropages.maldini.security.authc.provider.AuthenticationDetailsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.Map;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
@Service
@Validated
public class ApplicationManager implements AuthenticationDetailsProvider, GenericManager<Application, Long, ApplicationMapper> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public static final int AUTHENTICATION_DETAILS_PROVIDER_ID = 9527;

    @Value("#{ @environment['app.random.app-id-bytes-length'] ?: 8 }")
    private int randomAppIdBytesLength;

    @Value("#{ @environment['app.random.app-secure-bytes-length'] ?: 16 }")
    private int randomAppSecureBytesLength;

    @Value("#{ @environment['app.random.authentication-encoded'] ?: 'HEX' }")
    private String randomBytesEncoded;

    private ApplicationEntityRepository applicationEntityRepository;

    private AuthenticationManager authenticationManager;


    @Override
    @Transactional
    @Validated({Application.UpdateModel.class})
    public void save(@Valid Application application) {
        applicationEntityRepository.findById(application.getId()).ifPresent(applicationEntity -> {
            getModelMapper().mapForUpdate(application, applicationEntity);
            applicationEntityRepository.save(applicationEntity);
        });
    }

    @Transactional
    @Validated({Application.CreateModel.class})
    public Application create(@Valid Application application) {
        Authentication authentication = authenticationManager.randomAuthentication(randomAppIdBytesLength, randomAppSecureBytesLength, randomBytesEncoded);
        application.enable();
        application.setAppId(authentication.getPrincipal());
        ApplicationEntity savedApplication = applicationEntityRepository.save(getModelMapper().mapForSave(application));
        authentication.setAssociatedId(String.valueOf(savedApplication.getId()));
        authentication.setAssociatedType(AUTHENTICATION_DETAILS_PROVIDER_ID);
        authentication.enable();
        authenticationManager.save(authentication);
        application = new Application();
        application.setAppId(authentication.getPrincipal());
        application.setAppSecret(authentication.getCredentials());
        return application;
    }

    @Transactional(readOnly = true)
    public Application getApplication(Application application) {
        return getModelMapper().mapForRead(findByIdOrAppId(application.getId(), application.getAppId()));
    }

    @Transactional(readOnly = true)
    public Page<Application> findApplicationPage(Map<String, SearchFilter> searchFilters, Pageable pageable) {
        return applicationEntityRepository.findPage(searchFilters, pageable);
    }

    public void awareApplicationEntity(Long id, EntityAware entityAware) {
        entityAware.setEntity(applicationEntityRepository.findById(id).get());
    }

    protected ApplicationEntity findByIdOrAppId(Long id, String appId) {
        return applicationEntityRepository.findByIdOrAppId(id, appId);
    }

    @Override
    @Transactional(readOnly = true)
    public Application findById(Long id) {
        return getModelMapper().mapForRead(applicationEntityRepository.findById(id).get());
    }


    @Override
    @Transactional(readOnly = true)
    public AuthenticationDetails getAuthenticationDetails(Authenticating authenticating, String associatedId) {
        return findById(Long.valueOf(associatedId));
    }

    @Autowired
    public void setApplicationEntityRepository(ApplicationEntityRepository applicationEntityRepository) {
        this.applicationEntityRepository = applicationEntityRepository;
    }

    @Autowired
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public Integer getProviderId() {
        return AUTHENTICATION_DETAILS_PROVIDER_ID;
    }

}
