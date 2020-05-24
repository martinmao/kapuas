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

import org.apache.commons.lang3.StringUtils;
import org.scleropages.crud.GenericManager;
import org.scleropages.crud.dao.orm.SearchFilter;
import org.scleropages.crud.exception.BizError;
import org.scleropages.kapuas.app.entity.DomainEntity;
import org.scleropages.kapuas.app.entity.DomainEntityRepository;
import org.scleropages.kapuas.app.entity.DomainFunctionEntity;
import org.scleropages.kapuas.app.entity.DomainFunctionEntityRepository;
import org.scleropages.kapuas.app.model.Domain;
import org.scleropages.kapuas.app.model.DomainFunction;
import org.scleropages.kapuas.app.model.DomainFunctionMapper;
import org.scleropages.kapuas.app.model.DomainMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.List;
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


    @Validated({Domain.UpdateModel.class})
    @Transactional
    @BizError("01")
    public void save(@Valid Domain model) {
        save(model, false);
    }


    protected void save(Domain model, boolean forceDeepChanges) {
        DomainEntity domainEntity = domainEntityRepository.getByIdWithParentDomainEntity(model.getId());
        boolean namespaceChanged = isNameChangedAndApplyChanges(domainEntity, model);
        getModelMapper().mapForUpdate(model, domainEntity);
        DomainEntity associatedParent = domainEntity.getParentDomainEntity();
        if (null != model.getParentId()) {
            Assert.isTrue(!Objects.equals(model.getParentId(),domainEntity.getId()),"not allowed parent-id is equals to id.");
            Long currentParentId = model.getParentId();
            if (null == associatedParent || !Objects.equals(associatedParent.getId(), currentParentId)) {//parent has bean changed.
                DomainEntity currentParent = domainEntityRepository.getByIdOrNamespace(currentParentId, null);
                domainEntity.setParentDomainEntity(currentParent);
                namespaceChanged = true;
            }
        }
        if (forceDeepChanges || namespaceChanged) {
            DomainEntity currentParent = domainEntity.getParentDomainEntity();
            domainEntity.setNamespace(
                    (null != currentParent ? currentParent.getNamespace() + DOMAIN_SEPARATOR : StringUtils.EMPTY)
                            + domainEntity.getName());

            domainEntityRepository.saveAndFlush(domainEntity);//apply changes instantly.

            //apply changes to sub domains
            List<Long> subDomainIds = domainEntityRepository.findAllIdsByParentDomainId(domainEntity.getId());
            subDomainIds.forEach(subDomainId -> {
                Domain subDomain = new Domain();
                subDomain.setId(subDomainId);
                save(subDomain, true);
            });

            //apply changes to sub domain functions
            domainFunctionEntityRepository.findAllByDomainEntity_Id(domainEntity.getId()).forEach(
                    functionEntity -> computeAndApplyFunctionFullName(domainEntity, functionEntity));
        }
    }



    @Validated({Domain.CreateModel.class})
    @Transactional
    @BizError("02")
    public void create(@Valid Domain model) {
        DomainEntity domainEntity = getModelMapper().mapForSave(model);
        domainEntity.enable();
        Long parentId = model.getParentId();
        if (null != parentId) {
            DomainEntity parentDomainEntity = domainEntityRepository.getByIdOrNamespace(parentId, null);
            domainEntity.setParentDomainEntity(parentDomainEntity);
            domainEntity.setNamespace(parentDomainEntity.getNamespace() + DOMAIN_SEPARATOR + domainEntity.getName());
        } else {
            domainEntity.setNamespace(domainEntity.getName());
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
        DomainFunctionEntity functionEntity = getModelMapper(DomainFunctionMapper.class).mapForSave(function);
        functionEntity.enable();
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

    protected boolean isNameChangedAndApplyChanges(DomainEntity domainEntity, Domain model) {
        if (StringUtils.isBlank(model.getName()))
            return false;
        String currentName = domainEntity.getName();
        String applyName = model.getName();
        boolean nameChanged = !Objects.equals(currentName, applyName);
        if (nameChanged) {
            domainEntity.setNamespace(StringUtils.removeEnd(domainEntity.getNamespace(), currentName) + applyName);
            domainEntity.setName(applyName);
        }
        return nameChanged;
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
