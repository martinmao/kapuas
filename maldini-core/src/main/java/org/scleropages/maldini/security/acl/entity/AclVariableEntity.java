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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.scleropages.core.mapper.JsonMapper;
import org.scleropages.crud.dao.orm.jpa.entity.IdEntity;
import org.springframework.util.Assert;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
@Entity
@Table(name = "sec_acl_variable",
        uniqueConstraints = @UniqueConstraint(columnNames = {"acl_id", "resource_type_id", "name_"}),
        indexes = {
                @Index(columnList = "name_,text_,long_,date_,double_,resource_type_id")
        })
@SequenceGenerator(name = "sec_acl_variable_id", sequenceName = "seq_sec_acl_variable", allocationSize = IdEntity.SEQ_DEFAULT_ALLOCATION_SIZE, initialValue = IdEntity.SEQ_DEFAULT_INITIAL_VALUE)
public class AclVariableEntity extends IdEntity {

    private static final String[] SUPPORT_DATE_PATTERN = new String[]{
            "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd"
    };


    private String name;
    private Double doubleValue;
    private Long longValue;
    private String textValue;
    private Date dateValue;

    private Long aclId;
    private Long resourceTypeId;

    @Column(name = "name_", nullable = false)
    public String getName() {
        return name;
    }

    @Column(name = "double_")
    public Double getDoubleValue() {
        return doubleValue;
    }

    @Column(name = "long_")
    public Long getLongValue() {
        return longValue;
    }

    @Column(name = "text_")
    public String getTextValue() {
        return textValue;
    }

    @Column(name = "date_")
    public Date getDateValue() {
        return dateValue;
    }

    @Column(name = "acl_id", nullable = false)
    public Long getAclId() {
        return aclId;
    }

    @Column(name = "resource_type_id", nullable = false)
    public Long getResourceTypeId() {
        return resourceTypeId;
    }

    @Transient
    public Object getValue() {
        Object value;
        if (null != (value = getTextValue()))
            return value;
        else if (null != (value = getDoubleValue()))
            return value;
        else if (null != (value = getLongValue()))
            return value;
        else if (null != (value = getDateValue()))
            return value;
        throw new IllegalStateException("can't determined value type of variable: " + JsonMapper.nonDefaultMapper().toJson(this));
    }

    public void setName(String name) {
        this.name = name;
    }


    public void setDoubleValue(Double doubleValue) {
        this.doubleValue = doubleValue;
    }

    public void setLongValue(Long longValue) {
        this.longValue = longValue;
    }

    public void setTextValue(String textValue) {
        this.textValue = textValue;
    }

    public void setDateValue(Date dateValue) {
        this.dateValue = dateValue;
    }

    public void setAclId(Long aclId) {
        this.aclId = aclId;
    }

    public void setResourceTypeId(Long resourceTypeId) {
        this.resourceTypeId = resourceTypeId;
    }


    public void setValue(Object value) {
        Assert.notNull(value, "not allowed null value.");
        if (value instanceof String) {
            String text = (String) value;
            try {
                setDateValue(DateUtils.parseDate(text, SUPPORT_DATE_PATTERN));
            } catch (ParseException e) {
                setTextValue(text);
            }
            return;
        }
        if (value instanceof Double || value instanceof BigDecimal || value instanceof Float) {
            setDoubleValue(Double.valueOf(String.valueOf(value)));
            return;
        }
        if (value instanceof Number) {
            setLongValue(Long.valueOf(String.valueOf(value)));
            return;
        }
        if (value instanceof Date) {
            setDateValue((Date) value);
            return;
        } else
            throw new IllegalArgumentException("not supported value type of value: " + value);
    }

    protected static String getColumnByValue(Object value) {
        Assert.notNull(value, "not allowed null value.");
        if (value instanceof String) {
            String text = (String) value;
            try {
                if (text.contains(",")) {// Operator.IN,Operator.RANGE,Operator.RANGEIN use ',' as separator.
                    String[] split = StringUtils.split(text, ",");
                    text = split[0];
                    //此时无法通过值类型判断，只能通过按照优先级的转换顺序处理可能匹配的列
                    if (NumberUtils.isCreatable(text)) {
                        return text.contains(".") ? "double_" : "long_";
                    }
                    return getColumnByValue(text);
                }
                DateUtils.parseDate(text, SUPPORT_DATE_PATTERN);
                return "date_";
            } catch (ParseException e) {
                return "text_";
            }

        } else if (value instanceof Float || value instanceof Double || value instanceof BigDecimal)
            return "double_";
        else if (value instanceof Number)
            return "long_";
        else if (value instanceof Date)
            return "date_";
        else
            throw new IllegalArgumentException("not supported value type of value: " + value);
    }

}
