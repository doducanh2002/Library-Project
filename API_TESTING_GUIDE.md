# API Testing Guide - Library Management System

## 🚀 Khởi động hệ thống

### 1. **Khởi động tất cả services với Docker**
```bash
cd /path/to/Library-Project
docker-compose up -d
```

### 2. **Kiểm tra trạng thái services**
```bash
# Kiểm tra tất cả containers
docker-compose ps

# Xem logs của từng service
docker-compose logs api-gateway
docker-compose logs authentication-service
docker-compose logs book-catalog-service
docker-compose logs file-storage-service
```

### 3. **Health Check Endpoints**
```bash
# API Gateway Health
curl http://localhost:8080/actuator/health

# Authentication Service Health  
curl http://localhost:8081/actuator/health

# Book Catalog Service Health
curl http://localhost:8082/actuator/health

# File Storage Service Health
curl http://localhost:8083/actuator/health
```

---

## 🧪 Testing với Postman/Insomnia

### **Postman Collection Import**
Tạo collection mới trong Postman và import các endpoint sau:

**Base URL**: `http://localhost:8080` (API Gateway)

---

## 🔐 Authentication Service Tests

### **1. User Registration**
```http
POST http://localhost:8080/api/v1/auth/register
Content-Type: application/json

{
  "username": "testuser",
  "email": "test@example.com",
  "password": "Test123!@#",
  "confirmPassword": "Test123!@#",
  "firstName": "Test",
  "lastName": "User",
  "phoneNumber": "0123456789",
  "gender": "MALE"
}
```

### **2. User Login**
```http
POST http://localhost:8080/api/v1/auth/login
Content-Type: application/json

{
  "username": "testuser",
  "password": "Test123!@#"
}
```

**Response sẽ chứa JWT token:**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "userId": "1",
    "email": "test@example.com",
    "role": "USER"
  }
}
```

### **3. Get User Profile (Protected)**
```http
GET http://localhost:8080/api/v1/auth/profile
Authorization: Bearer YOUR_JWT_TOKEN
```

---

## 📚 Book Catalog Service Tests

### **Public Endpoints (Không cần authentication)**

### **1. Get All Books**
```http
GET http://localhost:8080/api/v1/books?page=0&size=10&sort=title,asc
```

### **2. Search Books**
```http
GET http://localhost:8080/api/v1/books/search?title=java&author=&category=&page=0&size=10
```

### **3. Get Book by ID**
```http
GET http://localhost:8080/api/v1/books/1
```

### **4. Get All Categories**
```http
GET http://localhost:8080/api/v1/categories?page=0&size=10
```

### **5. Get All Authors**
```http
GET http://localhost:8080/api/v1/authors?page=0&size=10
```

### **6. Get All Publishers**
```http
GET http://localhost:8080/api/v1/publishers?page=0&size=10
```

### **Protected Endpoints (Cần JWT token)**

### **7. Create Book (LIBRARIAN/ADMIN only)**
```http
POST http://localhost:8080/api/v1/books
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json

{
  "title": "Spring Boot in Action",
  "isbn": "978-1617292545",
  "description": "A comprehensive guide to Spring Boot",
  "publicationYear": 2024,
  "pageCount": 400,
  "stockQuantity": 50,
  "price": 45.99,
  "categoryId": 1,
  "publisherId": 1,
  "authorIds": [1, 2]
}
```

### **8. Create Category (LIBRARIAN/ADMIN only)**
```http
POST http://localhost:8080/api/v1/categories
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json

{
  "name": "Programming",
  "description": "Books about programming and software development",
  "parentCategoryId": null
}
```

### **9. Create Author (LIBRARIAN/ADMIN only)**
```http
POST http://localhost:8080/api/v1/authors
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json

{
  "firstName": "Craig",
  "lastName": "Walls",
  "biography": "Author of Spring in Action series",
  "nationality": "American",
  "birthDate": "1970-05-15",
  "email": "craig.walls@example.com"
}
```

### **10. Create Publisher (LIBRARIAN/ADMIN only)**
```http
POST http://localhost:8080/api/v1/publishers
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json

{
  "name": "Manning Publications",
  "description": "Technical book publisher",
  "establishedYear": 1990,
  "contactEmail": "info@manning.com",
  "website": "https://www.manning.com"
}
```

---

## 📁 File Storage Service Tests

### **1. Upload File (Protected)**
```http
POST http://localhost:8080/api/v1/files/upload
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: multipart/form-data

Form Data:
- file: [Choose your file]
- description: "Book cover image"
```

### **2. Get File Information**
```http
GET http://localhost:8080/api/v1/files/{fileId}
Authorization: Bearer YOUR_JWT_TOKEN
```

---

## 🧪 Testing với cURL Commands

### **Complete Test Flow:**

```bash
# 1. Register new user
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com", 
    "password": "Test123!@#",
    "confirmPassword": "Test123!@#",
    "firstName": "Test",
    "lastName": "User",
    "phoneNumber": "0123456789",
    "gender": "MALE"
  }'

# 2. Login and get JWT token
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Test123!@#"
  }'

# 3. Get books (public)
curl http://localhost:8080/api/v1/books

# 4. Create book (protected - replace TOKEN with actual JWT)
curl -X POST http://localhost:8080/api/v1/books \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Book",
    "isbn": "978-1234567890",
    "description": "A test book",
    "publicationYear": 2024,
    "pageCount": 200,
    "stockQuantity": 10,
    "price": 25.99,
    "categoryId": 1,
    "publisherId": 1,
    "authorIds": [1]
  }'
```

---

## 🌐 Testing với Swagger UI

### **Access Swagger Documentation:**
- **Book Catalog Service**: http://localhost:8082/swagger-ui.html
- **API Gateway**: http://localhost:8080/actuator/gateway/routes

---

## 🐛 Troubleshooting

### **Common Issues:**

1. **Services not starting:**
   ```bash
   # Check service logs
   docker-compose logs [service-name]
   
   # Restart services
   docker-compose down
   docker-compose up -d
   ```

2. **Database connection errors:**
   ```bash
   # Check database status
   docker-compose ps postgres-auth postgres-library
   
   # Check database logs
   docker-compose logs postgres-auth
   ```

3. **Authentication errors:**
   - Verify JWT token is valid and not expired
   - Check if user has required role (LIBRARIAN/ADMIN for protected endpoints)

4. **Rate limiting:**
   - API Gateway has rate limiting enabled
   - Wait or use different user/IP if hitting limits

---

## 📊 Expected Response Formats

### **Success Response:**
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... },
  "timestamp": "2024-01-15T10:30:00"
}
```

### **Error Response:**
```json
{
  "success": false,
  "message": "Error description",
  "error": "ERROR_CODE",
  "timestamp": "2024-01-15T10:30:00"
}
```

### **Validation Error:**
```json
{
  "success": false,
  "message": "Validation failed",
  "errors": [
    {
      "field": "title",
      "message": "Title is required"
    }
  ],
  "timestamp": "2024-01-15T10:30:00"
}
```

---

## 🎯 Test Scenarios to Cover

### **Basic Flow:**
1. ✅ Start all services
2. ✅ Register new user  
3. ✅ Login and get JWT token
4. ✅ Access public endpoints
5. ✅ Access protected endpoints with token
6. ✅ Test CRUD operations for books/categories/authors/publishers
7. ✅ Test file upload
8. ✅ Test rate limiting
9. ✅ Test role-based authorization

### **Error Scenarios:**
1. ❌ Access protected endpoint without token
2. ❌ Use invalid JWT token
3. ❌ Access admin endpoint with user role
4. ❌ Submit invalid data
5. ❌ Exceed rate limits

Bây giờ bạn có thể bắt đầu test hệ thống! 🚀