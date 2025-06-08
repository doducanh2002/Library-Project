package org.aibles.authenservice.controller.advice;

import org.aibles.authenservice.exception.base.RunException;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@RestControllerAdvice
public class AuthControllerAdvice {

    private final MessageSource messageSource;

    private static final Logger log = LoggerFactory.getLogger(AuthControllerAdvice.class);

    public AuthControllerAdvice(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(RunException.class)
    public ResponseEntity<Map<String, Object>> handleBaseException(
            RunException ex, @RequestHeader(name = "Accept-Language", required = false) Locale locale) {

        log.error("BaseException caught: ", ex);

        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("status", ex.getStatus());
        errorMap.put("code", ex.getCode());
        errorMap.put("timestamp", ex.getTimestamp());
        errorMap.put("message", resolveMessage(ex.getMessage(), locale));
        return ResponseEntity.status(HttpStatus.valueOf(ex.getStatus())).body(errorMap);
    }

    private String resolveMessage(String messageKey, Locale locale) {
        try {
            return messageSource.getMessage(messageKey, null, locale);
        } catch (Exception e) {
            return messageKey;
        }
    }
}
