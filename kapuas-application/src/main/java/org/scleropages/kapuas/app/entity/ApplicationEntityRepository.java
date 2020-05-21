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
package org.scleropages.kapuas.app.entity;

import org.scleropages.crud.dao.orm.jpa.GenericRepository;
import org.scleropages.crud.dao.orm.jpa.complement.JooqRepository;
import org.scleropages.kapuas.jooq.tables.AppInfo;
import org.scleropages.kapuas.jooq.tables.records.AppInfoRecord;
import org.springframework.util.Assert;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public interface ApplicationEntityRepository extends GenericRepository<ApplicationEntity, Long>, JooqRepository<AppInfo, AppInfoRecord, ApplicationEntity> {


    boolean existsByName(String name);

    default ApplicationEntity getByIdOrAppId(Long id, String appId) {
        AppInfo appInfo = dslTable();
        AppInfoRecord appInfoRecord;
        if (null != id) {
            appInfoRecord = dslContext().selectFrom(appInfo).where(appInfo.ID.eq(id)).fetchOne();
        } else if (null != appId) {
            appInfoRecord = dslContext().selectFrom(appInfo).where(appInfo.APP_ID.eq(appId)).fetchOne();
        } else
            throw new IllegalArgumentException("id or namespace is required.");
        Assert.notNull(appInfoRecord, "no application found. ");
        return dslRecordInto(appInfoRecord, new ApplicationEntity());
    }
}
