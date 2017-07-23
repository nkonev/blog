package com.github.nikit.cpp;

import com.github.nikit.cpp.controllers.PostController;
import org.junit.Assert;
import org.junit.Test;

public class OwaspSanitizerTest {


    @Test
    public void test() throws Exception {

        String unsafe = "<a href='ftp://ya.ru'>часто</a> используемый в печати и вэб-дизайне";

        String safe = PostController.sanitize(unsafe);
        Assert.assertEquals("часто используемый в печати и вэб-дизайне", safe);
    }

    @Test
    public void testNoClosed() throws Exception {

        String unsafe = "<a href='ftp://ya.ru'>часто используемый в печати и вэб-дизайне";

        String safe = PostController.sanitize(unsafe);
        Assert.assertEquals("часто используемый в печати и вэб-дизайне", safe);
    }

}
