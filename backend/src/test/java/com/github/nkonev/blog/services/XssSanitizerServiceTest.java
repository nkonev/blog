package com.github.nkonev.blog.services;

import com.github.nkonev.blog.AbstractUtTestRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class XssSanitizerServiceTest extends AbstractUtTestRunner {

    @Autowired
    private XssSanitizerService xssSanitizerService;

    @Before
    public void beforeXssTest() throws Exception {
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(new MockHttpServletRequest());
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
    }

    @After
    public void afterXssTest() throws Exception {
        RequestContextHolder.resetRequestAttributes();
    }


    @Test
    public void test() throws Exception {
        String unsafe = "<a href='javascript:alert('XSS')'>часто</a> используемый в печати и вэб-дизайне";

        String safe = xssSanitizerService.sanitize(unsafe);
        Assert.assertEquals("часто используемый в печати и вэб-дизайне", safe);
    }

    @Test
    public void testNoClosed() throws Exception {
        String unsafe = "<a href='javascript:alert('XSS')'>часто используемый в печати и вэб-дизайне";

        String safe = xssSanitizerService.sanitize(unsafe);
        Assert.assertEquals("часто используемый в печати и вэб-дизайне", safe);
    }

}
