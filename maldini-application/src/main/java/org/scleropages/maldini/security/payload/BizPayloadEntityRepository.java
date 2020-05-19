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
package org.scleropages.maldini.security.payload;

import org.scleropages.crud.dao.orm.jpa.complement.JooqRepository;
import org.scleropages.maldini.jooq.tables.SecBizPayload;
import org.scleropages.maldini.jooq.tables.records.SecBizPayloadRecord;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public interface BizPayloadEntityRepository extends CrudRepository<BizPayloadEntity, Long>, JooqRepository<SecBizPayload, SecBizPayloadRecord, BizPayloadEntity> {

    BizPayloadEntity getByBizIdAndTypeIdAndPayloadId(Integer bizId, Integer typeId, Long payloadId);

    default List<String> findAllTextByBizIdAndTypeIdAndPayloadId(Integer bizId, Long typeId, Long... payloadId) {
        SecBizPayload secBizPayload = dslTable();
        return dslContext().select(secBizPayload.TEXT_)
                .from(secBizPayload)
                .where(secBizPayload.BIZ_ID.eq(bizId)).and(secBizPayload.TYPE_ID.eq(typeId)).and(secBizPayload.PAYLOAD_ID.in(payloadId)).fetch(secBizPayload.TEXT_);
    }

    default boolean saveTextByBizIdAndTypeIdAndPayloadId(Integer bizId, Long typeId, Long payloadId, String text) {
        SecBizPayload secBizPayload = dslTable();
        SecBizPayloadRecord secBizPayloadRecord = dslContext().selectFrom(secBizPayload)
                .where(secBizPayload.BIZ_ID.eq(bizId)).and(secBizPayload.TYPE_ID.eq(typeId)).and(secBizPayload.PAYLOAD_ID.eq(payloadId))
                .fetchOptional().orElseGet(() -> dslContext().newRecord(secBizPayload));
        if (null == secBizPayloadRecord.getId()) {
            secBizPayloadRecord.setBizId(bizId);
            secBizPayloadRecord.setTypeId(typeId);
            secBizPayloadRecord.setPayloadId(payloadId);
        }
        secBizPayloadRecord.setText_(text);
        return secBizPayloadRecord.store() == 1;
    }
}
