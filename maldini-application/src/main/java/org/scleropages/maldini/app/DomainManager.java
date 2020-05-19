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

import org.apache.commons.lang3.StringUtils;
import org.scleropages.crud.GenericManager;
import org.scleropages.crud.dao.orm.SearchFilter;
import org.scleropages.crud.exception.BizError;
import org.scleropages.maldini.app.entity.DomainEntity;
import org.scleropages.maldini.app.entity.DomainEntityRepository;
import org.scleropages.maldini.app.entity.DomainFunctionEntity;
import org.scleropages.maldini.app.entity.DomainFunctionEntityRepository;
import org.scleropages.maldini.app.model.Domain;
import org.scleropages.maldini.app.model.DomainFunction;
import org.scleropages.maldini.app.model.DomainFunctionMapper;
import org.scleropages.maldini.app.model.DomainMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.Map;
import java.util.Objects;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
@Service
@Validated
@BizError("50")
public class DomainManager implements GenericManager<Domain, Long, DomainMapper> {


    private static final String DOMAIN_SEPARATOR = ".";

    private DomainEntityRepository domainEntityRepository;

    private DomainFunctionEntityRepository domainFunctionEntityRepository;

    private ApplicationManager applicationManager;

    @Override
    @Validated({Domain.UpdateModel.class})
    @Transactional
    @BizError("01")
    public void save(@Valid Domain model) {
        DomainEntity domainEntity = domainEntityRepository.getByIdWithParentDomainEntity(model.getId());
        boolean namespaceChanged = !Objects.equals(domainEntity.getNamespace(), model.getNamespace());
        getModelMapper().mapForUpdate(model, domainEntity);
        if (null != model.getParentId()) {
            DomainEntity associatedParent = domainEntity.getParentDomainEntity();
            Long currentParentId = model.getParentId();
            if (null == associatedParent || !Objects.equals(associatedParent.getId(), currentParentId)) {
                DomainEntity currentParent = domainEntityRepository.getByIdOrNamespace(currentParentId, null);
                domainEntity.setNamespace(
                        currentParent.getNamespace()
                                + DOMAIN_SEPARATOR
                                + StringUtils.removeStart(domainEntity.getNamespace(),
                                null != associatedParent ? associatedParent.getNamespace() : null));
                namespaceChanged = true;
            }
        }
        if (namespaceChanged) {
            domainFunctionEntityRepository.findAllByDomainEntity_Id(domainEntity.getId()).forEach(
                    functionEntity -> computeAndApplyFunctionFullName(domainEntity, functionEntity));
        }
    }

    @Validated({Domain.CreateModel.class})
    @Transactional
    @BizError("02")
    public void create(@Valid Domain model) {
        model.enable();
        DomainEntity domainEntity = getModelMapper().mapForSave(model);
        Long parentId = model.getParentId();
        if (null != parentId) {
            DomainEntity parentDomainEntity = domainEntityRepository.getByIdOrNamespace(parentId, null);
            domainEntity.setParentDomainEntity(parentDomainEntity);
            domainEntity.setNamespace(parentDomainEntity.getNamespace() + DOMAIN_SEPARATOR + domainEntity.getNamespace());
        }
        domainEntityRepository.save(domainEntity);
    }

    @Transactional(readOnly = true)
    @BizError("03")
    public Page<Domain> findDomainPage(Map<String, SearchFilter> searchFilters, Pageable pageable) {
        return domainEntityRepository.findPage(searchFilters, pageable).map(domainEntity -> getModelMapper().mapForRead(domainEntity));
    }

    @Validated({DomainFunction.UpdateModel.class})
    @Transactional
    @BizError("04")
    public void save(@Valid DomainFunction function) {
        DomainFunctionEntity functionEntity = domainFunctionEntityRepository.findById(function.getId()).get();
        getModelMapper(DomainFunctionMapper.class).mapForUpdate(function, functionEntity);
        computeAndApplyFunctionFullName(null, functionEntity);
        domainFunctionEntityRepository.save(functionEntity);
    }

    @Validated({DomainFunction.CreateModel.class})
    @Transactional
    @BizError("05")
    public void create(@Valid DomainFunction function) {
        function.enable();
        DomainFunctionEntity functionEntity = getModelMapper(DomainFunctionMapper.class).mapForSave(function);
        applicationManager.awareApiEntity(function.getApiId(), functionEntity);
        DomainEntity domainEntity = domainEntityRepository.getByIdOrNamespace(function.getDomainId(), null);
        functionEntity.setAppId(functionEntity.getApiEntity().getApplicationEntity().getAppId());
        functionEntity.setDomainEntity(domainEntity);
        computeAndApplyFunctionFullName(domainEntity, functionEntity);
        domainFunctionEntityRepository.save(functionEntity);
    }

    protected void computeAndApplyFunctionFullName(DomainEntity domainEntity, DomainFunctionEntity functionEntity) {
        if (null != domainEntity) {
            functionEntity.setFullName(domainEntity.getNamespace() + DOMAIN_SEPARATOR + functionEntity.getName());
        } else {
            String previousFullName = functionEntity.getFullName();
            if (!previousFullName.endsWith(functionEntity.getName())) {
                String previousName = StringUtils.substringAfterLast(previousFullName, DOMAIN_SEPARATOR);
                StringUtils.replace(previousFullName, previousName, functionEntity.getName());
            }
        }
    }

    @Transactional(readOnly = true)
    @BizError("06")
    public Page<DomainFunction> findDomainFunctionPage(Map<String, SearchFilter> searchFilters, Pageable pageable) {
        return domainFunctionEntityRepository.findPage(searchFilters, pageable).map(functionEntity -> getModelMapper(DomainFunctionMapper.class).mapForRead(functionEntity));
    }

    @Transactional(readOnly = true)
    @BizError("07")
    public String getAppIdByFunctionFullName(String funcFullName) {
        return domainFunctionEntityRepository.getAppIdByFunctionFullName(funcFullName);
    }

    @Override
    public Domain getById(Long id) {
        return getModelMapper().mapForRead(domainEntityRepository.findById(id).get());
    }

    @Autowired
    public void setDomainEntityRepository(DomainEntityRepository domainEntityRepository) {
        this.domainEntityRepository = domainEntityRepository;
    }

    @Autowired
    public void setDomainFunctionEntityRepository(DomainFunctionEntityRepository domainFunctionEntityRepository) {
        this.domainFunctionEntityRepository = domainFunctionEntityRepository;
    }

    @Autowired
    public void setApplicationManager(ApplicationManager applicationManager) {
        this.applicationManager = applicationManager;
    }
}
