package com.github.nkonev.blog.services;

import com.github.nkonev.blog.utils.XssHtmlChangeListener;
import org.owasp.html.PolicyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import static com.github.nkonev.blog.utils.ServletUtils.getIpAddressFromRequestContext;

@Service
public class XssSanitizerService {

    @Autowired
    private PolicyFactory policyFactory;

    @Autowired
    private XssHtmlChangeListener xssHtmlChangeListener;

    public String sanitize(String html) {
        return policyFactory.sanitize(
                html,
                xssHtmlChangeListener,
                "ip='"+ getIpAddressFromRequestContext()+"'"
        );
    }

}
