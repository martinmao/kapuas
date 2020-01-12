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
package org.scleropages.maldini.security.acl.provider;

import org.scleropages.maldini.security.acl.Acl;
import org.scleropages.maldini.security.acl.AclEntry;
import org.scleropages.maldini.security.acl.entity.AclPrincipalEntity;
import org.scleropages.maldini.security.acl.entity.PermissionEntity;
import org.scleropages.maldini.security.acl.model.AclPrincipalModel;
import org.scleropages.maldini.security.acl.model.PermissionModel;
import org.scleropages.maldini.security.acl.model.ResourceModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.Serializable;
import java.util.Optional;

/**
 * SPI interface. help any domain's easy way to management access control.
 *
 * <pre>
 * <b>!!!IMPORT INFORMATION: DO NOT MODIFY ARGUMENT(xxxEntity) state. all methods already executing-bind in transaction. any entity changes will override to database.</b>
 * </pre>
 *
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public interface AclProvider {

    /**
     * Return the resource type represented of this provider.
     * {@link org.scleropages.maldini.security.acl.AclManager} implementation use this method call result choose matches provider handle acl crud operations.
     *
     * @return
     */
    Serializable type();


    /**
     * create a acl for specify resource.
     *
     * @param resource           associated resource.
     * @param permissionEntity   associated acl strategy for given resource.
     * @param aclPrincipalEntity associated owner for given resource.
     */
    void createAcl(ResourceModel resource, PermissionEntity permissionEntity, AclPrincipalEntity aclPrincipalEntity);


    /**
     * get {@link Acl} by given resource.
     *
     * @param resourceModel    associated resource.
     * @param permissionEntity associated acl strategy for given resource.
     * @return
     */
    Acl readAcl(ResourceModel resourceModel, PermissionEntity permissionEntity);


    /**
     * read a page of acl(s) by resource type.
     *
     * @param resourceModel    associated resource type.
     * @param permissionEntity associated acl strategy for given resource.
     * @param pageable
     * @return
     */
    Page<Acl> readAcl(ResourceModel resourceModel, PermissionEntity permissionEntity, Pageable pageable);


    /**
     * create a acl entry for specify resource.
     *
     * @param resource           associated resource.
     * @param permissionEntity   associated acl strategy for given resource.
     * @param aclPrincipalEntity grant to principal for given resource.
     */
    void createAclEntry(ResourceModel resource, AclPrincipalEntity aclPrincipalEntity, PermissionEntity... permissionEntity);


    /**
     * create a acl entry for specify resource without permission(coarse-grained model: No permission was defined. just have one action for specify resource.)
     *
     * @param resource           associated resource.
     * @param aclPrincipalEntity grant to principal for given resource.
     */
    void createAclEntryWithoutPermission(ResourceModel resource, AclPrincipalEntity aclPrincipalEntity);


    /**
     * read acl entries by specify (id and type) resource model.
     * {@link org.scleropages.maldini.security.acl.AclManager} do not validate any rules(just make sure required arguments provided).other rules validation provided
     * by implementation(such as given resource exists and {@link org.scleropages.maldini.security.acl.model.AclStrategy} already defined)
     *
     * @param resourceModel  resource for query(id and type required)
     * @param principalModel optional for query
     * @param pageable
     * @return
     */
    Page<AclEntry> readEntries(ResourceModel resourceModel, Optional<AclPrincipalModel> principalModel, Pageable pageable);


    /**
     * read principal entries by specify resource type.
     * {@link org.scleropages.maldini.security.acl.AclManager} do not validate any rules(just make sure required arguments provided).other rules validation provided
     * by implementation(such as given resource exists and {@link org.scleropages.maldini.security.acl.model.AclStrategy} already defined)
     *
     * @param principal     granted principal
     * @param resourceModel resource type for query(type required)
     * @param permission    optional for query for specify permission returned
     * @param pageable
     * @return
     */
    Page<AclEntry> readPrincipalEntries(AclPrincipalModel principal, ResourceModel resourceModel, Optional<PermissionModel> permission, Pageable pageable);

}