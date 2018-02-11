package com.github.nkonev.blog.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.nkonev.blog.dto.UserAccountDetailsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.annotation.PostConstruct;
import javax.persistence.EntityManagerFactory;
import java.io.File;

@Configuration
public class AppConfig {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ServerProperties serverProperties;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

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

    @Bean
    public ServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();

        final File baseDir = serverProperties.getTomcat().getBasedir();
        if (baseDir!=null) {
            File docRoot = new File(baseDir, "document-root");
            docRoot.mkdirs();
            tomcat.setDocumentRoot(docRoot);
        }

        return tomcat;
    }
}
