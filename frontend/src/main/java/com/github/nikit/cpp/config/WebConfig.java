package com.github.nikit.cpp.config;

import com.github.nikit.cpp.Constants;
import org.springframework.beans.BeansException;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.web.servlet.ErrorPage;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

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
     * Настраиваем NotFoundFallback для работы History API роутинга, т. е. для урлов вида http://127.0.0.1:8080/user/3
     * В противном случае урлы будут вида http://127.0.0.1:8080#/user/3
     * @return
     */
    @Bean
    public EmbeddedServletContainerCustomizer containerCustomizer(){
        return new NotFoundFallback();
    }

    private static class NotFoundFallback implements EmbeddedServletContainerCustomizer {
        @Override
        public void customize(ConfigurableEmbeddedServletContainer container){
            container.addErrorPages(new ErrorPage(HttpStatus.NOT_FOUND, Constants.Uls.ROOT));
        }
    }

}
