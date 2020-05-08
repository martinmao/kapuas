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

import org.scleropages.crud.dao.orm.jpa.entity.IdEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
@Entity
@Table(name = "sec_biz_payload", uniqueConstraints = @UniqueConstraint(columnNames = {"biz_id", "type_id", "payload_id"}))
@SequenceGenerator(name = "sec_biz_payload_id", sequenceName = "seq_sec_biz_payload", allocationSize = IdEntity.SEQ_DEFAULT_ALLOCATION_SIZE, initialValue = IdEntity.SEQ_DEFAULT_INITIAL_VALUE)
public class BizPayloadEntity extends IdEntity {

    private Integer bizId;
    private Long typeId;
    private Long payloadId;
    private String text;


    @Column(name = "biz_id", nullable = false)
    public Integer getBizId() {
        return bizId;
    }

    @Column(name = "type_id", nullable = false)
    public Long getTypeId() {
        return typeId;
    }

    @Column(name = "payload_id", nullable = false)
    public Long getPayloadId() {
        return payloadId;
    }

    @Column(name = "text_", nullable = false)
    public String getText() {
        return text;
    }

    public void setBizId(Integer bizId) {
        this.bizId = bizId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    public void setPayloadId(Long payloadId) {
        this.payloadId = payloadId;
    }

    public void setText(String text) {
        this.text = text;
    }
}
