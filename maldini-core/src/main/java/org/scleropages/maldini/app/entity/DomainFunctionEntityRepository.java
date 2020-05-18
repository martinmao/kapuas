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
package org.scleropages.maldini.app.entity;

import org.scleropages.crud.dao.orm.jpa.GenericRepository;
import org.scleropages.crud.dao.orm.jpa.complement.JooqRepository;
import org.scleropages.maldini.jooq.tables.AppDomainFunc;
import org.scleropages.maldini.jooq.tables.records.AppDomainFuncRecord;
import org.springframework.cache.annotation.Cacheable;

import static org.scleropages.maldini.jooq.Tables.APP_DOMAIN_FUNC;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public interface DomainFunctionEntityRepository extends GenericRepository<DomainFunctionEntity, Long>, JooqRepository<AppDomainFunc, AppDomainFuncRecord, DomainFunctionEntity> {

    Iterable<DomainFunctionEntity> findAllByDomainEntity_Id(Long id);

    @Cacheable
    default String getAppIdByFunctionFullName(String fullName) {
        return dslContext()
                .select(APP_DOMAIN_FUNC.APP_ID).from(APP_DOMAIN_FUNC)
                .where(APP_DOMAIN_FUNC.FULL_NAME.eq(fullName))
                .fetchOptional().orElseThrow(() -> new IllegalArgumentException("not appId found by given fullName: " + fullName))
                .get(APP_DOMAIN_FUNC.APP_ID);
    }
}
