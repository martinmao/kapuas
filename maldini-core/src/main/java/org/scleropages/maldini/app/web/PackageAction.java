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

import org.scleropages.crud.web.GenericAction;
import org.scleropages.maldini.app.PackageManager;
import org.scleropages.maldini.app.model.Function;
import org.scleropages.maldini.app.model.Package;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("pkg")
public class PackageAction implements GenericAction {

    private PackageManager packageManager;

    @PostMapping
    public void createPackage(@RequestBody Package model) {
        packageManager.create(model);
    }

    @GetMapping("page")
    public Object findPagePackage(HttpServletRequest request) {
        return packageManager.findPackagePage(buildSearchFilterFromRequest(request), buildPageableFromRequest(request));
    }

    @PostMapping("update")
    public void updatePackage(@RequestBody Package model) {
        packageManager.save(model);
    }

    @PostMapping("func")
    public void createFunction(@RequestBody Function function) {
        packageManager.create(function);
    }

    @GetMapping("func/page")
    public Object findPageFunction(HttpServletRequest request) {
        return packageManager.findFunctionPage(buildSearchFilterFromRequest(request), buildPageableFromRequest(request));
    }

    @PostMapping("func/update")
    public void updatePackage(@RequestBody Function function) {
        packageManager.save(function);
    }


    @Autowired
    public void setPackageManager(PackageManager packageManager) {
        this.packageManager = packageManager;
    }
}
