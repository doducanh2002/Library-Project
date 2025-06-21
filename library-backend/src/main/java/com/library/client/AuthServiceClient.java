package com.library.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthServiceClient {

    private final RestTemplate restTemplate;

    @Value("${auth.service.url:http://localhost:8081}")
    private String authServiceUrl;

    public Long getTotalUsersCount() {
        try {
            String url = authServiceUrl + "/api/admin/users/count";
            log.info("Calling auth service to get total users count: {}", url);
            return restTemplate.getForObject(url, Long.class);
        } catch (Exception e) {
            log.error("Error calling auth service for total users count", e);
            return 0L;
        }
    }

    public Long getUsersCountByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            String url = String.format("%s/api/admin/users/count-by-date?startDate=%s&endDate=%s",
                    authServiceUrl,
                    startDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    endDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            log.info("Calling auth service to get users count by date range: {}", url);
            return restTemplate.getForObject(url, Long.class);
        } catch (Exception e) {
            log.error("Error calling auth service for users count by date range", e);
            return 0L;
        }
    }
}