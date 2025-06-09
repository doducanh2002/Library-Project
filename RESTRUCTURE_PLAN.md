# Microservices Restructure Plan

## 🎯 Objective
Chuyển đổi dự án từ monolith thành microservices architecture với các services được chuẩn hóa và tối ưu.

## 📋 Current State Analysis

### Existing Services:
1. **AuthenService** ✅ - Authentication service hoàn chỉnh
2. **MinIOService** ✅ - File storage service
3. **library-backend** ✅ - Book catalog service

### Issues to Address:
- ❌ Naming inconsistency
- ❌ No API Gateway
- ❌ Port conflicts potential
- ❌ No shared libraries
- ❌ No service discovery

## 🔄 Restructure Steps

### Step 1: Service Renaming & Standardization
```bash
# Current → New Names
AuthenService       → authentication-service
MinIOService        → file-storage-service  
library-backend     → book-catalog-service
```

### Step 2: Port Standardization
```yaml
api-gateway:           8080
authentication-service: 8081
book-catalog-service:  8082
file-storage-service:  8083
```

### Step 3: Package Structure Standardization
```
com.library.{service-name}
├── config/           # Configuration classes
├── controller/       # REST controllers
├── dto/             # Data transfer objects
├── entity/          # JPA entities (if applicable)
├── exception/       # Custom exceptions
├── service/         # Business logic
├── repository/      # Data access (if applicable)
├── client/          # Service clients
└── util/            # Utilities
```

## 🚀 Implementation Plan

### Phase 1: Authentication Service Optimization (1 day)
**Current**: AuthenService (Port 8081) ✅ Already good structure

**Tasks**:
- [ ] Rename to `authentication-service`
- [ ] Standardize package structure to `com.library.auth`
- [ ] Update application.properties for port 8081
- [ ] Add health check endpoint
- [ ] Document API endpoints

### Phase 2: File Storage Service Optimization (1 day) 
**Current**: MinIOService → **Target**: file-storage-service (Port 8083)

**Tasks**:
- [ ] Rename to `file-storage-service`
- [ ] Change package from `com.example.videoservice` to `com.library.filestorage`
- [ ] Update port to 8083
- [ ] Generalize from video-specific to generic file storage
- [ ] Add support for document/image types
- [ ] Implement file metadata management

### Phase 3: Book Catalog Service Optimization (1 day)
**Current**: library-backend → **Target**: book-catalog-service (Port 8082)

**Tasks**:
- [ ] Rename to `book-catalog-service`
- [ ] Keep package `com.library` (already good)
- [ ] Update port to 8082  
- [ ] Remove authentication logic (delegate to auth service)
- [ ] Add service client for authentication verification
- [ ] Implement health check endpoint

### Phase 4: API Gateway Implementation (2 days)
**New Service**: api-gateway (Port 8080)

**Tasks**:
- [ ] Create Spring Cloud Gateway project
- [ ] Implement authentication proxy
- [ ] Add rate limiting
- [ ] Configure CORS
- [ ] Add service routing rules
- [ ] Implement health checks aggregation

### Phase 5: Shared Library Creation (1 day)
**New Module**: shared-library

**Tasks**:
- [ ] Extract common DTOs (BaseResponse, etc.)
- [ ] Create common exception classes
- [ ] Add utility classes
- [ ] Create service client interfaces
- [ ] Implement common validation

### Phase 6: Docker Composition (1 day)
**File**: docker-compose.yml

**Services**:
- [ ] API Gateway
- [ ] Authentication Service  
- [ ] Book Catalog Service
- [ ] File Storage Service
- [ ] PostgreSQL (2 instances)
- [ ] Redis
- [ ] MinIO

## 📁 New Project Structure

```
Library-Project/
├── api-gateway/                    # Spring Cloud Gateway
│   ├── src/main/java/com/library/gateway/
│   ├── src/main/resources/
│   ├── pom.xml
│   └── Dockerfile
├── authentication-service/         # Renamed from AuthenService
│   ├── src/main/java/com/library/auth/
│   ├── src/main/resources/
│   ├── pom.xml
│   └── Dockerfile
├── book-catalog-service/           # Renamed from library-backend
│   ├── src/main/java/com/library/catalog/
│   ├── src/main/resources/
│   ├── pom.xml
│   └── Dockerfile
├── file-storage-service/          # Renamed from MinIOService
│   ├── src/main/java/com/library/filestorage/
│   ├── src/main/resources/
│   ├── pom.xml
│   └── Dockerfile
├── shared-library/                # Common components
│   ├── src/main/java/com/library/shared/
│   ├── pom.xml
│   └── README.md
├── docker-compose.yml            # All services
├── docker-compose.dev.yml        # Development override
├── pom.xml                       # Parent POM
├── README.md                     # Project overview
└── docs/                         # Documentation
    ├── api-documentation.md
    ├── deployment-guide.md
    └── development-setup.md
```

## 🔧 Technical Implementation Details

### 1. API Gateway Configuration
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: auth-service
          uri: http://authentication-service:8081
          predicates:
            - Path=/api/v1/auth/**
        - id: catalog-service
          uri: http://book-catalog-service:8082
          predicates:
            - Path=/api/v1/books/**,/api/v1/categories/**,/api/v1/authors/**
        - id: file-service
          uri: http://file-storage-service:8083
          predicates:
            - Path=/api/v1/files/**
```

### 2. Service Communication
```java
// In book-catalog-service
@FeignClient(name = "authentication-service")
public interface AuthServiceClient {
    @PostMapping("/api/v1/auth/validate")
    boolean validateToken(@RequestHeader("Authorization") String token);
}
```

### 3. Shared DTOs
```java
// In shared-library
public class BaseResponse<T> {
    private String status;
    private long timestamp;
    private T data;
    // ... rest of implementation
}
```

## 📊 Benefits After Restructure

✅ **Consistency**: All services follow same naming and structure  
✅ **Scalability**: Each service can scale independently  
✅ **Security**: Centralized authentication through gateway  
✅ **Maintainability**: Clear separation of concerns  
✅ **Development**: Easier to work on individual services  
✅ **Deployment**: Independent deployment capabilities  

## ⏱️ Timeline

- **Week 1**: Phases 1-3 (Service optimization)
- **Week 2**: Phases 4-5 (Gateway and shared library)  
- **Week 3**: Phase 6 (Docker composition and testing)
- **Week 4**: Documentation and deployment optimization

## 🚦 Success Criteria

- [ ] All services running independently
- [ ] API Gateway routing all requests correctly
- [ ] Authentication working across all services
- [ ] File storage integrated with catalog service
- [ ] All services containerized and orchestrated
- [ ] Health checks working for all services
- [ ] Documentation complete and accurate

## 🔄 Migration Strategy

1. **Parallel Development**: Keep existing services running while building new structure
2. **Gradual Migration**: Move one service at a time
3. **Feature Flags**: Use feature toggles for smooth transition
4. **Testing**: Comprehensive integration testing at each step
5. **Rollback Plan**: Ability to revert to previous structure if needed