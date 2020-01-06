package com.github.nkonev.blog.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nkonev.blog.Constants;
import com.github.nkonev.blog.config.CustomConfig;
import com.github.nkonev.blog.dto.UserRole;
import com.github.nkonev.blog.security.checks.BlogPostAuthenticationChecks;
import com.github.nkonev.blog.security.checks.BlogPreAuthenticationChecks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.filter.CompositeFilter;

import javax.servlet.Filter;
import java.util.ArrayList;
import java.util.List;

import static com.github.nkonev.blog.security.OAuth2BlogClientConfiguration.FACEBOOK_OAUTH2_CLIENT_CONTEXT;
import static com.github.nkonev.blog.security.OAuth2BlogClientConfiguration.VKONTAKTE_OAUTH2_CLIENT_CONTEXT;

/**
 * http://websystique.com/springmvc/spring-mvc-4-and-spring-security-4-integration-example/
 * Created by nik on 08.06.17.
 */
@Configuration
@EnableWebSecurity
@Import(OAuth2BlogClientConfiguration.class)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    public static final String API_LOGIN_URL = "/api/login";
    public static final String API_LOGOUT_URL = "/api/logout";

    public static final String USERNAME_PARAMETER = "username";
    public static final String PASSWORD_PARAMETER = "password";
    public static final String REMEMBER_ME_PARAMETER = "remember-me";
    public static final String API_LOGIN_FACEBOOK = "/api/login/facebook";
    public static final String API_LOGIN_VKONTAKTE = "/api/login/vkontakte";

    @Autowired
    private CustomConfig customConfig;

    @Autowired
    private RESTAuthenticationEntryPoint authenticationEntryPoint;
    @Autowired
    private RESTAuthenticationFailureHandler authenticationFailureHandler;
    @Autowired
    private RESTAuthenticationSuccessHandler authenticationSuccessHandler;
    @Autowired
    private RESTAuthenticationLogoutSuccessHandler authenticationLogoutSuccessHandler;

    @Autowired
    private BlogUserDetailsService blogUserDetailsService;

    @Qualifier(FACEBOOK_OAUTH2_CLIENT_CONTEXT)
    @Autowired
    private OAuth2ClientContext facebookOauth2ClientContext;

    @Qualifier(VKONTAKTE_OAUTH2_CLIENT_CONTEXT)
    @Autowired
    private OAuth2ClientContext vkontakteOauth2ClientContext;

    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        return CookieCsrfTokenRepository.withHttpOnlyFalse();
    }

    @Bean
    public RESTAuthenticationLogoutSuccessHandler restAuthenticationLogoutSuccessHandler(ObjectMapper objectMapper) {
        return new RESTAuthenticationLogoutSuccessHandler(csrfTokenRepository(), objectMapper);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // https://dzone.com/articles/spring-security-4-authenticate-and-authorize-users
        // http://www.programming-free.com/2015/09/spring-security-password-encryption.html
        auth.authenticationProvider(authenticationProvider());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // default strength is BCrypt.GENSALT_DEFAULT_LOG2_ROUNDS=10
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/favicon.ico", "/static/**", Constants.Urls.API+"/**").permitAll();
        http.authorizeRequests()
                .antMatchers(Constants.Urls.API+ Constants.Urls.ADMIN+"/**").hasAuthority(UserRole.ROLE_ADMIN.name());

        http.csrf()
                .csrfTokenRepository(csrfTokenRepository());
        http.exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint)
                /*.accessDeniedHandler(
                        (request, response, accessDeniedException) -> {
                            throw accessDeniedException;
                        }
                )*/
        ;

        http.addFilterBefore(new OAuth2ClientContextFilter(), BasicAuthenticationFilter.class);
        http.addFilterBefore(ssoFilter(), BasicAuthenticationFilter.class);

        http.formLogin()
                .loginPage(API_LOGIN_URL).usernameParameter(USERNAME_PARAMETER).passwordParameter(PASSWORD_PARAMETER).permitAll()
                .successHandler(authenticationSuccessHandler)
                .failureHandler(authenticationFailureHandler)

        .and().logout().logoutUrl(API_LOGOUT_URL).logoutSuccessHandler(authenticationLogoutSuccessHandler).permitAll();

        http.authorizeRequests().requestMatchers(EndpointRequest.toAnyEndpoint()).permitAll();

        http.headers().frameOptions().disable();

//        http.rememberMe().rememberMeParameter(REMEMBER_ME_PARAMETER).tokenRepository(tokenRepository)
//                .tokenValiditySeconds(86400);
        http.headers().cacheControl().disable(); // see also com.github.nkonev.blog.controllers.AbstractImageUploadController#shouldReturnLikeCache
    }

    @Bean
    @ConfigurationProperties("vkontakte.client")
    public AuthorizationCodeResourceDetails vkontakte() {
        AuthorizationCodeResourceDetails authorizationCodeResourceDetails = new AuthorizationCodeResourceDetails();
        authorizationCodeResourceDetails.setPreEstablishedRedirectUri(customConfig.getBaseUrl()+API_LOGIN_VKONTAKTE);
        authorizationCodeResourceDetails.setUseCurrentUri(false);
        return authorizationCodeResourceDetails;
    }

    @Bean
    @ConfigurationProperties("vkontakte.resource")
    public ResourceServerProperties vkontakteResource() {
        return new ResourceServerProperties();
    }

    @Bean
    @ConfigurationProperties("facebook.client")
    public AuthorizationCodeResourceDetails facebook() {
        AuthorizationCodeResourceDetails authorizationCodeResourceDetails = new AuthorizationCodeResourceDetails();
        authorizationCodeResourceDetails.setPreEstablishedRedirectUri(customConfig.getBaseUrl()+API_LOGIN_FACEBOOK);
        authorizationCodeResourceDetails.setUseCurrentUri(false);
        return authorizationCodeResourceDetails;
    }

    @Bean
    @ConfigurationProperties("facebook.resource")
    public ResourceServerProperties facebookResource() {
        return new ResourceServerProperties();
    }

    @Autowired
    private FacebookPrincipalExtractor facebookPrincipalExtractor;

    @Autowired
    private VkontaktePrincipalExtractor vkontaktePrincipalExtractor;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private OauthExceptionHandler oauthExceptionHandler;

    // https://spring.io/guides/tutorials/spring-boot-oauth2/#_social_login_github for compose facebook with github
    private Filter ssoFilter() {
        CompositeFilter filter = new CompositeFilter();
        List<Filter> filters = new ArrayList<>();

        {
            OAuth2ClientAuthenticationProcessingFilter facebookFilter = new OAuth2ClientAuthenticationProcessingFilter(API_LOGIN_FACEBOOK);
            facebookFilter.setApplicationEventPublisher(applicationContext);
            OAuth2RestTemplate facebookTemplate = new OAuth2RestTemplate(facebook(), facebookOauth2ClientContext);
            AuthorizationCodeAccessTokenProvider authorizationCodeAccessTokenProviderWithUrl = new AuthorizationCodeAccessTokenProvider();
            authorizationCodeAccessTokenProviderWithUrl.setStateKeyGenerator(new StateKeyGeneratorWithRedirectUrl());
            facebookTemplate.setAccessTokenProvider(authorizationCodeAccessTokenProviderWithUrl);
            facebookFilter.setRestTemplate(facebookTemplate);
            UserInfoTokenServices tokenServices = new CheckedUserInfoTokenServices(
                    facebookResource().getUserInfoUri(), facebook().getClientId(),
                    facebookPrincipalExtractor, blogPreAuthenticationChecks(), blogPostAuthenticationChecks());
            tokenServices.setAuthoritiesExtractor(new FacebookAuthoritiesExtractor());
            tokenServices.setRestTemplate(facebookTemplate);
            facebookFilter.setTokenServices(tokenServices);
            facebookFilter.setAuthenticationSuccessHandler(new OAuth2AuthenticationSuccessHandler());
            facebookFilter.setAuthenticationFailureHandler(oauthExceptionHandler);

            filters.add(facebookFilter);
        }

        {
            OAuth2ClientAuthenticationProcessingFilter vkontakteFilter = new OAuth2ClientAuthenticationProcessingFilter(API_LOGIN_VKONTAKTE);
            vkontakteFilter.setApplicationEventPublisher(applicationContext);
            OAuth2RestTemplate vkontakteTemplate = new OAuth2RestTemplate(vkontakte(), vkontakteOauth2ClientContext);
            AuthorizationCodeAccessTokenProvider authorizationCodeAccessTokenProviderWithUrl = new AuthorizationCodeAccessTokenProvider();
            authorizationCodeAccessTokenProviderWithUrl.setStateKeyGenerator(new StateKeyGeneratorWithRedirectUrl());
            vkontakteTemplate.setAccessTokenProvider(authorizationCodeAccessTokenProviderWithUrl);
            vkontakteFilter.setRestTemplate(vkontakteTemplate);
            UserInfoTokenServices tokenServices = new CheckedUserInfoTokenServices(
                    vkontakteResource().getUserInfoUri(), vkontakte().getClientId(),
                    vkontaktePrincipalExtractor, blogPreAuthenticationChecks(), blogPostAuthenticationChecks());
            tokenServices.setAuthoritiesExtractor(new VkontakteAuthoritiesExtractor());
            tokenServices.setRestTemplate(vkontakteTemplate);
            vkontakteFilter.setTokenServices(tokenServices);
            vkontakteFilter.setAuthenticationSuccessHandler(new OAuth2AuthenticationSuccessHandler());
            vkontakteFilter.setAuthenticationFailureHandler(oauthExceptionHandler);

            filters.add(vkontakteFilter);
        }
        filter.setFilters(filters);
        return filter;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(blogUserDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        authenticationProvider.setPreAuthenticationChecks(blogPreAuthenticationChecks());
        authenticationProvider.setPostAuthenticationChecks(blogPostAuthenticationChecks());
        return authenticationProvider;
    }

    @Bean
    public BlogPreAuthenticationChecks blogPreAuthenticationChecks(){
        return new BlogPreAuthenticationChecks();
    }

    @Bean
    public BlogPostAuthenticationChecks blogPostAuthenticationChecks(){
        return new BlogPostAuthenticationChecks();
    }
//    @Bean
//    public PersistentTokenBasedRememberMeServices getPersistentTokenBasedRememberMeServices() {
//        PersistentTokenBasedRememberMeServices tokenBasedservice = new PersistentTokenBasedRememberMeServices(
//                REMEMBER_ME_PARAMETER, userDetailsService, tokenRepository);
//        return tokenBasedservice;
//    }

//    @Bean
//    public AuthenticationTrustResolver getAuthenticationTrustResolver() {
//        return new AuthenticationTrustResolverImpl();
//    }

}
