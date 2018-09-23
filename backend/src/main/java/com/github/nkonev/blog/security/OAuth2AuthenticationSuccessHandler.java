package com.github.nkonev.blog.security;

import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    public static final String DEFAULT = "/";
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
        String stateEncoded = queryParams.getFirst("state");
        if (stateEncoded == null) {
            return DEFAULT;
        }
        String stateDecoded = URLDecoder.decode(stateEncoded, StandardCharsets.UTF_8);
        String[] split = stateDecoded.split(",");
        String redirect;
        if (split.length != 2){
            redirect = DEFAULT;
        } else {
            redirect = split[1];
        }
        return redirect;
    }
}
