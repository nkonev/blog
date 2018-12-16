package com.github.nkonev.blog.config;

import com.github.nkonev.blog.utils.XssHtmlChangeListener;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.regex.Pattern;

@Configuration
public class XssSanitizerConfig {

    @Bean
    public PolicyFactory policyFactory(@Value("${custom.xss.iframe.allow.src.pattern}") String iframePatternSrc){
        Pattern pattern = Pattern.compile(iframePatternSrc);
        return new HtmlPolicyBuilder()
                .allowElements(
                        "a", "b", "img", "p", "strong", "em", "s", "u",
                        "blockquote", "br", "pre",
                        "h1","h2","h3","h4","h5","h6",
                        "ol", "ul", "li",
                        "sub", "sup",
                        "span", "img", "code", "iframe", "div"
                )
                .allowUrlProtocols("https", "http")
                .allowAttributes("href", "target").onElements("a")
                .allowAttributes("src", "width", "height").onElements("img")
                .allowAttributes("class", "spellcheck").onElements("pre")
                .allowAttributes("class").onElements("p")
                .allowAttributes("style", "class").onElements("span")
                .allowAttributes("class").onElements("li")
                .allowAttributes("src").matching(pattern).onElements("iframe")
                .allowAttributes("class", "allowfullscreen", "frameborder").onElements("iframe")
                .allowAttributes("class").onElements("div")
                .requireRelNofollowOnLinks()
                .toFactory();
    }

    @Bean
    public XssHtmlChangeListener xssHtmlChangeListener() {
        return new XssHtmlChangeListener();
    }
}
