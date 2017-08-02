package com.github.nikit.cpp.utils;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

public class XssSanitizeUtil {
    // https://www.owasp.org/index.php/OWASP_Java_HTML_Sanitizer_Project
    private static final PolicyFactory SANITIZER_POLICY = new HtmlPolicyBuilder()
            .allowElements("a", "b", "img", "p")
            .allowUrlProtocols("https", "http")
            .allowAttributes("href").onElements("a")
            .allowAttributes("src").onElements("img")
            .requireRelNofollowOnLinks()
            .toFactory();
    private static final XssHtmlChangeListener XSS_HTML_CHANGE_LISTENER = new XssHtmlChangeListener();

    public static final String sanitize(String html) {
        return SANITIZER_POLICY.sanitize(
                html,
                XSS_HTML_CHANGE_LISTENER,
                "ip='"+IpUtil.getIpAddressFromRequestContext()+"'"
        );
    }

}
