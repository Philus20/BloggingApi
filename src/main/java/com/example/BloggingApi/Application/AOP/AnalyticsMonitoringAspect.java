package com.example.BloggingApi.Application.AOP;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**

 * @author BloggingApi Team
 * @version 1.0
 * @since 1.0
 * @see org.aspectj.lang.annotation.Around
 * @see org.aspectj.lang.ProceedingJoinPoint
 */
@Aspect
@Component
public class AnalyticsMonitoringAspect {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsMonitoringAspect.class);

    /**
     * Pointcut that matches all search operations.
     * 
     * <p>This pointcut targets any method with 'search' in the name within the Search classes.
     * It provides comprehensive monitoring for all search-related operations.</p>
     */
    @Pointcut("execution(* com.example.BloggingApi.Application.Queries.Search*.search*(..))")
    public void searchMethods() {}

    /**
     * Pointcut that matches keyword search operations.
     * 
     * <p>This pointcut specifically targets methods that search by keywords,
     * providing specialized analytics for keyword-based searches.</p>
     */
    @Pointcut("execution(* com.example.BloggingApi.Application.Queries.Search*.searchByKeyword(..))")
    public void keywordSearchMethods() {}

    /**
     * Pointcut that matches title search operations.
     * 
     * <p>This pointcut specifically targets methods that search by titles,
     * providing specialized analytics for title-based searches.</p>
     */
    @Pointcut("execution(* com.example.BloggingApi.Application.Queries.Search*.searchByTitle(..))")
    public void titleSearchMethods() {}

    /**
     * Pointcut that matches author search operations.
     * 
     * <p>This pointcut specifically targets methods that search by authors,
     * providing specialized analytics for author-based searches.</p>
     */
    @Pointcut("execution(* com.example.BloggingApi.Application.Queries.Search*.searchByAuthor(..))")
    public void authorSearchMethods() {}

    /**
     * Advice that monitors all search operations with comprehensive analytics.
     * 
     * <p>This {@link Around} advice wraps search method execution to provide detailed
     * analytics including search type detection, query extraction, performance measurement,
     * result count analysis, and slow operation warnings.</p>
     * 
     * <p><strong>Analytics Provided:</strong></p>
     * <ul>
     *   <li>Search type identification (KEYWORD, TITLE, AUTHOR, GENERAL)</li>
     *   <li>Query extraction and truncation for logging</li>
     *   <li>Execution duration measurement</li>
     *   <li>Result count analysis from Page objects</li>
     *   <li>Zero result query warnings</li>
     *   <li>Slow search operation alerts (>500ms)</li>
     * </ul>
     * 
     * @param joinPoint the join point representing the search method being monitored
     * @return the result of the search method execution
     * @throws Throwable if the search method throws an exception
     * @see org.aspectj.lang.annotation.Around
     */
    @Around("searchMethods()")
    public Object monitorSearchOperations(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();

        String searchType = determineSearchType(methodName);
        String searchQuery = extractSearchQuery(args);

        logger.info("🔍 ANALYTICS - Starting {} search: '{}'", searchType, searchQuery);

        try {
            Object result = joinPoint.proceed();
            long end = System.currentTimeMillis();
            long duration = end - start;

            logSearchAnalytics(searchType, searchQuery, duration, result);
            return result;

        } catch (Exception e) {
            long end = System.currentTimeMillis();
            long duration = end - start;
            logger.error("ANALYTICS - {} search failed for query '{}' after {}ms: {}",
                searchType, searchQuery, duration, e.getMessage());
            throw e;
        }
    }

    /**
     * Determines the search type based on method name.
     * 
     * <p>This helper method analyzes the method name to identify the type of search
     * operation being performed. It supports keyword, title, and author searches,
     * with a fallback to general search type.</p>
     * 
     * @param methodName the name of the search method
     * @return the search type (KEYWORD, TITLE, AUTHOR, or GENERAL)
     */
    private String determineSearchType(String methodName) {
        if (methodName.contains("searchByKeyword")) return "KEYWORD";
        if (methodName.contains("searchByTitle")) return "TITLE";
        if (methodName.contains("searchByAuthor")) return "AUTHOR";
        return "GENERAL";
    }

    /**
     * Extracts the search query from method arguments.
     * 
     * <p>This helper method extracts the search query from the first method argument.
     * Long queries are truncated to 50 characters to prevent excessive log output
     * while preserving essential search information.</p>
     * 
     * @param args the method arguments to extract the query from
     * @return the extracted search query, or "N/A" if not available
     */
    private String extractSearchQuery(Object[] args) {
        if (args.length > 0 && args[0] instanceof String) {
            String query = (String) args[0];
            return query.length() > 50 ? query.substring(0, 50) + "..." : query;
        }
        return "N/A";
    }

    /**
     * Logs comprehensive search analytics and metrics.
     * 
     * <p>This helper method logs detailed analytics information for search operations,
     * including search completion, query details, duration, result count analysis,
     * and performance warnings. It attempts to extract result counts from Page objects
     * and provides warnings for zero results and slow operations.</p>
     * 
     * <p><strong>Analytics Logged:</strong></p>
     * <ul>
     *   <li>Search completion confirmation</li>
     *   <li>Query and duration information</li>
     *   <li>Result count when available</li>
     *   <li>Zero result warnings</li>
     *   <li>Slow search operation alerts (>500ms)</li>
     * </ul>
     * 
     * @param searchType the type of search that was performed
     * @param query the search query that was executed
     * @param duration the execution duration in milliseconds
     * @param result the result object returned by the search method
     */
    private void logSearchAnalytics(String searchType, String query, long duration, Object result) {
        logger.info(" ANALYTICS - {} search completed", searchType);
        logger.info(" ANALYTICS - Query: '{}', Duration: {}ms", query, duration);

        if (result != null) {
            try {
                if (result.getClass().getSimpleName().equals("Page")) {
                    Object content = result.getClass().getMethod("getContent").invoke(result);
                    if (content instanceof java.util.Collection) {
                        int resultCount = ((java.util.Collection<?>) content).size();
                        logger.info("📊 ANALYTICS - Results found: {}", resultCount);
                        
                        if (resultCount == 0) {
                            logger.warn("⚠️ ANALYTICS - No results found for query: '{}'", query);
                        }
                    }
                }
            } catch (Exception e) {
                logger.debug("Could not extract result count from analytics result");
            }
        }

        if (duration > 500) {
            logger.warn("🚨 ANALYTICS - Slow search operation: {}ms for query '{}'", duration, query);
        }
    }
}
