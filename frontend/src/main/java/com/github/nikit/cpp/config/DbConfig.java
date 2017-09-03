package com.github.nikit.cpp.config;

import com.github.nikit.cpp.listener.hibernate.BlogPersistListener;
import org.hibernate.SessionFactory;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.internal.SessionFactoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManagerFactory;

@Configuration
@EntityScan(basePackages = "com.github.nikit.cpp.entity.jpa")
@EnableJpaRepositories(basePackages = "com.github.nikit.cpp.repo.jpa")
@EnableTransactionManagement
public class DbConfig {

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private BlogPersistListener blogPersistListener;


    @PostConstruct
    public void registerListeners() {
        // https://stackoverflow.com/questions/27570641/no-transactional-entitymanager-available-in-postconstruct/27571252#27571252
        // http://docs.jboss.org/hibernate/orm/4.3/topical/html/registries/ServiceRegistries.html
        // https://n1njahacks.wordpress.com/2016/10/07/jpa-callbacks-with-hibernates-sessionfactory-and-no-entitymanager/
        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        EventListenerRegistry registry = ((SessionFactoryImpl) sessionFactory).getServiceRegistry().getService(EventListenerRegistry.class);
        registry.getEventListenerGroup(EventType.PERSIST).appendListener(blogPersistListener);
    }
}
