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
package org.scleropages.kapuas.security.authc.repo;

import org.scleropages.crud.dao.orm.jpa.GenericRepository;
import org.scleropages.kapuas.security.authc.entity.JwtTokenTemplateEntity;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.Query;

import java.util.Map;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public interface JwtTokenTemplateEntityRepository extends GenericRepository<JwtTokenTemplateEntity, Long> {

    @Cacheable
    JwtTokenTemplateEntity getByAssociatedTypeAndAssociatedId(Integer associatedType, String associatedId);

    @Query(nativeQuery = true, value = "select case when verify_key_encoded is not null then verify_key_encoded else sign_key_encoded end as verify_key_encoded , alg_ from sec_jwtt where associated_type=? and associated_id=?")
    Map<String, Object> getVerifyKeyEncodedAndAlgorithmByAssociatedTypeAndAssociatedId(Integer associatedType, String associatedId);

}
