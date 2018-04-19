package com.github.nkonev.blog.config;

import com.github.nkonev.blog.services.RendertronInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;

@Configuration
public class WebConfig implements WebMvcConfigurer, ApplicationContextAware {

    private ApplicationContext applicationContext;

    private static final Logger LOGGER = LoggerFactory.getLogger(WebConfig.class);

    @Autowired
    private CustomConfig customConfig;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Autowired
    private Environment environment;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (environment.getProperty(com.github.nkonev.blog.Constants.CUSTOM_PRERENDER_ENABLE, Boolean.class, false)) {
            registry.addInterceptor(applicationContext.getBean(RendertronInterceptor.class));
        }
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

    @PostConstruct
    public void log(){
        LOGGER.info("Base url: {}", customConfig.getBaseUrl());
    }
}
