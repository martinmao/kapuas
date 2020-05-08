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
package org.scleropages.maldini.security.crypto.model;

import com.google.common.collect.Lists;
import org.mapstruct.Mapper;
import org.scleropages.crud.dao.orm.ModelMapper;
import org.scleropages.crud.dao.orm.ModelMapperRepository;
import org.scleropages.maldini.security.crypto.entity.CryptographyEntity;
import org.scleropages.maldini.security.crypto.entity.KeyEntity;

import java.util.List;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
@Mapper(config = ModelMapper.DefaultConfig.class)
public interface CryptographyMapper extends ModelMapper<CryptographyEntity, Cryptography> {

    default Cryptography mapForReadWithKeys(CryptographyEntity cryptographyEntity, boolean withKeys) {
        Cryptography cryptography = mapForRead(cryptographyEntity);
        if (withKeys) {
            KeyMapper keyMapper = (KeyMapper) ModelMapperRepository.getRequiredModelMapper(KeyMapper.class);
            KeyEntity key = cryptographyEntity.getKey();
            List<Key> cryptographyKeys = Lists.newArrayList();
            cryptography.setKeys(cryptographyKeys);
            cryptographyKeys.add(keyMapper.mapForRead(key));
            if (key.getKeyType().equals(Key.KEY_PAIR_TYPE_PRIVATE)) {
                cryptographyKeys.add(keyMapper.mapForRead(key.getRefKey()));
            }
        }
        return cryptography;
    }

}
