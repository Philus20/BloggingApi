package com.example.BloggingApi.Aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * AOP aspect for logging entry and exit of service layer methods.
 * Applies to CRUD and search/analytics methods across User, Post, Comment, Tag, and Review services.
 */
@Component
@Aspect
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    private static final String SERVICE_POINTCUT =
            "execution(* com.example.BloggingApi.Services..*(..))";

    @Before(SERVICE_POINTCUT)
    public void logBefore(JoinPoint joinPoint) {
        String method = joinPoint.getSignature().toShortString();
        log.info("[AOP Before] Entering: {}", method);
    }

    @After(SERVICE_POINTCUT)
    public void logAfter(JoinPoint joinPoint) {
        String method = joinPoint.getSignature().toShortString();
        log.info("[AOP After] Exited: {}", method);
    }

    @Around(SERVICE_POINTCUT)
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().toShortString();
        log.debug("[AOP Around] Invoking: {}", method);
        Object result = joinPoint.proceed();
        log.debug("[AOP Around] Completed: {}", method);
        return result;
    }
}
