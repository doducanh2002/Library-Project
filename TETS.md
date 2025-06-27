DEMO FLOW: COMPLETE LIBRARY SYSTEM

📋 Demo Scenario

"Một sinh viên mới đăng ký tài khoản, tìm và mượn sách, sau đó mua sách online"

  ---
🚀 PHASE 1: USER REGISTRATION & AUTHENTICATION

Step 1: Đăng ký tài khoản mới

POST http://localhost:8080/api/v1/auth/register
Content-Type: application/json

{
"username": "student2024",
"email": "student2024@university.edu",
"password": "StrongPass@123",
"confirmPassword": "StrongPass@123",
"fullName": "Nguyễn Văn An",
"phoneNumber": "0987654321",
"address": "123 Đường Đại Học, Hà Nội"
}

Expected Response:
{
"success": true,
"message": "Registration successful. Please check email for OTP verification.",
"data": {
"id": 15,
"username": "student2024",
"email": "student2024@university.edu",
"isActive": false
}
}

Step 2: Xác thực OTP (giả sử OTP là 123456)

POST http://localhost:8080/api/v1/auth/verify-otp
Content-Type: application/json

{
"email": "student2024@university.edu",
"otp": "123456"
}

Step 3: Đăng nhập và lấy JWT token

POST http://localhost:8080/api/v1/auth/login
Content-Type: application/json

{
"username": "student2024",
"password": "StrongPass@123"
}

Expected Response:
{
"success": true,
"data": {
"accessToken": "eyJhbGciOiJIUzUxMiJ9...",
"refreshToken": "refresh_token_here",
"tokenType": "Bearer",
"expiresIn": 86400,
"user": {
"id": 15,
"username": "student2024",
"email": "student2024@university.edu",
"fullName": "Nguyễn Văn An"
}
}
}

🔑 Lưu accessToken để sử dụng cho các API tiếp theo!

  ---
📚 PHASE 2: BOOK DISCOVERY & SEARCH

Step 4: Xem danh sách thể loại sách

GET http://localhost:8080/api/v1/categories
Authorization: Bearer YOUR_ACCESS_TOKEN

Step 5: Tìm kiếm sách theo từ khóa

GET http://localhost:8080/api/v1/books?keyword=java&page=0&size=10
Authorization: Bearer YOUR_ACCESS_TOKEN

Step 6: Tìm kiếm sách có thể mượn

GET http://localhost:8080/api/v1/books/available-for-loan?page=0&size=5
Authorization: Bearer YOUR_ACCESS_TOKEN

Step 7: Xem chi tiết một cuốn sách cụ thể

GET http://localhost:8080/api/v1/books/1
Authorization: Bearer YOUR_ACCESS_TOKEN

Expected Response:
{
"success": true,
"data": {
"id": 1,
"title": "Effective Java",
"isbn": "978-0134685991",
"description": "Best practices for Java programming",
"category": {
"id": 1,
"name": "Programming"
},
"authors": [
{
"id": 1,
"name": "Joshua Bloch"
}
],
"isLendable": true,
"availableCopiesForLoan": 3,
"isSellable": true,
"price": 45.99,
"stockForSale": 10
}
}

  ---
📖 PHASE 3: LOAN MANAGEMENT (BORROWING BOOKS)

Step 8: Tạo yêu cầu mượn sách

POST http://localhost:8080/api/v1/loans
Authorization: Bearer YOUR_ACCESS_TOKEN
Content-Type: application/json

{
"bookId": 1,
"requestedDays": 14,
"notes": "Cần cho dự án học tập"
}

Expected Response:
{
"success": true,
"message": "Loan request created successfully",
"data": {
"id": 25,
"bookId": 1,
"bookTitle": "Effective Java",
"status": "REQUESTED",
"requestDate": "2024-06-27T10:30:00",
"requestedDueDate": "2024-07-11T10:30:00"
}
}

Step 9: Kiểm tra trạng thái các khoản vay hiện tại

GET http://localhost:8080/api/v1/loans/current
Authorization: Bearer YOUR_ACCESS_TOKEN

Step 10: Xem lịch sử mượn sách

GET http://localhost:8080/api/v1/loans/history?page=0&size=10
Authorization: Bearer YOUR_ACCESS_TOKEN

  ---
🛒 PHASE 4: E-COMMERCE (BUYING BOOKS)

Step 11: Thêm sách vào giỏ hàng

POST http://localhost:8080/api/v1/cart/add
Authorization: Bearer YOUR_ACCESS_TOKEN
Content-Type: application/json

{
"bookId": 2,
"quantity": 2
}

Step 12: Xem giỏ hàng hiện tại

GET http://localhost:8080/api/v1/cart
Authorization: Bearer YOUR_ACCESS_TOKEN

Expected Response:
{
"success": true,
"data": {
"items": [
{
"id": 1,
"book": {
"id": 2,
"title": "Clean Code",
"price": 39.99
},
"quantity": 2,
"itemTotal": 79.98
}
],
"totalItems": 2,
"totalAmount": 79.98
}
}

Step 13: Cập nhật số lượng trong giỏ hàng

PUT http://localhost:8080/api/v1/cart/items/1
Authorization: Bearer YOUR_ACCESS_TOKEN
Content-Type: application/json

{
"quantity": 3
}

Step 14: Tạo đơn hàng từ giỏ hàng

POST http://localhost:8080/api/v1/orders
Authorization: Bearer YOUR_ACCESS_TOKEN
Content-Type: application/json

{
"shippingAddress": {
"line1": "123 Đường Đại Học",
"line2": "Tòa A1",
"city": "Hà Nội",
"postalCode": "100000",
"country": "Vietnam"
},
"customerNote": "Giao vào giờ hành chính"
}

Expected Response:
{
"success": true,
"message": "Order created successfully",
"data": {
"id": 12,
"orderCode": "ORD-2024-000012",
"status": "PENDING_PAYMENT",
"totalAmount": 119.97,
"items": [
{
"bookTitle": "Clean Code",
"quantity": 3,
"pricePerUnit": 39.99,
"itemTotal": 119.97
}
],
"paymentUrl": "http://vnpay-gateway/payment/ORD-2024-000012"
}
}

Step 15: Kiểm tra trạng thái đơn hàng

GET http://localhost:8080/api/v1/orders/12
Authorization: Bearer YOUR_ACCESS_TOKEN

Step 16: Xem lịch sử đơn hàng

GET http://localhost:8080/api/v1/orders/history?page=0&size=10
Authorization: Bearer YOUR_ACCESS_TOKEN

  ---
📬 PHASE 5: NOTIFICATIONS

Step 17: Xem thông báo chưa đọc

GET http://localhost:8080/api/v1/notifications/unread
Authorization: Bearer YOUR_ACCESS_TOKEN

Expected Response:
{
"success": true,
"data": [
{
"id": 45,
"title": "Loan Request Received",
"message": "Your loan request for 'Effective Java' has been received and is being processed.",
"type": "LOAN_REQUESTED",
"isRead": false,
"createdAt": "2024-06-27T10:30:00"
},
{
"id": 46,
"title": "Order Created",
"message": "Your order ORD-2024-000012 has been created successfully. Please proceed with payment.",
"type": "ORDER_CONFIRMED",
"isRead": false,
"createdAt": "2024-06-27T11:15:00"
}
]
}

Step 18: Đánh dấu thông báo đã đọc

PUT http://localhost:8080/api/v1/notifications/45/read
Authorization: Bearer YOUR_ACCESS_TOKEN

Step 19: Xem tóm tắt thông báo

GET http://localhost:8080/api/v1/notifications/summary
Authorization: Bearer YOUR_ACCESS_TOKEN

  ---
👤 PHASE 6: USER PROFILE MANAGEMENT

Step 20: Xem thông tin profile

GET http://localhost:8080/api/v1/users/me
Authorization: Bearer YOUR_ACCESS_TOKEN

Step 21: Cập nhật thông tin cá nhân

PUT http://localhost:8080/api/v1/users/me
Authorization: Bearer YOUR_ACCESS_TOKEN
Content-Type: application/json

{
"fullName": "Nguyễn Văn An (Updated)",
"phoneNumber": "0987654321",
"address": "456 Đường Mới, Hà Nội"
}

  ---
🔧 ADMIN DEMO FLOW (Optional)

Step 22: Đăng nhập với tài khoản admin

POST http://localhost:8080/api/v1/auth/login
Content-Type: application/json

{
"username": "admin",
"password": "admin123"
}

Step 23: Admin duyệt yêu cầu mượn sách

PUT http://localhost:8080/api/v1/admin/loans/25/approve
Authorization: Bearer ADMIN_ACCESS_TOKEN
Content-Type: application/json

{
"notes": "Approved for academic purpose",
"dueDate": "2024-07-11T23:59:59"
}

Step 24: Admin cập nhật trạng thái đơn hàng

PUT http://localhost:8080/api/v1/admin/orders/12/status
Authorization: Bearer ADMIN_ACCESS_TOKEN
Content-Type: application/json

{
"status": "PROCESSING",
"notes": "Order is being prepared for shipment"
}

  ---
📊 DEMO SCRIPT SUMMARY

🎯 Demo Flow Overview:

1. Authentication (Steps 1-3): Registration → OTP → Login
2. Discovery (Steps 4-7): Browse categories → Search books → View details
3. Borrowing (Steps 8-10): Request loan → Check status → View history
4. Shopping (Steps 11-16): Add to cart → Checkout → Create order
5. Notifications (Steps 17-19): View alerts → Mark read → Get summary
6. Profile (Steps 20-21): View profile → Update info
7. Admin (Steps 22-24): Admin login → Approve loans → Update orders

💡 Demo Tips:

