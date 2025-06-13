# Danh sách tất cả API Endpoints - Ứng dụng Web Thư viện

## 1. Authentication & Authorization (5 APIs)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/v1/auth/register` | Đăng ký người dùng mới | No |
| POST | `/api/v1/auth/login` | Đăng nhập | No |
| POST | `/api/v1/auth/logout` | Đăng xuất | Yes |
| POST | `/api/v1/auth/refresh-token` | Làm mới access token | No |
| POST | `/api/v1/auth/forgot-password` | Quên mật khẩu | No |

## 2. User Management (8 APIs)

| Method | Endpoint | Description | Auth Required | Role |
|--------|----------|-------------|---------------|------|
| GET | `/api/v1/users/me` | Lấy thông tin cá nhân | Yes | USER |
| PUT | `/api/v1/users/me` | Cập nhật thông tin cá nhân | Yes | USER |
| PUT | `/api/v1/users/me/change-password` | Đổi mật khẩu | Yes | USER |
| GET | `/api/v1/admin/users` | Lấy danh sách người dùng | Yes | ADMIN |
| GET | `/api/v1/admin/users/{userId}` | Lấy chi tiết người dùng | Yes | ADMIN |
| PUT | `/api/v1/admin/users/{userId}` | Cập nhật thông tin người dùng | Yes | ADMIN |
| PUT | `/api/v1/admin/users/{userId}/status` | Kích hoạt/vô hiệu hóa tài khoản | Yes | ADMIN |
| PUT | `/api/v1/admin/users/{userId}/roles` | Phân quyền người dùng | Yes | ADMIN |

## 3. Books Management (10 APIs)

| Method | Endpoint | Description | Auth Required | Role |
|--------|----------|-------------|---------------|------|
| GET | `/api/v1/books` | Lấy danh sách sách (tìm kiếm, lọc) | No | - |
| GET | `/api/v1/books/{bookId}` | Lấy chi tiết sách | No | - |
| GET | `/api/v1/books/isbn/{isbn}` | Lấy sách theo ISBN | No | - |
| POST | `/api/v1/admin/books` | Thêm sách mới | Yes | LIBRARIAN |
| PUT | `/api/v1/admin/books/{bookId}` | Cập nhật thông tin sách | Yes | LIBRARIAN |
| DELETE | `/api/v1/admin/books/{bookId}` | Xóa sách | Yes | LIBRARIAN |
| PUT | `/api/v1/admin/books/{bookId}/stock` | Cập nhật số lượng tồn kho | Yes | LIBRARIAN |
| POST | `/api/v1/admin/books/{bookId}/cover` | Upload ảnh bìa sách | Yes | LIBRARIAN |
| GET | `/api/v1/admin/books` | Quản lý sách (Admin view) | Yes | LIBRARIAN |
| GET | `/api/v1/books/popular` | Lấy sách phổ biến | No | - |

## 4. Categories, Authors, Publishers (15 APIs)

### Categories (5 APIs)
| Method | Endpoint | Description | Auth Required | Role |
|--------|----------|-------------|---------------|------|
| GET | `/api/v1/categories` | Lấy danh sách thể loại | No | - |
| GET | `/api/v1/categories/{categoryId}` | Lấy chi tiết thể loại | No | - |
| POST | `/api/v1/admin/categories` | Thêm thể loại mới | Yes | LIBRARIAN |
| PUT | `/api/v1/admin/categories/{categoryId}` | Cập nhật thể loại | Yes | LIBRARIAN |
| DELETE | `/api/v1/admin/categories/{categoryId}` | Xóa thể loại | Yes | LIBRARIAN |

### Authors (5 APIs)
| Method | Endpoint | Description | Auth Required | Role |
|--------|----------|-------------|---------------|------|
| GET | `/api/v1/authors` | Lấy danh sách tác giả | No | - |
| GET | `/api/v1/authors/{authorId}` | Lấy chi tiết tác giả | No | - |
| POST | `/api/v1/admin/authors` | Thêm tác giả mới | Yes | LIBRARIAN |
| PUT | `/api/v1/admin/authors/{authorId}` | Cập nhật thông tin tác giả | Yes | LIBRARIAN |
| DELETE | `/api/v1/admin/authors/{authorId}` | Xóa tác giả | Yes | LIBRARIAN |

### Publishers (5 APIs)
| Method | Endpoint | Description | Auth Required | Role |
|--------|----------|-------------|---------------|------|
| GET | `/api/v1/publishers` | Lấy danh sách nhà xuất bản | No | - |
| GET | `/api/v1/publishers/{publisherId}` | Lấy chi tiết nhà xuất bản | No | - |
| POST | `/api/v1/admin/publishers` | Thêm nhà xuất bản mới | Yes | LIBRARIAN |
| PUT | `/api/v1/admin/publishers/{publisherId}` | Cập nhật thông tin nhà xuất bản | Yes | LIBRARIAN |
| DELETE | `/api/v1/admin/publishers/{publisherId}` | Xóa nhà xuất bản | Yes | LIBRARIAN |

## 5. Loan Management (12 APIs)

| Method | Endpoint | Description | Auth Required | Role |
|--------|----------|-------------|---------------|------|
| POST | `/api/v1/loans/request` | Yêu cầu mượn sách | Yes | USER |
| GET | `/api/v1/loans/my-history` | Lịch sử mượn sách cá nhân | Yes | USER |
| GET | `/api/v1/loans/my-current` | Sách đang mượn | Yes | USER |
| POST | `/api/v1/loans/{loanId}/renew` | Gia hạn mượn sách | Yes | USER |
| GET | `/api/v1/admin/loans` | Quản lý tất cả phiếu mượn | Yes | LIBRARIAN |
| GET | `/api/v1/admin/loans/{loanId}` | Chi tiết phiếu mượn | Yes | LIBRARIAN |
| POST | `/api/v1/admin/loans/{loanId}/approve` | Xác nhận yêu cầu mượn | Yes | LIBRARIAN |
| POST | `/api/v1/admin/loans/{loanId}/reject` | Từ chối yêu cầu mượn | Yes | LIBRARIAN |
| POST | `/api/v1/admin/loans/{loanId}/return` | Ghi nhận trả sách | Yes | LIBRARIAN |
| PUT | `/api/v1/admin/loans/{loanId}` | Cập nhật thông tin phiếu mượn | Yes | LIBRARIAN |
| GET | `/api/v1/admin/loans/overdue` | Danh sách sách quá hạn | Yes | LIBRARIAN |
| POST | `/api/v1/admin/loans/{loanId}/calculate-fine` | Tính tiền phạt | Yes | LIBRARIAN |

## 6. Shopping Cart (5 APIs)

| Method | Endpoint | Description | Auth Required | Role |
|--------|----------|-------------|---------------|------|
| GET | `/api/v1/cart` | Lấy giỏ hàng hiện tại | Yes | USER |
| POST | `/api/v1/cart/items` | Thêm sách vào giỏ hàng | Yes | USER |
| PUT | `/api/v1/cart/items/{bookId}` | Cập nhật số lượng trong giỏ | Yes | USER |
| DELETE | `/api/v1/cart/items/{bookId}` | Xóa sách khỏi giỏ hàng | Yes | USER |
| DELETE | `/api/v1/cart` | Xóa toàn bộ giỏ hàng | Yes | USER |

## 7. Order Management (10 APIs)

| Method | Endpoint | Description | Auth Required | Role |
|--------|----------|-------------|---------------|------|
| POST | `/api/v1/orders/checkout` | Đặt hàng từ giỏ hàng | Yes | USER |
| GET | `/api/v1/orders` | Lịch sử đơn hàng cá nhân | Yes | USER |
| GET | `/api/v1/orders/{orderCode}` | Chi tiết đơn hàng | Yes | USER |
| POST | `/api/v1/orders/{orderCode}/cancel` | Hủy đơn hàng | Yes | USER |
| GET | `/api/v1/admin/orders` | Quản lý tất cả đơn hàng | Yes | LIBRARIAN |
| GET | `/api/v1/admin/orders/{orderCode}` | Chi tiết đơn hàng (Admin) | Yes | LIBRARIAN |
| PUT | `/api/v1/admin/orders/{orderCode}/status` | Cập nhật trạng thái đơn hàng | Yes | LIBRARIAN |
| PUT | `/api/v1/admin/orders/{orderCode}/payment-status` | Cập nhật trạng thái thanh toán | Yes | LIBRARIAN |
| POST | `/api/v1/admin/orders/{orderCode}/refund` | Hoàn tiền đơn hàng | Yes | LIBRARIAN |
| GET | `/api/v1/admin/orders/statistics` | Thống kê đơn hàng | Yes | LIBRARIAN |

## 8. Payment (4 APIs)

| Method | Endpoint | Description | Auth Required | Role |
|--------|----------|-------------|---------------|------|
| POST | `/api/v1/payments/create-payment-intent` | Tạo Payment Intent | Yes | USER |
| POST | `/api/v1/payments/webhook/stripe` | Webhook xử lý thanh toán Stripe | No | - |
| POST | `/api/v1/payments/webhook/vnpay` | Webhook xử lý thanh toán VNPay | No | - |
| GET | `/api/v1/payments/{paymentId}/status` | Kiểm tra trạng thái thanh toán | Yes | USER |

## 9. Document Management (10 APIs)

| Method | Endpoint | Description | Auth Required | Role |
|--------|----------|-------------|---------------|------|
| GET | `/api/v1/documents` | Lấy danh sách tài liệu | No | - |
| GET | `/api/v1/documents/{documentId}` | Lấy chi tiết tài liệu | No | - |
| GET | `/api/v1/documents/{documentId}/view-url` | Lấy URL xem tài liệu | Optional | - |
| GET | `/api/v1/documents/search` | Tìm kiếm tài liệu | No | - |
| POST | `/api/v1/admin/documents/upload` | Tải lên tài liệu mới | Yes | LIBRARIAN |
| PUT | `/api/v1/admin/documents/{documentId}` | Cập nhật metadata tài liệu | Yes | LIBRARIAN |
| POST | `/api/v1/admin/documents/{documentId}/replace-file` | Thay thế file tài liệu | Yes | LIBRARIAN |
| DELETE | `/api/v1/admin/documents/{documentId}` | Xóa tài liệu | Yes | LIBRARIAN |
| GET | `/api/v1/admin/documents` | Quản lý tài liệu (Admin view) | Yes | LIBRARIAN |
| GET | `/api/v1/admin/documents/statistics` | Thống kê tài liệu | Yes | LIBRARIAN |

## 10. Admin Dashboard (8 APIs)

| Method | Endpoint | Description | Auth Required | Role |
|--------|----------|-------------|---------------|------|
| GET | `/api/v1/admin/dashboard/overview` | Tổng quan dashboard | Yes | LIBRARIAN |
| GET | `/api/v1/admin/dashboard/statistics` | Thống kê tổng quan | Yes | LIBRARIAN |
| GET | `/api/v1/admin/dashboard/recent-activities` | Hoạt động gần đây | Yes | LIBRARIAN |
| GET | `/api/v1/admin/dashboard/popular-books` | Sách phổ biến | Yes | LIBRARIAN |
| GET | `/api/v1/admin/dashboard/user-analytics` | Phân tích người dùng | Yes | LIBRARIAN |
| GET | `/api/v1/admin/dashboard/revenue-analytics` | Phân tích doanh thu | Yes | LIBRARIAN |
| GET | `/api/v1/admin/dashboard/loan-analytics` | Phân tích mượn sách | Yes | LIBRARIAN |
| GET | `/api/v1/admin/dashboard/system-health` | Tình trạng hệ thống | Yes | ADMIN |

## 11. Reports (6 APIs)

| Method | Endpoint | Description | Auth Required | Role |
|--------|----------|-------------|---------------|------|
| GET | `/api/v1/admin/reports/books` | Báo cáo sách | Yes | LIBRARIAN |
| GET | `/api/v1/admin/reports/loans` | Báo cáo mượn sách | Yes | LIBRARIAN |
| GET | `/api/v1/admin/reports/sales` | Báo cáo bán hàng | Yes | LIBRARIAN |
| GET | `/api/v1/admin/reports/users` | Báo cáo người dùng | Yes | LIBRARIAN |
| POST | `/api/v1/admin/reports/export` | Xuất báo cáo (PDF/Excel) | Yes | LIBRARIAN |
| GET | `/api/v1/admin/reports/financial` | Báo cáo tài chính | Yes | ADMIN |

## 12. System Configuration (5 APIs)

| Method | Endpoint | Description | Auth Required | Role |
|--------|----------|-------------|---------------|------|
| GET | `/api/v1/admin/config/system` | Lấy cấu hình hệ thống | Yes | ADMIN |
| PUT | `/api/v1/admin/config/system` | Cập nhật cấu hình hệ thống | Yes | ADMIN |
| GET | `/api/v1/admin/config/loan-policies` | Lấy chính sách mượn sách | Yes | LIBRARIAN |
| PUT | `/api/v1/admin/config/loan-policies` | Cập nhật chính sách mượn sách | Yes | LIBRARIAN |
| POST | `/api/v1/admin/config/backup` | Sao lưu dữ liệu | Yes | ADMIN |

## 13. Notifications (4 APIs)

| Method | Endpoint | Description | Auth Required | Role |
|--------|----------|-------------|---------------|------|
| GET | `/api/v1/notifications` | Lấy danh sách thông báo | Yes | USER |
| PUT | `/api/v1/notifications/{notificationId}/read` | Đánh dấu đã đọc | Yes | USER |
| PUT | `/api/v1/notifications/mark-all-read` | Đánh dấu tất cả đã đọc | Yes | USER |
| DELETE | `/api/v1/notifications/{notificationId}` | Xóa thông báo | Yes | USER |

---

## **TỔNG KẾT:**

### **Số lượng API theo module:**
- **Authentication & Authorization**: 5 APIs
- **User Management**: 8 APIs  
- **Books Management**: 10 APIs
- **Categories/Authors/Publishers**: 15 APIs
- **Loan Management**: 12 APIs
- **Shopping Cart**: 5 APIs
- **Order Management**: 10 APIs
- **Payment**: 4 APIs
- **Document Management**: 10 APIs
- **Admin Dashboard**: 8 APIs
- **Reports**: 6 APIs
- **System Configuration**: 5 APIs
- **Notifications**: 4 APIs

### **TỔNG CỘNG: 102 APIs**

### **Phân loại theo quyền truy cập:**
- **Public APIs** (không cần auth): 20 APIs
- **User APIs** (cần đăng nhập): 30 APIs  
- **Librarian/Admin APIs**: 52 APIs

### **Phân loại theo HTTP Methods:**
- **GET**: 52 APIs (51%)
- **POST**: 25 APIs (24.5%)
- **PUT**: 20 APIs (19.6%)
- **DELETE**: 5 APIs (4.9%)
