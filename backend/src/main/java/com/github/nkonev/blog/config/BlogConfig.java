package com.github.nkonev.blog.config;

import ch.qos.logback.access.servlet.TeeFilter;
import ch.qos.logback.access.tomcat.LogbackValve;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.nkonev.blog.dto.UserAccountDetailsDTO;
import com.github.nkonev.blog.utils.ResourceUtils;
import org.apache.catalina.Context;
import org.apache.catalina.Valve;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;

@Configuration
public class BlogConfig {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ServerProperties serverProperties;

    @Value("classpath:/static/git.json")
    private Resource resource;

    private static final Logger LOGGER = LoggerFactory.getLogger(BlogConfig.class);

    @PostConstruct
    public void pc() throws Exception {
        SimpleModule rejectUserAccountDetailsDTOModule = new SimpleModule("Reject serialize UserAccountDetailsDTO");
        rejectUserAccountDetailsDTOModule.addSerializer(UserAccountDetailsDTO.class, new JsonSerializer<UserAccountDetailsDTO>() {
            @Override
            public void serialize(UserAccountDetailsDTO value, JsonGenerator jgen, SerializerProvider provider){
                throw new RuntimeException("You shouldn't to serialize UserAccountDetailsDTO");
            }
        });
        objectMapper.registerModule(rejectUserAccountDetailsDTOModule);
    }

    // see https://github.com/spring-projects/spring-boot/issues/14302#issuecomment-418712080 if you want to customize management tomcat
    @Bean
    public ServletWebServerFactory servletContainer(Valve... valves) {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
        tomcat.addContextValves(valves);

        final File baseDir = serverProperties.getTomcat().getBasedir();
        if (baseDir!=null) {
            File docRoot = new File(baseDir, "document-root");
            docRoot.mkdirs();
            tomcat.setDocumentRoot(docRoot);
        }

        tomcat.setProtocol("org.apache.coyote.http11.Http11Nio2Protocol");

        return tomcat;
    }

    @PostConstruct
    public void printVersion() throws IOException {
        if(resource.exists()){
            String text = ResourceUtils.stringFromResource(resource);
            LOGGER.info("Version {}", text);
        }
    }
}
