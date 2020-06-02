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
package org.scleropages.kapuas.app.web;

import org.scleropages.crud.web.GenericAction;
import org.scleropages.kapuas.app.DomainManager;
import org.scleropages.kapuas.app.model.Domain;
import org.scleropages.kapuas.app.model.DomainFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
@RestController
@RequestMapping("domain")
public class DomainAction implements GenericAction {

    private DomainManager domainManager;

    @PostMapping
    public void createDomain(@RequestBody Domain model) {
        domainManager.create(model);
    }

    @GetMapping("page")
    public Page<Domain> findDomainPage(HttpServletRequest request) {
        return domainManager.findDomainPage(buildSearchFilterFromRequest(request), buildPageableFromRequest(request));
    }

    @PostMapping("update")
    public void updateDomain(@RequestBody Domain model) {
        domainManager.save(model);
    }

    @PostMapping("func")
    public void createDomainFunction(@RequestBody DomainFunction function) {
        domainManager.create(function);
    }

    @GetMapping("func/page")
    public Page<DomainFunction> findDomainFunctionPage(HttpServletRequest request) {
        return domainManager.findDomainFunctionPage(buildSearchFilterFromRequest(request), buildPageableFromRequest(request));
    }

    @PostMapping("func/update")
    public void updateDomainFunction(@RequestBody DomainFunction function) {
        domainManager.save(function);
    }


    @Autowired
    public void setDomainManager(DomainManager domainManager) {
        this.domainManager = domainManager;
    }
}
