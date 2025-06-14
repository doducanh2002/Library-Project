# 🎯 COMPLETE PAYMENT FLOW - TESTING GUIDE

## ✅ **LUỒNG THANH TOÁN HOÀN CHỈNH ĐÃ SẴNS SÀNG!**

### **📋 Tóm tắt luồng**
```
Cart → Checkout with Payment → Order (PENDING_PAYMENT) → VNPay URL → User Payment → Webhook → Order (PAID)
```

## 🚀 **API Testing Sequence**

### **1. Thêm sách vào giỏ hàng**
```bash
POST /api/v1/cart/items
Content-Type: application/json
X-User-Id: 1
Authorization: Bearer {jwt_token}

{
  "bookId": 1,
  "quantity": 2
}
```

### **2. Kiểm tra giỏ hàng**
```bash
GET /api/v1/cart/summary
X-User-Id: 1
Authorization: Bearer {jwt_token}
```

### **3. ⭐ CHECKOUT VỚI PAYMENT (NEW API)**
```bash
POST /api/v1/orders/checkout-with-payment
Content-Type: application/json
X-User-Id: 1
Authorization: Bearer {jwt_token}

{
  "shippingAddressLine1": "123 Nguyen Van Linh",
  "shippingCity": "Ho Chi Minh City",
  "shippingPostalCode": "70000",
  "shippingCountry": "Vietnam",
  "customerNote": "Please deliver during business hours",
  "paymentMethod": "VNPAY_QR"
}
```

### **4. Response từ checkout-with-payment**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "order": {
      "id": 123,
      "orderCode": "ORD-2025-000123",
      "status": "PENDING_PAYMENT",
      "totalAmount": 500000
      // ... other order fields
    },
    "payment": {
      "id": 456,
      "paymentCode": "PAY_1749888999_123",
      "vnpPaymentUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Amount=...",
      "paymentStatus": "PENDING",
      "expiresAt": "2025-01-14T10:30:00"
    },
    "nextAction": "redirect_to_payment",
    "message": "Order created successfully. Please complete payment."
  }
}
```

### **5. Frontend redirect user đến VNPay**
```javascript
// Frontend code
const response = await fetch('/api/v1/orders/checkout-with-payment', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'X-User-Id': userId,
    'Authorization': `Bearer ${token}`
  },
  body: JSON.stringify(orderData)
});

const result = await response.json();

if (result.data.nextAction === 'redirect_to_payment') {
  // Redirect user to VNPay
  window.location.href = result.data.payment.vnpPaymentUrl;
}
```

### **6. User thanh toán trên VNPay**
- User được redirect đến VNPay
- User chọn phương thức thanh toán (QR, ATM, Card)
- User hoàn thành thanh toán

### **7. VNPay webhook tự động xử lý**
```bash
# VNPay sẽ tự động gọi webhook
POST /api/v1/payments/webhook/vnpay
Content-Type: application/json

{
  "vnp_Amount": "50000000",
  "vnp_ResponseCode": "00",
  "vnp_TransactionStatus": "00",
  "vnp_TxnRef": "ORD123_1749888999",
  "vnp_TransactionNo": "14123456",
  // ... other VNPay fields
}
```

### **8. Kiểm tra order status sau payment**
```bash
GET /api/v1/orders/{orderCode}
X-User-Id: 1
Authorization: Bearer {jwt_token}

# Response:
{
  "status": "PAID",
  "paymentStatus": "PAID",
  "paymentTransactionId": "14123456"
}
```

## 🔄 **Alternative APIs (nếu cần tách riêng)**

### **Legacy Checkout (tạo order trước)**
```bash
# 1. Create order
POST /api/v1/orders/checkout
# 2. Manually create payment
POST /api/v1/payments/create
```

### **Check payment status**
```bash
GET /api/v1/payments/{paymentId}
GET /api/v1/payments/order/{orderId}
GET /api/v1/payments/status/{vnpTxnRef}
```

### **Admin operations**
```bash
POST /api/v1/admin/payments/{paymentCode}/refund
POST /api/v1/admin/payments/process-expired
```

## 🎯 **Test Scenarios**

### **✅ Happy Path**
1. Add items to cart
2. Call `/checkout-with-payment`
3. Redirect to VNPay URL
4. Complete payment
5. Webhook updates order to PAID
6. User sees successful order

### **❌ Error Scenarios**
1. **Empty cart**: Should return 400 error
2. **Insufficient stock**: Should return 409 error
3. **Payment timeout**: Scheduled task marks as EXPIRED
4. **Invalid VNPay signature**: Webhook rejects
5. **Network failure**: User can retry payment

### **🔄 Edge Cases**
1. **Duplicate payment**: Should prevent multiple payments for same order
2. **Expired payment**: Should allow new payment creation
3. **Partial refund**: Admin can process refunds
4. **Order cancellation**: Only allowed before payment

## 🛠️ **Configuration Required**

### **1. Update VNPay credentials in application.properties**
```properties
vnpay.tmn-code=YOUR_REAL_TMN_CODE
vnpay.hash-secret=YOUR_REAL_HASH_SECRET
vnpay.return-url=http://your-frontend.com/payment/return
vnpay.notify-url=http://your-backend.com/api/v1/payments/webhook/vnpay
```

### **2. Database migration**
```bash
# Payment tables đã được tạo trong migration 005
# Chạy application để auto-apply migrations
```

### **3. Frontend integration**
```javascript
// Frontend cần handle redirect và return URLs
// Implement payment result pages
// Add payment status polling if needed
```

## 📊 **Monitoring & Debugging**

### **Database queries để check**
```sql
-- Check orders
SELECT * FROM orders WHERE user_id = 1 ORDER BY created_at DESC;

-- Check payments
SELECT * FROM payments WHERE order_id = 123;

-- Check payment transactions (audit log)
SELECT * FROM payment_transactions WHERE payment_id = 456;
```

### **Logs để monitor**
```bash
# Payment creation
grep "Creating VNPay payment URL" logs/application.log

# Webhook processing
grep "Processing VNPay webhook" logs/application.log

# Order status updates
grep "Order status updated" logs/application.log
```

---

## 🎉 **LUỒNG THANH TOÁN HOÀN CHỈNH 100%!**

✅ **Backend**: Order → Payment → Webhook → Status Update  
✅ **APIs**: Complete checkout with single API call  
✅ **Database**: Payment tracking and audit trail  
✅ **Security**: VNPay signature validation  
✅ **Error handling**: Comprehensive error scenarios  
✅ **Admin tools**: Refund and payment management  
✅ **Testing**: Ready for sandbox and production testing  

**Luồng thanh toán đã HOÀN TẤT và sẵn sàng production!** 🚀