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
package org.scleropages.maldini.security.acl.web;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.scleropages.core.mapper.JsonMapper;
import org.scleropages.maldini.security.acl.model.AclPrincipalModel;
import org.scleropages.maldini.security.acl.model.AclStrategy;
import org.scleropages.maldini.security.acl.model.ResourceModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.nio.charset.Charset;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@AutoConfigureMockMvc
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
//@Transactional
public class AclActionTest {

    private static final String RESOURCE_TYPE = "open_docs";

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void _1_strategy() throws Exception {

        AclStrategy strategy = new AclStrategy();
        strategy.setResource(RESOURCE_TYPE);
        strategy.setExpression("read=读取,write=修改");

        mockMvc.perform(MockMvcRequestBuilders.post("/acl/strategy")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonMapper.nonEmptyMapper().toJson(strategy))).andExpect(MockMvcResultMatchers.status().isOk());


        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/acl/strategy/" + RESOURCE_TYPE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.resource").value(RESOURCE_TYPE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.permissions.length()").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.permissions[0].name").value("write"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.permissions[1].name").value("read"))
                .andReturn();

        System.out.println(result.getResponse().getContentAsString(Charset.forName("utf-8")));
    }

    private static long PRINCIPAL_ID_START;

    @Test
    public void _2_principal() throws Exception {
        PRINCIPAL_ID_START = System.currentTimeMillis();
        int iterations = 10;
        for (int i = 0; i < iterations; i++) {
            AclPrincipalModel principal = new AclPrincipalModel();
            String id = String.valueOf(PRINCIPAL_ID_START + i);
            principal.setName(id);
            principal.setTag("docs_user" + id);

            mockMvc.perform(MockMvcRequestBuilders.post("/acl/principal")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(JsonMapper.nonEmptyMapper().toJson(principal))).andExpect(MockMvcResultMatchers.status().isOk());
        }
    }

    private static long RESOURCE_ID_START;

    @Test
    public void _3_acl() throws Exception {
        RESOURCE_ID_START = System.currentTimeMillis();
        int iterations = 10;
        for (int i = 0; i < iterations; i++) {
            ResourceModel resource = new ResourceModel();
            String id = String.valueOf(RESOURCE_ID_START + i);
            resource.setId(id);
            resource.setTag("/shared/docs/" + id + ".pdf");
            resource.setType(RESOURCE_TYPE);
            resource.setOwner(PRINCIPAL_ID_START + i + "");

            mockMvc.perform(MockMvcRequestBuilders.post("/acl")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(JsonMapper.nonEmptyMapper().toJson(resource))).andExpect(MockMvcResultMatchers.status().isOk());
        }
    }

    @Test
    public void _4_aclEntry() throws Exception {
        int iterations = 10;
        for (int i = 0; i < iterations; i += 4) {
            CreateAclEntryRequest request = new CreateAclEntryRequest();
            request.setPrincipal(PRINCIPAL_ID_START + i + "");
            request.setPermission(new String[]{"read"});
            for (int j = 0; j < iterations; j += 4) {
                mockMvc.perform(MockMvcRequestBuilders.post("/acl/entries/" + RESOURCE_TYPE + "/" + (RESOURCE_ID_START + j))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonMapper.nonEmptyMapper().toJson(request))).andExpect(MockMvcResultMatchers.status().isOk());
            }
        }
    }

}
