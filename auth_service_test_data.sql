-- =====================================================
-- AUTHENTICATION SERVICE - TEST DATA SQL SCRIPT
-- =====================================================
-- Script cho database của Authentication Service
-- Database: authentication_service (hoặc tên DB của AuthenService)
-- =====================================================

-- Chọn database
USE authentication_service;

-- Tắt safe update mode và foreign key checks tạm thời
SET SQL_SAFE_UPDATES = 0;
SET FOREIGN_KEY_CHECKS = 0;

-- Xóa dữ liệu cũ (nếu có) với WHERE clause an toàn
DELETE FROM account_role WHERE id IS NOT NULL;
DELETE FROM account WHERE id IS NOT NULL;
DELETE FROM user WHERE id IS NOT NULL;
DELETE FROM role WHERE id IS NOT NULL;

-- =====================================================
-- AUTHENTICATION SERVICE DATA
-- =====================================================

-- Tạo roles
INSERT INTO `role` (id, name) VALUES 
('role-001', 'USER'),
('role-002', 'LIBRARIAN'),
('role-003', 'ADMIN');

-- Tạo users với ID cố định để đồng bộ với library_project
INSERT INTO `user` (id, email, name, gender, address) VALUES 
('user-001', 'user1@example.com', 'Nguyen Van A', 'MALE', '123 Main St, Ho Chi Minh City'),
('user-002', 'user2@example.com', 'Tran Thi B', 'FEMALE', '456 Oak Ave, Hanoi'),
('user-003', 'user3@example.com', 'Le Van C', 'MALE', '789 Pine Rd, Da Nang'),
('user-004', 'librarian@example.com', 'Pham Thi D', 'FEMALE', '321 Elm St, Ho Chi Minh City'),
('user-005', 'admin@example.com', 'Hoang Van E', 'MALE', '654 Maple Ave, Hanoi');

-- Tạo accounts (password: "password123" được mã hóa bằng BCrypt)
-- BCrypt hash của "password123": $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM1JshMhLwLjrV/oQqYi
INSERT INTO `account` (id, username, password, is_activated, is_locked, user_id) VALUES 
('acc-001', 'user1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM1JshMhLwLjrV/oQqYi', true, false, 'user-001'),
('acc-002', 'user2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM1JshMhLwLjrV/oQqYi', true, false, 'user-002'),
('acc-003', 'user3', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM1JshMhLwLjrV/oQqYi', true, false, 'user-003'),
('acc-004', 'librarian', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM1JshMhLwLjrV/oQqYi', true, false, 'user-004'),
('acc-005', 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM1JshMhLwLjrV/oQqYi', true, false, 'user-005');

-- Gán roles cho accounts
INSERT INTO `account_role` (id, account_id, role_id) VALUES 
('ar-001', 'acc-001', 'role-001'),  -- user1 -> USER
('ar-002', 'acc-002', 'role-001'),  -- user2 -> USER
('ar-003', 'acc-003', 'role-001'),  -- user3 -> USER
('ar-004', 'acc-004', 'role-002'),  -- librarian -> LIBRARIAN
('ar-005', 'acc-005', 'role-003');  -- admin -> ADMIN

-- Bật lại foreign key checks và safe update mode
SET FOREIGN_KEY_CHECKS = 1;
SET SQL_SAFE_UPDATES = 1;

-- =====================================================
-- THÔNG TIN TÀI KHOẢN TEST
-- =====================================================
/*
Tài khoản test cho Authentication Service:

1. Username: user1, Password: password123, Role: USER
   - user_id: user-001
   - account_id: acc-001
   
2. Username: user2, Password: password123, Role: USER
   - user_id: user-002
   - account_id: acc-002
   
3. Username: user3, Password: password123, Role: USER
   - user_id: user-003
   - account_id: acc-003
   
4. Username: librarian, Password: password123, Role: LIBRARIAN
   - user_id: user-004
   - account_id: acc-004
   
5. Username: admin, Password: password123, Role: ADMIN
   - user_id: user-005
   - account_id: acc-005

Lưu ý:
- Các user_id này sẽ được sử dụng làm foreign key trong library_project database
- JWT token sẽ chứa user_id để đồng bộ giữa 2 services
- Password đã được mã hóa bằng BCrypt với strength 10
*/

-- =====================================================
-- KIỂM TRA DỮ LIỆU
-- =====================================================

SELECT 'Authentication Service test data inserted successfully!' as message;
SELECT 'Total roles: ' as info, COUNT(*) as count FROM role;
SELECT 'Total users: ' as info, COUNT(*) as count FROM user;
SELECT 'Total accounts: ' as info, COUNT(*) as count FROM account;
SELECT 'Total account-role mappings: ' as info, COUNT(*) as count FROM account_role;

-- Hiển thị tài khoản test
SELECT 
    u.id as user_id,
    u.name as full_name,
    u.email,
    a.username,
    r.name as role_name
FROM user u
JOIN account a ON u.id = a.user_id
JOIN account_role ar ON a.id = ar.account_id
JOIN role r ON ar.role_id = r.id
ORDER BY u.id;

-- =====================================================
-- SCRIPT HOÀN THÀNH
-- =====================================================