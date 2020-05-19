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
package org.scleropages.maldini.security.crypto.entity;

import org.scleropages.crud.dao.orm.jpa.GenericRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Optional;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public interface CryptographyEntityRepository extends GenericRepository<CryptographyEntity, Long> {

    Page<CryptographyEntity> findAllByAssociatedType(Integer associatedType, Pageable pageable);

    Page<CryptographyEntity> findAllByAssociatedTypeAndAssociatedId(Integer associatedType, String associatedId, Pageable pageable);

    @EntityGraph(attributePaths = "key")
    Optional<CryptographyEntity> findById(Long id);

    @EntityGraph(attributePaths = "key")
    Optional<CryptographyEntity> findByAssociatedIdAndNameAndAssociatedType(String associatedId, String name, Integer associatedType);

}
