package com.github.nikit.cpp.security;

import com.github.nikit.cpp.Constants;
import com.github.nikit.cpp.entity.jpa.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.data.repository.query.SecurityEvaluationContextExtension;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;

/**
 * http://websystique.com/springmvc/spring-mvc-4-and-spring-security-4-integration-example/
 * Created by nik on 08.06.17.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    public static final String API_LOGIN_URL = "/api/login";
    public static final String API_LOGOUT_URL = "/api/logout";

    public static final String USERNAME_PARAMETER = "username";
    public static final String PASSWORD_PARAMETER = "password";
    public static final String REMEMBER_ME_PARAMETER = "remember-me";


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

    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        return CookieCsrfTokenRepository.withHttpOnlyFalse();
    }

    @Bean
    public RESTAuthenticationLogoutSuccessHandler restAuthenticationLogoutSuccessHandler() {
        return new RESTAuthenticationLogoutSuccessHandler(csrfTokenRepository());
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // https://dzone.com/articles/spring-security-4-authenticate-and-authorize-users
        // http://www.programming-free.com/2015/09/spring-security-password-encryption.html
        auth.authenticationProvider(authenticationProvider());
    }

    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // default strength is BCrypt.GENSALT_DEFAULT_LOG2_ROUNDS=10
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/favicon.ico", "/static/**", Constants.Uls.API+"/**").permitAll();
        http.authorizeRequests()
                .antMatchers(Constants.Uls.API+Constants.Uls.ADMIN+"/**").hasAuthority(UserRole.ROLE_ADMIN.name());

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
        http.formLogin()
                .loginPage(API_LOGIN_URL).usernameParameter(USERNAME_PARAMETER).passwordParameter(PASSWORD_PARAMETER).permitAll()
                .successHandler(authenticationSuccessHandler)
                .failureHandler(authenticationFailureHandler)

        .and().logout().logoutUrl(API_LOGOUT_URL).logoutSuccessHandler(authenticationLogoutSuccessHandler).permitAll();


//        http.rememberMe().rememberMeParameter(REMEMBER_ME_PARAMETER).tokenRepository(tokenRepository)
//                .tokenValiditySeconds(86400);

    }

    // https://docs.spring.io/spring-security/site/docs/current/reference/htmlsingle/#data-configuration
    @Bean
    public SecurityEvaluationContextExtension securityEvaluationContextExtension() {
        return new SecurityEvaluationContextExtension();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(blogUserDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
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
