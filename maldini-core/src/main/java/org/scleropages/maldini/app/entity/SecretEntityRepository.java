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
package org.scleropages.maldini.app.entity;

import org.scleropages.crud.orm.jpa.GenericRepository;
import org.scleropages.maldini.app.model.Secret;
import org.scleropages.maldini.app.model.SecretMapper;
import org.springframework.data.jpa.repository.EntityGraph;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public interface SecretEntityRepository extends GenericRepository<Secret, SecretMapper, SecretEntity, Long> {


    Iterable<SecretEntity> findAllByApplication_Id(Long applicationId);

    @EntityGraph(attributePaths = "key")
    SecretEntity findByIdOrSecretId(Long id, String secretId);
}
