package com.github.nkonev.util;

import com.github.nkonev.utils.XssSanitizeUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class XssSanitizeUtilTest {

    @Before
    public void before() throws Exception {
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(new MockHttpServletRequest());
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
    }

    @After
    public void after() throws Exception {
        RequestContextHolder.resetRequestAttributes();
    }


    @Test
    public void test() throws Exception {
        String unsafe = "<a href='javascript:alert('XSS')'>часто</a> используемый в печати и вэб-дизайне";

        String safe = XssSanitizeUtil.sanitize(unsafe);
        Assert.assertEquals("часто используемый в печати и вэб-дизайне", safe);
    }

    @Test
    public void testNoClosed() throws Exception {
        String unsafe = "<a href='javascript:alert('XSS')'>часто используемый в печати и вэб-дизайне";

        String safe = XssSanitizeUtil.sanitize(unsafe);
        Assert.assertEquals("часто используемый в печати и вэб-дизайне", safe);
    }

}
