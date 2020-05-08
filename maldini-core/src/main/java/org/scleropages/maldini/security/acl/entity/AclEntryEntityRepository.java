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

import org.apache.commons.collections.MapUtils;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.SelectQuery;
import org.jooq.Table;
import org.scleropages.crud.dao.orm.SearchFilter;
import org.scleropages.maldini.jooq.tables.SecAcl;
import org.scleropages.maldini.jooq.tables.SecAclEntries;
import org.scleropages.maldini.jooq.tables.records.SecAclEntriesRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public interface AclEntryEntityRepository extends AbstractAclEntryEntityRepository<AclEntryEntity, SecAclEntries, SecAclEntriesRecord> {

    boolean existsByAcl_IdAndGrant_IdAndPermission_Id(Long aclId, Long principalId, Long permissionId);


    default Long getIdByAcl_IdAndGrant_IdAndPermission_Id(Long aclId, Long grantId, Long permissionId) {
        SecAclEntries aclEntries = dslTable();
        Record1<Long> id = dslContext().select(aclEntries.ID).from(aclEntries)
                .where(aclEntries.SEC_ACL_ID.eq(aclId))
                .and(aclEntries.SEC_ACL_PRINCIPAL_ID.eq(grantId))
                .and(aclEntries.SEC_ACL_PERMISSION_ID.eq(permissionId)).fetchOne();
        Assert.notNull(id, "no acl entry found.");
        return id.get(aclEntries.ID);
    }

    @EntityGraph(attributePaths = "permission")
    List<AclEntryEntity> findByAcl_IdAndGrant_Id(Long aclId, Long principalId);

    //权限继承时合并所有权限为一条记录，需要like匹配
    Page<AclEntryEntity> findByAclPrincipalNameAndResourceTypeIdAndPermissionNameLike(String principalName, Long resourceTypeId, String permissionName, Pageable pageable);

    Page<AclEntryEntity> findByAclPrincipalNameAndResourceTypeIdAndResourceIdAndPermissionNameLike(String principalName, Long resourceTypeId,String resourceId, String permissionName, Pageable pageable);

    //非权限继承不会合并记录，每一条acl entry对应一个权限记录，通过 equals匹配
    Page<AclEntryEntity> findByAclPrincipalNameAndResourceTypeIdAndPermissionName(String principalName, Long resourceTypeId, String permissionName, Pageable pageable);

    Page<AclEntryEntity> findByAclPrincipalNameAndResourceTypeIdAndResourceIdAndPermissionName(String principalName, Long resourceTypeId,String resourceId, String permissionName, Pageable pageable);


    default Page<AclEntryEntity> findByAclPrincipalNameAndResourceTypeIdAndPermissionNameLike(String principalName, Long resourceTypeId, String permissionName, Pageable pageable, Map<String, SearchFilter> variablesSearchFilters) {
        if (MapUtils.isEmpty(variablesSearchFilters))
            return findByAclPrincipalNameAndResourceTypeIdAndPermissionNameLike(principalName, resourceTypeId, permissionName, pageable);

        SelectQuery<Record> query = buildBaseVariableSearchQuery(pageable, variablesSearchFilters, principalName, resourceTypeId).get();
        query.addConditions(dslTable().PERMISSION_.like(permissionName));
        return dslPage(() -> query, pageable, false, false).map(o -> {
            AclEntryEntity entity = createActualAclEntryEntity();
            dslRecordInto(o, entity);
            return entity;
        });
    }

    default Page<AclEntryEntity> findByAclPrincipalNameAndResourceTypeIdAndPermissionName(String principalName, Long resourceTypeId, String permissionName, Pageable pageable, Map<String, SearchFilter> variablesSearchFilters) {
        if (MapUtils.isEmpty(variablesSearchFilters))
            return findByAclPrincipalNameAndResourceTypeIdAndPermissionName(principalName, resourceTypeId, permissionName, pageable);

        SelectQuery<Record> query = buildBaseVariableSearchQuery(pageable, variablesSearchFilters, principalName, resourceTypeId).get();
        query.addConditions(dslTable().PERMISSION_.eq(permissionName));
        return dslPage(() -> query, pageable, false, false).map(o -> {
            AclEntryEntity entity = createActualAclEntryEntity();
            dslRecordInto(o, entity);
            return entity;
        });
    }


    @Override
    default AclEntryEntity createActualAclEntryEntity() {
        return new AclEntryEntity();
    }

    @Override
    default Table actualAclTable() {
        return SecAcl.SEC_ACL;
    }
}
