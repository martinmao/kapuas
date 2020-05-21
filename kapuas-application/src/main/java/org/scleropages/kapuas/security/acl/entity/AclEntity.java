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

import org.scleropages.crud.dao.orm.jpa.entity.IdEntity;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.List;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
@Entity
@Table(name = "sec_acl", uniqueConstraints = @UniqueConstraint(columnNames = {"resource_type_id","resource_id"}))
@SequenceGenerator(name = "sec_acl_id", sequenceName = "seq_sec_acl", allocationSize = IdEntity.SEQ_DEFAULT_ALLOCATION_SIZE, initialValue = IdEntity.SEQ_DEFAULT_INITIAL_VALUE)
public class AclEntity extends AbstractAclEntity {

    private List<AclEntryEntity> entries;


    @OneToMany(mappedBy = "acl")
    public List<AclEntryEntity> getEntries() {
        return entries;
    }


    public void setEntries(List<AclEntryEntity> entries) {
        this.entries = entries;
    }
}
