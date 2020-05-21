package org.scleropages.kapuas.security.acl;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.scleropages.kapuas.security.acl.model.ResourceModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class AclManagerTest {

    @Autowired
    private AclManager aclManager;


    @BeforeClass
    public static void setup() {

    }

    @AfterClass
    public static void clear() {

    }

    @Test
    @Transactional
    public void testAcl() {
        String id = String.valueOf(System.currentTimeMillis());
        String tag = "/usr/local/private/" + id + ".p12";
        ResourceModel model = new ResourceModel();
        model.setType("file");
        model.setId(id);
        model.setTag(tag);
        model.setOwner("martinmao@icloud.com");
        aclManager.createAcl(model);
        Acl acl = aclManager.getAcl(model);
        Assert.assertEquals(id, acl.resource().id());
        Assert.assertEquals(tag, acl.resource().tag());
        Assert.assertEquals("file", acl.resource().type());
        Assert.assertEquals("martinmao@icloud.com", acl.owners().get(0).id());
        Assert.assertEquals("毛崇", acl.owners().get(0).tag());
    }
}