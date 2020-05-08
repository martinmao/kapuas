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

import com.google.common.collect.Maps;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.scleropages.core.mapper.JsonMapper;
import org.scleropages.core.mapper.JsonMapper2;
import org.scleropages.maldini.security.acl.model.AclStrategy;
import org.scleropages.maldini.security.acl.model.ResourceModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@AutoConfigureMockMvc
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AclActionTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void _1_strategy() throws Exception {


//        createStrategy("linux_files", "write=修改>read=读取>execute=执行", resultActions -> {
//            try {
//                resultActions.andExpect(MockMvcResultMatchers.status().isOk())
//                        .andExpect(MockMvcResultMatchers.jsonPath("$.resource").value("linux_files"))
//                        .andExpect(MockMvcResultMatchers.jsonPath("$.permissions.length()").value(3))
//                        .andExpect(MockMvcResultMatchers.jsonPath("$.permissions[0].name").value("execute"))
//                        .andExpect(MockMvcResultMatchers.jsonPath("$.permissions[0].tag").value("执行"))
//                        .andExpect(MockMvcResultMatchers.jsonPath("$.permissions[0].extension").doesNotExist())
//
//                        .andExpect(MockMvcResultMatchers.jsonPath("$.permissions[1].name").value("read"))
//                        .andExpect(MockMvcResultMatchers.jsonPath("$.permissions[1].tag").value("读取"))
//                        .andExpect(MockMvcResultMatchers.jsonPath("$.permissions[1].extension").value("execute"))
//
//
//                        .andExpect(MockMvcResultMatchers.jsonPath("$.permissions[2].name").value("write"))
//                        .andExpect(MockMvcResultMatchers.jsonPath("$.permissions[2].tag").value("修改"))
//                        .andExpect(MockMvcResultMatchers.jsonPath("$.permissions[2].extension").value("execute,read"));
//            } catch (Exception e) {
//                throw new IllegalStateException(e);
//            }
//        });
//
//
//        createStrategy("item_category", "buying=采购,qc=品控,marketing=营销,guiding=导购,after_sales=售后", resultActions -> {
//            try {
//                resultActions
//                        .andExpect(MockMvcResultMatchers.status().isOk())
//                        .andExpect(MockMvcResultMatchers.jsonPath("$.resource").value("item_category"))
//                        .andExpect(MockMvcResultMatchers.jsonPath("$.permissions.length()").value(5))
//                        .andExpect(MockMvcResultMatchers.jsonPath("$.permissions[0].name").value("after_sales"))
//                        .andExpect(MockMvcResultMatchers.jsonPath("$.permissions[0].tag").value("售后"))
//                        .andExpect(MockMvcResultMatchers.jsonPath("$.permissions[0].extension").doesNotExist())
//
//                        .andExpect(MockMvcResultMatchers.status().isOk())
//                        .andExpect(MockMvcResultMatchers.jsonPath("$.permissions[4].name").value("buying"))
//                        .andExpect(MockMvcResultMatchers.jsonPath("$.permissions[4].tag").value("采购"))
//                        .andExpect(MockMvcResultMatchers.jsonPath("$.permissions[4].extension").doesNotExist());
//
//            } catch (Exception e) {
//                throw new IllegalStateException(e);
//            }
//        });
//
//        createStrategy("url_access", null, resultActions -> {
//            try {
//                resultActions
//                        .andExpect(MockMvcResultMatchers.status().isOk())
//                        .andExpect(MockMvcResultMatchers.jsonPath("$.resource").value("url_access"))
//                        .andExpect(MockMvcResultMatchers.jsonPath("$.permissions").doesNotExist());
//            } catch (Exception e) {
//                throw new IllegalStateException(e);
//            }
//        });
//
//        for (int i = 10; i < 1000; i++) {
//            createStrategy("url_access_" + i, null, resultActions -> {
//            });
//        }
//
//        long principalIdStart = System.currentTimeMillis();

        long principalIdStart = 1588878225529l;

//        for (int i = 0; i < 100000; i++) {
//            AclPrincipalModel principal = new AclPrincipalModel();
//            String id = String.valueOf(principalIdStart + i);
//            principal.setName(id);
//            principal.setTag("user_tag" + id);
//
//            mockMvc.perform(MockMvcRequestBuilders.post("/acl/principal")
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .content(JsonMapper.nonEmptyMapper().toJson(principal))).andExpect(MockMvcResultMatchers.status().isOk());
//        }
        long resourceIdStart = System.currentTimeMillis();


        for (int i = 0; i < 500000; i++) {
            ResourceModel resource = new ResourceModel();
            String id = String.valueOf(resourceIdStart + i);
            resource.setId(id);
            resource.setTag("/shared/docs/" + id + ".pdf");
            resource.setType("linux_files");
            resource.setOwner(principalIdStart + i + "");
            resource.setBizPayload(JsonMapper2.toJson(resource));

            Map<String, Object> variables = Maps.newHashMap();
            variables.put("created", new Date());
            variables.put("path", resource.getTag());
            variables.put("size", i);
            variables.put("order", i + 0.01);
            resource.setVariables(variables);

            mockMvc.perform(MockMvcRequestBuilders.post("/acl/resource")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(JsonMapper2.toJson(resource))).andExpect(MockMvcResultMatchers.status().isOk());
        }

        for (int i = 0; i < 100000; i += 5000) {
            CreateAclEntryRequest request = new CreateAclEntryRequest();
            request.setPrincipal(principalIdStart + i + "");
            request.setPermission(new String[]{"read"});
            for (int j = 0; j < 20000; j += 5000) {
                mockMvc.perform(MockMvcRequestBuilders.post("/acl/entries/" + "linux_files" + "/" + (resourceIdStart + j))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonMapper2.toJson(request))).andExpect(MockMvcResultMatchers.status().isOk());
            }
        }

        resourceIdStart += 20000;


        for (int i = 0; i < 500000; i++) {
            ResourceModel resource = new ResourceModel();
            String id = String.valueOf(resourceIdStart + i);
            resource.setId(id);
            resource.setTag("item_category_" + id);
            resource.setType("item_category");
            resource.setOwner(principalIdStart + i + "");
            resource.setBizPayload(JsonMapper2.toJson(resource));

            Map<String, Object> variables = Maps.newHashMap();
            variables.put("created", new Date());
            variables.put("name", resource.getTag());
            variables.put("item_count", i + 1000);
            variables.put("order", i + 0.01);
            resource.setVariables(variables);


            mockMvc.perform(MockMvcRequestBuilders.post("/acl/resource")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(JsonMapper2.toJson(resource))).andExpect(MockMvcResultMatchers.status().isOk());
        }

        for (int i = 0; i < 100000; i += 5000) {
            CreateAclEntryRequest request = new CreateAclEntryRequest();
            request.setPrincipal(principalIdStart + i + "");
            request.setPermission(new String[]{"buying"});
            for (int j = 0; j < 20000; j += 5000) {
                mockMvc.perform(MockMvcRequestBuilders.post("/acl/entries/" + "item_category" + "/" + (resourceIdStart + j))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonMapper.nonEmptyMapper().toJson(request))).andExpect(MockMvcResultMatchers.status().isOk());
            }
        }

        resourceIdStart += 20000;

        for (int i = 0; i < 500000; i++) {
            ResourceModel resource = new ResourceModel();
            String id = String.valueOf(resourceIdStart + i);
            resource.setId(id);
            resource.setTag("url_access" + id);
            resource.setType("url_access");
            resource.setOwner(principalIdStart + i + "");
            resource.setBizPayload(JsonMapper2.toJson(resource));

            Map<String, Object> variables = Maps.newHashMap();
            variables.put("created", new Date());
            variables.put("name", resource.getTag());
            resource.setVariables(variables);


            mockMvc.perform(MockMvcRequestBuilders.post("/acl/resource")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(JsonMapper.nonEmptyMapper().toJson(resource))).andExpect(MockMvcResultMatchers.status().isOk());
        }

        for (int i = 0; i < 100000; i += 5000) {
            CreateAclEntryRequest request = new CreateAclEntryRequest();
            request.setPrincipal(principalIdStart + i + "");
            for (int j = 0; j < 20000; j += 5000) {
                mockMvc.perform(MockMvcRequestBuilders.post("/acl/entries/" + "url_access" + "/" + (resourceIdStart + j))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonMapper.nonEmptyMapper().toJson(request))).andExpect(MockMvcResultMatchers.status().isOk());
            }
        }
    }


    private void createStrategy(String resource, String expression, Consumer<ResultActions> resultActionsConsumer) throws Exception {
        AclStrategy strategy = new AclStrategy();
        strategy.setResource(resource);
        strategy.setExpression(expression);

        mockMvc.perform(MockMvcRequestBuilders.post("/acl/strategy")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonMapper.nonEmptyMapper().toJson(strategy))).andExpect(MockMvcResultMatchers.status().isOk());
        ResultActions perform = mockMvc.perform(MockMvcRequestBuilders.get("/acl/strategy/" + resource));
        resultActionsConsumer.accept(perform);
        System.out.println(perform.andReturn().getResponse().getContentAsString(Charset.forName("utf-8")));
    }

}
