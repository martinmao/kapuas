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
package org.scleropages.maldini.security.acl.mgmt;

import com.google.common.collect.Lists;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.scleropages.core.mapper.JsonMapper2;
import org.scleropages.crud.dao.orm.SearchFilter;
import org.scleropages.crud.dao.orm.jpa.SearchFilterSpecifications;
import org.scleropages.crud.exception.BizViolationException;
import org.scleropages.maldini.security.acl.Acl;
import org.scleropages.maldini.security.acl.AclEntry;
import org.scleropages.maldini.security.acl.AclManager;
import org.scleropages.maldini.security.acl.AclPrincipal;
import org.scleropages.maldini.security.acl.entity.*;
import org.scleropages.maldini.security.acl.model.AclEntryModel;
import org.scleropages.maldini.security.acl.model.AclModel;
import org.scleropages.maldini.security.acl.model.AclPrincipalModel;
import org.scleropages.maldini.security.acl.model.AclStrategy;
import org.scleropages.maldini.security.acl.model.PermissionModel;
import org.scleropages.maldini.security.acl.model.ResourceModel;
import org.scleropages.maldini.security.acl.provider.AclProvider;
import org.scleropages.maldini.security.payload.BizPayloadEntity;
import org.scleropages.maldini.security.payload.BizPayloadEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
@Service
@Validated
public class GenericAclManager implements AclManager {


    private static final int ACL_BIZ_PAYLOAD_ID = 1;

    @Value("#{ @environment['security.acl.query_acl_with_max_variable_conditions'] ?: -1 }")
    private int maximumVariableConditionsAllowedInAclQuery;

    @Value("#{ @environment['security.acl.maximum_payload_in_query'] ?: -1 }")
    private int maximumBizPayloadAllowedInBatchQuery;

    private Map<Serializable, AclProvider> aclProviders;

    private AclProvider defaultAclProvider = new DefaultAclProvider();

    private PermissionEntityRepository permissionEntityRepository;

    private AclPrincipalEntityRepository aclPrincipalEntityRepository;

    private AclEntityRepository aclEntityRepository;

    private SimpleAclEntityRepository simpleAclEntityRepository;

    private AclEntryEntityRepository aclEntryEntityRepository;

    private SimpleAclPrincipalEntityRepository simpleAclPrincipalEntityRepository;

    private AclVariableEntityRepository aclVariableEntityRepository;

    private BizPayloadEntityRepository bizPayloadEntityRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<AclEntry> findEntries(@Validated(ResourceModel.ReadEntriesBySpecifyResource.class) ResourceModel resourceModel,
                                      AclPrincipalModel principal, Pageable pageable) {

        boolean principalProvided = null != principal && null != principal.getName();
        return getRequiredAclProvider(resourceModel).readEntries(resourceModel, principalProvided ? Optional.of(principal) : Optional.empty(), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AclEntry> findPrincipalEntries(@Validated(AclPrincipalModel.CreateAclModel.class) AclPrincipalModel principal, @Validated(ResourceModel.ReadEntriesBySpecifyResourceType.class) ResourceModel resourceModel,
                                               PermissionModel permissionModel, Pageable pageable) {
        return findPrincipalEntries(principal, resourceModel, permissionModel, pageable, MapUtils.EMPTY_MAP);

    }

    @Override
    public Page<AclEntry> findPrincipalEntries(@Validated(AclPrincipalModel.CreateAclModel.class) AclPrincipalModel principal, @Validated(ResourceModel.ReadEntriesBySpecifyResourceType.class) ResourceModel resourceModel, PermissionModel permissionModel, Pageable pageable, Map<String, SearchFilter> variablesSearchFilters) {

        boolean permissionProvided = null != permissionModel && StringUtils.isNotBlank(permissionModel.getName());

        if (-1 != maximumVariableConditionsAllowedInAclQuery && null != variablesSearchFilters && variablesSearchFilters.size() > maximumVariableConditionsAllowedInAclQuery) {
            throw new IllegalArgumentException("Not allowed many more variable search filter. maximum: " + maximumVariableConditionsAllowedInAclQuery + ". current: " + variablesSearchFilters.size());
        }

        return getRequiredAclProvider(resourceModel).readPrincipalEntries(principal, resourceModel, permissionProvided ?
                Optional.of(permissionModel) : Optional.empty(), pageable, variablesSearchFilters);
    }

    @Override
    public boolean isAccessible(@Valid ResourceModel resource, @Valid AclPrincipalModel principal, PermissionModel permission) {
        boolean permissionProvided = null != permission && StringUtils.isNotBlank(permission.getName());
        return getRequiredAclProvider(resource).existsPrincipalEntries(principal, resource, permissionProvided ?
                Optional.of(permission) : Optional.empty());
    }

    @Override
    @Transactional(readOnly = true)
    @Validated(ResourceModel.ReadAclModel.class)
    public Acl getAcl(ResourceModel resource) {
        PermissionEntity permissionEntity = permissionEntityRepository.getLocalPermissionEntityRepository().getFirstByResourceType(resource.getType());
        resource.setTypeId(permissionEntity.getResourceTypeId());
        return getRequiredAclProvider(resource).readAcl(resource, permissionEntity);
    }

    @Override
    @Transactional(readOnly = true)
    @Validated(ResourceModel.ReadEntriesBySpecifyResourceType.class)
    public Page<Acl> findAcl(ResourceModel resourceModel, Pageable pageable) {
        return findAcl(resourceModel, pageable, MapUtils.EMPTY_MAP);
    }

    @Override
    @Transactional(readOnly = true)
    @Validated(ResourceModel.ReadEntriesBySpecifyResourceType.class)
    public Page<Acl> findAcl(ResourceModel resourceModel, Pageable pageable, Map<String, SearchFilter> variablesSearchFilters) {
        PermissionEntity permissionEntity = permissionEntityRepository.getLocalPermissionEntityRepository().getFirstByResourceType(resourceModel.getType());
        if (-1 != maximumVariableConditionsAllowedInAclQuery && null != variablesSearchFilters && variablesSearchFilters.size() > maximumVariableConditionsAllowedInAclQuery) {
            throw new IllegalArgumentException("Not allowed many more variable search filter. maximum: " + maximumVariableConditionsAllowedInAclQuery + ". current: " + variablesSearchFilters.size());
        }
        return getRequiredAclProvider(resourceModel).readAcl(resourceModel, permissionEntity, pageable, variablesSearchFilters);
    }

    @Override
    @Transactional(readOnly = true)
    @Validated(ResourceModel.ReadEntriesBySpecifyResourceType.class)
    public List<String> findAllAclBizPayload(ResourceModel resourceModel, Long... aclIds) {
        if (-1 != maximumBizPayloadAllowedInBatchQuery && null != aclIds && aclIds.length > maximumBizPayloadAllowedInBatchQuery) {
            throw new IllegalArgumentException("Not allowed many more payload search. maximum: " + maximumBizPayloadAllowedInBatchQuery + ". current: " + aclIds.length);
        }

        Long typeId = permissionEntityRepository.getLocalPermissionEntityRepository().getResourceTypeIdByResourceType(resourceModel.getType());
        return bizPayloadEntityRepository.findAllTextByBizIdAndTypeIdAndPayloadId(ACL_BIZ_PAYLOAD_ID, typeId, aclIds);
    }

    @Override
    @Transactional
    @Validated({ResourceModel.CreateModel.class})
    public void createAcl(ResourceModel resource) {
        PermissionEntity permissionEntity = permissionEntityRepository.getLocalPermissionEntityRepository().getFirstByResourceType(resource.getType());
        AclPrincipalEntity principalEntity = aclPrincipalEntityRepository.getByName(resource.getOwner());
        Assert.notNull(principalEntity, "no principal found by given resource owner.");
        resource.setTypeId(permissionEntity.getResourceTypeId());
        getRequiredAclProvider(resource).createAcl(resource, permissionEntity, principalEntity);
    }

    @Override
    @Transactional
    @Validated(ResourceModel.UpdateModel.class)
    public void updateAcl(ResourceModel resource) {
        PermissionEntity permissionEntity = permissionEntityRepository.getLocalPermissionEntityRepository().getFirstByResourceType(resource.getType());
        Long resourceTypeId = permissionEntityRepository.getLocalPermissionEntityRepository().getResourceTypeIdByResourceType(resource.getType());
        resource.setTypeId(resourceTypeId);
        String owner = resource.getOwner();
        if (StringUtils.isNotBlank(owner)) {
            AclPrincipalEntity principalEntity = aclPrincipalEntityRepository.getByName(owner);
            Assert.notNull(principalEntity, "no principal found by given resource owner.");
            getRequiredAclProvider(resource).updateAcl(resource, permissionEntity, Optional.of(principalEntity));
        } else
            getRequiredAclProvider(resource).updateAcl(resource, permissionEntity, Optional.empty());
    }

    @Override
    @Transactional
    @Validated(ResourceModel.UpdateModel.class)
    public void deleteAcl(@Valid ResourceModel resource) {
        PermissionEntity permissionEntity = permissionEntityRepository.getLocalPermissionEntityRepository().getFirstByResourceType(resource.getType());
        Long resourceTypeId = permissionEntityRepository.getLocalPermissionEntityRepository().getResourceTypeIdByResourceType(resource.getType());
        resource.setTypeId(resourceTypeId);
        getRequiredAclProvider(resource).deleteAcl(resource, permissionEntity);
    }

    @Override
    @Transactional
    public void createAclEntry(@Validated(ResourceModel.ReadAclModel.class) ResourceModel resource,
                               @Validated(AclPrincipalModel.CreateAclModel.class) AclPrincipalModel grant,
                               PermissionModel... permission) {
        AclPrincipalEntity principalEntity = aclPrincipalEntityRepository.getByName(grant.getName());
        List<PermissionEntity> permissionEntities = permissionEntityRepository.getLocalPermissionEntityRepository().findAllByResourceType(resource.getType());
        assertAclEntryArgumentsValid(resource, principalEntity, permissionEntities, permission);
        List<PermissionEntity> hits = Lists.newArrayList();

        /*
        for each grants permissions do follow things:
        1.check given permission already defined in acl strategy
        2.inherit permission merge:
        admin>write>read(given admin,write,read)->admin
        admin>publish,subscribe(given publish,subscribe)->publish,subscribe
        */
        if (null != permission) {
            PermissionEntity permissionEntity = null;
            for (PermissionModel model : permission) {
                for (PermissionEntity entity : permissionEntities) {
                    if (Objects.equals(entity.getName(), model.getName())) {
                        permissionEntity = entity;
                        break;
                    }
                }
                Assert.notNull(permissionEntity, "no match permission[" + model.getName() + "] definition found by given resource: " + resource.getType());
                int action = 0;
                int replaceIdx = -1;
                for (int i = 0; i < hits.size(); i++) {
                    if (StringUtils.contains(hits.get(i).getExtension(), permissionEntity.getName())) {
                        action = -1;
                        break;
                    }
                    if (StringUtils.contains(permissionEntity.getExtension(), hits.get(i).getName())) {
                        action = 1;
                        replaceIdx = i;
                        break;
                    }
                }
                if (action == 0)
                    hits.add(permissionEntity);
                if (action == 1)
                    hits.set(replaceIdx, permissionEntity);
            }
        }
        if (hits.size() > 0)
            getRequiredAclProvider(resource).createAclEntry(resource, principalEntity, hits.toArray(new PermissionEntity[hits.size()]));
        else
            getRequiredAclProvider(resource).createAclEntryWithoutPermission(resource, principalEntity);
    }


    @Override
    @Transactional
    public void deleteAclEntry(@Validated(ResourceModel.ReadAclModel.class) ResourceModel resource, @Validated(AclPrincipalModel.CreateAclModel.class) AclPrincipalModel grant, PermissionModel... permission) {
        AclPrincipalEntity principalEntity = aclPrincipalEntityRepository.getByName(grant.getName());
        List<PermissionEntity> permissionEntities = permissionEntityRepository.getLocalPermissionEntityRepository().findAllByResourceType(resource.getType());
        assertAclEntryArgumentsValid(resource, principalEntity, permissionEntities, permission);
        List<PermissionEntity> deletedPermissionEntries = Lists.newArrayList();

        if (null != permission) {
            for (PermissionModel model : permission) {
                boolean hit = false;
                for (PermissionEntity entity : permissionEntities) {
                    if (hit = Objects.equals(entity.getName(), model.getName())) {
                        deletedPermissionEntries.add(entity);
                        break;
                    }
                }
                Assert.isTrue(hit, "no permission definition found by given permission: " + model.getName());
            }
        }
        if (deletedPermissionEntries.size() > 0)
            getRequiredAclProvider(resource).deleteAclEntry(resource, principalEntity, deletedPermissionEntries.toArray(new PermissionEntity[deletedPermissionEntries.size()]));
        else
            getRequiredAclProvider(resource).deleteAclEntryWithoutPermission(resource, principalEntity);
    }

    protected void assertAclEntryArgumentsValid(ResourceModel resource, AclPrincipalEntity principalEntity, List<PermissionEntity> permissionEntities, PermissionModel... permission) {
        Assert.notNull(principalEntity, "grant principal not found.");
        Assert.notEmpty(permissionEntities, "no acl strategy found by given resource.");
        PermissionEntity firstPermissionEntity = permissionEntities.get(0);

        if (permissionEntities.size() == 1 && firstPermissionEntity.isNotSupport() && null != permission) {
            throw new BizViolationException("current resource is coarse-grained acl model(see acl strategy). not support permission argument.");
        }
        if (permissionEntities.size() > 1 && null == permission) {
            throw new BizViolationException("current resource is fine-grained acl model(see acl strategy). permission is required.");
        }
        resource.setTypeId(permissionEntities.get(0).getResourceTypeId());
    }


    class DefaultAclProvider implements AclProvider {

        @Override
        public Serializable type() {
            throw new IllegalStateException("Never call this method. this class was default acl provider.");
        }

        @Override
        public void createAcl(ResourceModel resource, PermissionEntity permissionEntity, AclPrincipalEntity principalEntity) {
            AbstractAclEntityRepository abstractAclEntityRepository = getAclEntityRepository(permissionEntity);
            Assert.isTrue(!abstractAclEntityRepository.existsByResourceTypeIdAndResourceId(resource.getTypeId(), resource.getId()), "resource exists.");
            AbstractAclEntity abstractAclEntity = permissionEntity.isNotSupport() ? new SimpleAclEntity() : new AclEntity();
            mapAbstractAclEntity(abstractAclEntity, resource, permissionEntity, principalEntity);
            abstractAclEntity = (AbstractAclEntity) abstractAclEntityRepository.save(abstractAclEntity);

            aclVariableEntityRepository.save(mapAclVariableEntities(resource, abstractAclEntity));

            String bizPayLoad = resource.getBizPayload();
            if (StringUtils.isBlank(bizPayLoad) && null != resource.getBizBody()) {
                bizPayLoad = JsonMapper2.toJson(resource.getBizBody());
            }
            if (StringUtils.isNotBlank(bizPayLoad)) {
                BizPayloadEntity bizPayloadEntity = new BizPayloadEntity();
                bizPayloadEntity.setBizId(ACL_BIZ_PAYLOAD_ID);
                bizPayloadEntity.setTypeId(resource.getTypeId());
                bizPayloadEntity.setPayloadId(abstractAclEntity.getId());
                bizPayloadEntity.setText(bizPayLoad);
                bizPayloadEntityRepository.save(bizPayloadEntity);
            }
        }

        @Override
        public void updateAcl(ResourceModel resource, PermissionEntity permissionEntity, Optional<AclPrincipalEntity> aclPrincipalEntity) {
            AbstractAclEntityRepository abstractAclEntityRepository = getAclEntityRepository(permissionEntity);

            AbstractAclEntity abstractAclEntity = abstractAclEntityRepository.getByResourceTypeIdAndResourceId(resource.getTypeId(), resource.getId());
            Assert.notNull(abstractAclEntity, "no acl found by given resource.");
            aclPrincipalEntity.ifPresent(principalEntity -> abstractAclEntity.setOwner(principalEntity));
            if (StringUtils.isNotBlank(resource.getTag())) {
                abstractAclEntity.setResourceTag(resource.getTag());
            }
            String bizPayLoad = resource.getBizPayload();
            if (StringUtils.isBlank(bizPayLoad) && null != resource.getBizBody()) {
                bizPayLoad = JsonMapper2.toJson(resource.getBizBody());
            }
            if (StringUtils.isNotBlank(bizPayLoad)) {
                bizPayloadEntityRepository.saveTextByBizIdAndTypeIdAndPayloadId(ACL_BIZ_PAYLOAD_ID, abstractAclEntity.getResourceTypeId(), abstractAclEntity.getId(), bizPayLoad);
            }
            aclVariableEntityRepository.deleteAllByAclIdAndResourceTypeId(abstractAclEntity.getId(), abstractAclEntity.getResourceTypeId());
            aclVariableEntityRepository.save(mapAclVariableEntities(resource, abstractAclEntity));
        }

        @Override
        public void deleteAcl(ResourceModel resource, PermissionEntity permissionEntity) {
            AbstractAclEntityRepository aclEntityRepository = getAclEntityRepository(permissionEntity);

            Long aclId = aclEntityRepository.getIdByResourceTypeIdAndResourceId(resource.getTypeId(), resource.getId());

            AbstractAclEntryEntityRepository aclEntryEntityRepository = getAclEntryEntityRepository(permissionEntity);

            Assert.isTrue(!aclEntryEntityRepository.existsByResourceTypeIdAndResourceId(resource.getTypeId(), resource.getId()), "can't delete acl when contains exists associated entries.");

            aclEntityRepository.deleteById(aclId);
        }

        private List<AclVariableEntity> mapAclVariableEntities(ResourceModel resource, AbstractAclEntity aclEntity) {
            if (MapUtils.isEmpty(resource.getVariables()))
                return Collections.emptyList();
            Map<String, Object> variables = resource.getVariables();
            List<AclVariableEntity> variablesToSave = new ArrayList<>(variables.size());
            for (Map.Entry<String, Object> kv :
                    variables.entrySet()) {
                AclVariableEntity variableEntity = new AclVariableEntity();
                variableEntity.setAclId(aclEntity.getId());
                variableEntity.setResourceTypeId(aclEntity.getResourceTypeId());
                variableEntity.setName(kv.getKey());
                variableEntity.setValue(kv.getValue());
                variablesToSave.add(variableEntity);
            }
            return variablesToSave;
        }


        @Override
        public Acl readAcl(ResourceModel resourceModel, PermissionEntity permissionEntity) {
            AclModel aclModel = new AclModel();
            mapAclModel(aclModel, getAclEntityRepository(permissionEntity).getByResourceTypeIdAndResourceIdWithOwner(resourceModel.getTypeId(), resourceModel.getId()), true, true);
            return aclModel;
        }

        protected AbstractAclEntityRepository getAclEntityRepository(PermissionEntity permissionEntity) {
            if (permissionEntity.isNotSupport())
                return simpleAclEntityRepository;
            return aclEntityRepository;
        }

        @Override
        public Page<Acl> readAcl(ResourceModel resourceModel, PermissionEntity permissionEntity, Pageable pageable, Map<String, SearchFilter> variablesSearchFilters) {
            return getAclEntityRepository(permissionEntity).findByResourceTypeId(permissionEntity.getResourceTypeId(), pageable, variablesSearchFilters).map(entity -> {
                AclModel aclModel = new AclModel();
                mapAclModel(aclModel, (AbstractAclEntity) entity, false, false);
                return aclModel;
            });
        }

        @Override
        public void createAclEntry(ResourceModel resource, AclPrincipalEntity aclPrincipalEntity, PermissionEntity... permissionEntity) {

            AclEntity aclEntity = aclEntityRepository.getByResourceTypeIdAndResourceId(resource.getTypeId(), resource.getId());
            Assert.notNull(aclEntity, "resource not found.");

            for (PermissionEntity item : permissionEntity) {
                Assert.isTrue(!aclEntryEntityRepository.existsByAcl_IdAndGrant_IdAndPermission_Id(aclEntity.getId(), aclPrincipalEntity.getId(), item.getId()),
                        "principal already have permit: " + item.getName());
                AclEntryEntity aclEntryEntity = new AclEntryEntity();
                aclEntryEntity.setResourceId(resource.getId());
                aclEntryEntity.setResourceTypeId(item.getResourceTypeId());
                aclEntryEntity.setAclPrincipalName(aclPrincipalEntity.getName());
                aclEntryEntity.setPermissionName((null != item.getExtension() ? item.getExtension() + PermissionEntity.PERMISSION_EXTENSION_SEPARATOR : "") + item.getName());
                aclEntryEntity.setAcl(aclEntity);
                aclEntryEntity.setGrant(aclPrincipalEntity);
                aclEntryEntity.setPermission(item);
                aclEntryEntityRepository.save(aclEntryEntity);
            }
            if (!permissionEntityRepository.getLocalPermissionEntityRepository().isHierarchyPermissionResource(permissionEntity[0].getResourceType())) {
                return;
            }
            //对比分配权限列表，如果存在权限包含关系，则删除被包含的权限，此实现思路可以支持权限升级，但不支持权限降级，即admin->write->read，如果已经具备了admin权限的，此时赋予write或read，不会产生任何变化
            //要实现权限降级，必须先删除高级的acl entry在创建新的条目
            List<AclEntryEntity> grants = aclEntryEntityRepository.findByAcl_IdAndGrant_Id(aclEntity.getId(), aclPrincipalEntity.getId());
            for (AclEntryEntity grant : grants) {
                for (AclEntryEntity compareGrant : grants) {
                    if (compareGrant.getPermission().isInheritInclude(grant.getPermission())) {
                        aclEntryEntityRepository.delete(grant);
                    }
                }
            }
        }

        @Override
        public void createAclEntryWithoutPermission(ResourceModel resource, AclPrincipalEntity aclPrincipalEntity) {
            SimpleAclEntity aclEntity = simpleAclEntityRepository.getByResourceTypeIdAndResourceId(resource.getTypeId(), resource.getId());
            Assert.notNull(aclEntity, "resource not found.");
            Assert.isTrue(!simpleAclPrincipalEntityRepository.existsByAcl_IdAndGrant_Id(aclEntity.getId(), aclPrincipalEntity.getId()),
                    "principal already have owned by resource.");

            SimpleAclPrincipalEntity simpleAclPrincipalEntity = new SimpleAclPrincipalEntity();
            simpleAclPrincipalEntity.setGrant(aclPrincipalEntity);
            simpleAclPrincipalEntity.setAcl(aclEntity);
            simpleAclPrincipalEntity.setAclPrincipalName(aclPrincipalEntity.getName());
            simpleAclPrincipalEntity.setResourceId(resource.getId());
            simpleAclPrincipalEntity.setResourceTypeId(resource.getTypeId());
            simpleAclPrincipalEntityRepository.save(simpleAclPrincipalEntity);
        }

        @Override
        public void deleteAclEntry(ResourceModel resource, AclPrincipalEntity aclPrincipalEntity, PermissionEntity... permissionEntity) {
            Long aclId = aclEntityRepository.getIdByResourceTypeIdAndResourceId(resource.getTypeId(), resource.getId());
            Stream.of(permissionEntity).forEach(permission -> {
                Long entryId = aclEntryEntityRepository.getIdByAcl_IdAndGrant_IdAndPermission_Id(aclId, aclPrincipalEntity.getId(), permission.getId());
                aclEntryEntityRepository.deleteById(entryId);
            });
        }

        @Override
        public void deleteAclEntryWithoutPermission(ResourceModel resource, AclPrincipalEntity aclPrincipalEntity) {

            Long aclId = simpleAclEntityRepository.getIdByResourceTypeIdAndResourceId(resource.getTypeId(), resource.getId());
            Long entryId = simpleAclPrincipalEntityRepository.getIdByAcl_IdAndGrant_Id(aclId, aclPrincipalEntity.getId());
            simpleAclPrincipalEntityRepository.deleteById(entryId);
        }

        protected AbstractAclEntryEntityRepository getAclEntryEntityRepository(PermissionEntity permissionEntity) {
            if (permissionEntity.isNotSupport())
                return simpleAclPrincipalEntityRepository;
            return aclEntryEntityRepository;
        }

        @Override
        public Page<AclEntry> readEntries(ResourceModel resourceModel, Optional<AclPrincipalModel> principalModel, Pageable pageable) {
            PermissionEntity permissionEntity = permissionEntityRepository.getLocalPermissionEntityRepository().getFirstByResourceType(resourceModel.getType());
            Page<? extends AbstractAclEntryEntity> entryEntities;

            AbstractAclEntryEntityRepository abstractAclEntryEntityRepository = getAclEntryEntityRepository(permissionEntity);
            entryEntities = principalModel.isPresent() ?
                    abstractAclEntryEntityRepository.findByAclPrincipalNameAndResourceTypeIdAndResourceId(principalModel.get().getName(), getResourceTypeIdByResourceModel(resourceModel), resourceModel.getId(), pageable) :
                    abstractAclEntryEntityRepository.findByResourceTypeIdAndResourceId(getResourceTypeIdByResourceModel(resourceModel), resourceModel.getId(), pageable);

            return entryEntities.map(this::mapAclEntryModel);
        }

        @Override
        public Page<AclEntry> readPrincipalEntries(AclPrincipalModel principal, ResourceModel resourceModel, Optional<PermissionModel> permission, Pageable pageable, Map<String, SearchFilter> variablesSearchFilters) {
            String resourceType = resourceModel.getType();
            PermissionEntity permissionEntity = assertPermissionModel(resourceModel, permission);

            String resourceId = resourceModel.getId();
            Long resourceTypeId = getResourceTypeIdByResourceModel(resourceModel);

            Page<? extends AbstractAclEntryEntity> entryEntities;

            AbstractAclEntryEntityRepository abstractAclEntryEntityRepository = getAclEntryEntityRepository(permissionEntity);

            boolean hierarchyPermission = permissionEntityRepository.getLocalPermissionEntityRepository().isHierarchyPermissionResource(resourceType);

            if (StringUtils.isNotBlank(resourceId)) {
                if (permission.isPresent()) {
                    String permissionToMatch = permissionEntityRepository.getLocalPermissionEntityRepository().getPermissionEntity(resourceType, permission.get().getName()).getExtensionAndName();
                    entryEntities = hierarchyPermission ?
                            aclEntryEntityRepository.findByAclPrincipalNameAndResourceTypeIdAndResourceIdAndPermissionNameLike(principal.getName(), resourceTypeId, resourceId, permissionToMatch + "%", pageable)
                            : aclEntryEntityRepository.findByAclPrincipalNameAndResourceTypeIdAndResourceIdAndPermissionName(principal.getName(), resourceTypeId, resourceId, permissionToMatch, pageable);
                } else
                    entryEntities = abstractAclEntryEntityRepository.findByAclPrincipalNameAndResourceTypeIdAndResourceId(principal.getName(), resourceTypeId, resourceId, pageable);
            } else {
                if (permission.isPresent()) {
                    String permissionToMatch = permissionEntityRepository.getLocalPermissionEntityRepository().getPermissionEntity(resourceType, permission.get().getName()).getExtensionAndName();
                    entryEntities = hierarchyPermission ?
                            aclEntryEntityRepository.findByAclPrincipalNameAndResourceTypeIdAndPermissionNameLike(principal.getName(), resourceTypeId, permissionToMatch + "%", pageable, variablesSearchFilters)
                            : aclEntryEntityRepository.findByAclPrincipalNameAndResourceTypeIdAndPermissionName(principal.getName(), resourceTypeId, permissionToMatch, pageable, variablesSearchFilters);
                } else
                    entryEntities = abstractAclEntryEntityRepository.findByAclPrincipalNameAndResourceTypeId(principal.getName(), resourceTypeId, pageable, variablesSearchFilters);
            }
            return entryEntities.map(this::mapAclEntryModel);
        }


        @Override
        public Boolean existsPrincipalEntries(AclPrincipalModel principal, ResourceModel resourceModel, Optional<PermissionModel> permission) {
            PermissionEntity permissionEntity = assertPermissionModel(resourceModel, permission);

            String resourceType = resourceModel.getType();
            String resourceId = resourceModel.getId();
            Long resourceTypeId = getResourceTypeIdByResourceModel(resourceModel);

            AbstractAclEntryEntityRepository abstractAclEntryEntityRepository = getAclEntryEntityRepository(permissionEntity);

            boolean hierarchyPermission = permissionEntityRepository.getLocalPermissionEntityRepository().isHierarchyPermissionResource(resourceType);

            if (StringUtils.isNotBlank(resourceId)) {
                if (permission.isPresent()) {
                    String permissionToMatch = permissionEntityRepository.getLocalPermissionEntityRepository().getPermissionEntity(resourceType, permission.get().getName()).getExtensionAndName();
                    return hierarchyPermission ?
                            aclEntryEntityRepository.existsByAclPrincipalNameAndResourceTypeIdAndResourceIdAndPermissionNameLike(principal.getName(), resourceTypeId, resourceId, permissionToMatch + "%")
                            : aclEntryEntityRepository.existsByAclPrincipalNameAndResourceTypeIdAndResourceIdAndPermissionName(principal.getName(), resourceTypeId, resourceId, permissionToMatch);
                } else
                    return abstractAclEntryEntityRepository.existsByAclPrincipalNameAndResourceTypeIdAndResourceId(principal.getName(), resourceTypeId, resourceId);
            } else {
                if (permission.isPresent()) {
                    String permissionToMatch = permissionEntityRepository.getLocalPermissionEntityRepository().getPermissionEntity(resourceType, permission.get().getName()).getExtensionAndName();
                    return hierarchyPermission ?
                            aclEntryEntityRepository.existsByAclPrincipalNameAndResourceTypeIdAndPermissionNameLike(principal.getName(), resourceTypeId, permissionToMatch + "%")
                            : aclEntryEntityRepository.existsByAclPrincipalNameAndResourceTypeIdAndPermissionName(principal.getName(), resourceTypeId, permissionToMatch);
                } else
                    return abstractAclEntryEntityRepository.existsByAclPrincipalNameAndResourceTypeId(principal.getName(), resourceTypeId);
            }
        }

        protected Long getResourceTypeIdByResourceModel(ResourceModel resourceModel) {
            return null != resourceModel.getTypeId() ? resourceModel.getTypeId() : permissionEntityRepository.getLocalPermissionEntityRepository().getResourceTypeIdByResourceType(resourceModel.getType());
        }

        protected AclEntryModel mapAclEntryModel(AbstractAclEntryEntity entryEntity) {
            String permission = entryEntity instanceof AclEntryEntity ? ((AclEntryEntity) entryEntity).getPermissionName() : PermissionEntity.NOT_SUPPORT;
            AclEntryModel aclEntryModel = new AclEntryModel(entryEntity.getId(), entryEntity.getAclPrincipalName(), permission, permissionEntityRepository.getLocalPermissionEntityRepository().getResourceTypeByResourceId(entryEntity.getResourceTypeId()), entryEntity.getResourceId());
            return aclEntryModel;
        }

        protected void mapAbstractAclEntity(AbstractAclEntity acl, ResourceModel resource, PermissionEntity permission, AclPrincipalEntity principalEntity) {
            acl.setResourceId(resource.getId());
            acl.setResourceTypeId(permission.getResourceTypeId());
            acl.setResourceTag(resource.getTag());
            acl.setOwner(principalEntity);
        }

        protected PermissionEntity assertPermissionModel(ResourceModel resourceModel, Optional<PermissionModel> permission) {
            PermissionEntity permissionEntity = permissionEntityRepository.getLocalPermissionEntityRepository().getFirstByResourceType(resourceModel.getType());

            Assert.isTrue(!(permissionEntity.isNotSupport() && permission.isPresent()), "current resource is coarse-grained acl model(see acl strategy). not support permission argument.");
            Assert.isTrue((!permissionEntity.isNotSupport()) && permission.isPresent(), "current resource is fine-grained acl model(see acl strategy). permission is required argument.");
            return permissionEntity;
        }

        protected void mapAclModel(AclModel aclModel, AbstractAclEntity aclEntity, boolean mapOwner, boolean mapVariables) {
            Assert.notNull(aclEntity, "resource not found.");
            aclModel.setId(aclEntity.getId());
            aclModel.setResourceId(aclEntity.getResourceId());
            aclModel.setResourceType(permissionEntityRepository.getLocalPermissionEntityRepository().getResourceTypeByResourceId(aclEntity.getResourceTypeId()));
            aclModel.setTag(aclEntity.getResourceTag());
            if (mapOwner) {
                AclPrincipalEntity ownerEntity = aclEntity.getOwner();
                AclPrincipalModel aclPrincipalModel = new AclPrincipalModel();
                aclPrincipalModel.setName(ownerEntity.getName());
                aclPrincipalModel.setTag(ownerEntity.getTag());
                aclModel.setOwners(Lists.newArrayList(aclPrincipalModel));
            }
            if (mapVariables) {
                List<AclVariableEntity> variableEntities = aclVariableEntityRepository.findAllByAclIdAndResourceTypeId(aclEntity.getId(), aclEntity.getResourceTypeId());
                if (!CollectionUtils.isEmpty(variableEntities)) {
                    Map<String, Object> variables = new HashMap<>(variableEntities.size());
                    aclModel.setVariables(variables);
                    variableEntities.forEach(variableEntity -> {
                        variables.put(variableEntity.getName(), variableEntity.getValue());
                    });
                }
            }
        }
    }

    @Override
    @Validated(AclPrincipalModel.CreateModel.class)
    @Transactional
    public void createAclPrincipal(AclPrincipalModel aclPrincipalModel) {
        Assert.isTrue(!aclPrincipalEntityRepository.existsByName(aclPrincipalModel.getName()), "principal exists.");
        AclPrincipalEntity aclPrincipalEntity = new AclPrincipalEntity();
        aclPrincipalEntity.setTag(aclPrincipalModel.getTag());
        aclPrincipalEntity.setName(aclPrincipalModel.getName());
        aclPrincipalEntityRepository.save(aclPrincipalEntity);
    }


    @Override
    @Transactional(readOnly = true)
    public Page<AclPrincipal> findAclPrincipals(Map<String, SearchFilter> searchFilters, Pageable pageable) {

        Page<AclPrincipalEntity> entities = aclPrincipalEntityRepository.findAll(SearchFilterSpecifications.bySearchFilter(searchFilters.values(), AclPrincipalEntity.class), pageable);

        return entities.map(aclPrincipalEntity -> {
            AclPrincipalModel aclPrincipalModel = new AclPrincipalModel();
            aclPrincipalModel.setId(aclPrincipalEntity.getId());
            aclPrincipalModel.setName(aclPrincipalEntity.getName());
            aclPrincipalModel.setTag(aclPrincipalEntity.getTag());
            return aclPrincipalModel;
        });
    }

    @Override
    @Transactional
    @Validated(AclStrategy.CreateModel.class)
    public void createAclStrategy(AclStrategy aclStrategy) {
        String resource = aclStrategy.getResource();
        Assert.isTrue(permissionEntityRepository.getLocalPermissionEntityRepository().getResourceTypeIdByResourceType(resource) == null, resource + " already configure.");
        String expression = aclStrategy.getExpression();
        long resourceTypeId = 0L;
        if (aclStrategy.isFineGrainedAclModel()) {
            String[] splitExpressions;
            boolean inherit;
            if (inherit = aclStrategy.isInheritPermissionExpression()) {
                if (expression.contains(AclStrategy.EXP_FORMAT_PERMISSION_SEPARATOR)) {
                    throw new IllegalArgumentException("not support mixed mode for permission separator( '>' or ',' Only one you can be selected).");
                }
                splitExpressions = aclStrategy.splitExpressionByPermissionInheritSeparator();
            } else {
                splitExpressions = aclStrategy.splitExpressionByPermissionSeparator();
            }
            String extension = "";
            for (int i = splitExpressions.length - 1; i > -1; i--) {
                PermissionEntity permissionEntity = new PermissionEntity();
                permissionEntity.setResourceType(resource);
                String[] nameTag = StringUtils.split(splitExpressions[i], AclStrategy.EXP_FORMAT_PERMISSION_NAME_TAG_SEPARATOR);
                String name = nameTag[0];
                permissionEntity.setName(name);
                permissionEntity.setTag(nameTag.length == 2 ? nameTag[1] : name);
                if (inherit && StringUtils.isNotBlank(extension)) {
                    permissionEntity.setExtension(StringUtils.removeEnd(extension, PermissionEntity.PERMISSION_EXTENSION_SEPARATOR));
                }
                extension = extension + name + PermissionEntity.PERMISSION_EXTENSION_SEPARATOR;
                permissionEntity.setResourceTypeId(resourceTypeId);
                permissionEntity = permissionEntityRepository.save(permissionEntity);
                if (resourceTypeId == 0L) {//at first must saved payload to fetch id. and use id as resourceTypeId
                    resourceTypeId = permissionEntity.getId();
                    permissionEntity.setResourceTypeId(resourceTypeId);
                }
            }
        } else {
            PermissionEntity permissionEntity = new PermissionEntity();
            permissionEntity.setResourceType(resource);
            permissionEntity.setResourceTypeId(resourceTypeId);
            permissionEntity = permissionEntityRepository.save(permissionEntity);
            permissionEntity.setResourceTypeId(permissionEntity.getId());
        }
    }

    @Override
    public String[] getAllAclStrategyResourceTypes() {
        return permissionEntityRepository.getLocalPermissionEntityRepository().getAllResourceTypes();
    }

    @Override
    @Transactional(readOnly = true)
    public AclStrategy getAclStrategy(String resource) {
        Assert.hasText(resource, "given resource must not be empty text.");
        List<PermissionEntity> permissions = permissionEntityRepository.getLocalPermissionEntityRepository().findAllByResourceType(resource);
        AclStrategy aclStrategy = new AclStrategy();
        if (permissions.size() == 0)
            throw new IllegalArgumentException("resource not found.");
        List<PermissionModel> permissionModels = Lists.newArrayList();
        for (int i = 0; i < permissions.size(); i++) {
            PermissionEntity permissionEntity = permissions.get(i);
            if (permissions.size() == 1 && permissionEntity.isNotSupport()) {
                aclStrategy.setResource(permissionEntity.getResourceType());
//                aclStrategy.setExpression(PermissionEntity.NOT_SUPPORT + ": not supported fine-grained acl model." +
//                        " Current resource just have one action for specify resource). The Acl entries is not required. principal can associated resource directly.");
                return aclStrategy;
            }
            PermissionModel permissionModel = new PermissionModel();
            permissionModel.setName(permissionEntity.getName());
            if (null != permissionEntity.getExtension()) {
                permissionModel.setExtension(permissionEntity.getExtension());
            }
            permissionModel.setTag(permissionEntity.getTag());
            permissionModel.setId(String.valueOf(permissionEntity.getId()));
            permissionModels.add(permissionModel);
        }
        return new AclStrategy(resource, permissionModels.toArray(new PermissionModel[permissionModels.size()]));
    }


    protected AclProvider getRequiredAclProvider(ResourceModel resourceModel) {
        Assert.notNull(resourceModel, "resourceModel must not be null.");
        Assert.notNull(resourceModel.getType(), "resourceModel.type must not be null.");
        if (null == aclProviders)
            return defaultAclProvider;
        AclProvider aclProvider = aclProviders.get(resourceModel.getType());
        return null != aclProvider ? aclProvider : defaultAclProvider;
    }

    public void setAclProviders(Map<Serializable, AclProvider> aclProviders) {
        this.aclProviders = aclProviders;
    }

    @Autowired(required = false)
    public void setDefaultAclProvider(AclProvider defaultAclProvider) {
        this.defaultAclProvider = defaultAclProvider;
    }

    @Autowired
    public void setPermissionEntityRepository(PermissionEntityRepository permissionEntityRepository) {
        this.permissionEntityRepository = permissionEntityRepository;
    }

    @Autowired
    public void setAclPrincipalEntityRepository(AclPrincipalEntityRepository aclPrincipalEntityRepository) {
        this.aclPrincipalEntityRepository = aclPrincipalEntityRepository;
    }

    @Autowired
    public void setAclEntityRepository(AclEntityRepository aclEntityRepository) {
        this.aclEntityRepository = aclEntityRepository;
    }

    @Autowired
    public void setSimpleAclEntityRepository(SimpleAclEntityRepository simpleAclEntityRepository) {
        this.simpleAclEntityRepository = simpleAclEntityRepository;
    }

    @Autowired
    public void setAclEntryEntityRepository(AclEntryEntityRepository aclEntryEntityRepository) {
        this.aclEntryEntityRepository = aclEntryEntityRepository;
    }

    @Autowired
    public void setSimpleAclPrincipalEntityRepository(SimpleAclPrincipalEntityRepository simpleAclPrincipalEntityRepository) {
        this.simpleAclPrincipalEntityRepository = simpleAclPrincipalEntityRepository;
    }

    @Autowired
    public void setAclVariableEntityRepository(AclVariableEntityRepository aclVariableEntityRepository) {
        this.aclVariableEntityRepository = aclVariableEntityRepository;
    }

    @Autowired
    public void setBizPayloadEntityRepository(BizPayloadEntityRepository bizPayloadEntityRepository) {
        this.bizPayloadEntityRepository = bizPayloadEntityRepository;
    }
}
