package com.github.nikit.cpp.config;

import com.github.nikit.cpp.Constants;
import com.github.nikit.cpp.config.security.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.encoding.MessageDigestPasswordEncoder;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.data.repository.query.SecurityEvaluationContextExtension;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import javax.sql.DataSource;

/**
 * Created by nik on 08.06.17.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    public static final String API_LOGIN_URL = "/api/login";
    public static final String API_LOGOUT_URL = "/api/logout";
    @Autowired
    private RESTAuthenticationEntryPoint authenticationEntryPoint;
    @Autowired
    private RESTAuthenticationFailureHandler authenticationFailureHandler;
    @Autowired
    private RESTAuthenticationSuccessHandler authenticationSuccessHandler;
    @Autowired
    private RESTAuthenticationLogoutSuccessHandler authenticationLogoutSuccessHandler;

    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_USER = "ROLE_USER";

    @Autowired
    @Qualifier(value = DbConfig.AUTH_DATASOURCE_BEAN_NAME)
    private DataSource dataSource;

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
//        auth
//                .eraseCredentials(true) // erase password from the Authentication. https://docs.spring.io/spring-security/site/docs/current/reference/htmlsingle/#core-services-erasing-credentials
//                .inMemoryAuthentication().withUser("user").password("user").roles("USER").and().withUser("admin")
//                .password("admin").roles("ADMIN");

        // https://dzone.com/articles/spring-security-4-authenticate-and-authorize-users
        // http://www.programming-free.com/2015/09/spring-security-password-encryption.html
        auth.jdbcAuthentication()
                .dataSource(dataSource)
                .passwordEncoder(passwordencoder())
        ;

    }

    public MessageDigestPasswordEncoder passwordencoder(){
        return new ShaPasswordEncoder();
    }


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests().antMatchers("/favicon.ico", "/static/**", Constants.Uls.API_PUBLIC+"/**")
                .permitAll();

        http.authorizeRequests().antMatchers(Constants.Uls.API+"/**").authenticated();
        http.csrf()
                .csrfTokenRepository(csrfTokenRepository());
        http.exceptionHandling().authenticationEntryPoint(authenticationEntryPoint);
        http.formLogin()
                .loginPage(API_LOGIN_URL).permitAll()
                .successHandler(authenticationSuccessHandler)
                .failureHandler(authenticationFailureHandler)

        .and().logout().logoutUrl(API_LOGOUT_URL).logoutSuccessHandler(authenticationLogoutSuccessHandler).permitAll()
        ;
    }

    // https://docs.spring.io/spring-security/site/docs/current/reference/htmlsingle/#data-configuration
    @Bean
    public SecurityEvaluationContextExtension securityEvaluationContextExtension() {
        return new SecurityEvaluationContextExtension();
    }
}
