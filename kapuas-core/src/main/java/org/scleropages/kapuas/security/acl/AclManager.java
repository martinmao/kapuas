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
package org.scleropages.kapuas.security.acl;

import org.scleropages.crud.dao.orm.SearchFilter;
import org.scleropages.kapuas.security.acl.model.AclPrincipalModel;
import org.scleropages.kapuas.security.acl.model.AclStrategy;
import org.scleropages.kapuas.security.acl.model.PermissionModel;
import org.scleropages.kapuas.security.acl.model.ResourceModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * Manager used for management access control list (acl).
 *
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public interface AclManager {


    /**
     * list all acl strategy resource types
     *
     * @return
     */
    String[] getAllAclStrategyResourceTypes();

    /**
     * get {@link AclStrategy} by given resource.
     *
     * @param resource
     * @return
     */
    AclStrategy getAclStrategy(String resource);


    /**
     * get {@link AclPrincipal} by given search items.
     *
     * @param searchFilters search items.
     * @param pageable      pageable to query.
     * @return
     */
    Page<AclPrincipal> findAclPrincipals(Map<String, SearchFilter> searchFilters, Pageable pageable);


    /**
     * get {@link Acl} by specify resource(id and type).
     *
     * @param resourceModel (id and type is required)
     * @return
     */
    Acl getAcl(@Valid ResourceModel resourceModel);

    /**
     * read acl(s) by given resource type
     *
     * @param resourceModel (type is required)
     * @param pageable
     * @return
     */
    Page<Acl> findAcl(@Valid ResourceModel resourceModel, Pageable pageable);


    /**
     * read acl(s) by given resource type and filtered by variables.
     *
     * @param resourceModel
     * @param pageable
     * @param variablesSearchFilters
     * @return
     */
    Page<Acl> findAcl(@Valid ResourceModel resourceModel, Pageable pageable, Map<String, SearchFilter> variablesSearchFilters);


    /**
     * fetch all business payload by given resource(type is required) and acl ids.
     *
     * @param resourceModel
     * @param aclIds
     * @return
     */
    List<String> findAllAclBizPayload(@Valid ResourceModel resourceModel, Long... aclIds);

    /**
     * read acl entries by specify (id and type) resource.
     *
     * @param resourceModel (id and type is required)
     * @param principal     optional query condition.
     * @param pageable
     * @return
     */
    Page<AclEntry> findEntries(@Valid ResourceModel resourceModel, AclPrincipalModel principal, Pageable pageable);


    /**
     * read acl entries by specify(from principal) resource.
     *
     * @param principal     grant to specify principal.
     * @param resourceModel by resource type(type is required and id is optional)
     * @param permission    optional query condition
     * @param pageable
     * @return
     */
    Page<AclEntry> findPrincipalEntries(@Valid AclPrincipalModel principal, @Valid ResourceModel resourceModel, PermissionModel permission, Pageable pageable);


    /**
     * read acl entries by specify(from principal) resource and variable search filters(resource id not provided.)
     *
     * @param principal              grant to specify principal.
     * @param resourceModel          by resource type(type is required and id is optional)
     * @param permission             optional query condition
     * @param pageable
     * @param variablesSearchFilters
     * @return
     */
    Page<AclEntry> findPrincipalEntries(@Valid AclPrincipalModel principal, @Valid ResourceModel resourceModel, PermissionModel permission, Pageable pageable, Map<String, SearchFilter> variablesSearchFilters);


    /**
     * create acl model by given {@link AclStrategy}
     *
     * @param aclStrategy
     */
    void createAclStrategy(@Valid AclStrategy aclStrategy);

    /**
     * create a acl principal.
     *
     * @param aclPrincipalModel
     */
    void createAclPrincipal(@Valid AclPrincipalModel aclPrincipalModel);

    /**
     * create acl by given resource and owned principal.
     *
     * @param resource (id type and owner is required.)
     */
    void createAcl(@Valid ResourceModel resource);


    /**
     * update acl by given resource.
     *
     * @param resource
     */
    void updateAcl(@Valid ResourceModel resource);


    /**
     * delete a acl by given resource.
     *
     * @param resource
     */
    void deleteAcl(@Valid ResourceModel resource);


    /**
     * create acl entry for given resource
     *
     * @param resource   resource associated acl must exists(type id is required.)
     * @param grant      grants principal.
     * @param permission a group optional permissions.
     */
    void createAclEntry(@Valid ResourceModel resource, @Valid AclPrincipalModel grant, PermissionModel... permission);


    /**
     * delete acl entry by given resource
     *
     * @param resource   resource associated acl must exists(type id is required.)
     * @param grant      grants principal.
     * @param permission a group optional permissions.
     */
    void deleteAclEntry(@Valid ResourceModel resource, @Valid AclPrincipalModel grant, PermissionModel... permission);


    /**
     * Return true if given principal was granted permissions for given resource.
     *
     * @param resource   asserts resource.
     * @param principal  grants principal.
     * @param permission a group optional permissions.
     */
    boolean isAccessible(@Valid ResourceModel resource, @Valid AclPrincipalModel principal, PermissionModel permission);


    /**
     * check if given principal was granted permissions for given resource.
     *
     * @param resource   asserts resource.
     * @param principal  grants principal.
     * @param permission a group optional permissions.
     * @throws IllegalArgumentException when access denied.
     */
    void accessible(@Valid ResourceModel resource, @Valid AclPrincipalModel principal, PermissionModel permission);

}
