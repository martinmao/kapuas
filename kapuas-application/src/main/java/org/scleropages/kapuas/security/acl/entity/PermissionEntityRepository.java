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
package org.scleropages.kapuas.security.acl.entity;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
@CacheConfig(cacheResolver = "defaultCacheResolver")
public interface PermissionEntityRepository extends CrudRepository<PermissionEntity, Long>, PermissionEntityRepositoryCustom {

    @Cacheable
    default LocalPermissionEntityRepository getLocalPermissionEntityRepository() {
        return new LocalPermissionEntityRepository(findAll());
    }

    @CacheEvict(value = "org.scleropages.kapuas.security.acl.entity.PermissionEntityRepository.getLocalPermissionEntityRepository", allEntries = true)
    @Override
    <S extends PermissionEntity> S save(S entity);

    @CacheEvict(value = "org.scleropages.kapuas.security.acl.entity.PermissionEntityRepository.getLocalPermissionEntityRepository", allEntries = true)
    @Override
    <S extends PermissionEntity> Iterable<S> saveAll(Iterable<S> entities);

    @CacheEvict(value = "org.scleropages.kapuas.security.acl.entity.PermissionEntityRepository.getLocalPermissionEntityRepository", allEntries = true)
    @Override
    void deleteById(Long aLong);

    @CacheEvict(value = "org.scleropages.kapuas.security.acl.entity.PermissionEntityRepository.getLocalPermissionEntityRepository", allEntries = true)
    @Override
    void deleteAll();

    @CacheEvict(value = "org.scleropages.kapuas.security.acl.entity.PermissionEntityRepository.getLocalPermissionEntityRepository", allEntries = true)
    @Override
    void delete(PermissionEntity entity);

    @CacheEvict(value = "org.scleropages.kapuas.security.acl.entity.PermissionEntityRepository.getLocalPermissionEntityRepository", allEntries = true)
    @Override
    void deleteAll(Iterable<? extends PermissionEntity> entities);


    class LocalPermissionEntityRepository {

        private BiMap<String, Long> resourceTypeToId;
        private Map<String, List<PermissionEntity>> resourceTypeToPermissions;
        private MultiKeyMap resourceTypeAndPermissionNameToPermission;

        private LocalPermissionEntityRepository(Iterable<PermissionEntity> iterable) {
            resourceTypeToId = HashBiMap.create();
            resourceTypeToPermissions = Maps.newHashMap();
            resourceTypeAndPermissionNameToPermission = new MultiKeyMap();
            iterable.forEach(permissionEntity -> {
                resourceTypeToId.computeIfAbsent(permissionEntity.getResourceType(), s -> permissionEntity.getResourceTypeId());
                List<PermissionEntity> associatedEntities = resourceTypeToPermissions.computeIfAbsent(permissionEntity.getResourceType(), l -> Lists.newArrayList());
                associatedEntities.add(permissionEntity);
                resourceTypeAndPermissionNameToPermission.put(permissionEntity.getResourceType(), permissionEntity.getName(), permissionEntity);
            });
        }

        public Long getResourceTypeIdByResourceType(String resourceType) {
            return resourceTypeToId.get(resourceType);
        }

        public String getResourceTypeByResourceId(Long resourceTypeId) {
            return resourceTypeToId.inverse().get(resourceTypeId);
        }

        public PermissionEntity getFirstByResourceType(String resourceType) {
            List<PermissionEntity> associatedEntities = resourceTypeToPermissions.get(resourceType);
            Assert.notEmpty(associatedEntities, () -> "no associated permission definitions found by given resource type: " + resourceType);
            return associatedEntities.get(0);
        }

        public List<PermissionEntity> findAllByResourceType(String resourceType) {
            List<PermissionEntity> associatedEntities = resourceTypeToPermissions.get(resourceType);
            if (null == associatedEntities)
                return Collections.EMPTY_LIST;
            return Collections.unmodifiableList(associatedEntities);
        }

        public String[] getAllResourceTypes() {
            return resourceTypeToId.keySet().toArray(new String[resourceTypeToId.size()]);
        }

        public boolean isHierarchyPermissionResource(String resourceType) {

            //int totalOfExtensions = 0;
            List<PermissionEntity> allByResourceType = findAllByResourceType(resourceType);
            for (int i = 0; i < allByResourceType.size(); i++) {
                if (StringUtils.hasText(allByResourceType.get(i).getExtension())) {
                    return true;
                    //totalOfExtensions++;
                }
            }
            return false;
            //return totalOfExtensions == allByResourceType.size() - 1;
        }

        public PermissionEntity getPermissionEntity(String resourceType, String permissionName) {
            Object o = resourceTypeAndPermissionNameToPermission.get(resourceType, permissionName);
            Assert.notNull(o, "no permission definition found for given: " + resourceType + "," + permissionName);
            return (PermissionEntity) o;
        }
    }
}
