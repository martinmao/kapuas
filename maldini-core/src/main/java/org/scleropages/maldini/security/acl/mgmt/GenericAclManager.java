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
import org.apache.commons.lang3.StringUtils;
import org.scleropages.crud.exception.BizViolationException;
import org.scleropages.crud.orm.SearchFilter;
import org.scleropages.crud.orm.jpa.DynamicSpecifications;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
@Service
@Validated
public class GenericAclManager implements AclManager {

    private Map<Serializable, AclProvider> aclProviders;

    private AclProvider defaultAclProvider = new DefaultAclProvider();

    private PermissionEntityRepository permissionEntityRepository;

    private AclPrincipalEntityRepository aclPrincipalEntityRepository;

    private AclEntityRepository aclEntityRepository;

    private SimpleAclEntityRepository simpleAclEntityRepository;

    private AclEntryEntityRepository aclEntryEntityRepository;

    private SimpleAclPrincipalEntityRepository simpleAclPrincipalEntityRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<AclEntry> readEntries(@Validated(ResourceModel.ReadEntriesBySpecifyResource.class) ResourceModel resourceModel,
                                      AclPrincipalModel principal, Pageable pageable) {

        boolean principalProvided = null != principal && null != principal.getName();
        return getRequiredAclProvider(resourceModel).readEntries(resourceModel, principalProvided ? Optional.of(principal) : Optional.empty(), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AclEntry> readPrincipalEntries(@Validated(AclPrincipalModel.CreateAclModel.class) AclPrincipalModel principal, @Validated(ResourceModel.ReadEntriesBySpecifyResourceType.class) ResourceModel resourceModel,
                                               PermissionModel permissionModel, Pageable pageable) {

        boolean permissionProvided = null != permissionModel && null != permissionModel.getName();

        return getRequiredAclProvider(resourceModel).readPrincipalEntries(principal, resourceModel, permissionProvided ?
                Optional.of(permissionModel) : Optional.empty(), pageable);
    }


    @Override
    public boolean isAccessible(@Valid ResourceModel resource, @Valid AclPrincipalModel principal, PermissionModel permission) {
        return readPrincipalEntries(principal, resource, permission, Pageable.unpaged()).isEmpty();
    }

    @Override
    @Transactional(readOnly = true)
    @Validated(ResourceModel.ReadAclModel.class)
    public Acl readAcl(ResourceModel resource) {
        PermissionEntity permissionEntity = permissionEntityRepository.findFirstByResourceType(resource.getType());
        Assert.notNull(permissionEntity, "no acl strategy found by given resource.");
        resource.setTypeId(permissionEntity.getResourceTypeId());
        return getRequiredAclProvider(resource).readAcl(resource, permissionEntity);
    }

    @Override
    @Transactional(readOnly = true)
    @Validated(ResourceModel.ReadEntriesBySpecifyResourceType.class)
    public Page<Acl> readAcl(ResourceModel resourceModel, Pageable pageable) {
        PermissionEntity permissionEntity = permissionEntityRepository.findFirstByResourceType(resourceModel.getType());
        Assert.notNull(permissionEntity, "no acl strategy found by given resource.");
        return getRequiredAclProvider(resourceModel).readAcl(resourceModel, permissionEntity, pageable);
    }

    @Override
    @Transactional
    @Validated({ResourceModel.CreateModel.class})
    public void createAcl(ResourceModel resource) {
        PermissionEntity permissionEntity = permissionEntityRepository.findFirstByResourceType(resource.getType());
        Assert.notNull(permissionEntity, "no acl strategy found by given resource.");
        AclPrincipalEntity principalEntity = aclPrincipalEntityRepository.findByName(resource.getOwner());
        Assert.notNull(principalEntity, "no principal found by given resource owner.");
        resource.setTypeId(permissionEntity.getResourceTypeId());
        getRequiredAclProvider(resource).createAcl(resource, permissionEntity, principalEntity);
    }

    @Override
    @Transactional
    public void createAclEntry(@Validated(ResourceModel.ReadAclModel.class) ResourceModel resource,
                               @Validated(AclPrincipalModel.CreateAclModel.class) AclPrincipalModel grant,
                               PermissionModel... permission) {

        AclPrincipalEntity principalEntity = aclPrincipalEntityRepository.findByName(grant.getName());
        Assert.notNull(principalEntity, "grant principal not found.");

        List<PermissionEntity> permissionEntities = permissionEntityRepository.findByResourceType(resource.getType());
        Assert.notEmpty(permissionEntities, "no acl strategy found by given resource.");

        resource.setTypeId(permissionEntities.get(0).getResourceTypeId());

        if (permissionEntities.size() == 1 && permissionEntities.get(0).isNotSupport() && null != permission) {
            throw new BizViolationException("current resource is coarse-grained acl model(see acl strategy). not support permission argument(make sure set as null.)");
        }
        if (permissionEntities.size() > 1 && null == permission) {
            throw new BizViolationException("current resource is fine-grained acl model(see acl strategy). permission is required.");
        }

        List<PermissionEntity> hits = Lists.newArrayList();
        if (null != permission) {
            PermissionEntity permissionEntity = null;
            for (PermissionModel model : permission) {
                for (PermissionEntity entity : permissionEntities) {
                    if (Objects.equals(entity.getName(), model.getName())) {
                        permissionEntity = entity;
                    }
                }
                Assert.notNull(permissionEntity, "no match permission[" + model.getName() + "] found by given resource.");
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


    class DefaultAclProvider implements AclProvider {

        @Override
        public Serializable type() {
            throw new IllegalStateException("Never call this method. this class was default acl provider.");
        }

        @Override
        public void createAcl(ResourceModel resource, PermissionEntity permissionEntity, AclPrincipalEntity principalEntity) {
            if (permissionEntity.isNotSupport()) {
                Assert.isTrue(simpleAclEntityRepository.countByResourceIdAndResourceTypeId(resource.getId(), resource.getTypeId()) < 1, "resource exists.");
                SimpleAclEntity aclEntity = new SimpleAclEntity();
                mapAbstractEntity(aclEntity, resource, permissionEntity);
                aclEntity.setOwner(principalEntity);
                simpleAclEntityRepository.save(aclEntity);
            } else {
                Assert.isTrue(aclEntityRepository.countByResourceIdAndResourceTypeId(resource.getId(), resource.getTypeId()) < 1, "resource exists.");
                AclEntity aclEntity = new AclEntity();
                mapAbstractEntity(aclEntity, resource, permissionEntity);
                aclEntity.setOwner(principalEntity);
                aclEntityRepository.save(aclEntity);
            }
        }

        @Override
        public Acl readAcl(ResourceModel resourceModel, PermissionEntity permissionEntity) {
            AclModel aclModel = new AclModel();
            if (permissionEntity.isNotSupport()) {
                SimpleAclEntity aclEntity = simpleAclEntityRepository.getByResourceIdAndResourceTypeId(resourceModel.getId(), resourceModel.getTypeId());
                Assert.notNull(aclEntity, "resource not found.");
                mapAclModel(aclModel, aclEntity, true);
            } else {
                AclEntity aclEntity = aclEntityRepository.getByResourceIdAndResourceTypeId(resourceModel.getId(), resourceModel.getTypeId());
                Assert.notNull(aclEntity, "resource not found.");
                mapAclModel(aclModel, aclEntity, true);
            }
            return aclModel;
        }

        @Override
        public Page<Acl> readAcl(ResourceModel resourceModel, PermissionEntity permissionEntity, Pageable pageable) {
            if (permissionEntity.isNotSupport()) {
                return simpleAclEntityRepository.findByResourceTypeId(permissionEntity.getResourceTypeId(), pageable).map(entity -> {
                    AclModel aclModel = new AclModel();
                    mapAclModel(aclModel, entity, false);
                    return aclModel;
                });
            } else {
                return aclEntityRepository.findByResourceTypeId(permissionEntity.getResourceTypeId(), pageable).map(entity -> {
                    AclModel aclModel = new AclModel();
                    mapAclModel(aclModel, entity, false);
                    return aclModel;
                });
            }
        }

        @Override
        public void createAclEntry(ResourceModel resource, AclPrincipalEntity aclPrincipalEntity, PermissionEntity... permissionEntity) {

            AclEntity aclEntity = aclEntityRepository.getByResourceIdAndResourceTypeIdAndIdIsNotNull(resource.getId(), resource.getTypeId());
            Assert.notNull(aclEntity, "resource not found.");

            for (PermissionEntity item : permissionEntity) {
                Assert.isNull(aclEntryEntityRepository.findByAcl_IdAndGrant_IdAndPermission_Id(aclEntity.getId(), aclPrincipalEntity.getId(), item.getId()),
                        "principal already have permit: " + item.getName());
                AclEntryEntity aclEntryEntity = new AclEntryEntity();
                aclEntryEntity.setResourceId(resource.getId());
                aclEntryEntity.setResourceType(item.getResourceType());
                aclEntryEntity.setAclPrincipalName(aclPrincipalEntity.getName());
                aclEntryEntity.setPermissionName((null != item.getExtension() ? item.getExtension() + PermissionEntity.PERMISSION_EXTENSION_SEPARATOR : "") + item.getName());
                aclEntryEntity.setAcl(aclEntity);
                aclEntryEntity.setGrant(aclPrincipalEntity);
                aclEntryEntity.setPermission(item);
                aclEntryEntityRepository.save(aclEntryEntity);
            }

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
            SimpleAclEntity aclEntity = simpleAclEntityRepository.getByResourceIdAndResourceTypeId(resource.getId(), resource.getTypeId());
            Assert.notNull(aclEntity, "resource not found.");
            Assert.isTrue(0 == simpleAclEntityRepository.countByIdAndOwnersId(aclEntity.getId(), aclPrincipalEntity.getId()),
                    "principal already have owned by resource.");

            SimpleAclPrincipalEntity simpleAclPrincipalEntity = new SimpleAclPrincipalEntity();
            simpleAclPrincipalEntity.setGrant(aclPrincipalEntity);
            simpleAclPrincipalEntity.setAcl(aclEntity);
            simpleAclPrincipalEntity.setAclPrincipalName(aclPrincipalEntity.getName());
            simpleAclPrincipalEntity.setResourceId(resource.getId());
            simpleAclPrincipalEntity.setResourceType(resource.getType());
            simpleAclPrincipalEntityRepository.save(simpleAclPrincipalEntity);
        }

        @Override
        public Page<AclEntry> readEntries(ResourceModel resourceModel, Optional<AclPrincipalModel> principalModel, Pageable pageable) {
            PermissionEntity permissionEntity = permissionEntityRepository.findFirstByResourceType(resourceModel.getType());
            Assert.notNull(permissionEntity, "no acl strategy found by given resource.");

            Page<? extends AbstractAclEntryEntity> entryEntities;
            if (permissionEntity.isNotSupport()) {
                entryEntities = principalModel.isPresent() ?
                        simpleAclPrincipalEntityRepository.findByResourceIdAndResourceTypeAndAclPrincipalName(resourceModel.getId(), resourceModel.getType(), principalModel.get().getName(), pageable) :
                        simpleAclPrincipalEntityRepository.findByResourceIdAndResourceType(resourceModel.getId(), resourceModel.getType(), pageable);
            } else {
                entryEntities = principalModel.isPresent() ?
                        aclEntryEntityRepository.findByResourceIdAndResourceTypeAndAclPrincipalName(resourceModel.getId(), resourceModel.getType(), principalModel.get().getName(), pageable) :
                        aclEntryEntityRepository.findByResourceIdAndResourceType(resourceModel.getId(), resourceModel.getType(), pageable);
            }
            return entryEntities.map(this::mapAclEntryModel);
        }

        @Override
        public Page<AclEntry> readPrincipalEntries(AclPrincipalModel principal, ResourceModel resourceModel, Optional<PermissionModel> permission, Pageable pageable) {
            PermissionEntity permissionEntity = permissionEntityRepository.findFirstByResourceType(resourceModel.getType());
            Assert.notNull(permissionEntity, "no acl strategy found by given resource.");
            String resourceId = resourceModel.getId();
            Page<? extends AbstractAclEntryEntity> entryEntities;
            if (permissionEntity.isNotSupport()) {
                if (StringUtils.isNotBlank(resourceId))
                    entryEntities = simpleAclPrincipalEntityRepository.findByAclPrincipalNameAndResourceIdAndResourceType(principal.getName(), resourceId, resourceModel.getType(), pageable);
                else
                    entryEntities = simpleAclPrincipalEntityRepository.findByAclPrincipalNameAndResourceType(principal.getName(), resourceModel.getType(), pageable);
            } else {
                if (StringUtils.isNotBlank(resourceId)) {
                    entryEntities = permission.isPresent() ?
                            aclEntryEntityRepository.findByAclPrincipalNameAndResourceIdAndResourceTypeAndPermissionNameLike(principal.getName(), resourceId, resourceModel.getType(), "%" + permission.get().getName() + "%", pageable) :
                            aclEntryEntityRepository.findByAclPrincipalNameAndResourceIdAndResourceType(principal.getName(), resourceId, resourceModel.getType(), pageable);
                } else
                    entryEntities = permission.isPresent() ?
                            aclEntryEntityRepository.findByAclPrincipalNameAndResourceTypeAndPermissionNameLike(principal.getName(), resourceModel.getType(), "%" + permission.get().getName() + "%", pageable) :
                            aclEntryEntityRepository.findByAclPrincipalNameAndResourceType(principal.getName(), resourceModel.getType(), pageable);

            }
            return entryEntities.map(this::mapAclEntryModel);
        }

        protected AclEntryModel mapAclEntryModel(AbstractAclEntryEntity entryEntity) {
            String permission = entryEntity instanceof AclEntryEntity ? ((AclEntryEntity) entryEntity).getPermissionName() : PermissionEntity.NOT_SUPPORT;
            AclEntryModel aclEntryModel = new AclEntryModel(entryEntity.getId(), entryEntity.getAclPrincipalName(), permission, entryEntity.getResourceType(), entryEntity.getResourceId());
            return aclEntryModel;
        }

        protected void mapAbstractEntity(AbstractAclEntity acl, ResourceModel resource, PermissionEntity permission) {
            acl.setResourceId(resource.getId());
            acl.setResourceTypeId(permission.getResourceTypeId());
            acl.setResourceType(permission.getResourceType());
            acl.setResourceTag(resource.getTag());
        }


        protected void mapAclModel(AclModel aclModel, AbstractAclEntity aclEntity, boolean mapOwner) {
            aclModel.setId(aclEntity.getId());
            aclModel.setResourceId(aclEntity.getResourceId());
            aclModel.setResourceType(aclEntity.getResourceType());
            aclModel.setTag(aclEntity.getResourceTag());
            if (mapOwner) {
                AclPrincipalEntity ownerEntity = aclEntity.getOwner();
                AclPrincipalModel aclPrincipalModel = new AclPrincipalModel();
                aclPrincipalModel.setName(ownerEntity.getName());
                aclPrincipalModel.setTag(ownerEntity.getTag());
                aclModel.setOwners(Lists.newArrayList(aclPrincipalModel));
            }
        }
    }

    @Override
    @Validated(AclPrincipalModel.CreateModel.class)
    @Transactional
    public void createAclPrincipal(AclPrincipalModel aclPrincipalModel) {
        Assert.isNull(aclPrincipalEntityRepository.findByName(aclPrincipalModel.getName()), "principal exists.");
        AclPrincipalEntity aclPrincipalEntity = new AclPrincipalEntity();
        aclPrincipalEntity.setTag(aclPrincipalModel.getTag());
        aclPrincipalEntity.setName(aclPrincipalModel.getName());
        aclPrincipalEntityRepository.save(aclPrincipalEntity);
    }


    @Override
    @Transactional(readOnly = true)
    public Page<AclPrincipal> readAclPrincipals(Map<String, SearchFilter> searchFilters, Pageable pageable) {

        Page<AclPrincipalEntity> entities = aclPrincipalEntityRepository.findAll(DynamicSpecifications.bySearchFilter(searchFilters.values(), AclPrincipalEntity.class), pageable);

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
        Assert.isTrue(permissionEntityRepository.findAllByResourceType(resource).size() == 0, resource + " already configure.");
        String expression = aclStrategy.getExpression();
        long resourceTypeId = System.currentTimeMillis() + resource.hashCode();
        if (StringUtils.isNotBlank(expression)) {
            String[] splitExpressions = StringUtils.split(expression, AclStrategy.EXP_FORMAT_PERMISSION_INHERIT_SEPARATOR);
            boolean inherit = splitExpressions.length > 1;
            if (inherit && expression.contains(AclStrategy.EXP_FORMAT_PERMISSION_SEPARATOR)) {
                throw new IllegalArgumentException("not support mixed mode for permission separator( '>' or ',' Only one you can be selected).");
            }
            if (!inherit)
                splitExpressions = StringUtils.split(expression, AclStrategy.EXP_FORMAT_PERMISSION_SEPARATOR);
            String extension = "";
            for (int i = splitExpressions.length - 1; i > -1; i--) {
                PermissionEntity permissionEntity = new PermissionEntity();
                permissionEntity.setResourceType(resource);
                String[] nameTag = StringUtils.split(splitExpressions[i], AclStrategy.EXP_FORMAT_PERMISSION_NAME_TAG_SEPARATOR);
                String name = nameTag[0];
                permissionEntity.setName(name);
                if (inherit && StringUtils.isNotBlank(extension)) {
                    permissionEntity.setExtension(StringUtils.removeEnd(extension, PermissionEntity.PERMISSION_EXTENSION_SEPARATOR));
                }
                permissionEntity.setTag(nameTag.length == 2 ? nameTag[1] : name);
                permissionEntity.setResourceTypeId(resourceTypeId);
                permissionEntityRepository.save(permissionEntity);
                extension = extension + name + PermissionEntity.PERMISSION_EXTENSION_SEPARATOR;
            }
        } else {
            PermissionEntity permissionEntity = new PermissionEntity();
            permissionEntity.setResourceType(resource);
            permissionEntity.setResourceTypeId(resourceTypeId);
            permissionEntityRepository.save(permissionEntity);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AclStrategy getAclStrategy(String resource) {
        Assert.hasText(resource, "given resource must not be empty text.");
        List<PermissionEntity> permissions = permissionEntityRepository.findAllByResourceType(resource);
        AclStrategy aclStrategy = new AclStrategy();
        if (permissions.size() == 0)
            throw new IllegalArgumentException("resource not found.");
        List<PermissionModel> permissionModels = Lists.newArrayList();
        for (int i = 0; i < permissions.size(); i++) {
            PermissionEntity permissionEntity = permissions.get(i);
            if (permissions.size() == 1 && permissionEntity.isNotSupport()) {
                aclStrategy.setResource(permissionEntity.getResourceType());
                aclStrategy.setExpression("not supported fine-grained acl model." +
                        " Current resource just have one action for specify resource). The Acl entries is not required. principal can associated resource directly.");
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
}
