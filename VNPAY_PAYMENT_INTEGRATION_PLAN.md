# 🏦 VNPAY PAYMENT INTEGRATION PLAN

## 📋 **TỔNG QUAN DỰ ÁN**

### Mục tiêu
Tích hợp VNPay Gateway vào hệ thống Library E-commerce để hoàn thiện luồng thanh toán từ Order → Payment → Confirmation.

### Scope
- Tích hợp VNPay Payment Gateway
- Xây dựng Payment Service
- Cập nhật Order workflow
- Implement webhook handling
- Frontend payment integration

---

## 🎯 **LUỒNG THANH TOÁN VNPAY**

### Luồng tổng quát
```
1. User Checkout → Order (PENDING_PAYMENT)
2. Create VNPay Payment URL
3. Redirect User to VNPay
4. User complete payment on VNPay
5. VNPay redirect back + Webhook
6. Update Order status → PAID
7. Trigger order processing
```

### Sequence Diagram
```
User → Frontend → Order Service → Payment Service → VNPay → Webhook → Order Update → Notification
```

---

## 🏗️ **KIẾN TRÚC TECHNICAL**

### Microservices Architecture
```
📦 book-catalog-service (Port 8082)
  ├── 📁 payment/
  │   ├── 📁 controller/
  │   │   ├── PaymentController.java
  │   │   └── VNPayWebhookController.java
  │   ├── 📁 service/
  │   │   ├── PaymentService.java
  │   │   ├── VNPayService.java
  │   │   └── PaymentWebhookService.java
  │   ├── 📁 gateway/
  │   │   ├── VNPayGateway.java
  │   │   └── PaymentGatewayInterface.java
  │   ├── 📁 entity/
  │   │   └── Payment.java
  │   ├── 📁 dto/
  │   │   ├── PaymentCreateRequest.java
  │   │   ├── PaymentResponse.java
  │   │   └── VNPayCallbackRequest.java
  │   └── 📁 config/
  │       └── VNPayConfig.java
```

---

## 🗃️ **DATABASE SCHEMA**

### Bảng `payments`
```sql
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    payment_code VARCHAR(50) UNIQUE NOT NULL,
    order_id BIGINT NOT NULL REFERENCES orders(id),
    
    -- Payment Info
    amount DECIMAL(15,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'VND',
    payment_method VARCHAR(50) NOT NULL, -- 'VNPAY_QR', 'VNPAY_CARD', etc.
    
    -- VNPay Specific
    vnp_txn_ref VARCHAR(100) UNIQUE NOT NULL,
    vnp_transaction_no VARCHAR(100),
    vnp_order_info TEXT,
    vnp_payment_url TEXT,
    
    -- Status & Tracking
    payment_status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, COMPLETED, FAILED, EXPIRED, REFUNDED
    gateway_status VARCHAR(50), -- VNPay response code
    gateway_message TEXT,
    
    -- Timestamps
    expires_at TIMESTAMP NOT NULL,
    paid_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Metadata
    ip_address VARCHAR(45),
    user_agent TEXT,
    metadata JSONB,
    
    -- Indexes
    INDEX idx_order_id (order_id),
    INDEX idx_payment_code (payment_code),
    INDEX idx_vnp_txn_ref (vnp_txn_ref),
    INDEX idx_payment_status (payment_status),
    INDEX idx_created_at (created_at)
);
```

### Bảng `payment_transactions` (Audit Log)
```sql
CREATE TABLE payment_transactions (
    id BIGSERIAL PRIMARY KEY,
    payment_id BIGINT NOT NULL REFERENCES payments(id),
    transaction_type VARCHAR(50) NOT NULL, -- 'PAYMENT', 'REFUND', 'WEBHOOK'
    status VARCHAR(20) NOT NULL,
    amount DECIMAL(15,2),
    gateway_response JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## 🔧 **VNPAY CONFIGURATION**

### Environment Variables
```properties
# VNPay Configuration
vnpay.enabled=true
vnpay.url=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
vnpay.return-url=http://localhost:3000/payment/vnpay/return
vnpay.notify-url=http://localhost:8080/api/v1/payments/webhook/vnpay
vnpay.tmn-code=YOUR_TMN_CODE
vnpay.hash-secret=YOUR_HASH_SECRET
vnpay.api-url=https://sandbox.vnpayment.vn/merchant_webapi/api/transaction
vnpay.timeout-minutes=15
```

### VNPay Config Class
```java
@Configuration
@ConfigurationProperties(prefix = "vnpay")
@Data
public class VNPayConfig {
    private boolean enabled;
    private String url;
    private String returnUrl;
    private String notifyUrl;
    private String tmnCode;
    private String hashSecret;
    private String apiUrl;
    private int timeoutMinutes;
    
    @Bean
    public VNPayGateway vnPayGateway() {
        return new VNPayGateway(this);
    }
}
```

---

## 🚀 **IMPLEMENTATION ROADMAP**

### **Phase 1: Foundation Setup (2-3 days)**
- [ ] Create Payment entities & repositories
- [ ] Setup VNPay configuration
- [ ] Database migration scripts
- [ ] Basic Payment service structure

### **Phase 2: VNPay Integration (3-4 days)**
- [ ] VNPay Gateway implementation
- [ ] Payment URL generation
- [ ] Hash signature validation
- [ ] Payment status checking

### **Phase 3: API Development (2-3 days)**
- [ ] Payment Controller APIs
- [ ] Webhook endpoint
- [ ] Order-Payment integration
- [ ] Error handling & validation

### **Phase 4: Frontend Integration (2-3 days)**
- [ ] Payment page UI
- [ ] VNPay redirect handling
- [ ] Payment status polling
- [ ] Success/failure pages

### **Phase 5: Testing & Deployment (2-3 days)**
- [ ] Unit tests
- [ ] Integration tests
- [ ] VNPay sandbox testing
- [ ] Performance testing
- [ ] Production deployment

---

## 📡 **API ENDPOINTS**

### Payment APIs
```http
POST /api/v1/payments/create
Content-Type: application/json
{
    "orderId": 123,
    "paymentMethod": "VNPAY_QR",
    "returnUrl": "http://localhost:3000/payment/success"
}

GET /api/v1/payments/{paymentId}
GET /api/v1/payments/order/{orderId}
POST /api/v1/payments/{paymentId}/cancel
```

### Webhook APIs
```http
POST /api/v1/payments/webhook/vnpay
Content-Type: application/x-www-form-urlencoded
```

### Admin APIs
```http
GET /api/v1/admin/payments
POST /api/v1/admin/payments/{paymentId}/refund
GET /api/v1/admin/payments/statistics
```

---

## 🔐 **SECURITY MEASURES**

### VNPay Security
- Hash SHA256 signature validation
- IP whitelist for webhooks
- Secure hash secret management
- Request timeout handling

### Application Security
- HTTPS only for payment URLs
- CSRF protection
- Rate limiting on payment endpoints
- Audit logging for all transactions

### Data Protection
- PCI DSS compliance considerations
- No sensitive card data storage
- Encrypted configuration values
- Secure webhook validation

---

## 🧪 **TESTING STRATEGY**

### Unit Tests
```java
@Test
public void testCreateVNPayPaymentURL() {
    // Test payment URL generation
}

@Test
public void testValidateVNPaySignature() {
    // Test signature validation
}

@Test
public void testPaymentStatusUpdate() {
    // Test order status update
}
```

### Integration Tests
- VNPay sandbox integration
- Database transaction testing
- Webhook endpoint testing
- Order workflow testing

### Test Data
```json
{
    "vnp_TmnCode": "TEST_TMN_CODE",
    "vnp_Amount": "5000000",
    "vnp_Command": "pay",
    "vnp_CreateDate": "20241213150000",
    "vnp_CurrCode": "VND",
    "vnp_IpAddr": "127.0.0.1",
    "vnp_Locale": "vn",
    "vnp_OrderInfo": "Thanh toan don hang #ORDER123",
    "vnp_OrderType": "other",
    "vnp_ReturnUrl": "http://localhost:3000/payment/return",
    "vnp_TxnRef": "ORDER123_20241213150000",
    "vnp_Version": "2.1.0"
}
```

---

## 📊 **MONITORING & LOGGING**

### Metrics to Track
- Payment success rate
- Average payment time
- Failed payment reasons
- Webhook delivery status
- Order conversion rate

### Logging Strategy
```java
@Slf4j
public class PaymentService {
    
    @Autowired
    private MeterRegistry meterRegistry;
    
    public PaymentResponse createPayment(PaymentCreateRequest request) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            log.info("Creating payment for order: {}", request.getOrderId());
            // Payment logic
            meterRegistry.counter("payment.created", "method", "vnpay").increment();
            return response;
        } catch (Exception e) {
            log.error("Payment creation failed for order: {}", request.getOrderId(), e);
            meterRegistry.counter("payment.failed", "method", "vnpay").increment();
            throw e;
        } finally {
            sample.stop(Timer.builder("payment.duration").register(meterRegistry));
        }
    }
}
```

---

## 🚨 **ERROR HANDLING**

### VNPay Error Codes
```java
public enum VNPayResponseCode {
    SUCCESS("00", "Giao dịch thành công"),
    PENDING("01", "Giao dịch chờ xử lý"),
    FAILED("02", "Giao dịch thất bại"),
    INVALID_AMOUNT("04", "Số tiền không hợp lệ"),
    INVALID_SIGNATURE("97", "Chữ ký không hợp lệ"),
    SYSTEM_ERROR("99", "Lỗi hệ thống");
    
    private final String code;
    private final String message;
}
```

### Exception Handling
```java
@ControllerAdvice
public class PaymentExceptionHandler {
    
    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ErrorResponse> handlePaymentException(PaymentException e) {
        return ResponseEntity.badRequest()
            .body(ErrorResponse.builder()
                .code(e.getErrorCode())
                .message(e.getMessage())
                .timestamp(Instant.now())
                .build());
    }
}
```

---

## 📈 **PERFORMANCE OPTIMIZATION**

### Caching Strategy
- Cache VNPay configuration
- Cache payment status for 5 minutes
- Redis for payment session data

### Database Optimization
- Proper indexing on payment tables
- Archived old payment records
- Connection pooling optimization

### API Optimization
- Async webhook processing
- Batch payment status updates
- Response compression

---

## 🔄 **ROLLBACK PLAN**

### Rollback Strategy
1. **Database Rollback**: Migration scripts reversal
2. **Feature Toggle**: Disable VNPay integration
3. **Graceful Degradation**: Fallback to manual payment
4. **Monitoring**: Track rollback metrics

### Contingency Plans
- Manual payment processing
- Alternative payment gateway
- Customer service escalation
- Refund processing procedures

---

## 📝 **DOCUMENTATION**

### Developer Documentation
- API documentation (Swagger)
- Database schema documentation
- Deployment guide
- Troubleshooting guide

### User Documentation
- Payment flow guide
- FAQ for common issues
- Customer support procedures

---

## ✅ **ACCEPTANCE CRITERIA**

### Functional Requirements
- [ ] User can create payment from order
- [ ] VNPay payment URL generation works
- [ ] Payment status updates correctly
- [ ] Webhook processing is reliable
- [ ] Order status updates after payment
- [ ] Refund functionality works
- [ ] Admin can monitor payments

### Non-Functional Requirements
- [ ] 99.9% payment availability
- [ ] < 3 seconds payment URL generation
- [ ] < 5 seconds webhook processingđể
- [ ] Support 1000 concurrent payments
- [ ] PCI DSS compliance ready
- [ ] Comprehensive error handling

---

## 🚀 **DEPLOYMENT CHECKLIST**

### Pre-Deployment
- [ ] VNPay sandbox testing completed
- [ ] All unit tests passing
- [ ] Integration tests passing
- [ ] Performance tests completed
- [ ] Security audit completed
- [ ] Documentation updated

### Deployment
- [ ] Database migration executed
- [ ] Configuration updated
- [ ] Feature flags enabled
- [ ] Monitoring setup
- [ ] Alerts configured

### Post-Deployment
- [ ] Smoke tests executed
- [ ] Payment flow verified
- [ ] Webhook delivery confirmed
- [ ] Performance metrics normal
- [ ] Error rates acceptable

---

## 🎯 **SUCCESS METRICS**

### KPIs
- **Payment Success Rate**: > 95%
- **Average Payment Time**: < 5 minutes
- **Webhook Delivery Rate**: > 99%
- **Customer Satisfaction**: > 4.5/5
- **System Uptime**: > 99.9%

### Business Impact
- Increased conversion rate
- Reduced manual payment processing
- Improved customer experience
- Better payment tracking
- Automated order processing

---

*Kế hoạch này sẽ được cập nhật định kỳ theo tiến độ triển khai và feedback từ team.*