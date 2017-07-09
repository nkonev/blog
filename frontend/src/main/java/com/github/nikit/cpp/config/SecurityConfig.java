package com.github.nikit.cpp.config;

import com.github.nikit.cpp.Constants;
import com.github.nikit.cpp.config.security.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;

/**
 * Created by nik on 08.06.17.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private RESTAuthenticationEntryPoint authenticationEntryPoint;
    @Autowired
    private RESTAuthenticationFailureHandler authenticationFailureHandler;
    @Autowired
    private RESTAuthenticationSuccessHandler authenticationSuccessHandler;
    @Autowired
    private RESTAuthenticationLogoutSuccessHandler authenticationLogoutSuccessHandler;

    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        return CookieCsrfTokenRepository.withHttpOnlyFalse();
    }

    @Bean
    public RESTAuthenticationLogoutSuccessHandler restAuthenticationLogoutSuccessHandler() {
        return new RESTAuthenticationLogoutSuccessHandler(csrfTokenRepository());
    }

    @Override
    protected void configure(AuthenticationManagerBuilder builder) throws Exception {
        builder.inMemoryAuthentication().withUser("user").password("user").roles("USER").and().withUser("admin")
                .password("admin").roles("ADMIN");
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
                .loginPage("/api/login").permitAll()
                .successHandler(authenticationSuccessHandler)
                .failureHandler(authenticationFailureHandler)

        .and().logout().logoutUrl("/api/logout").logoutSuccessHandler(authenticationLogoutSuccessHandler).permitAll()
        ;
    }
}
