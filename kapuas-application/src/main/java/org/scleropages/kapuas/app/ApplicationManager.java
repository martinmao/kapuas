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
package org.scleropages.kapuas.app;

import org.scleropages.crud.GenericManager;
import org.scleropages.crud.dao.orm.SearchFilter;
import org.scleropages.crud.dao.orm.jpa.entity.EntityAware;
import org.scleropages.crud.exception.BizError;
import org.scleropages.crud.exception.BizParamViolationException;
import org.scleropages.kapuas.app.entity.ApiEntity;
import org.scleropages.kapuas.app.entity.ApiEntityRepository;
import org.scleropages.kapuas.app.entity.ApplicationEntity;
import org.scleropages.kapuas.app.entity.ApplicationEntityRepository;
import org.scleropages.kapuas.app.model.Api;
import org.scleropages.kapuas.app.model.ApiMapper;
import org.scleropages.kapuas.app.model.Application;
import org.scleropages.kapuas.app.model.ApplicationMapper;
import org.scleropages.kapuas.security.authc.Authentication;
import org.scleropages.kapuas.security.authc.AuthenticationManager;
import org.scleropages.kapuas.security.authc.mgmt.model.AuthenticationModel;
import org.scleropages.kapuas.security.authc.provider.Authenticating;
import org.scleropages.kapuas.security.authc.provider.AuthenticationDetailsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.Map;
import java.util.Objects;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
@Service
@Validated
@BizError("40")
public class ApplicationManager implements AuthenticationDetailsProvider<Application>, GenericManager<Application, Long, ApplicationMapper> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public static final int AUTHENTICATION_DETAILS_PROVIDER_ID = 9527;

    @Value("#{ @environment['app.random.app-id-bytes-length'] ?: 8 }")
    private int randomAppIdBytesLength;

    @Value("#{ @environment['app.random.app-secure-bytes-length'] ?: 16 }")
    private int randomAppSecureBytesLength;

    @Value("#{ @environment['app.random.authentication-encoded'] ?: 'HEX' }")
    private String randomBytesEncoded;

    private ApplicationEntityRepository applicationEntityRepository;

    private ApiEntityRepository apiEntityRepository;

    private AuthenticationManager authenticationManager;

    @Transactional
    @Validated({Api.Create.class})
    @BizError("01")
    public void createApi(@Valid Api api) {
        ApplicationEntity applicationEntity = applicationEntityRepository.get(api.getApplicationId()).orElseThrow(() -> new BizParamViolationException("no application found by given id: " + api.getApplicationId()));
        ApiEntity apiEntity = getModelMapper(ApiMapper.class).mapForSave(api);
        apiEntity.setApplicationEntity(applicationEntity);
        apiEntityRepository.save(apiEntity);
    }

    @Transactional
    @Validated({Api.Update.class})
    @BizError("02")
    public void saveApi(@Valid Api api) {
        apiEntityRepository.findById(api.getId()).ifPresent(apiEntity -> {
            getModelMapper(ApiMapper.class).mapForUpdate(api, apiEntity);
            apiEntityRepository.save(apiEntity);
        });
    }

    @Transactional(readOnly = true)
    @BizError("03")
    public Page<Api> findApiPage(Map<String, SearchFilter> searchFilters, Pageable pageable) {
        return apiEntityRepository.findPage(searchFilters, pageable).map(apiEntity -> getModelMapper(ApiMapper.class).mapForRead(apiEntity));
    }

    @Transactional
    @Validated({Application.Update.class})
    @BizError("04")
    public void save(@Valid Application application) {
        applicationEntityRepository.findById(application.getId()).ifPresent(applicationEntity -> {
            if (!Objects.equals(applicationEntity.getName(), application.getName())) {
                Assert.isTrue(!applicationEntityRepository.existsByName(application.getName()), "application name already exists.");
            }
            getModelMapper().mapForUpdate(application, applicationEntity);
            applicationEntityRepository.save(applicationEntity);
        });
    }

    @Transactional
    @Validated({Application.Create.class})
    @BizError("05")
    public Application create(@Valid Application application) {
        Assert.isTrue(!applicationEntityRepository.existsByName(application.getName()), "application name already exists.");
        Authentication authentication = authenticationManager.randomAuthentication(randomAppIdBytesLength, randomAppSecureBytesLength, randomBytesEncoded);
        application.setAppId(authentication.getPrincipal());
        ApplicationEntity savedApplication = getModelMapper().mapForSave(application);
        savedApplication.enable();
        savedApplication = applicationEntityRepository.save(savedApplication);


        AuthenticationModel authenticationModel = new AuthenticationModel(authentication.getPrincipal(), authentication.getCredentials());
        authenticationModel.setAssociatedId(String.valueOf(savedApplication.getId()));
        authenticationModel.setAssociatedType(AUTHENTICATION_DETAILS_PROVIDER_ID);
        authenticationManager.create(authenticationModel);

        Application result = new Application();
        result.setAppId(authentication.getPrincipal());
        result.setAppSecret(authentication.getCredentials());
        return result;
    }

    @Transactional(readOnly = true)
    @BizError("06")
    public Application getApplication(Application application) {
        return getModelMapper().mapForRead(applicationEntityRepository.getByIdOrAppId(application.getId(), application.getAppId()));
    }

    @Transactional(readOnly = true)
    @BizError("07")
    public Api getApi(Long id) {
        return getModelMapper(ApiMapper.class).mapForRead(apiEntityRepository.getById(id));
    }

    @Transactional(readOnly = true)
    @BizError("08")
    public Page<Application> findApplicationPage(Map<String, SearchFilter> searchFilters, Pageable pageable) {
        return applicationEntityRepository.findPage(searchFilters, pageable).map(applicationEntity -> getModelMapper().mapForRead(applicationEntity));
    }

    public void awareApiEntity(Long id, EntityAware entityAware) {
        entityAware.setEntity(apiEntityRepository.getById(id));
    }

    @Transactional(readOnly = true)
    @BizError("09")
    public Application getById(Long id) {
        return getModelMapper().mapForRead(applicationEntityRepository.findById(id).get());
    }


    @Override
    @Transactional(readOnly = true)
    @BizError("10")
    public Application getAuthenticationDetails(Authenticating authenticating, String associatedId) {
        return getById(Long.valueOf(associatedId));
    }

    @Autowired
    public void setApplicationEntityRepository(ApplicationEntityRepository applicationEntityRepository) {
        this.applicationEntityRepository = applicationEntityRepository;
    }

    @Autowired
    public void setApiEntityRepository(ApiEntityRepository apiEntityRepository) {
        this.apiEntityRepository = apiEntityRepository;
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
