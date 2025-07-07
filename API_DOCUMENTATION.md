# TỔNG HỢP CÁC API VÀ ROLE TRONG HỆ THỐNG LIBRARY PROJECT

## 1. TỔNG QUAN HỆ THỐNG

### Kiến trúc Microservice
Hệ thống Library Project được xây dựng theo kiến trúc microservice với 4 service chính:

| Service | Port | Mô tả | Database |
|---------|------|-------|----------|
| **api-gateway** | 8080 | API Gateway với JWT authentication | - |
| **AuthenService** | 8081 | Service xác thực và quản lý người dùng | PostgreSQL (auth_db) |
| **library-backend** | 8082 | Service quản lý thư viện chính | PostgreSQL (library_db) |
| **MinIOService** | 8083 | Service quản lý file | MinIO Object Storage |

### Hệ thống Role
Hệ thống sử dụng Role-Based Access Control (RBAC) với 3 role chính:

| Role | Mô tả | Quyền hạn |
|------|-------|-----------|
| **USER** | Người dùng thông thường | Sử dụng các chức năng cơ bản của thư viện |
| **LIBRARIAN** | Thủ thư | Quản lý sách, đơn hàng, mượn trả, thống kê |
| **ADMIN** | Quản trị viên | Toàn quyền quản lý hệ thống, người dùng, cấu hình |

### Cơ chế Xác thực
- **JWT Token**: Sử dụng JWT để xác thực người dùng
- **RSA Signature**: JWT được ký bằng RSA256
- **Role Claims**: Token chứa thông tin role của người dùng
- **Stateless**: Không lưu trữ session trên server

## 2. AUTHENSERVICE APIs

### 2.1 Authentication APIs (`/api/v1/auth`)

#### Đăng ký và Kích hoạt tài khoản
```http
POST /api/v1/auth/register
POST /api/v1/auth/sendotp
POST /api/v1/auth/active
```

| Method | Endpoint | Mô tả | Role yêu cầu | Ghi chú |
|--------|----------|--------|---------------|---------|
| POST | `/register` | Đăng ký tài khoản mới | Public | Tự động gán role USER |
| POST | `/sendotp` | Gửi mã OTP qua email | Public | Rate limit: 5 req/10 min |
| POST | `/active` | Kích hoạt tài khoản với OTP | Public | Xác thực OTP |

#### Đăng nhập và Xác thực
```http
POST /api/v1/auth/login
GET /api/v1/auth/jwk/token
```

| Method | Endpoint | Mô tả | Role yêu cầu | Ghi chú |
|--------|----------|--------|---------------|---------|
| POST | `/login` | Đăng nhập hệ thống | Public | Rate limit: 5 req/10 min |
| GET | `/jwk/token` | Lấy JWK public key | Public | Để verify JWT token |

#### Quản lý Mật khẩu
```http
POST /api/v1/auth/change-password
POST /api/v1/auth/forgot-password
POST /api/v1/auth/reset-password
```

| Method | Endpoint | Mô tả | Role yêu cầu | Ghi chú |
|--------|----------|--------|---------------|---------|
| POST | `/change-password` | Đổi mật khẩu | USER | Yêu cầu mật khẩu cũ |
| POST | `/forgot-password` | Yêu cầu reset mật khẩu | Public | Gửi OTP qua email |
| POST | `/reset-password` | Đặt lại mật khẩu | Public | Sử dụng OTP |

### 2.2 Admin User Management APIs (`/api/v1/admin/users`)

#### Quản lý Người dùng
```http
GET /api/v1/admin/users
GET /api/v1/admin/users/{userId}
PUT /api/v1/admin/users/{userId}/status
DELETE /api/v1/admin/users/{userId}
```

| Method | Endpoint | Mô tả | Role yêu cầu | Ghi chú |
|--------|----------|--------|---------------|---------|
| GET | `/` | Lấy danh sách người dùng | ADMIN, LIBRARIAN | Hỗ trợ pagination, filtering |
| GET | `/{userId}` | Lấy thông tin người dùng | ADMIN, LIBRARIAN | Chi tiết đầy đủ |
| PUT | `/{userId}/status` | Cập nhật trạng thái tài khoản | ADMIN | Active/Inactive, Lock/Unlock |
| DELETE | `/{userId}` | Xóa tài khoản | ADMIN | Xóa vĩnh viễn |

#### Quản lý Role
```http
PUT /api/v1/admin/users/{userId}/roles
GET /api/v1/admin/users/roles
```

| Method | Endpoint | Mô tả | Role yêu cầu | Ghi chú |
|--------|----------|--------|---------------|---------|
| PUT | `/{userId}/roles` | Cập nhật role người dùng | ADMIN | Assign/Remove roles |
| GET | `/roles` | Lấy danh sách role | ADMIN, LIBRARIAN | Tất cả roles available |

#### Thống kê và Báo cáo
```http
GET /api/v1/admin/users/statistics
GET /api/v1/admin/users/inactive
POST /api/v1/admin/users/{userId}/reset-password
```

| Method | Endpoint | Mô tả | Role yêu cầu | Ghi chú |
|--------|----------|--------|---------------|---------|
| GET | `/statistics` | Thống kê người dùng | ADMIN, LIBRARIAN | Dashboard metrics |
| GET | `/inactive` | Người dùng không hoạt động | ADMIN, LIBRARIAN | Configurable days |
| POST | `/{userId}/reset-password` | Admin reset mật khẩu | ADMIN | Force password reset |

### 2.3 Test APIs (`/api/v1/test`)

| Method | Endpoint | Mô tả | Role yêu cầu | Ghi chú |
|--------|----------|--------|---------------|---------|
| GET | `/test` | Test endpoint | USER | Kiểm tra authentication |

## 3. LIBRARY-BACKEND APIs

### 3.1 Book Management APIs

#### Public Book APIs (`/api/v1/books`)
```http
GET /api/v1/books
POST /api/v1/books/search
GET /api/v1/books/{id}
GET /api/v1/books/isbn/{isbn}
GET /api/v1/books/popular
```

| Method | Endpoint | Mô tả | Role yêu cầu | Ghi chú |
|--------|----------|--------|---------------|---------|
| GET | `/` | Tìm kiếm và liệt kê sách | Public | Pagination, filtering, sorting |
| POST | `/search` | Tìm kiếm nâng cao | Public | Advanced search criteria |
| GET | `/{id}` | Lấy thông tin sách theo ID | Public | Chi tiết đầy đủ |
| GET | `/isbn/{isbn}` | Lấy sách theo ISBN | Public | ISBN-10 hoặc ISBN-13 |
| GET | `/popular` | Lấy sách phổ biến | Public | Dựa trên views, orders |
| GET | `/category/{categoryId}` | Lấy sách theo danh mục | Public | Bao gồm subcategories |
| GET | `/author/{authorId}` | Lấy sách theo tác giả | Public | Tất cả sách của tác giả |
| GET | `/publisher/{publisherId}` | Lấy sách theo nhà xuất bản | Public | Tất cả sách của NXB |

#### Admin Book APIs (`/api/v1/admin/books`)
```http
POST /api/v1/admin/books
PUT /api/v1/admin/books/{id}
DELETE /api/v1/admin/books/{id}
PUT /api/v1/admin/books/{id}/stock
```

| Method | Endpoint | Mô tả | Role yêu cầu | Ghi chú |
|--------|----------|--------|---------------|---------|
| POST | `/` | Tạo sách mới | ADMIN, LIBRARIAN | Tạo book record |
| PUT | `/{id}` | Cập nhật thông tin sách | ADMIN, LIBRARIAN | Cập nhật metadata |
| DELETE | `/{id}` | Xóa sách | ADMIN, LIBRARIAN | Soft delete |
| GET | `/` | Lấy tất cả sách (admin) | ADMIN, LIBRARIAN | Bao gồm inactive |
| PUT | `/{id}/stock` | Cập nhật tồn kho | ADMIN, LIBRARIAN | Inventory management |
| PUT | `/{id}/loan-copies` | Cập nhật số bản cho mượn | ADMIN, LIBRARIAN | Loan inventory |

### 3.2 Category Management APIs

#### Public Category APIs (`/api/v1/categories`)
```http
GET /api/v1/categories
GET /api/v1/categories/active
GET /api/v1/categories/{categoryId}
GET /api/v1/categories/{categoryId}/hierarchy
```

| Method | Endpoint | Mô tả | Role yêu cầu | Ghi chú |
|--------|----------|--------|---------------|---------|
| GET | `/` | Lấy danh mục với phân trang | Public | Hierarchical structure |
| GET | `/active` | Lấy danh mục đang hoạt động | Public | Chỉ active categories |
| GET | `/root` | Lấy danh mục gốc | Public | Root level categories |
| GET | `/{categoryId}` | Lấy danh mục theo ID | Public | Chi tiết category |
| GET | `/slug/{slug}` | Lấy danh mục theo slug | Public | SEO-friendly URLs |
| GET | `/{categoryId}/subcategories` | Lấy danh mục con | Public | Child categories |
| GET | `/{categoryId}/hierarchy` | Lấy cấu trúc danh mục | Public | Full hierarchy path |
| GET | `/check-name` | Kiểm tra tên danh mục | Public | Validate uniqueness |
| GET | `/check-slug` | Kiểm tra slug danh mục | Public | Validate URL slug |

#### Admin Category APIs (`/api/v1/admin/categories`)
```http
POST /api/v1/admin/categories
PUT /api/v1/admin/categories/{categoryId}
DELETE /api/v1/admin/categories/{categoryId}
```

| Method | Endpoint | Mô tả | Role yêu cầu | Ghi chú |
|--------|----------|--------|---------------|---------|
| POST | `/` | Tạo danh mục mới | ADMIN, LIBRARIAN | Tạo category |
| PUT | `/{categoryId}` | Cập nhật danh mục | ADMIN, LIBRARIAN | Cập nhật metadata |
| DELETE | `/{categoryId}` | Xóa danh mục | ADMIN, LIBRARIAN | Cascade handling |
| GET | `/` | Lấy tất cả danh mục (admin) | ADMIN, LIBRARIAN | Bao gồm inactive |

### 3.3 Author Management APIs

#### Public Author APIs (`/api/v1/authors`)
```http
GET /api/v1/authors
GET /api/v1/authors/search
GET /api/v1/authors/{authorId}
GET /api/v1/authors/{authorId}/books
```

| Method | Endpoint | Mô tả | Role yêu cầu | Ghi chú |
|--------|----------|--------|---------------|---------|
| GET | `/` | Lấy tác giả với phân trang | Public | Pagination, sorting |
| GET | `/search` | Tìm kiếm tác giả | Public | Full-text search |
| GET | `/{authorId}` | Lấy tác giả theo ID | Public | Chi tiết tác giả |
| GET | `/{authorId}/books` | Lấy tác giả với sách | Public | Author + books |
| GET | `/name/{name}` | Tìm tác giả theo tên | Public | Exact/partial match |
| GET | `/nationality/{nationality}` | Lấy tác giả theo quốc tịch | Public | Filter by country |
| GET | `/prolific` | Lấy tác giả nhiều tác phẩm | Public | Most productive authors |
| GET | `/nationalities` | Lấy tất cả quốc tịch | Public | Unique countries list |

#### Admin Author APIs (`/api/v1/admin/authors`)
```http
POST /api/v1/admin/authors
PUT /api/v1/admin/authors/{authorId}
DELETE /api/v1/admin/authors/{authorId}
```

| Method | Endpoint | Mô tả | Role yêu cầu | Ghi chú |
|--------|----------|--------|---------------|---------|
| POST | `/` | Tạo tác giả mới | ADMIN, LIBRARIAN | Tạo author record |
| PUT | `/{authorId}` | Cập nhật tác giả | ADMIN, LIBRARIAN | Cập nhật thông tin |
| DELETE | `/{authorId}` | Xóa tác giả | ADMIN, LIBRARIAN | Cascade books check |
| GET | `/` | Lấy tất cả tác giả (admin) | ADMIN, LIBRARIAN | Bao gồm inactive |

### 3.4 Publisher Management APIs

#### Public Publisher APIs (`/api/v1/publishers`)
```http
GET /api/v1/publishers
GET /api/v1/publishers/{publisherId}
GET /api/v1/publishers/search
GET /api/v1/publishers/established
```

| Method | Endpoint | Mô tả | Role yêu cầu | Ghi chú |
|--------|----------|--------|---------------|---------|
| GET | `/` | Lấy nhà xuất bản với phân trang | Public | Pagination, filtering |
| GET | `/{publisherId}` | Lấy nhà xuất bản theo ID | Public | Chi tiết NXB |
| GET | `/{publisherId}/books` | Lấy nhà xuất bản với sách | Public | Publisher + books |
| GET | `/search` | Tìm kiếm nhà xuất bản | Public | Name-based search |
| GET | `/established` | Lấy NXB theo năm thành lập | Public | Year range filter |
| GET | `/active` | Lấy nhà xuất bản hoạt động | Public | Most active publishers |
| GET | `/check-name` | Kiểm tra tên nhà xuất bản | Public | Validate uniqueness |

#### Admin Publisher APIs (`/api/v1/admin/publishers`)
```http
POST /api/v1/admin/publishers
PUT /api/v1/admin/publishers/{publisherId}
DELETE /api/v1/admin/publishers/{publisherId}
```

| Method | Endpoint | Mô tả | Role yêu cầu | Ghi chú |
|--------|----------|--------|---------------|---------|
| POST | `/` | Tạo nhà xuất bản mới | ADMIN, LIBRARIAN | Tạo publisher record |
| PUT | `/{publisherId}` | Cập nhật nhà xuất bản | ADMIN, LIBRARIAN | Cập nhật thông tin |
| DELETE | `/{publisherId}` | Xóa nhà xuất bản | ADMIN, LIBRARIAN | Cascade books check |
| GET | `/` | Lấy tất cả NXB (admin) | ADMIN, LIBRARIAN | Bao gồm inactive |

### 3.5 Shopping Cart APIs (`/api/v1/cart`)

```http
GET /api/v1/cart
POST /api/v1/cart/items
PUT /api/v1/cart/items/{bookId}
DELETE /api/v1/cart/items/{bookId}
DELETE /api/v1/cart
```

| Method | Endpoint | Mô tả | Role yêu cầu | Ghi chú |
|--------|----------|--------|---------------|---------|
| GET | `/` | Lấy giỏ hàng | USER, ADMIN | Current cart items |
| GET | `/summary` | Tóm tắt giỏ hàng | USER, ADMIN | Count, total price |
| POST | `/items` | Thêm sản phẩm vào giỏ | USER, ADMIN | Add book to cart |
| PUT | `/items/{bookId}` | Cập nhật sản phẩm trong giỏ | USER, ADMIN | Update quantity |
| DELETE | `/items/{bookId}` | Xóa sản phẩm khỏi giỏ | USER, ADMIN | Remove specific item |
| DELETE | `/` | Xóa toàn bộ giỏ hàng | USER, ADMIN | Clear all items |
| GET | `/count` | Số lượng sản phẩm trong giỏ | USER, ADMIN | Item count |
| GET | `/quantity` | Tổng số lượng | USER, ADMIN | Total quantity |
| POST | `/validate` | Xác thực giỏ hàng | USER, ADMIN | Check availability |

### 3.6 Order Management APIs

#### User Order APIs (`/api/v1/orders`)
```http
POST /api/v1/orders/checkout
GET /api/v1/orders
GET /api/v1/orders/{orderCode}
POST /api/v1/orders/{orderCode}/cancel
```

| Method | Endpoint | Mô tả | Role yêu cầu | Ghi chú |
|--------|----------|--------|---------------|---------|
| POST | `/checkout` | Tạo đơn hàng từ giỏ hàng | USER, ADMIN | Create order |
| GET | `/calculate` | Tính tổng đơn hàng | USER, ADMIN | Price calculation |
| GET | `/` | Lịch sử đơn hàng | USER, ADMIN | User's orders |
| GET | `/current` | Đơn hàng hiện tại | USER, ADMIN | Active orders |
| GET | `/{orderCode}` | Chi tiết đơn hàng | USER, ADMIN | Order details |
| POST | `/{orderCode}/cancel` | Hủy đơn hàng | USER, ADMIN | Cancel order |
| GET | `/can-place-order` | Kiểm tra có thể đặt hàng | USER, ADMIN | Validation check |
| GET | `/history` | Lịch sử đơn hàng chi tiết | USER, ADMIN | Detailed history |
| GET | `/statistics` | Thống kê đơn hàng | USER, ADMIN | User statistics |
| POST | `/{orderCode}/reorder` | Đặt lại đơn hàng | USER, ADMIN | Reorder items |
| GET | `/{orderCode}/track` | Theo dõi đơn hàng | USER, ADMIN | Order tracking |

#### Admin Order APIs (`/api/v1/admin/orders`)
```http
GET /api/v1/admin/orders
PUT /api/v1/admin/orders/{orderId}/status
POST /api/v1/admin/orders/{orderId}/refund
GET /api/v1/admin/orders/statistics
```

| Method | Endpoint | Mô tả | Role yêu cầu | Ghi chú |
|--------|----------|--------|---------------|---------|
| GET | `/` | Lấy tất cả đơn hàng | ADMIN, LIBRARIAN | All orders |
| GET | `/status/{status}` | Lấy đơn hàng theo trạng thái | ADMIN, LIBRARIAN | Filter by status |
| GET | `/payment-status/{paymentStatus}` | Lấy đơn hàng theo trạng thái thanh toán | ADMIN, LIBRARIAN | Payment filter |
| GET | `/{orderId}` | Chi tiết đơn hàng | ADMIN, LIBRARIAN | Order details |
| PUT | `/{orderId}/status` | Cập nhật trạng thái đơn hàng | ADMIN, LIBRARIAN | Update status |
| PUT | `/{orderId}/payment-status` | Cập nhật trạng thái thanh toán | ADMIN, LIBRARIAN | Payment status |
| POST | `/{orderId}/refund` | Hoàn tiền | ADMIN | Process refund |
| GET | `/need-attention` | Đơn hàng cần chú ý | ADMIN, LIBRARIAN | Urgent orders |
| GET | `/statistics` | Thống kê đơn hàng | ADMIN, LIBRARIAN | Order metrics |
| PUT | `/bulk-update-status` | Cập nhật trạng thái hàng loạt | ADMIN, LIBRARIAN | Bulk operations |
| POST | `/{orderId}/notes` | Thêm ghi chú admin | ADMIN, LIBRARIAN | Admin notes |

### 3.7 Loan Management APIs

#### User Loan APIs (`/api/v1/loans`)
```http
POST /api/v1/loans/request
GET /api/v1/loans/my-loans
GET /api/v1/loans/my-loans/current
GET /api/v1/loans/{loanId}
```

| Method | Endpoint | Mô tả | Role yêu cầu | Ghi chú |
|--------|----------|--------|---------------|---------|
| POST | `/request` | Tạo yêu cầu mượn sách | USER | Create loan request |
| GET | `/my-loans` | Lịch sử mượn sách | USER | User's loan history |
| GET | `/my-loans/current` | Sách đang mượn | USER | Active loans |
| GET | `/{loanId}` | Chi tiết mượn sách | USER | Loan details |
| GET | `/can-borrow` | Kiểm tra có thể mượn | USER | Borrowing eligibility |
| GET | `/my-history` | Lịch sử mượn chi tiết | USER | Detailed history |
| GET | `/current/{loanId}` | Chi tiết sách đang mượn | USER | Current loan info |

#### Admin Loan APIs (`/api/v1/admin/loans`)
```http
GET /api/v1/admin/loans
POST /api/v1/admin/loans/{loanId}/approve
POST /api/v1/admin/loans/{loanId}/reject
POST /api/v1/admin/loans/{loanId}/return
```

| Method | Endpoint | Mô tả | Role yêu cầu | Ghi chú |
|--------|----------|--------|---------------|---------|
| GET | `/` | Lấy tất cả mượn sách | ADMIN, LIBRARIAN | All loans |
| GET | `/status/{status}` | Lấy mượn sách theo trạng thái | ADMIN, LIBRARIAN | Filter by status |
| GET | `/{loanId}` | Chi tiết mượn sách | ADMIN, LIBRARIAN | Loan details |
| POST | `/{loanId}/approve` | Phê duyệt mượn sách | ADMIN, LIBRARIAN | Approve request |
| POST | `/{loanId}/reject` | Từ chối mượn sách | ADMIN, LIBRARIAN | Reject request |
| POST | `/{loanId}/return` | Xử lý trả sách | ADMIN, LIBRARIAN | Process return |
| GET | `/overdue` | Lấy sách quá hạn | ADMIN, LIBRARIAN | Overdue loans |
| POST | `/update-overdue` | Cập nhật trạng thái quá hạn | ADMIN, LIBRARIAN | Update overdue |
| POST | `/{loanId}/calculate-fine` | Tính và cập nhật phạt | ADMIN, LIBRARIAN | Calculate fine |
| GET | `/unpaid-fines` | Lấy mượn sách chưa trả phạt | ADMIN, LIBRARIAN | Unpaid fines |

### 3.8 Search APIs (`/api/v1/search`)

```http
GET /api/v1/search/fulltext
POST /api/v1/search/advanced
GET /api/v1/search/suggestions
GET /api/v1/search/popular-terms
```

| Method | Endpoint | Mô tả | Role yêu cầu | Ghi chú |
|--------|----------|--------|---------------|---------|
| GET | `/fulltext` | Tìm kiếm toàn văn | Public | Full-text search |
| POST | `/advanced` | Tìm kiếm nâng cao | Public | Advanced criteria |
| GET | `/categories` | Tìm kiếm theo danh mục | Public | Category-based |
| GET | `/authors` | Tìm kiếm theo tác giả | Public | Author-based |
| GET | `/available-for-loan` | Tìm sách có thể mượn | Public | Loan availability |
| GET | `/available-for-sale` | Tìm sách có thể mua | Public | Sale availability |
| GET | `/recent` | Tìm sách mới thêm | Public | Recently added |
| GET | `/suggestions` | Gợi ý tìm kiếm | Public | Search suggestions |
| GET | `/popular-terms` | Từ khóa phổ biến | Public | Popular searches |
| GET | `/filters/performance` | Tìm kiếm tối ưu hiệu năng | Public | Performance optimized |

### 3.9 Document Management APIs

#### Public Document APIs (`/api/v1/documents`)
```http
GET /api/v1/documents
POST /api/v1/documents/search
GET /api/v1/documents/{id}
GET /api/v1/documents/{id}/download
```

| Method | Endpoint | Mô tả | Role yêu cầu | Ghi chú |
|--------|----------|--------|---------------|---------|
| GET | `/` | Tìm kiếm tài liệu | Public | Document search |
| POST | `/search` | Tìm kiếm tài liệu nâng cao | Public | Advanced search |
| GET | `/public` | Lấy tài liệu công khai | Public | Public documents |
| GET | `/{id}` | Lấy tài liệu theo ID | Public | Document details |
| GET | `/book/{bookId}` | Lấy tài liệu theo sách | Public | Book-related docs |
| GET | `/{id}/download` | Tải tài liệu | Public | Download document |
| GET | `/{id}/download-url` | Lấy URL tải | Public | Download URL |
| GET | `/{id}/view` | Xem tài liệu | Public | View document |
| GET | `/{id}/view-url` | Lấy URL xem | Public | View URL |
| GET | `/popular` | Tài liệu phổ biến | Public | Popular documents |

#### Protected Document APIs (`/api/v1/documents`)
```http
POST /api/v1/documents
PUT /api/v1/documents/{id}
DELETE /api/v1/documents/{id}
```

| Method | Endpoint | Mô tả | Role yêu cầu | Ghi chú |
|--------|----------|--------|---------------|---------|
| POST | `/` | Tải lên tài liệu | Authenticated | Upload document |
| PUT | `/{id}` | Cập nhật tài liệu | Authenticated | Update document |
| DELETE | `/{id}` | Xóa tài liệu | Authenticated | Delete document |

#### Admin Document APIs (`/api/v1/admin/documents`)
```http
GET /api/v1/admin/documents/statistics
GET /api/v1/admin/documents/{id}/access-logs
PUT /api/v1/admin/documents/{id}/activate
DELETE /api/v1/admin/documents/bulk-delete
```

| Method | Endpoint | Mô tả | Role yêu cầu | Ghi chú |
|--------|----------|--------|---------------|---------|
| GET | `/statistics` | Thống kê tài liệu | ADMIN, LIBRARIAN | Document stats |
| GET | `/{id}/access-logs` | Lịch sử truy cập tài liệu | ADMIN, LIBRARIAN | Access logs |
| GET | `/all` | Lấy tất cả tài liệu | ADMIN, LIBRARIAN | All documents |
| PUT | `/{id}/activate` | Kích hoạt tài liệu | ADMIN, LIBRARIAN | Activate document |
| PUT | `/{id}/deactivate` | Vô hiệu hóa tài liệu | ADMIN, LIBRARIAN | Deactivate document |
| GET | `/by-uploader/{uploaderId}` | Lấy tài liệu theo người tải | ADMIN, LIBRARIAN | By uploader |
| GET | `/orphaned` | Lấy tài liệu mồ côi | ADMIN, LIBRARIAN | Orphaned documents |
| PUT | `/bulk-update-access-level` | Cập nhật quyền truy cập hàng loạt | ADMIN, LIBRARIAN | Bulk access update |
| DELETE | `/bulk-delete` | Xóa hàng loạt | ADMIN, LIBRARIAN | Bulk delete |

### 3.10 Notification APIs

#### User Notification APIs (`/api/v1/notifications`)
```http
GET /api/v1/notifications
GET /api/v1/notifications/{id}
PUT /api/v1/notifications/{id}/read
DELETE /api/v1/notifications/{id}
```

| Method | Endpoint | Mô tả | Role yêu cầu | Ghi chú |
|--------|----------|--------|---------------|---------|
| GET | `/` | Lấy thông báo người dùng | USER, ADMIN, LIBRARIAN | User notifications |
| GET | `/{id}` | Lấy thông báo theo ID | USER, ADMIN, LIBRARIAN | Notification details |
| GET | `/recent` | Lấy thông báo gần đây | USER, ADMIN, LIBRARIAN | Recent notifications |
| GET | `/high-priority` | Lấy thông báo ưu tiên cao | USER, ADMIN, LIBRARIAN | High priority |
| GET | `/summary` | Tóm tắt thông báo | USER, ADMIN, LIBRARIAN | Notification summary |
| GET | `/unread-count` | Số thông báo chưa đọc | USER, ADMIN, LIBRARIAN | Unread count |
| PUT | `/{id}/read` | Đánh dấu đã đọc | USER, ADMIN, LIBRARIAN | Mark as read |
| PUT | `/mark-all-read` | Đánh dấu tất cả đã đọc | USER, ADMIN, LIBRARIAN | Mark all read |
| PUT | `/bulk-mark-read` | Đánh dấu đã đọc hàng loạt | USER, ADMIN, LIBRARIAN | Bulk mark read |
| PUT | `/{id}/archive` | Lưu trữ thông báo | USER, ADMIN, LIBRARIAN | Archive notification |
| DELETE | `/{id}` | Xóa thông báo | USER, ADMIN, LIBRARIAN | Delete notification |
| DELETE | `/bulk-delete` | Xóa hàng loạt | USER, ADMIN, LIBRARIAN | Bulk delete |
| POST | `/` | Tạo thông báo | ADMIN | Create notification |

#### Admin Notification APIs (`/api/v1/admin/notifications`)
```http
GET /api/v1/admin/notifications
POST /api/v1/admin/notifications
POST /api/v1/admin/notifications/broadcast
GET /api/v1/admin/notifications/statistics
```

| Method | Endpoint | Mô tả | Role yêu cầu | Ghi chú |
|--------|----------|--------|---------------|---------|
| GET | `/` | Lấy tất cả thông báo | ADMIN, LIBRARIAN | All notifications |
| GET | `/search` | Tìm kiếm thông báo | ADMIN, LIBRARIAN | Search notifications |
| POST | `/` | Tạo thông báo | ADMIN, LIBRARIAN | Create notification |
| POST | `/broadcast` | Phát thông báo | ADMIN, LIBRARIAN | Broadcast message |
| POST | `/system-maintenance` | Thông báo bảo trì hệ thống | ADMIN, LIBRARIAN | System maintenance |
| GET | `/email-pending` | Thông báo chờ gửi email | ADMIN, LIBRARIAN | Email queue |
| PUT | `/{id}/email-sent` | Đánh dấu email đã gửi | ADMIN, LIBRARIAN | Email sent status |
| POST | `/cleanup-expired` | Dọn dẹp thông báo hết hạn | ADMIN, LIBRARIAN | Cleanup expired |
| POST | `/cleanup-old` | Dọn dẹp thông báo cũ | ADMIN, LIBRARIAN | Cleanup old |
| GET | `/statistics` | Thống kê thông báo | ADMIN, LIBRARIAN | Notification stats |

### 3.11 Dashboard APIs (`/api/v1/admin/dashboard`)

```http
GET /api/v1/admin/dashboard/overview
GET /api/v1/admin/dashboard/statistics
GET /api/v1/admin/dashboard/system-config
PUT /api/v1/admin/dashboard/system-config
```

| Method | Endpoint | Mô tả | Role yêu cầu | Ghi chú |
|--------|----------|--------|---------------|---------|
| GET | `/overview` | Tổng quan dashboard | ADMIN, LIBRARIAN | Dashboard overview |
| GET | `/statistics` | Thống kê chi tiết | ADMIN, LIBRARIAN | Detailed statistics |
| GET | `/daily-stats` | Thống kê hàng ngày | ADMIN, LIBRARIAN | Daily metrics |
| GET | `/monthly-stats` | Thống kê hàng tháng | ADMIN, LIBRARIAN | Monthly metrics |
| GET | `/system-config` | Cấu hình hệ thống | ADMIN | System configuration |
| PUT | `/system-config` | Cập nhật cấu hình hệ thống | ADMIN | Update config |
| POST | `/refresh-cache` | Làm mới cache dashboard | ADMIN | Refresh cache |
| GET | `/health` | Trạng thái sức khỏe hệ thống | ADMIN, LIBRARIAN | System health |

### 3.12 Health Check API (`/api/v1/health`)

| Method | Endpoint | Mô tả | Role yêu cầu | Ghi chú |
|--------|----------|--------|---------------|---------|
| GET | `/` | Kiểm tra sức khỏe service | Public | Health check |

## 4. MINIOSERVICE APIs

### File Management APIs (`/api/v1/files`)

```http
POST /api/v1/files/upload
GET /api/v1/files/{fileId}
GET /api/v1/files/{fileId}/download
DELETE /api/v1/files/{fileId}
```

| Method | Endpoint | Mô tả | Role yêu cầu | Ghi chú |
|--------|----------|--------|---------------|---------|
| POST | `/upload` | Tải lên file | Authenticated | Upload file to MinIO |
| GET | `/{fileId}` | Lấy/xem file | Authenticated | View file inline |
| GET | `/{fileId}/info` | Lấy thông tin file | Authenticated | File metadata |
| GET | `/` | Danh sách tất cả file | Authenticated | List all files |
| GET | `/{fileId}/download` | Tải xuống file | Authenticated | Download file |
| DELETE | `/{fileId}` | Xóa file | Authenticated | Delete file |
| GET | `/health` | Kiểm tra sức khỏe service | Public | Health check |

## 5. API GATEWAY CONFIGURATION

### 5.1 Route Protection
API Gateway sử dụng hai layer bảo mật:

1. **GlobalAuthenticationFilter**: Kiểm tra Bearer token
2. **JwtAuthenticationGatewayFilterFactory**: Xác thực JWT và extract user info

### 5.2 Public Routes (Không yêu cầu JWT)
```yaml
# Authentication endpoints
- /api/v1/auth/login
- /api/v1/auth/register
- /api/v1/auth/forgot-password
- /api/v1/auth/reset-password
- /api/v1/auth/jwk/token

# Public catalog endpoints
- /api/v1/books (GET only)
- /api/v1/categories/public
- /api/v1/authors/public
- /api/v1/publishers/public

# Health endpoints
- /actuator
- /health
```

### 5.3 Protected Routes (Yêu cầu JWT)
```yaml
# User management
- /api/v1/auth/me
- /api/v1/auth/change-password
- /api/v1/admin/users/**

# Business logic
- /api/v1/cart/**
- /api/v1/orders/**
- /api/v1/loans/**
- /api/v1/notifications/**
- /api/v1/admin/**

# File operations
- /api/v1/files/**
```

### 5.4 Rate Limiting
```yaml
# Login endpoint: 5 requests per 10 minutes
- /api/v1/auth/login: 5/10

# Register endpoint: 2 requests per 5 minutes
- /api/v1/auth/register: 2/5
```

## 6. SECURITY MATRIX

### 6.1 Phân quyền theo Role

#### PUBLIC (Không yêu cầu authentication)
- ✅ Xem danh sách sách, tác giả, nhà xuất bản, danh mục
- ✅ Tìm kiếm sách và tài liệu
- ✅ Đăng ký, đăng nhập, quên mật khẩu
- ✅ Xem tài liệu công khai
- ✅ Health check endpoints

#### USER Role
- ✅ Tất cả quyền PUBLIC
- ✅ Quản lý giỏ hàng cá nhân
- ✅ Đặt hàng và theo dõi đơn hàng
- ✅ Mượn sách và xem lịch sử mượn
- ✅ Quản lý thông báo cá nhân
- ✅ Đổi mật khẩu
- ✅ Tải lên và quản lý tài liệu cá nhân
- ❌ Không có quyền admin

#### LIBRARIAN Role
- ✅ Tất cả quyền USER
- ✅ Quản lý sách, tác giả, nhà xuất bản, danh mục
- ✅ Xem và quản lý đơn hàng của người dùng
- ✅ Phê duyệt/từ chối yêu cầu mượn sách
- ✅ Quản lý việc trả sách và tính phạt
- ✅ Xem thống kê người dùng
- ✅ Quản lý tài liệu hệ thống
- ✅ Gửi thông báo hệ thống
- ✅ Xem dashboard và báo cáo
- ❌ Không thể thay đổi role người dùng
- ❌ Không thể xóa người dùng
- ❌ Không thể cấu hình hệ thống

#### ADMIN Role
- ✅ Tất cả quyền LIBRARIAN
- ✅ Quản lý đầy đủ người dùng (tạo, sửa, xóa)
- ✅ Thay đổi role người dùng
- ✅ Kích hoạt/vô hiệu hóa tài khoản
- ✅ Reset mật khẩu người dùng
- ✅ Xử lý hoàn tiền đơn hàng
- ✅ Cấu hình hệ thống
- ✅ Làm mới cache hệ thống
- ✅ Tất cả quyền cao nhất

### 6.2 Endpoint Security Summary

| Endpoint Pattern | Public | USER | LIBRARIAN | ADMIN |
|------------------|--------|------|-----------|--------|
| `/api/v1/auth/login,register,forgot-password` | ✅ | ✅ | ✅ | ✅ |
| `/api/v1/auth/change-password` | ❌ | ✅ | ✅ | ✅ |
| `/api/v1/books` (GET) | ✅ | ✅ | ✅ | ✅ |
| `/api/v1/admin/books` | ❌ | ❌ | ✅ | ✅ |
| `/api/v1/cart/**` | ❌ | ✅ | ✅ | ✅ |
| `/api/v1/orders/**` | ❌ | ✅ | ✅ | ✅ |
| `/api/v1/admin/orders/**` | ❌ | ❌ | ✅ | ✅ |
| `/api/v1/admin/orders/*/refund` | ❌ | ❌ | ❌ | ✅ |
| `/api/v1/loans/**` | ❌ | ✅ | ✅ | ✅ |
| `/api/v1/admin/loans/**` | ❌ | ❌ | ✅ | ✅ |
| `/api/v1/admin/users/**` | ❌ | ❌ | ✅* | ✅ |
| `/api/v1/admin/users/*/roles` | ❌ | ❌ | ❌ | ✅ |
| `/api/v1/admin/dashboard/system-config` | ❌ | ❌ | ❌ | ✅ |
| `/api/v1/files/**` | ❌ | ✅ | ✅ | ✅ |
| `/api/v1/search/**` | ✅ | ✅ | ✅ | ✅ |

*LIBRARIAN có thể xem user info nhưng không thể thay đổi role hoặc xóa user

## 7. TECHNICAL IMPLEMENTATION

### 7.1 Authentication Flow
```
1. User login → JWT token issued
2. Token contains: username, roles, expiration
3. API Gateway validates token on each request
4. User info propagated to microservices via headers
```

### 7.2 JWT Token Structure
```json
{
  "sub": "username",
  "roles": ["USER", "ADMIN"],
  "userId": "uuid",
  "email": "user@example.com",
  "iat": 1625097600,
  "exp": 1625184000
}
```

### 7.3 Database Schema
```sql
-- User management tables
users (id, username, email, password_hash, created_at, status)
roles (id, name)
user_roles (user_id, role_id)

-- Business logic tables
books (id, title, isbn, author_id, publisher_id, category_id)
orders (id, user_id, total_amount, status, created_at)
loans (id, user_id, book_id, loan_date, return_date, status)
notifications (id, user_id, title, content, type, read_at)
```

### 7.4 Error Handling
```json
{
  "status": 401,
  "message": "Unauthorized: Invalid or expired token",
  "timestamp": "2023-07-01T10:30:00Z",
  "path": "/api/v1/admin/users"
}
```

### 7.5 Rate Limiting
- Redis-based rate limiting
- Different limits for different endpoints
- Configurable per environment

## 8. DEPLOYMENT NOTES

### 8.1 Environment Variables
```bash
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=library_db

# JWT
JWT_SECRET=your-secret-key
JWT_EXPIRATION=86400

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# MinIO
MINIO_URL=http://localhost:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin
```

### 8.2 Docker Compose Services
```yaml
services:
  - api-gateway:8080
  - authen-service:8081
  - library-backend:8082
  - minio-service:8083
  - postgres-auth:5433
  - postgres-library:5434
  - redis:6379
  - minio:9000
```

### 8.3 Health Check Endpoints
```
GET /health - Service health status
GET /actuator/health - Spring Boot actuator health
```

---

## 9. CHANGELOG

### Version 1.0 (Current)
- Initial API documentation
- Complete role-based access control mapping
- All microservices endpoints documented
- Security configuration detailed

### Planned Features
- API versioning strategy
- Advanced search improvements
- Real-time notifications
- Enhanced file management
- Microservice monitoring

---

**Generated on:** 2025-01-07  
**Last Updated:** 2025-01-07  
**Version:** 1.0  
**Author:** Claude Code Analysis