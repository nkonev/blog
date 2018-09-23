package com.github.nkonev.blog.security;

import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import static com.github.nkonev.blog.security.OAuth2ClientContextFilterWithRedirectUrlFix.QUERY_PARAM_REDIRECT;

public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final String base;

    public OAuth2AuthenticationSuccessHandler(String base) {
        this.base = base;
    }


    @Override
    protected String determineTargetUrl(HttpServletRequest request,
                                        HttpServletResponse response) {

        UriComponents uriComponents = UriComponentsBuilder
                .fromHttpUrl(base)
                .query(request.getQueryString())
                .build();
        MultiValueMap<String, String> queryParams = uriComponents.getQueryParams();
        String redirect = queryParams.getFirst(QUERY_PARAM_REDIRECT);
        if (StringUtils.isEmpty(redirect)){
            redirect = "/";
        } else {
            redirect = URLDecoder.decode(redirect, StandardCharsets.UTF_8);
            redirect = URLDecoder.decode(redirect, StandardCharsets.UTF_8);
        }
        return redirect;
    }
}
