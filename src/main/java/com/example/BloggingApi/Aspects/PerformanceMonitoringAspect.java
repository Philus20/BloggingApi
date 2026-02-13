package com.example.BloggingApi.Aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * AOP aspect for performance measurement of service layer methods.
 * Logs execution time for CRUD and search/analytics operations.
 */
@Component
@Aspect
public class PerformanceMonitoringAspect {

    private static final Logger log = LoggerFactory.getLogger(PerformanceMonitoringAspect.class);

    private static final String SERVICE_POINTCUT =
            "execution(* com.example.BloggingApi.Services..*(..))";

    @Around(SERVICE_POINTCUT)
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        String method = joinPoint.getSignature().toShortString();
        Object result;
        try {
            result = joinPoint.proceed();
            return result;
        } finally {
            long duration = System.currentTimeMillis() - start;
            log.info("[Performance] {} executed in {} ms", method, duration);
            if (duration > 500) {
                log.warn("[Performance] Slow operation: {} took {} ms", method, duration);
            }
        }
    }
}
