package com.github.nkonev.config;

import com.github.greengerong.PreRenderSEOFilter;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.Map;

@Configuration
public class WebConfig extends WebMvcConfigurerAdapter implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

    }

    /**
     *  https://spring.io/blog/2013/05/11/content-negotiation-using-spring-mvc
     */
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
                // we disable file extension type resolving
                // because if it will be enabled so whenBlogExceptionHandler will return 404 and json error
                // and http://127.0.0.1:8080/api/user/avatar/3/avatar.png was requested
                // so spring won't negotiates between image/png and application/json
                // so 500 will be returned
                .favorPathExtension(false)
                .defaultContentType(MediaType.APPLICATION_JSON)
        ;
    }

    @ConditionalOnProperty("custom.prerender.enable")
    @Bean
    public FilterRegistrationBean someFilterRegistration(PrerenderConfig prerenderConfig) {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new PreRenderSEOFilter());
        registration.addUrlPatterns("/*");
        for (Map.Entry<String, String> e : prerenderConfig.getPrerender().entrySet()) {
            registration.addInitParameter(e.getKey(), e.getValue());
        }
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}
