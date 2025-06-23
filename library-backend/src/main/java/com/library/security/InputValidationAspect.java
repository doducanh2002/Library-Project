package com.library.security;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.regex.Pattern;

@Aspect
@Component
@Slf4j
public class InputValidationAspect {

    // Patterns for potentially malicious content
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
            "(?i)(union|select|insert|update|delete|drop|create|alter|exec|execute|script|javascript|vbscript)", 
            Pattern.CASE_INSENSITIVE);
    
    private static final Pattern XSS_PATTERN = Pattern.compile(
            "(?i)(<script|</script|javascript:|vbscript:|onload|onerror|onclick)", 
            Pattern.CASE_INSENSITIVE);
    
    private static final Pattern HTML_INJECTION_PATTERN = Pattern.compile(
            "(?i)(<[^>]*>|&[^;]+;)", 
            Pattern.CASE_INSENSITIVE);

    @Around("execution(* com.library.controller.*Controller.*(..)) && args(.., @RequestBody requestBody, ..)")
    public Object validateInput(ProceedingJoinPoint joinPoint, Object requestBody) throws Throwable {
        if (requestBody != null) {
            validateObject(requestBody);
        }
        return joinPoint.proceed();
    }

    @Around("execution(* com.library.controller.*Controller.*(..)) && args(.., @RequestParam String param, ..)")
    public Object validateParam(ProceedingJoinPoint joinPoint, String param) throws Throwable {
        if (param != null) {
            validateString(param, "Request parameter");
        }
        return joinPoint.proceed();
    }

    private void validateObject(Object obj) {
        if (obj == null) return;
        
        // Use reflection to check string fields
        java.lang.reflect.Field[] fields = obj.getClass().getDeclaredFields();
        for (java.lang.reflect.Field field : fields) {
            if (field.getType() == String.class) {
                try {
                    field.setAccessible(true);
                    String value = (String) field.get(obj);
                    if (value != null) {
                        validateString(value, field.getName());
                    }
                } catch (IllegalAccessException e) {
                    log.warn("Could not access field {} for validation", field.getName());
                }
            }
        }
    }

    private void validateString(String input, String fieldName) {
        if (input == null || input.trim().isEmpty()) {
            return;
        }

        // Check for SQL injection patterns
        if (SQL_INJECTION_PATTERN.matcher(input).find()) {
            log.warn("Potential SQL injection detected in {}: {}", fieldName, input);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, 
                    "Invalid input: Potentially malicious content detected");
        }

        // Check for XSS patterns
        if (XSS_PATTERN.matcher(input).find()) {
            log.warn("Potential XSS detected in {}: {}", fieldName, input);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, 
                    "Invalid input: Potentially malicious content detected");
        }

        // Check for excessive HTML (be more lenient for notification messages)
        if (!fieldName.toLowerCase().contains("message") && 
            !fieldName.toLowerCase().contains("description") &&
            HTML_INJECTION_PATTERN.matcher(input).find()) {
            log.warn("Potential HTML injection detected in {}: {}", fieldName, input);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, 
                    "Invalid input: HTML content not allowed in this field");
        }

        // Check for excessively long input
        if (input.length() > 10000) {
            log.warn("Excessively long input detected in {}: {} characters", fieldName, input.length());
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, 
                    "Invalid input: Input too long");
        }

        // Check for null bytes
        if (input.contains("\0")) {
            log.warn("Null byte detected in {}", fieldName);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, 
                    "Invalid input: Null bytes not allowed");
        }
    }
}