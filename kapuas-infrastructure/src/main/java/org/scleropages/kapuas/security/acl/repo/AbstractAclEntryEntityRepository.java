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

import org.apache.commons.collections.MapUtils;
import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Record;
import org.jooq.SelectQuery;
import org.jooq.Table;
import org.scleropages.crud.dao.orm.SearchFilter;
import org.scleropages.crud.dao.orm.jpa.complement.JooqRepository;
import org.scleropages.kapuas.security.acl.entity.AbstractAclEntity;
import org.scleropages.kapuas.security.acl.entity.AbstractAclEntryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
@NoRepositoryBean
public interface AbstractAclEntryEntityRepository<E extends AbstractAclEntryEntity, T extends Table, R extends Record> extends PagingAndSortingRepository<E, Long>, JooqRepository<T, R, E>, JpaSpecificationExecutor<E> {

    String NEVER_USED_PRINCIPAL_NAME = "never_used_principal_name";


    boolean existsByAcl_IdAndGrant_Id(Long aclId, Long principalId);

    //从资源维度进行检索方法列表
    default Page<E> findByResourceTypeIdAndResourceId(Long resourceTypeId, String resourceId, Pageable pageable) {
        Specification<E> specification = (Specification<E>) (root, query, builder) ->
                builder.and(
                        builder.notEqual(root.get("aclPrincipalName"), NEVER_USED_PRINCIPAL_NAME),
                        builder.equal(root.get("resourceTypeId"), resourceTypeId),
                        builder.equal(root.get("resourceId"), resourceId)
                );
        return findAll(specification, pageable);
    }

    Boolean existsByResourceTypeIdAndResourceId(Long resourceTypeId, String resourceId);

    //从拥有者维度进行检索方法列表
    Page<E> findByAclPrincipalNameAndResourceTypeIdAndResourceId(String principalName, Long resourceTypeId, String resourceId, Pageable pageable);

    Page<E> findByAclPrincipalNameAndResourceTypeId(String principalName, Long resourceTypeId, Pageable pageable);

    Boolean existsByAclPrincipalNameAndResourceTypeIdAndResourceId(String principalName, Long resourceTypeId, String resourceId);

    Boolean existsByAclPrincipalNameAndResourceTypeId(String principalName, Long resourceTypeId);



    /**
     * 仅对resourceType+variable 过滤，resourceTypeId+resourceId结果唯一，没有必要在提供variable过滤
     *
     * @param principalName
     * @param resourceTypeId
     * @param pageable
     * @param variablesSearchFilters
     * @return
     */
    default Page<E> findByAclPrincipalNameAndResourceTypeId(String principalName, Long resourceTypeId, Pageable pageable, Map<String, SearchFilter> variablesSearchFilters) {
        if (MapUtils.isEmpty(variablesSearchFilters))
            return findByAclPrincipalNameAndResourceTypeId(principalName, resourceTypeId, pageable);

        SelectQuery<Record> query = buildBaseVariableSearchQuery(pageable, variablesSearchFilters, principalName, resourceTypeId).get();

        return dslPage(() -> query, pageable, false, false).map(o -> {
            E entity = createActualAclEntryEntity();
            dslRecordInto(o, entity);
            return entity;
        });
    }


    /**
     * Never call this method in manager layer. this is a utility method for repository sub classes.
     *
     * @param pageable
     * @param variablesSearchFilters
     * @param principalName
     * @param resourceTypeId
     * @return
     */
    default Optional<SelectQuery<Record>> buildBaseVariableSearchQuery(Pageable pageable, Map<String, SearchFilter> variablesSearchFilters, String principalName, Long resourceTypeId) {
        T actualEntryTable = dslTable();
        Table actualAclTable = actualAclTable();

        Field actualAclPk = actualAclPrimaryField(actualAclTable);

        SelectQuery<Record> query = dslContext().selectDistinct(actualEntryTable.fields())
                .from(actualAclTable)
                .join(actualEntryTable).on(actualAclPk.eq(refFieldNameToAclTable())).getQuery();

        query.addConditions(actualEntryTable.field(AbstractAclEntryEntity.PRINCIPAL_COLUMN.toUpperCase()).eq(principalName));
        query.addConditions(actualAclTable.field(AbstractAclEntity.RESOURCE_TYPE_ID_COLUMN.toUpperCase()).eq(resourceTypeId));

        AbstractAclEntityRepository.VariableConditionsAssembler.applyVariableConditions(query, pageable, variablesSearchFilters, actualAclTable);
        return Optional.of(query);
    }


    /**
     * NOT API METHOD. JUST SPI METHOD.
     * sub interface must provided default implementation tell your actual payload instance that as child of {@link AbstractAclEntryEntity}
     *
     * @return
     */
    default E createActualAclEntryEntity() {
        throw new IllegalStateException("not implementation.you must create and return your actual acl payload implemented AbstractAclEntity.");
    }

    /**
     * NOT API METHOD. JUST SPI METHOD.
     * sub interface must provided default implementation tell your actual {@link Table} instance that referenced by actual entry table.
     *
     * @return
     */
    default Table actualAclTable() {
        throw new IllegalStateException("not implementation.you must return your actual acl table.");
    }

    /**
     * NOT REQUIRED TO IMPLEMENTED
     * By default read referenced from {@link #dslTable()} to {@link #actualAclTable()}. if any error occurred  you must implementation this your self.
     *
     * @return
     */
    default Field refFieldNameToAclTable() {
        List<ForeignKey> referencesFrom = dslTable().getReferencesTo(actualAclTable());
        try {
            return referencesFrom.get(0).getFieldsArray()[0];
        } catch (Exception e) {
            throw new IllegalStateException("can not determine ref field from: " + actualAclTable().getName() + ". you must implementation this your self. cause: " + e.getClass() + ": " + e.getMessage(), e);
        }
//      return dslTable().field("sec_acl_id");
    }

    /**
     * NOT REQUIRED TO IMPLEMENTED
     * By default use {@link Table#getPrimaryKey()} fields[0]. if any error occurred  you must implementation this your self.
     *
     * @return
     */
    default Field actualAclPrimaryField(Table actualAclTable) {
        try {
            return (Field) actualAclTable.getPrimaryKey().getFields().get(0);
        } catch (Exception e) {
            throw new IllegalStateException("can not determine primary field from: " + actualAclTable.getName() + ". you must implementation this your self. cause: " + e.getClass() + ": " + e.getMessage(), e);
        }
    }

}
