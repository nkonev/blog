package com.github.nkonev.blog.security;

import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


public class OAuth2ClientContextFilterWithRedirectUrlFix extends OAuth2ClientContextFilter {

    private final String redirectUrl;

    public static final String QUERY_PARAM_REDIRECT = "blog";

    public OAuth2ClientContextFilterWithRedirectUrlFix(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    @Override
    protected String calculateCurrentUri(HttpServletRequest request) throws UnsupportedEncodingException {
        String referer = request.getHeader("Referer");
        UriComponentsBuilder builder = ServletUriComponentsBuilder.fromUriString(redirectUrl);
        UriComponents uriComponents;
        if (!StringUtils.isEmpty(referer)) {
            builder = builder.query(QUERY_PARAM_REDIRECT+"={r}");
            //String path = UriComponentsBuilder.fromHttpUrl(referer).build().getPath();
            uriComponents = builder.buildAndExpand(URLEncoder.encode(referer, StandardCharsets.UTF_8));
        } else {
            uriComponents = builder.build();
        }
        return uriComponents.toUri().toString();
    }
}
