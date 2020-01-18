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
package org.scleropages.maldini.app.web;

import org.apache.commons.lang3.math.NumberUtils;
import org.scleropages.crud.web.GenericAction;
import org.scleropages.maldini.app.ApplicationManager;
import org.scleropages.maldini.app.model.Application;
import org.scleropages.maldini.security.crypto.CryptographyManager;
import org.scleropages.maldini.security.crypto.model.Cryptography;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("app")
public class ApplicationAction implements GenericAction {

    private ApplicationManager applicationManager;

    private CryptographyManager cryptographyManager;

    @PostMapping
    public Application createApplication(@RequestBody Application application) {
        return applicationManager.create(application);
    }

    @PostMapping("update")
    public void updateApplication(@RequestBody Application application) {
        applicationManager.save(application);
    }

    @GetMapping("{appId}")
    public Application getApplication(@PathVariable("appId") String appId) {
        return applicationManager.getApplication(createApplicationFromRequestParam(appId));
    }

    @GetMapping("page")
    public Object findPageApplication(HttpServletRequest request) {
        return applicationManager.findApplicationPage(buildSearchFilterFromRequest(request), buildPageableFromRequest(request));
    }

    @PostMapping("cryptography/{appId}")
    public void createCryptography(@PathVariable("appId") String appId, @RequestBody Cryptography cryptography) {
        populateApplicationInformation(createApplicationFromRequestParam(appId), cryptography);
        cryptographyManager.save(cryptography);
    }

    @GetMapping("cryptography/{appId}/{name}")
    public Object getCryptography(@PathVariable("appId") String appId, @PathVariable("name") String name) {
        Application application = applicationManager.getApplication(createApplicationFromRequestParam(appId));
        return cryptographyManager.findOne(applicationManager.getProviderId(), String.valueOf(application.getId()), name);
    }

    @GetMapping("cryptography/item/{id}")
    public Object getCryptography(@PathVariable("id") Long id) {
        Cryptography cryptography = new Cryptography();
        cryptography.setId(id);
        return cryptographyManager.findById(id);
    }

    @GetMapping("cryptography")
    public Object findPageCryptography(HttpServletRequest request) {
        return cryptographyManager.findPage(applicationManager.getProviderId(), buildPageableFromRequest(request));
    }

    @GetMapping("cryptography/{appId}")
    public Object findPageCryptography(@PathVariable("appId") String appId, HttpServletRequest request) {
        return cryptographyManager.findPage(applicationManager.getProviderId(), appId, buildPageableFromRequest(request));
    }


    protected void populateApplicationInformation(Application application, Cryptography cryptography) {
        cryptography.setAssociatedType(applicationManager.getProviderId());
        cryptography.setAssociatedId(String.valueOf(application.getId()));
    }

    protected Application createApplicationFromRequestParam(String id) {
        Application application = new Application();
        if (NumberUtils.isCreatable(id)) {
            application.setId(Long.valueOf(id));
        }
        application.setAppId(id);
        return application;
    }

    @Autowired
    public void setApplicationManager(ApplicationManager applicationManager) {
        this.applicationManager = applicationManager;
    }

    @Autowired
    public void setCryptographyManager(CryptographyManager cryptographyManager) {
        this.cryptographyManager = cryptographyManager;
    }
}
