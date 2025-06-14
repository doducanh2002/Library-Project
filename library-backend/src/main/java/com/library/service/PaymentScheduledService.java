package com.library.service;

import com.library.service.VNPayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentScheduledService {

    private final VNPayService vnPayService;

    /**
     * Process expired payments every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void processExpiredPayments() {
        log.debug("Running scheduled expired payments processing");
        
        try {
            vnPayService.processExpiredPayments();
        } catch (Exception e) {
            log.error("Error in scheduled expired payments processing", e);
        }
    }
}