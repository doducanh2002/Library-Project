package com.library.service;

import com.library.config.VNPayConfig;
import com.library.util.VNPayUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class VNPayServiceTest {

    @Test
    public void testVNPaySignatureGeneration() {
        // Test VNPay signature generation
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", "TEST123");
        params.put("vnp_Amount", "1000000");
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", "TEST_TXN_REF");
        params.put("vnp_OrderInfo", "Test Order");
        
        String hashSecret = "TESTHASGSECRET";
        String signature = VNPayUtil.hashAllFields(params, hashSecret);
        
        assertNotNull(signature);
        assertFalse(signature.isEmpty());
        assertTrue(signature.length() > 10);
    }

    @Test
    public void testVNPaySignatureValidation() {
        // Test signature validation
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", "TEST123");
        params.put("vnp_Amount", "1000000");
        
        String hashSecret = "TESTHASGSECRET";
        String signature = VNPayUtil.hashAllFields(params, hashSecret);
        
        // Valid signature should return true
        assertTrue(VNPayUtil.validateSignature(params, signature, hashSecret));
        
        // Invalid signature should return false
        assertFalse(VNPayUtil.validateSignature(params, "invalid_signature", hashSecret));
    }

    @Test
    public void testVNPayUrlBuilding() {
        // Test URL building with parameters
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", "TEST123");
        params.put("vnp_Amount", "1000000");
        
        String query = VNPayUtil.buildQuery(params);
        
        assertNotNull(query);
        assertTrue(query.contains("vnp_Version=2.1.0"));
        assertTrue(query.contains("vnp_Command=pay"));
        assertTrue(query.contains("vnp_TmnCode=TEST123"));
        assertTrue(query.contains("vnp_Amount=1000000"));
    }

    @Test
    public void testRandomNumberGeneration() {
        String randomNumber = VNPayUtil.getRandomNumber(8);
        
        assertNotNull(randomNumber);
        assertEquals(8, randomNumber.length());
        assertTrue(randomNumber.matches("\\d+")); // Only digits
    }

    @Test
    public void testPayDateGeneration() {
        String payDate = VNPayUtil.getPayDate();
        
        assertNotNull(payDate);
        assertEquals(14, payDate.length());
        assertTrue(payDate.matches("\\d{14}")); // Format: yyyyMMddHHmmss
    }
}