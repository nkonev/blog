package com.github.nkonev.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.nkonev.dto.UserAccountDetailsDTO;
import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class AppConfig {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ServerProperties serverProperties;

    @Value("${custom.redirect.additional.http.port:8080}")
    private int fromHttpPost;

    @Value("${custom.redirect.to.http.port:443}")
    private int toHttpsPort;

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
    public EmbeddedServletContainerFactory servletContainer() {
        if (serverProperties.getSsl()!=null && serverProperties.getSsl().isEnabled()) {
            TomcatEmbeddedServletContainerFactory tomcat = new TomcatEmbeddedServletContainerFactory() {
                @Override
                protected void postProcessContext(Context context) {
                    SecurityConstraint securityConstraint = new SecurityConstraint();
                    securityConstraint.setUserConstraint("CONFIDENTIAL");
                    SecurityCollection collection = new SecurityCollection();
                    collection.addPattern("/*");
                    securityConstraint.addCollection(collection);
                    context.addConstraint(securityConstraint);
                }
            };
            tomcat.addAdditionalTomcatConnectors(additionalRedirectHttpToHttpsConnector(fromHttpPost, toHttpsPort));
            return tomcat;
        } else {
            return new TomcatEmbeddedServletContainerFactory();
        }
    }

    private Connector additionalRedirectHttpToHttpsConnector(int httpPort, int redirectHttpsPort) {
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setScheme("http");
        connector.setPort(httpPort);
        connector.setRedirectPort(redirectHttpsPort);
        return connector;
    }
}
