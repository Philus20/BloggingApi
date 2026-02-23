package com.example.BloggingApi.AOP;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Order(1) 
public class CacheLoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(CacheLoggingAspect.class);

   
    private static final long CACHE_HIT_THRESHOLD_MS = 2;

    @Pointcut("@annotation(org.springframework.cache.annotation.Cacheable)")
    public void cacheableMethods() {
    }

    @Around("cacheableMethods()")
    public Object logCacheAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getMethod().getName();
        String cacheName = getCacheName(signature);
        Object[] args = joinPoint.getArgs();
        String keyInfo = args.length > 0 ? String.valueOf(args[0]) : "N/A";

        logger.info(" Before cache lookup - method: {} | cache: {} | key: {}",
                methodName, cacheName, keyInfo);

        long start = System.nanoTime();
        Object result = joinPoint.proceed();
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        logger.info("After cache - method: {} completed in {} ms", methodName, elapsedMs);

        if (elapsedMs < CACHE_HIT_THRESHOLD_MS) {
            logger.info(" Fetched from CACHE in {} ms | method: {} | cache: {} | key: {}",
                    elapsedMs, methodName, cacheName, keyInfo);
        } else {
            logger.info("Cache MISS - fetched from source in {} ms (next call will be from cache) | method: {} | key: {}",
                    elapsedMs, methodName, keyInfo);
        }

        return result;
    }

    private String getCacheName(MethodSignature signature) {
        Cacheable cacheable = signature.getMethod().getAnnotation(Cacheable.class);
        if (cacheable != null && cacheable.value().length > 0) {
            return Arrays.toString(cacheable.value());
        }
        return "unknown";
    }
}
