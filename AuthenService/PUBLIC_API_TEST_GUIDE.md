# HƯỚNG DẪN TEST CÁC API PUBLIC

## 📋 **Tổng quan các thay đổi**

Tôi đã cập nhật các file sau để đảm bảo tất cả API public hoạt động đúng theo documentation:

### 1. **API Gateway** (`api-gateway/src/main/resources/application.yml`)
- ✅ Cập nhật routes để bao gồm tất cả endpoints public
- ✅ Thêm các endpoints Books, Categories, Authors, Publishers, Search, Documents
- ✅ Thêm health endpoints cho tất cả services

### 2. **GlobalAuthenticationFilter** (`api-gateway/.../GlobalAuthenticationFilter.java`)
- ✅ Cập nhật danh sách PUBLIC_PATHS bao gồm tất cả API public
- ✅ Thêm authentication service public endpoints
- ✅ Thêm library backend public endpoints

### 3. **Library Backend SecurityConfig** (`library-backend/.../SecurityConfig.java`)
- ✅ Cấu hình permitAll() cho tất cả endpoints public
- ✅ Thêm role-based access control cho admin endpoints
- ✅ Bảo vệ các endpoints yêu cầu authentication

### 4. **MinIO Service SecurityConfig** 
- ✅ Đã được cấu hình đúng (chỉ health endpoint public)

## 🔧 **Cách Test**

### Bước 1: Khởi động các services
```bash
# Từ thư mục gốc
docker-compose up -d

# Hoặc khởi động từng service riêng biệt
cd api-gateway && mvn spring-boot:run
cd AuthenService && mvn spring-boot:run  
cd library-backend && mvn spring-boot:run
cd MinIOService && mvn spring-boot:run
```

### Bước 2: Import Postman Collections
1. Import file `LIBRARY_BACKEND_PUBLIC_APIs.postman_collection.json`
2. Set environment variable `baseUrl` = `http://localhost:8080`
3. Test các endpoints mà không cần JWT token

### Bước 3: Test Manual các API Public

#### 🟢 **AuthenService Public APIs**
```bash
# Register
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","email":"test@example.com","password":"123456"}'

# Login  
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"123456"}'

# Send OTP
curl -X POST http://localhost:8080/api/v1/auth/sendotp \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com"}'

# Get JWK Token
curl -X GET http://localhost:8080/api/v1/auth/jwk/token
```

#### 🟢 **Library Backend Public APIs**

**Books:**
```bash
# Get all books
curl -X GET "http://localhost:8080/api/v1/books?page=0&size=10"

# Get book by ID
curl -X GET "http://localhost:8080/api/v1/books/1"

# Get popular books
curl -X GET "http://localhost:8080/api/v1/books/popular"

# Advanced search
curl -X POST http://localhost:8080/api/v1/books/search \
  -H "Content-Type: application/json" \
  -d '{"title":"java","author":"spring"}'
```

**Categories:**
```bash
# Get all categories
curl -X GET "http://localhost:8080/api/v1/categories?page=0&size=10"

# Get active categories
curl -X GET "http://localhost:8080/api/v1/categories/active"

# Get category by ID
curl -X GET "http://localhost:8080/api/v1/categories/1"
```

**Authors:**
```bash
# Get all authors
curl -X GET "http://localhost:8080/api/v1/authors?page=0&size=10"

# Search authors
curl -X GET "http://localhost:8080/api/v1/authors/search?keyword=robert"

# Get author with books
curl -X GET "http://localhost:8080/api/v1/authors/1/books"
```

**Publishers:**
```bash
# Get all publishers
curl -X GET "http://localhost:8080/api/v1/publishers?page=0&size=10"

# Search publishers
curl -X GET "http://localhost:8080/api/v1/publishers/search?name=OReilly"
```

**Search:**
```bash
# Full-text search
curl -X GET "http://localhost:8080/api/v1/search/fulltext?q=java+programming"

# Advanced search
curl -X POST http://localhost:8080/api/v1/search/advanced \
  -H "Content-Type: application/json" \
  -d '{"query":"java","categories":["programming"]}'

# Get search suggestions
curl -X GET "http://localhost:8080/api/v1/search/suggestions?q=java"
```

**Documents:**
```bash
# Search documents
curl -X GET "http://localhost:8080/api/v1/documents?q=programming"

# Get public documents
curl -X GET "http://localhost:8080/api/v1/documents/public"

# Get popular documents
curl -X GET "http://localhost:8080/api/v1/documents/popular"
```

#### 🟢 **Health Check APIs**
```bash
# Library Backend Health
curl -X GET "http://localhost:8080/api/v1/health"

# MinIO Service Health
curl -X GET "http://localhost:8080/api/v1/files/health"

# API Gateway Health
curl -X GET "http://localhost:8080/actuator/health"
```

## ❌ **Test Endpoints Protected (Sẽ trả về 401 Unauthorized)**

```bash
# Cart endpoints - yêu cầu USER role
curl -X GET "http://localhost:8080/api/v1/cart"

# Order endpoints - yêu cầu USER role  
curl -X GET "http://localhost:8080/api/v1/orders"

# Admin endpoints - yêu cầu ADMIN/LIBRARIAN role
curl -X GET "http://localhost:8080/api/v1/admin/books"

# File upload - yêu cầu authentication
curl -X POST "http://localhost:8080/api/v1/files/upload"
```

## 🔍 **Expected Responses**

### ✅ **Public APIs - Success (200/201)**
```json
{
  "status": "SUCCESS",
  "timestamp": 1704708000000,
  "data": { ... }
}
```

### ❌ **Protected APIs - Unauthorized (401)**
```json
{
  "code": "Unauthorized", 
  "message": "Unauthorized",
  "status": 401,
  "timestamp": "2024-01-08T10:00:00Z"
}
```

## 🐛 **Troubleshooting**

### Nếu Public APIs trả về 401:
1. Kiểm tra API Gateway có đang chạy không (`http://localhost:8080`)
2. Kiểm tra routes trong `application.yml`
3. Kiểm tra GlobalAuthenticationFilter
4. Kiểm tra SecurityConfig của từng service

### Nếu API Gateway không route đúng:
1. Kiểm tra logs của API Gateway
2. Kiểm tra các services backend có đang chạy không
3. Kiểm tra order của routes trong application.yml

### Nếu Database errors:
1. Kiểm tra PostgreSQL containers đang chạy
2. Kiểm tra connection strings trong application.properties
3. Chạy database migrations

## 📊 **Summary of Public Endpoints**

| Service | Public Endpoints | Total |
|---------|------------------|-------|
| **AuthenService** | login, register, otp, forgot-password, jwk | 6 |
| **Library Backend** | books, categories, authors, publishers, search, documents, health | 50+ |
| **MinIO Service** | health | 1 |
| **API Gateway** | actuator/health | 1 |

**Tổng cộng: 60+ public endpoints** có thể test mà không cần authentication!

## 🎯 **Next Steps**

1. **Test all public endpoints** bằng Postman Collection
2. **Verify error responses** cho protected endpoints
3. **Test authentication flow** để lấy JWT tokens
4. **Test protected endpoints** với JWT tokens
5. **Test role-based access control** với ADMIN/LIBRARIAN roles

---

**Generated on:** 2025-01-07  
**Updated by:** Claude Code Analysis  
**Status:** ✅ Ready for Testing