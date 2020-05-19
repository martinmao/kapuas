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
package org.scleropages.maldini.security.authc.mgmt.web;

import org.scleropages.maldini.security.authc.mgmt.JwtTokenTemplateManager;
import org.scleropages.maldini.security.authc.mgmt.model.JwtTokenTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
@RestController
@RequestMapping("jwt")
public class JwtTokenTemplateAction {


    private JwtTokenTemplateManager jwtTokenTemplateManager;

    @PostMapping
    public void createJwtTokenTemplate(@RequestBody JwtTokenTemplate template) {
        jwtTokenTemplateManager.save(template);
    }

    @GetMapping("{associatedType}/{associatedId}")
    public JwtTokenTemplate getVerifyKeyEncoded(@PathVariable Integer associatedType, @PathVariable String associatedId) {
        return jwtTokenTemplateManager.getVerifyKeyEncodedAndAlgorithmByAssociatedTypeAndAssociatedId(associatedType, associatedId);
    }

    @GetMapping("{id}")
    public JwtTokenTemplate getJwtTokenTemplate(@PathVariable Long id) {
        return jwtTokenTemplateManager.getById(id);
    }


    @Autowired
    public void setJwtTokenTemplateManager(JwtTokenTemplateManager jwtTokenTemplateManager) {
        this.jwtTokenTemplateManager = jwtTokenTemplateManager;
    }
}
