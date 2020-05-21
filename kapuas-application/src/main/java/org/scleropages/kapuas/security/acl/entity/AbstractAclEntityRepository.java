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
import com.google.common.collect.Maps;
import org.apache.commons.collections.MapUtils;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SelectQuery;
import org.jooq.Table;
import org.scleropages.crud.dao.orm.SearchFilter;
import org.scleropages.crud.dao.orm.jpa.complement.JooqRepository;
import org.scleropages.crud.dao.orm.jpa.complement.JpaSupportJooqConditions;
import org.scleropages.kapuas.jooq.tables.SecAclVariable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;
import static org.scleropages.kapuas.jooq.tables.SecAclVariable.SEC_ACL_VARIABLE;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
@NoRepositoryBean
public interface AbstractAclEntityRepository<E extends AbstractAclEntity, T extends Table, R extends Record> extends PagingAndSortingRepository<E, Long>, JooqRepository<T, R, E>, JpaSpecificationExecutor<E> {

    E getByResourceTypeIdAndResourceId(Long typeId, String resourceId);


    default E getByResourceTypeIdAndResourceIdWithOwner(Long typeId, String resourceId) {
        Specification<E> specification = (root, query, builder) -> {
            root.fetch("owner");
            return builder.and(builder.equal(root.get("resourceTypeId"), typeId), builder.equal(root.get("resourceId"), resourceId));
        };
        return findOne(specification).get();
    }

    default Long getIdByResourceTypeIdAndResourceId(Long typeId, String resourceId) {
        T actualAclTable = dslTable();
        Record record = dslContext().select(actualAclTable.field(AbstractAclEntity.ID_COLUMN)).from(actualAclTable)
                .where(actualAclTable.field(AbstractAclEntity.RESOURCE_TYPE_ID_COLUMN).eq(typeId))
                .and(actualAclTable.field(AbstractAclEntity.RESOURCE_ID_COLUMN).eq(resourceId)).fetchOne();
        if (null == record)
            throw new IllegalArgumentException("can't find acl by given resource.");
        return (Long) record.get(actualAclTable.field(AbstractAclEntity.ID_COLUMN));
    }

    Boolean existsByResourceTypeIdAndResourceId(Long typeId, String resourceId);

    Page<E> findByResourceTypeId(Long resourceTypeId, Pageable pageable);

    default Page<E> findByResourceTypeId(Long resourceTypeId, Pageable pageable, Map<String, SearchFilter> variablesSearchFilters) {

        if (MapUtils.isEmpty(variablesSearchFilters))
            return findByResourceTypeId(resourceTypeId, pageable);

        T actualAclTable = dslTable();
        SelectQuery<Record> query = dslContext().selectDistinct(actualAclTable.fields()).from(actualAclTable).getQuery();

        query.addConditions(actualAclTable.field(AbstractAclEntity.RESOURCE_TYPE_ID_COLUMN.toUpperCase()).eq(resourceTypeId));

        VariableConditionsAssembler.applyVariableConditions(query, pageable, variablesSearchFilters, actualAclTable);

        return dslPage(() -> query, pageable, false, false).map(o -> {
            E entity = createActualAclEntity();
            dslRecordInto(o, entity);
            return entity;
        });
    }

    abstract class VariableConditionsAssembler {

        protected static void applyVariableConditions(SelectQuery<Record> query, Pageable pageable, Map<String, SearchFilter> variablesSearchFilters, Table actualAclTable) {

            List<Condition> variableConditions = Lists.newArrayList();//use this to keep variable conditions.

            Map<String, Field> variableFields = Maps.newHashMap();// use this to process variable sorting query.

            variablesSearchFilters.forEach((s, searchFilter) -> {// apply join to query. and build variableConditions, variableFields
                Assert.isTrue(searchFilter.fieldNames.length == 1, "not support multiple field names.");
                String variableName = searchFilter.fieldNames[0];

                SecAclVariable variableJoin = SEC_ACL_VARIABLE.as(s + "_");

                query.addJoin(variableJoin, actualAclTable.field(AbstractAclEntity.ID_COLUMN.toUpperCase()).eq(variableJoin.ACL_ID));


                Field valueField = field(name(variableJoin.getName(), AclVariableEntity.getColumnByValue(searchFilter.value)));

                Condition eachVariableCondition = variableJoin.NAME_.eq(variableName).and(JpaSupportJooqConditions.bySearchFilter(valueField, searchFilter));
                variableConditions.add(eachVariableCondition);
                variableFields.put(variableName, valueField);
            });
            query.addConditions(variableConditions);

            pageable.getSort().forEach(order -> {// check sort variable must containing in variable condition.
                String property = order.getProperty();
                Field field = variableFields.get(property);
                Assert.notNull(field, "sorted variable must as a query condition: " + property);
                query.addOrderBy(order.isAscending() ? field.asc() : field.desc());
            });
        }

    }


    /**
     * NOT API METHOD. JUST SPI METHOD.
     * sub interface must provided default implementation tell your actual payload instance that as child of {@link AbstractAclEntity}
     *
     * @return
     */
    default E createActualAclEntity() {
        throw new IllegalStateException("not implementation.you must create and return your actual acl payload implemented AbstractAclEntity.");
    }
}
