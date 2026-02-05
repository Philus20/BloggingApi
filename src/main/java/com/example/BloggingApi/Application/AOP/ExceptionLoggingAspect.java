package com.example.BloggingApi.Application.AOP;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**

 * @author BloggingApi Team
 * @version 1.0
 * @since 1.0
 * @see org.aspectj.lang.annotation.AfterThrowing
 * @see org.aspectj.lang.JoinPoint
 */
@Aspect
@Component
public class ExceptionLoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionLoggingAspect.class);

    /**
     * Pointcut that matches all methods in the Queries package.
     * 
     * <p>This pointcut targets query operations that retrieve data from the system.
     * Query methods are expected to be read-only and return data without modifying state.</p>
     */
    @Pointcut("execution(* com.example.BloggingApi.Application.Queries.*.*(..))")
    public void queryMethods() {}

    /**
     * Pointcut that matches all methods in the Commands package.
     * 
     * <p>This pointcut targets command operations that modify system state.
     * Command methods typically perform create, update, or delete operations.</p>
     */
    @Pointcut("execution(* com.example.BloggingApi.Application.Commands.*.*(..))")
    public void commandMethods() {}

    /**
     * Pointcut that combines both query and command methods.
     * 
     * <p>This pointcut provides a convenient way to target all application service
     * methods regardless of whether they are queries or commands.</p>
     */
    @Pointcut("queryMethods() || commandMethods()")
    public void applicationServiceMethods() {}

    /**
     * Advice that logs exceptions thrown by any application service method.
     * 
     * <p>This method is triggered whenever an exception is thrown by any method
     * matching the {@code applicationServiceMethods()} pointcut. It logs comprehensive
     * exception information including the method signature, exception message,
     * method parameters, and stack trace details.</p>
     * 
     * <p><strong>Logged Information:</strong></p>
     * <ul>
     *   <li>Class and method name where exception occurred</li>
     *   <li>Exception message and type</li>
     *   <li>Method parameters for debugging context</li>
     *   <li>Stack trace information and root cause</li>
     *   <li>Critical exception flagging</li>
     * </ul>
     * 
     * @param joinPoint the join point representing the method where exception occurred
     * @param exception the exception that was thrown
     * @see org.aspectj.lang.JoinPoint
     */
    @AfterThrowing(pointcut = "applicationServiceMethods()", throwing = "exception")
    public void logApplicationExceptions(JoinPoint joinPoint, Exception exception) {
        String methodName = joinPoint.getSignature().toShortString();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        
        logger.error("💥 EXCEPTION in {}.{}: {}", className, methodName, exception.getMessage());
        logger.error("📍 Exception location: {}", joinPoint.getSignature().toLongString());
        
        logExceptionDetails(exception);
        logMethodContext(joinPoint);
        
        if (isCriticalException(exception)) {
            logger.error("🚨 CRITICAL EXCEPTION detected in {}: {}", className, exception.getClass().getSimpleName());
        }
    }

    /**
     * Advice that logs exceptions thrown by query methods specifically.
     * 
     * <p>This method provides specialized logging for query operations,
     * including query parameter context. This helps in debugging data retrieval
     * issues and understanding the context of query failures.</p>
     * 
     * @param joinPoint the join point representing the query method where exception occurred
     * @param exception the exception that was thrown
     */
    @AfterThrowing(pointcut = "queryMethods()", throwing = "exception")
    public void logQueryExceptions(JoinPoint joinPoint, Exception exception) {
        String methodName = joinPoint.getSignature().toShortString();
        logger.error("🔍 QUERY EXCEPTION - {} failed: {}", methodName, exception.getMessage());
        logQueryContext(joinPoint);
    }

    /**
     * Advice that logs exceptions thrown by command methods specifically.
     * 
     * <p>This method provides specialized logging for command operations,
     * including command input context. This helps in debugging state modification
     * issues and understanding the context of command failures.</p>
     * 
     * @param joinPoint the join point representing the command method where exception occurred
     * @param exception the exception that was thrown
     */
    @AfterThrowing(pointcut = "commandMethods()", throwing = "exception")
    public void logCommandExceptions(JoinPoint joinPoint, Exception exception) {
        String methodName = joinPoint.getSignature().toShortString();
        logger.error("⚡ COMMAND EXCEPTION - {} failed: {}", methodName, exception.getMessage());
        logCommandContext(joinPoint);
    }

    /**
     * Logs detailed exception information including type, root cause, and stack trace.
     * 
     * <p>This helper method extracts and logs key exception details to aid in debugging.
     * It includes the exception type, root cause if available, and the first stack
     * trace element to provide context about where the exception originated.</p>
     * 
     * @param exception the exception to analyze and log
     */
    private void logExceptionDetails(Exception exception) {
        logger.error("🔍 Exception type: {}", exception.getClass().getSimpleName());
        
        if (exception.getCause() != null) {
            logger.error("🔗 Root cause: {}", exception.getCause().getMessage());
        }
        
        StackTraceElement[] stackTrace = exception.getStackTrace();
        if (stackTrace.length > 0) {
            StackTraceElement firstElement = stackTrace[0];
            logger.error("📍 First stack trace: {}.{}:{}",
                firstElement.getClassName(),
                firstElement.getMethodName(),
                firstElement.getLineNumber());
        }
    }

    /**
     * Logs method context including parameter values for debugging.
     * 
     * <p>This helper method logs all parameters passed to the method where the
     * exception occurred. Parameter values are truncated if they are too long
     * to prevent log flooding with large objects.</p>
     * 
     * @param joinPoint the join point representing the method where exception occurred
     */
    private void logMethodContext(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 0) {
            logger.error("📋 Method arguments ({}):", args.length);
            for (int i = 0; i < args.length; i++) {
                String argValue = args[i] != null ? args[i].toString() : "null";
                if (argValue.length() > 100) {
                    argValue = argValue.substring(0, 100) + "...";
                }
                logger.error("   arg[{}]: {}", i, argValue);
            }
        }
    }

    /**
     * Logs query-specific parameter context.
     * 
     * <p>This helper method formats and logs parameters specifically for query
     * operations, providing context for debugging data retrieval issues.</p>
     * 
     * @param joinPoint the join point representing the query method
     */
    private void logQueryContext(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 0) {
            logger.error("🔍 Query parameters: {}", formatQueryParameters(args));
        }
    }

    /**
     * Logs command-specific parameter context.
     * 
     * <p>This helper method formats and logs parameters specifically for command
     * operations, providing context for debugging state modification issues.
     * Parameters are truncated to prevent excessive log output.</p>
     * 
     * @param joinPoint the join point representing the command method
     */
    private void logCommandContext(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 0) {
            logger.error("⚡ Command parameters: {}", formatCommandParameters(args));
        }
    }

    /**
     * Formats query parameters for logging.
     * 
     * <p>This helper method creates a readable string representation of query
     * parameters for logging purposes.</p>
     * 
     * @param args the method arguments to format
     * @return formatted string representation of the parameters
     */
    private String formatQueryParameters(Object[] args) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append("param[").append(i).append("]=");
            if (args[i] != null) {
                sb.append(args[i].toString());
            } else {
                sb.append("null");
            }
        }
        return sb.toString();
    }

    /**
     * Formats command parameters for logging.
     * 
     * <p>This helper method creates a readable string representation of command
     * parameters for logging purposes. Long parameter values are truncated to
     * prevent excessive log output.</p>
     * 
     * @param args the method arguments to format
     * @return formatted string representation of the parameters
     */
    private String formatCommandParameters(Object[] args) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append("input[").append(i).append("]=");
            if (args[i] != null) {
                String argString = args[i].toString();
                if (argString.length() > 50) {
                    sb.append(argString.substring(0, 50)).append("...");
                } else {
                    sb.append(argString);
                }
            } else {
                sb.append("null");
            }
        }
        return sb.toString();
    }

    /**
     * Determines if an exception should be considered critical.
     * 
     * <p>This helper method identifies critical exceptions based on their type.
     * Critical exceptions include null pointer exceptions, entity not found
     * exceptions, duplicate entity exceptions, and runtime exceptions.</p>
     * 
     * @param exception the exception to evaluate
     * @return true if the exception is considered critical, false otherwise
     */
    private boolean isCriticalException(Exception exception) {
        String exceptionName = exception.getClass().getSimpleName();
        return exceptionName.contains("Null") || 
               exceptionName.contains("NotFound") || 
               exceptionName.contains("Duplicate") ||
               exceptionName.contains("Runtime");
    }
}
