package com.github.nkonev.blog.security;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.client.token.DefaultAccessTokenRequest;

import javax.annotation.Resource;
import java.util.Map;

import static org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter.CURRENT_URI;

/**
 * Copy-paste from {@link org.springframework.security.oauth2.config.annotation.web.configuration.OAuth2ClientConfiguration}
 * Previously was enabled with @EnableOAuth2Client
 */
@Configuration
public class OAuth2BlogClientConfiguration {

    public static final String FACEBOOK_OAUTH2_CLIENT_CONTEXT = "facebookOauth2ClientContext";
    public static final String VKONTAKTE_OAUTH2_CLIENT_CONTEXT = "vkontakteOauth2ClientContext";

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

        @Bean(FACEBOOK_OAUTH2_CLIENT_CONTEXT)
        @Scope(value = "session", proxyMode = ScopedProxyMode.INTERFACES)
        public OAuth2ClientContext vkontakteOauth2ClientContext() {
            return new DefaultOAuth2ClientContext(accessTokenRequest);
        }

        @Bean(VKONTAKTE_OAUTH2_CLIENT_CONTEXT)
        @Scope(value = "session", proxyMode = ScopedProxyMode.INTERFACES)
        public OAuth2ClientContext facebookOauth2ClientContext() {
            return new DefaultOAuth2ClientContext(accessTokenRequest);
        }
    }

}
