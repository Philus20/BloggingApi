package com.example.BloggingApi.AOP;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Aspect
@Component
@Order(1)
public class CacheLoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(CacheLoggingAspect.class);

    private static final long CACHE_HIT_THRESHOLD_MS = 2;

    private final Map<String, CacheStats> statsMap = new ConcurrentHashMap<>();

    @Pointcut("@annotation(org.springframework.cache.annotation.Cacheable)")
    public void cacheableMethods() {}

    @Pointcut("@annotation(org.springframework.cache.annotation.CacheEvict)")
    public void cacheEvictMethods() {}

    @Around("cacheableMethods()")
    public Object logCacheAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getMethod().getName();
        String cacheName = getCacheName(signature);
        String keyInfo = extractKey(joinPoint.getArgs());

        long start = System.nanoTime();
        Object result = joinPoint.proceed();
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        CacheStats stats = statsMap.computeIfAbsent(cacheName, k -> new CacheStats());

        if (elapsedMs < CACHE_HIT_THRESHOLD_MS) {
            stats.recordHit(elapsedMs);
            logger.info("CACHE HIT  | cache: {} | key: {} | method: {} | {} ms",
                    cacheName, keyInfo, methodName, elapsedMs);
        } else {
            stats.recordMiss(elapsedMs);
            logger.info("CACHE MISS | cache: {} | key: {} | method: {} | {} ms",
                    cacheName, keyInfo, methodName, elapsedMs);
        }

        return result;
    }

    @Around("cacheEvictMethods()")
    public Object logCacheEviction(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getMethod().getName();
        String cacheName = getEvictCacheName(signature);

        Object result = joinPoint.proceed();

        CacheStats stats = statsMap.computeIfAbsent(cacheName, k -> new CacheStats());
        stats.recordEviction();
        logger.info("CACHE EVICT | cache: {} | method: {}", cacheName, methodName);

        return result;
    }

    @Scheduled(fixedRate = 300_000)
    public void logPerformanceReport() {
        if (statsMap.isEmpty()) {
            return;
        }

        StringBuilder report = new StringBuilder("\n===== Cache Performance Report =====\n");

        statsMap.forEach((cache, stats) -> {
            long hits = stats.hitCount.get();
            long misses = stats.missCount.get();
            long evictions = stats.evictionCount.get();
            long total = hits + misses;
            double hitRate = total > 0 ? (hits * 100.0 / total) : 0;
            double avgHitMs = hits > 0 ? (stats.totalHitTimeMs.get() / (double) hits) : 0;
            double avgMissMs = misses > 0 ? (stats.totalMissTimeMs.get() / (double) misses) : 0;

            report.append(String.format(
                    "  [%s] hits: %d | misses: %d | hit rate: %.1f%% | avg hit: %.1f ms | avg miss: %.1f ms | evictions: %d%n",
                    cache, hits, misses, hitRate, avgHitMs, avgMissMs, evictions));
        });

        report.append("====================================");
        logger.info(report.toString());
    }

    private String getCacheName(MethodSignature signature) {
        Cacheable cacheable = signature.getMethod().getAnnotation(Cacheable.class);
        if (cacheable != null && cacheable.value().length > 0) {
            return Arrays.toString(cacheable.value());
        }
        return "unknown";
    }

    private String getEvictCacheName(MethodSignature signature) {
        CacheEvict cacheEvict = signature.getMethod().getAnnotation(CacheEvict.class);
        if (cacheEvict != null && cacheEvict.value().length > 0) {
            return Arrays.toString(cacheEvict.value());
        }
        return "unknown";
    }

    private String extractKey(Object[] args) {
        return args.length > 0 ? String.valueOf(args[0]) : "N/A";
    }

    private static class CacheStats {
        final AtomicLong hitCount = new AtomicLong();
        final AtomicLong missCount = new AtomicLong();
        final AtomicLong evictionCount = new AtomicLong();
        final AtomicLong totalHitTimeMs = new AtomicLong();
        final AtomicLong totalMissTimeMs = new AtomicLong();

        void recordHit(long ms) {
            hitCount.incrementAndGet();
            totalHitTimeMs.addAndGet(ms);
        }

        void recordMiss(long ms) {
            missCount.incrementAndGet();
            totalMissTimeMs.addAndGet(ms);
        }

        void recordEviction() {
            evictionCount.incrementAndGet();
        }
    }
}
