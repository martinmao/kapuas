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
package org.scleropages.kapuas.security.acl.repo;

import org.jooq.Record1;
import org.jooq.Table;
import org.scleropages.kapuas.jooq.tables.SecAclS;
import org.scleropages.kapuas.jooq.tables.SecAclSPrincipal;
import org.scleropages.kapuas.jooq.tables.records.SecAclSPrincipalRecord;
import org.scleropages.kapuas.security.acl.entity.SimpleAclPrincipalEntity;
import org.springframework.util.Assert;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public interface SimpleAclPrincipalEntityRepository extends AbstractAclEntryEntityRepository<SimpleAclPrincipalEntity, SecAclSPrincipal, SecAclSPrincipalRecord> {


    default Long getIdByAcl_IdAndGrant_Id(Long aclId, Long grantId) {
        SecAclSPrincipal secAclSPrincipal = dslTable();
        Record1<Long> id = dslContext().select(secAclSPrincipal.ID).from(secAclSPrincipal)
                .where(secAclSPrincipal.SEC_ACL_ID.eq(aclId))
                .and(secAclSPrincipal.SEC_ACL_PRINCIPAL_ID.eq(grantId)).fetchOne();
        Assert.notNull(id, "no acl entry found.");
        return id.get(secAclSPrincipal.ID);
    }

    @Override
    default SimpleAclPrincipalEntity createActualAclEntryEntity() {
        return new SimpleAclPrincipalEntity();
    }

    @Override
    default Table actualAclTable() {
        return SecAclS.SEC_ACL_S;
    }
}
