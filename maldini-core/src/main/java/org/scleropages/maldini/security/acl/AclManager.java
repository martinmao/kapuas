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
package org.scleropages.maldini.security.acl;

import org.scleropages.crud.orm.SearchFilter;
import org.scleropages.maldini.security.acl.model.AclPrincipalModel;
import org.scleropages.maldini.security.acl.model.AclStrategy;
import org.scleropages.maldini.security.acl.model.PermissionModel;
import org.scleropages.maldini.security.acl.model.ResourceModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.validation.Valid;
import java.util.Map;

/**
 * Manager used for management access control list (acl).
 *
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public interface AclManager {

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
     * @param aclPrincipal
     * @return
     */
    Page<AclPrincipal> readAclPrincipals(Map<String, SearchFilter> searchFilters, Pageable pageable);


    /**
     * get {@link Acl} by specify resource(id and type).
     *
     * @param resourceModel (id and type is required)
     * @return
     */
    Acl readAcl(@Valid ResourceModel resourceModel);

    /**
     * read acl(s) by given resource type
     *
     * @param resourceModel (type is required)
     * @return
     */
    Page<Acl> readAcl(@Valid ResourceModel resourceModel, Pageable pageable);


    /**
     * read acl entries by specify (id and type) resource.
     *
     * @param resourceModel (id and type is required)
     * @param principal     optional query condition.
     * @param pageable
     * @return
     */
    Page<AclEntry> readEntries(@Valid ResourceModel resourceModel, AclPrincipalModel principal, Pageable pageable);


    /**
     * read principal entries by specify resource.
     *
     * @param principal     grant to specify principal.
     * @param resourceModel by resource type(type is required and id is optional)
     * @param permission    optional query condition
     * @param pageable
     * @return
     */
    Page<AclEntry> readPrincipalEntries(@Valid AclPrincipalModel principal, @Valid ResourceModel resourceModel, PermissionModel permission, Pageable pageable);


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
     * create acl entry for given resource
     *
     * @param resource   resource associated acl must exists(type id is required.)
     * @param grant      grants principal.
     * @param permission a group optional permissions.
     */
    void createAclEntry(@Valid ResourceModel resource, @Valid AclPrincipalModel grant, PermissionModel... permission);


    /**
     * Return true if given principal was granted permissions for given resource.
     *
     * @param resource   asserts resource.
     * @param principal  grants principal.
     * @param permission a group optional permissions.
     */
    boolean isAccessible(@Valid ResourceModel resource, @Valid AclPrincipalModel principal, PermissionModel permission);

}
