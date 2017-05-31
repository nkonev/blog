package com.github.nikit.cpp.config;

import com.github.nikit.cpp.Constants;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

}
