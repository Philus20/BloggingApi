package com.example.BloggingApi.Application.AOP;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PerformanceMonitoringAspect {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceMonitoringAspect.class);

    // Pointcut: all classes in Queries package
    @Pointcut("within(com.example.BloggingApi.Application.Queries..*)")
    public void monitoredMethods() {}

    // Around advice
    @Around("monitoredMethods()")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        // Run the actual method
        Object result = joinPoint.proceed();

        long end = System.currentTimeMillis();
        long duration = end - start;

        logger.info("⏱️ Method {} executed in {} ms", joinPoint.getSignature(), duration);

        return result;
    }
}

