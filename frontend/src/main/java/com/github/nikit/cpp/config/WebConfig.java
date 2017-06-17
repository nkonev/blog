package com.github.nikit.cpp.config;

import com.github.nikit.cpp.Constants;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.web.servlet.ErrorPage;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ITemplateResolver;
import java.io.File;
import java.nio.charset.StandardCharsets;

@Configuration
public class WebConfig extends WebMvcConfigurerAdapter implements ApplicationContextAware {

    @Value(Constants.FRONTEND_RESOURCES)
    private String filesystemResources;

    private ApplicationContext applicationContext;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String staticStr = filesystemResources + "/static/";
        if (new File (staticStr).exists()){
            registry.addResourceHandler("/static/**").addResourceLocations(
                    "file:" + staticStr
            );
        } else {
            registry.addResourceHandler("/static/**").addResourceLocations(
                    "classpath:/static/"
            );
        }
    }

    @Bean
    public ViewResolver viewResolver() {
        ThymeleafViewResolver resolver = new ThymeleafViewResolver();
        resolver.setTemplateEngine(templateEngine());
        resolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        return resolver;
    }

    /**
     * При помощи этого резолвера
     * Thymeleaf будет сначала искать в файловой системе,
     * что позволит не копировать шаблоны в target/... (это является classpath)
     * @return
     */
    @Bean
    public ITemplateResolver templateResolver() {
        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
        resolver.setApplicationContext(applicationContext);
        final String templates = filesystemResources + "/templates/";
        if (new File(templates).exists()) {
            resolver.setPrefix("file:" + templates);
            resolver.setCacheable(false);
        } else {
            resolver.setPrefix("classpath:/templates/");
            resolver.setCacheable(true);
        }
        resolver.setOrder(1);
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        return resolver;
    }

    @Bean
    public SpringTemplateEngine templateEngine() {
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setEnableSpringELCompiler(true);
        engine.addTemplateResolver(templateResolver());
        return engine;
    }


    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // registry.addViewController("/").setViewName("index"); only for old index.html
    }

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
            container.addErrorPages(new ErrorPage(HttpStatus.NOT_FOUND, "/"));
        }
    }

}
