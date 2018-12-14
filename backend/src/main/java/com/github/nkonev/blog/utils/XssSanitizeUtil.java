package com.github.nkonev.blog.utils;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

import java.util.regex.Pattern;

public class XssSanitizeUtil {

    private static final Pattern IFRAME_SRC_PATTERN = Pattern.compile("^(https://www\\.youtube\\.com.*)|(https://coub\\.com/.*)|(https://player\\.vimeo\\.com.*)$");

    // https://www.owasp.org/index.php/OWASP_Java_HTML_Sanitizer_Project
    private static final PolicyFactory SANITIZER_POLICY = new HtmlPolicyBuilder()
            .allowElements(
                    "a", "b", "img", "p", "strong", "em", "s", "u",
                    "blockquote", "br", "pre",
                    "h1","h2","h3","h4","h5","h6",
                    "ol", "ul", "li",
                    "sub", "sup",
                    "span", "img", "code", "iframe"
            )
            .allowUrlProtocols("https", "http")
            .allowAttributes("href", "target").onElements("a")
            .allowAttributes("src", "width", "height").onElements("img")
            .allowAttributes("class", "spellcheck").onElements("pre")
            .allowAttributes("class").onElements("p")
            .allowAttributes("style", "class").onElements("span")
            .allowAttributes("class").onElements("li")
            .allowAttributes("src").matching(IFRAME_SRC_PATTERN).onElements("iframe")
            .allowAttributes("class", "allowfullscreen", "frameborder").onElements("iframe")
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
