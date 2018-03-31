package com.github.nkonev.blog.config;

import com.github.nkonev.rendertron.Constants;
import com.github.nkonev.rendertron.EventHandler;
import com.github.nkonev.rendertron.SeoFilter;
import org.apache.http.HttpResponse;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.concurrent.TimeUnit;

import static com.github.nkonev.blog.services.ApplicationContextProvider.getHtmlCache;

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

    public static class CacheEventHandler implements EventHandler {

        @Override
        public String beforeRender(HttpServletRequest clientRequest) {
            final String key = getKey(clientRequest);
            return getRedisTemplate().opsForValue().get(key);
        }

        @Override
        public String afterRender(HttpServletRequest clientRequest, HttpServletResponse clientResponse, HttpResponse renderServiceResponse, String responseHtml) {
            final String key = getKey(clientRequest);
            getRedisTemplate().opsForValue().set(key, responseHtml);
            getRedisTemplate().expire(key, 2, TimeUnit.HOURS);
            return responseHtml;
        }

        private RedisTemplate<String, String> getRedisTemplate() {
            return getHtmlCache();
        }

        private String getKey(HttpServletRequest clientRequest) {
            return "rendertron_" + clientRequest.getRequestURI() + nullToEmpty(clientRequest.getQueryString());
        }

        private String nullToEmpty(String s){
            if (s == null) {
                return "";
            } else {
                return s;
            }
        }

        @Override
        public void destroy() { }
    }

    @ConditionalOnProperty("custom.prerender.enable")
    @Bean
    public FilterRegistrationBean prerenderFilterRegistration(PrerenderConfig prerenderConfig) {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new SeoFilter());
        registration.addUrlPatterns("/*");
        final String baseUrl = customConfig.getBaseUrl();
        if (!StringUtils.isEmpty(baseUrl)) {
            registration.addInitParameter(Constants.InitFilterParams.FORWARDED_URL_PREFIX, baseUrl);
        }
        LOGGER.info("Prerender configuration: {}", prerenderConfig);

        if (!StringUtils.isEmpty(prerenderConfig.getForwardedURLPrefix())){
            registration.addInitParameter(Constants.InitFilterParams.FORWARDED_URL_PREFIX, prerenderConfig.getForwardedURLPrefix());
        }
        registration.addInitParameter(Constants.InitFilterParams.CRAWLER_USER_AGENTS, prerenderConfig.getCrawlerUserAgents());
        registration.addInitParameter(Constants.InitFilterParams.RENDERTRON_SERVICE_URL, prerenderConfig.getPrerenderServiceUrl());
        registration.addInitParameter(Constants.InitFilterParams.RENDERTRON_EVENT_HANDLER, CacheEventHandler.class.getName());

        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

}
