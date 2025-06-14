package com.library.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "vnpay")
@Data
public class VNPayConfig {
    
    private boolean enabled = true;
    private String url = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    private String returnUrl;
    private String notifyUrl;
    private String tmnCode;
    private String hashSecret;
    private String apiUrl = "https://sandbox.vnpayment.vn/merchant_webapi/api/transaction";
    private int timeoutMinutes = 15;
    private String version = "2.1.0";
    private String command = "pay";
    private String orderType = "other";
    private String locale = "vn";
    private String currCode = "VND";
    
    // Payment methods
    public static final String PAYMENT_METHOD_QR = "VNPAY_QR";
    public static final String PAYMENT_METHOD_CARD = "VNPAY_CARD";
    public static final String PAYMENT_METHOD_ATM = "VNPAY_ATM";
    public static final String PAYMENT_METHOD_BANK = "VNPAY_BANK";
}