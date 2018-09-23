package com.github.nkonev.blog.security;

import org.springframework.security.oauth2.client.filter.state.DefaultStateKeyGenerator;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.common.util.RandomValueStringGenerator;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

import static com.github.nkonev.blog.security.OAuth2AuthenticationSuccessHandler.SEPARATOR;

public class StateKeyGeneratorWithRedirectUrl extends DefaultStateKeyGenerator {
    private RandomValueStringGenerator generator = new RandomValueStringGenerator();

    @Override
    public String generateKey(OAuth2ProtectedResourceDetails resource) {
        HttpServletRequest currentHttpRequest = getCurrentHttpRequest();
        if (currentHttpRequest!=null){
            String referer = currentHttpRequest.getHeader("Referer");
            if (!StringUtils.isEmpty(referer)){
                return generator.generate()+SEPARATOR+referer;
            }
        }
        return generator.generate();
    }

    private static HttpServletRequest getCurrentHttpRequest(){
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            return ((ServletRequestAttributes)requestAttributes).getRequest();
        }
        return null;
    }
}
