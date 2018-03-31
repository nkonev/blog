package com.github.nkonev.blog.services;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.ResolvableType;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContextProvider implements ApplicationContextAware {
    private static ApplicationContext context;

    public static ApplicationContext getApplicationContext() {
        return context;
    }

    public static RedisTemplate<String, String> getHtmlCache() {
        String[] beanNamesForType = context.getBeanNamesForType(ResolvableType.forClassWithGenerics(RedisTemplate.class, String.class, String.class));
        RedisTemplate<String, String> redisTemplate = (RedisTemplate<String, String>) context.getBean(beanNamesForType[0]);
        return redisTemplate;
    }


    @Override
    public void setApplicationContext(ApplicationContext ac) throws BeansException {
        context = ac;
    }
}
