package com.github.nikit.cpp.utils;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

public class XssSanitizeUtil {
    // https://www.owasp.org/index.php/OWASP_Java_HTML_Sanitizer_Project
    private static final PolicyFactory SANITIZER_POLICY = new HtmlPolicyBuilder()
            .allowElements(
                    "a", "b", "img", "p", "strong", "em", "s",
                    "blockquote", "br", "pre",
                    "h1","h2","h3","h4","h5","h6",
                    "ol", "ul", "li",
                    "sub", "sup",
                    "span", "img"
            )
            .allowUrlProtocols("https", "http", "data") // "data" - for quill img
            .allowAttributes("href", "target").onElements("a")
            .allowAttributes("src").onElements("img")
            .allowAttributes("class", "spellcheck").onElements("pre")
            .allowAttributes("class").onElements("p")
            .allowAttributes("style", "class").onElements("span")
            .allowAttributes("src").onElements("img") // for quill img
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
