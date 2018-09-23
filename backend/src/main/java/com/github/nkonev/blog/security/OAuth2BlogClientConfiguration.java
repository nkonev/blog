package com.github.nkonev.blog.security;

import com.github.nkonev.blog.config.CustomConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.client.token.DefaultAccessTokenRequest;

import javax.annotation.Resource;
import java.util.Map;

import static com.github.nkonev.blog.security.SecurityConfig.API_LOGIN_FACEBOOK;
import static org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter.CURRENT_URI;

/**
 * Copy-paste from {@link org.springframework.security.oauth2.config.annotation.web.configuration.OAuth2ClientConfiguration}
 * Previously was enabled with @EnableOAuth2Client
 */
@Configuration
public class OAuth2BlogClientConfiguration {

    @Autowired
    private CustomConfig customConfig;

    @Bean
    public FilterRegistrationBean oauth2ClientFilterRegistration() {
        OAuth2ClientContextFilter filter = new OAuth2ClientContextFilter();

        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(filter);
        // magic constant from https://spring.io/guides/tutorials/spring-boot-oauth2/#_social_login_manual
        registration.setOrder(-100);
        return registration;
    }

    @Bean
    @Scope(value = "request", proxyMode = ScopedProxyMode.INTERFACES)
    protected AccessTokenRequest accessTokenRequest(
            @Value("#{request.parameterMap}") Map<String, String[]> parameters,
            @Value("#{request.getAttribute('"+CURRENT_URI+"')}") String currentUri
    ) {
        DefaultAccessTokenRequest request = new DefaultAccessTokenRequest(parameters);
        request.setCurrentUri(currentUri);
        return request;
    }

    @Configuration
    protected static class OAuth2ClientContextConfiguration {

        @Resource
        @Qualifier("accessTokenRequest")
        private AccessTokenRequest accessTokenRequest;

        @Bean
        @Scope(value = "session", proxyMode = ScopedProxyMode.INTERFACES)
        public OAuth2ClientContext oauth2ClientContext() {
            return new DefaultOAuth2ClientContext(accessTokenRequest);
        }

    }

}
