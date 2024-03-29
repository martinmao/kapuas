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
package org.scleropages.kapuas.app.repo;

import org.scleropages.crud.dao.orm.jpa.GenericRepository;
import org.scleropages.crud.dao.orm.jpa.complement.JooqRepository;
import org.scleropages.kapuas.app.entity.DomainEntity;
import org.scleropages.kapuas.jooq.tables.AppDomain;
import org.scleropages.kapuas.jooq.tables.records.AppDomainRecord;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.Assert;

import javax.persistence.criteria.JoinType;
import java.util.List;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public interface DomainEntityRepository extends GenericRepository<DomainEntity, Long>, JooqRepository<AppDomain, AppDomainRecord, DomainEntity> {


    default DomainEntity getByIdOrNamespace(Long id, String namespace) {
        AppDomain appDomain = dslTable();
        AppDomainRecord appDomainRecord;
        if (null != id) {
            appDomainRecord = dslContext().selectFrom(appDomain).where(appDomain.ID.eq(id)).fetchOne();
        } else if (null != namespace) {
            appDomainRecord = dslContext().selectFrom(appDomain).where(appDomain.NS_.eq(namespace)).fetchOne();
        } else
            throw new IllegalArgumentException("id or namespace is required.");
        Assert.notNull(appDomainRecord, "no domain found. ");
        DomainEntity domainEntity = new DomainEntity();
        dslRecordInto(appDomainRecord, domainEntity);
        return domainEntity;
    }

    default DomainEntity getByIdWithParentDomainEntity(Long id) {
        Specification<DomainEntity> specification = (root, query, builder) -> {
            root.fetch("parentDomainEntity", JoinType.LEFT);
            return builder.equal(root.get("id"), id);
        };
        return findOne(specification).orElseThrow(() -> new IllegalArgumentException("no domain found."));
    }

    default List<Long> findAllIdsByParentDomainId(Long parentId) {
        AppDomain appDomain = dslTable();
        return dslContext().select(appDomain.ID).from(appDomain).where(appDomain.PARENT_APP_DOMAIN_ID.eq(parentId)).fetchInto(Long.class);
    }
}
