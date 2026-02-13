package com.example.BloggingApi.Aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;


@Aspect
@Component
public class MonitoringPerformance {


    @Pointcut("execution(* com.example.BloggingApi.Services.UserService.getAllUsers(..))")
    public void monitorPerformanceForUser() {
    }

    @Pointcut("execution(* com.example.BloggingApi.Services.PostService.getAllPosts(..))")
    public void monitorPerformanceForPosts() {
    }


    @Around("monitorPerformanceForUser() || monitorPerformanceForPosts()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object proceed = joinPoint.proceed();
        long executionTime = System.currentTimeMillis() - startTime;
        System.out.println(joinPoint.getSignature() + " executed in " + executionTime + "ms");
        return proceed;


    }

}