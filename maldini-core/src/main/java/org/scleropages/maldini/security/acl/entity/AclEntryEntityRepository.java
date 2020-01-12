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
package org.scleropages.maldini.security.acl.entity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public interface AclEntryEntityRepository extends PagingAndSortingRepository<AclEntryEntity, Long>, JpaSpecificationExecutor<AclEntryEntity> {

    AclEntryEntity findByAcl_IdAndGrant_IdAndPermission_Id(Long aclId, Long principalId, Long permissionId);

    @EntityGraph(attributePaths = {"permission"})
    List<AclEntryEntity> findByAcl_IdAndGrant_Id(Long aclId, Long principalId);

    Page<AclEntryEntity> findByResourceIdAndResourceType(String resourceId, String resourceType, Pageable pageable);

    Page<AclEntryEntity> findByResourceIdAndResourceTypeAndAclPrincipalName(String resourceId, String resourceType, String principalName, Pageable pageable);

    Page<AclEntryEntity> findByAclPrincipalNameAndResourceType(String principalName, String resourceType, Pageable pageable);

    Page<AclEntryEntity> findByAclPrincipalNameAndResourceIdAndResourceType(String principalName, String resourceId, String resourceType, Pageable pageable);

    Page<AclEntryEntity> findByAclPrincipalNameAndResourceTypeAndPermissionNameLike(String principalName, String resourceType, String permissionName, Pageable pageable);

    Page<AclEntryEntity> findByAclPrincipalNameAndResourceIdAndResourceTypeAndPermissionNameLike(String principalName, String resourceId, String resourceType, String permissionName, Pageable pageable);

}
