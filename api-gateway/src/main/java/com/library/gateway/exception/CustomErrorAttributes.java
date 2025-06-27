package com.library.gateway.exception;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
public class CustomErrorAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Map<String, Object> errorAttributes = new HashMap<>();
        Throwable error = getError(request);
        
        HttpStatus status = determineHttpStatus(error);
        
        // Always return 401 instead of 403
        if (status == HttpStatus.FORBIDDEN) {
            status = HttpStatus.UNAUTHORIZED;
        }
        
        errorAttributes.put("code", "Unauthorized");
        errorAttributes.put("message", "Unauthorized");
        errorAttributes.put("status", status.value());
        errorAttributes.put("timestamp", Instant.now().toString());
        
        return errorAttributes;
    }
    
    private HttpStatus determineHttpStatus(Throwable error) {
        if (error instanceof ResponseStatusException) {
            return HttpStatus.valueOf(((ResponseStatusException) error).getStatusCode().value());
        }
        return HttpStatus.UNAUTHORIZED;
    }
}