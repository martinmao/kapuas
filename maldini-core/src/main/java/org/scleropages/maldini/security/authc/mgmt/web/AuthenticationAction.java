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

import org.scleropages.crud.web.Servlets;
import org.scleropages.maldini.security.SecurityOption;
import org.scleropages.maldini.security.authc.AuthenticationManager;
import org.scleropages.maldini.security.authc.mgmt.model.Authentication;
import org.scleropages.maldini.security.authc.token.client.StatelessUsernamePasswordToken;
import org.scleropages.maldini.security.authc.token.client.UsernamePasswordToken;
import org.scleropages.maldini.security.authc.token.client.jwt.JwtEncodedToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
@RestController
@RequestMapping("auth")
public class AuthenticationAction {

    private AuthenticationManager authenticationManager;

    @PostMapping
    public void authentication(@RequestParam String username, @RequestParam String password, HttpServletRequest request) {
        authenticationManager.authentication(new UsernamePasswordToken(username, password, false, Servlets.getRemoteAddr(request)));
    }

    @PostMapping("jwt")
    public Object createJwtToken(@RequestParam String username, @RequestParam String password, HttpServletRequest request) {
        return authenticationManager.createEncodedToken(
                new StatelessUsernamePasswordToken(username, password, false, Servlets.getRemoteAddr(request)),
                Servlets.getParametersStartingWith(request, "jwt_"), JwtEncodedToken.class);
    }

    @PostMapping("jwt/application")
    public Object createApplicationJwtToken(@RequestParam String username, @RequestParam String password, HttpServletRequest request) {
        StatelessUsernamePasswordToken statelessUsernamePasswordToken = new StatelessUsernamePasswordToken(username, password, false, Servlets.getRemoteAddr(request));
        statelessUsernamePasswordToken.addAuthenticationOption(SecurityOption.AUTHENTICATION_OPTION_AUTO_LOAD_DETAILS, false);
        return authenticationManager.createEncodedToken(statelessUsernamePasswordToken, Servlets.getParametersStartingWith(request, "jwt_"), JwtEncodedToken.class);
    }

    @PostMapping("login")
    public void login(@RequestParam String username, @RequestParam String password, boolean rememberMe, HttpServletRequest request) {
        authenticationManager.login(new UsernamePasswordToken(username, password, rememberMe, Servlets.getRemoteAddr(request)));
    }


    @PostMapping("create")
    public void create(Authentication authentication) {
        authenticationManager.create(authentication);
    }

    @PostMapping("credentials/reset/{id}")
    public Object resetCredentials(@PathVariable("id") Long id) {
        return authenticationManager.resetCredentials(id);
    }

    @Autowired
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }
}
