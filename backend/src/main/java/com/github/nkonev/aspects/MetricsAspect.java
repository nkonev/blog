package com.github.nkonev.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.lang.reflect.Method;

// TODO see MethodTimer

@Aspect
@ControllerAdvice
public class MetricsAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetricsAspect.class);

    @Pointcut(
            "@annotation(org.springframework.web.bind.annotation.GetMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PatchMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.RequestMapping)"
    )
    private void interested() {}

    @Around("interested()")
    public Object doBasicProfiling(ProceedingJoinPoint pjp) throws Throwable {
        // start stopwatch
        org.aspectj.lang.Signature signature = pjp.getSignature();
        String clazz = signature.getDeclaringTypeName();
        String method = signature.getName();
        LOGGER.info("Start class={} method={}", clazz, method);

        Object retVal = pjp.proceed();

        // stop stopwatch
        LOGGER.info("Complete class={} method={}", clazz, method);

        return retVal;
    }
}
