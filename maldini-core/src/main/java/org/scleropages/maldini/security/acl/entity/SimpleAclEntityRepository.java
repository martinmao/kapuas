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
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public interface SimpleAclEntityRepository extends PagingAndSortingRepository<SimpleAclEntity, Long> {

    @EntityGraph(attributePaths = {"owner"})
    SimpleAclEntity getByResourceIdAndResourceTypeId(String resourceId, Long typeId);

    @Query(value = "select count(1) from SEC_ACL_S  where RESOURCE_ID=? and RESOURCE_TYPE_ID=?", nativeQuery = true)
    Integer countByResourceIdAndResourceTypeId(String resourceId, Long resourceTypeId);

    @Query(nativeQuery = true, value = "select  count(1) from SEC_ACL_S_PRINCIPAL where sec_acl_id=? and SEC_ACL_PRINCIPAL_ID=?")
    Integer countByIdAndOwnersId(Long aclId, Long principalId);

    Page<SimpleAclEntity> findByResourceTypeId(Long typeId, Pageable pageable);

}