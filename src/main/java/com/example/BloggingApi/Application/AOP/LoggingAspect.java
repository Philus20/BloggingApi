package com.example.BloggingApi.Application.AOP;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Pointcut("within(@org.springframework.stereotype.Service *)")
    public void serviceLayer() {
    }


    @Before("serviceLayer()")
    public void logBeforeServiceMethods(JoinPoint joinPoint) {
        logger.info("A method in the service layer is about to be executed.");
        logger.info("➡️ Entering method: {}", joinPoint.getSignature());

    }


}