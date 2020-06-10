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
package org.scleropages.kapuas.security.crypto.web;

import org.scleropages.crud.web.GenericAction;
import org.scleropages.kapuas.security.crypto.CryptographyManager;
import org.scleropages.kapuas.security.crypto.model.Cryptography;
import org.scleropages.openapi.annotation.ApiIgnore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
@RestController
@RequestMapping("cryptography")
public class CryptographyAction implements GenericAction {


    private CryptographyManager cryptographyManager;

    @PostMapping
    public void createCryptography(@ApiIgnore({Cryptography.Create.class}) @RequestBody Cryptography cryptography) {
        cryptographyManager.save(cryptography);
    }

    @ApiIgnore({Cryptography.Create.class})
    @GetMapping("item/{id}")
    public Cryptography getCryptography(@PathVariable("id") Long id) {
        return cryptographyManager.getById(id);
    }

    @GetMapping("{associatedType}")
    public Page<Cryptography> findPageCryptography(@PathVariable("associatedType") Integer associatedType, HttpServletRequest request) {
        return cryptographyManager.findPage(associatedType, buildPageableFromRequest(request));
    }

    @GetMapping("{associatedType}/{associatedId}")
    public Page<Cryptography> findPageCryptography(@PathVariable("associatedType") Integer associatedType, @PathVariable("associatedId") String associatedId, HttpServletRequest request) {
        return cryptographyManager.findPage(associatedType, associatedId, buildPageableFromRequest(request));
    }


    @GetMapping("{associatedType}/{associatedId}/{name}")
    public Cryptography getCryptography(@PathVariable("associatedType") Integer associatedType, @PathVariable("associatedId") String associatedId, @PathVariable("name") String name) {
        return cryptographyManager.findOne(associatedType, associatedId, name);
    }


    @Autowired
    public void setCryptographyManager(CryptographyManager cryptographyManager) {
        this.cryptographyManager = cryptographyManager;
    }
}
