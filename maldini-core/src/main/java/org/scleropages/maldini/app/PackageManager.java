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
import org.scleropages.crud.orm.SearchFilter;
import org.scleropages.maldini.app.entity.FunctionEntity;
import org.scleropages.maldini.app.entity.FunctionEntityRepository;
import org.scleropages.maldini.app.entity.PackageEntity;
import org.scleropages.maldini.app.entity.PackageEntityRepository;
import org.scleropages.maldini.app.model.Function;
import org.scleropages.maldini.app.model.FunctionMapper;
import org.scleropages.maldini.app.model.Package;
import org.scleropages.maldini.app.model.PackageMapper;
import org.springframework.beans.factory.annotation.Autowired;
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
public class PackageManager implements GenericManager<Package, Long, PackageMapper> {


    private PackageEntityRepository packageEntityRepository;

    private FunctionEntityRepository functionEntityRepository;

    private ApplicationManager applicationManager;

    @Override
    @Validated({Package.UpdateModel.class})
    @Transactional
    public void save(@Valid Package model) {
        PackageEntity packageEntity = packageEntityRepository.findById(model.getId()).get();
        boolean namespaceChanged = !Objects.equals(packageEntity.getNamespace(), model.getNamespace());
        getModelMapper().mapForUpdate(model, packageEntity);
        if (namespaceChanged)
            functionEntityRepository.findAllByPackageEntity_Id(packageEntity.getId()).forEach(
                    functionEntity -> populateFunctionFullName(packageEntity, functionEntity));
    }

    @Validated({Package.CreateModel.class})
    @Transactional
    public void create(@Valid Package model) {
        model.enable();
        packageEntityRepository.save(getModelMapper().mapForSave(model));
    }

    @Transactional(readOnly = true)
    public Page<Package> findPackagePage(Map<String, SearchFilter> searchFilters, Pageable pageable) {
        return packageEntityRepository.findPage(searchFilters, pageable);
    }

    @Validated({Function.UpdateModel.class})
    @Transactional
    public void save(@Valid Function function) {
        FunctionEntity functionEntity = functionEntityRepository.findById(function.getId()).get();
        getModelMapperByType(FunctionMapper.class).mapForUpdate(function, functionEntity);
        populateFunctionFullName(null, functionEntity);
        functionEntityRepository.save(functionEntity);
    }

    @Validated({Function.CreateModel.class})
    @Transactional
    public void create(@Valid Function function) {
        function.enable();
        FunctionEntity functionEntity = getModelMapperByType(FunctionMapper.class).mapForSave(function);
        applicationManager.awareApplicationEntity(function.getApplicationId(), functionEntity);
        PackageEntity packageEntity = findByIdOrNamespace(function.getPackageId(), null);
        function.setName(packageEntity.getNamespace() + function.getName());
        Assert.notNull(packageEntity, "package not found.");
        functionEntity.setPackageEntity(packageEntity);
        populateFunctionFullName(packageEntity, functionEntity);
        functionEntityRepository.save(functionEntity);
    }

    protected void populateFunctionFullName(PackageEntity packageEntity, FunctionEntity functionEntity) {
        if (null != packageEntity) {
            functionEntity.setFullName(packageEntity.getNamespace() + "." + functionEntity.getName());
        } else {
            String previousFullName = functionEntity.getFullName();
            if (!previousFullName.endsWith(functionEntity.getName())) {
                String previousName = StringUtils.substringAfterLast(previousFullName, ".");
                StringUtils.replace(previousFullName, previousName, functionEntity.getName());
            }
        }
    }

    @Transactional(readOnly = true)
    public Page<Function> findFunctionPage(Map<String, SearchFilter> searchFilters, Pageable pageable) {
        return functionEntityRepository.findPage(searchFilters, pageable);
    }

    @Transactional(readOnly = true)
    public String getAppIdByFunctionFullName(String funcFullName){
        return functionEntityRepository.findAppIdByFunctionFullName(funcFullName);
    }




    @Override
    public Package findById(Long id) {
        return getModelMapper().mapForRead(packageEntityRepository.findById(id).get());
    }

    protected PackageEntity findByIdOrNamespace(Long id, String namespace) {
        return packageEntityRepository.findByIdOrNamespace(id, namespace);
    }

    @Autowired
    public void setPackageEntityRepository(PackageEntityRepository packageEntityRepository) {
        this.packageEntityRepository = packageEntityRepository;
    }

    @Autowired
    public void setFunctionEntityRepository(FunctionEntityRepository functionEntityRepository) {
        this.functionEntityRepository = functionEntityRepository;
    }

    @Autowired
    public void setApplicationManager(ApplicationManager applicationManager) {
        this.applicationManager = applicationManager;
    }
}
