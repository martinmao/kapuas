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

import com.google.common.collect.Lists;
import org.scleropages.crud.dao.orm.jpa.complement.JooqRepository;
import org.scleropages.kapuas.jooq.Tables;
import org.scleropages.kapuas.jooq.tables.SecAclVariable;
import org.scleropages.kapuas.jooq.tables.records.SecAclVariableRecord;
import org.springframework.data.repository.CrudRepository;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public interface AclVariableEntityRepository extends CrudRepository<AclVariableEntity, Long>, JooqRepository<SecAclVariable, SecAclVariableRecord, AclVariableEntity> {


    List<AclVariableEntity> findAllByAclIdAndResourceTypeId(Long aclId, Long resourceTypeId);

    default void save(Iterable<AclVariableEntity> variableEntities) {
        List<SecAclVariableRecord> variableEntitiesToSave = Lists.newArrayList();
        variableEntities.forEach(aclVariableEntity -> {
            SecAclVariableRecord secAclVariableRecord = dslContext().newRecord(Tables.SEC_ACL_VARIABLE);
            secAclVariableRecord.setAclId(aclVariableEntity.getAclId());
            secAclVariableRecord.setResourceTypeId(aclVariableEntity.getResourceTypeId());
            secAclVariableRecord.setName_(aclVariableEntity.getName());
            secAclVariableRecord.setDouble_(aclVariableEntity.getDoubleValue());
            secAclVariableRecord.setLong_(aclVariableEntity.getLongValue());
            secAclVariableRecord.setText_(aclVariableEntity.getTextValue());
            if (null != aclVariableEntity.getDateValue())
                secAclVariableRecord.setDate_(new Timestamp(aclVariableEntity.getDateValue().getTime()));
            variableEntitiesToSave.add(secAclVariableRecord);
        });
        dslContext().batchInsert(variableEntitiesToSave).execute();
    }

    default int deleteAllByAclIdAndResourceTypeId(Long aclId, Long resourceTypeId) {
        SecAclVariable variable = dslTable();
        return dslContext().deleteFrom(variable).where(variable.ACL_ID.eq(aclId)).and(variable.RESOURCE_TYPE_ID.eq(resourceTypeId)).execute();
    }
}
