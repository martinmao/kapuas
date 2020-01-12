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
import org.scleropages.maldini.app.model.Secret;
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

    @PostMapping
    public Application createApplication(@RequestBody Application application) {
        return applicationManager.create(application);
    }

    @PostMapping("update")
    public void updateApplication(@RequestBody Application application) {
        applicationManager.save(application);
    }

    @GetMapping("{appId}")
    public Application findApplication(@PathVariable("appId") String appId) {
        return applicationManager.getApplication(createApplicationFromRequestParam(appId));
    }

    @GetMapping("page")
    public Object findPageApplication(HttpServletRequest request) {
        return applicationManager.findApplicationPage(buildSearchFilterFromRequest(request), buildPageableFromRequest(request));
    }


    @PostMapping("secret/{appId}")
    public void createSecret(@PathVariable("appId") String appId, @RequestBody Secret secret) {
        applicationManager.createSecret(createApplicationFromRequestParam(appId), secret);
    }

    @PostMapping("secret/{appId}/hmac")
    public void createHmacSecret(@PathVariable("appId") String appId, @RequestBody Secret secret) {
        applicationManager.createHmacSha256Secret(createApplicationFromRequestParam(appId), secret);
    }

    @PostMapping("secret/{appId}/rsa")
    public void createRsaSecret(@PathVariable("appId") String appId, @RequestBody Secret secret) {
        applicationManager.createRSASecret(createApplicationFromRequestParam(appId), secret);
    }


    @GetMapping("secret/item/{secretId}")
    public Object findSecret(@PathVariable("secretId") String secretId) {
        return applicationManager.getSecret(createSecretFromPathVariable(secretId));
    }


    @GetMapping("secret/{appId}")
    public Object findAllSecretsByApp(@PathVariable("appId") String appId) {
        return applicationManager.findAllSecrets(createApplicationFromRequestParam(appId));
    }

    protected Secret createSecretFromPathVariable(String id) {
        Secret secret = new Secret();
        if (NumberUtils.isCreatable(id)) {
            secret.setId(Long.valueOf(id));
        }
        secret.setSecretId(id);
        return secret;
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
}
