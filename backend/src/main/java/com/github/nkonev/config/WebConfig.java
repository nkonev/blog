package com.github.nkonev.config;

import com.github.greengerong.PreRenderConstants;
import com.github.greengerong.PreRenderSEOFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;
import java.util.Map;

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

    @PostConstruct
    public void log(){
        LOGGER.info("Base url: {}", customConfig.getBaseUrl());
    }

    @ConditionalOnProperty("custom.prerender.enable")
    @Bean
    public FilterRegistrationBean prerenderFilterRegistration(PrerenderConfig prerenderConfig) {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new PreRenderSEOFilter());
        registration.addUrlPatterns("/*");
        final String baseUrl = customConfig.getBaseUrl();
        if (!StringUtils.isEmpty(baseUrl)) {
            registration.addInitParameter(PreRenderConstants.InitFilterParams.FORWARDED_URL_PREFIX, baseUrl);
        }
        LOGGER.info("Prerender configuration: {}", prerenderConfig);

        if (!StringUtils.isEmpty(prerenderConfig.getForwardedURLPrefix())){
            registration.addInitParameter(PreRenderConstants.InitFilterParams.FORWARDED_URL_PREFIX, prerenderConfig.getForwardedURLPrefix());
        }
        registration.addInitParameter(PreRenderConstants.InitFilterParams.CRAWLER_USER_AGENTS, prerenderConfig.getCrawlerUserAgents());
        registration.addInitParameter(PreRenderConstants.InitFilterParams.PRERENDER_SERVICE_URL, prerenderConfig.getPrerenderServiceUrl());

        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

}
