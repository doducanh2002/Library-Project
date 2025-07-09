-- =====================================================
-- LIBRARY PROJECT - TEST DATA SQL SCRIPT
-- =====================================================
-- Script cho database chính của Library Project
-- Database: library_project 
-- =====================================================

-- Chọn database
USE library_project;

-- Tắt safe update mode và foreign key checks tạm thời
SET SQL_SAFE_UPDATES = 0;
SET FOREIGN_KEY_CHECKS = 0;

-- Xóa dữ liệu cũ (nếu có) theo thứ tự dependency với WHERE clause an toàn
DELETE FROM document_access_logs WHERE id IS NOT NULL;
DELETE FROM documents WHERE id IS NOT NULL;
DELETE FROM notifications WHERE id IS NOT NULL;
DELETE FROM loans WHERE id IS NOT NULL;
DELETE FROM order_items WHERE id IS NOT NULL;
DELETE FROM orders WHERE id IS NOT NULL;
DELETE FROM cart_items WHERE id IS NOT NULL;
DELETE FROM book_authors WHERE book_id IS NOT NULL;
DELETE FROM books WHERE id IS NOT NULL;
DELETE FROM authors WHERE id IS NOT NULL;
DELETE FROM publishers WHERE id IS NOT NULL;
DELETE FROM categories WHERE id IS NOT NULL;

-- =====================================================
-- LIBRARY PROJECT DATA
-- =====================================================

-- Tạo categories
INSERT INTO `categories` (id, name, description, parent_category_id, slug, is_active, created_at) VALUES 
(1, 'Công nghệ thông tin', 'Sách về lập trình, phần mềm, máy tính', NULL, 'cong-nghe-thong-tin', true, NOW()),
(2, 'Văn học', 'Tiểu thuyết, thơ, văn học cổ điển', NULL, 'van-hoc', true, NOW()),
(3, 'Kinh tế', 'Sách về kinh tế, tài chính, kinh doanh', NULL, 'kinh-te', true, NOW()),
(4, 'Khoa học', 'Sách về toán học, vật lý, hóa học', NULL, 'khoa-hoc', true, NOW()),
(5, 'Lịch sử', 'Sách về lịch sử thế giới và Việt Nam', NULL, 'lich-su', true, NOW()),
(6, 'Lập trình', 'Sách về các ngôn ngữ lập trình', 1, 'lap-trinh', true, NOW()),
(7, 'Cơ sở dữ liệu', 'Sách về database, SQL, NoSQL', 1, 'co-so-du-lieu', true, NOW()),
(8, 'Tiểu thuyết', 'Tiểu thuyết Việt Nam và nước ngoài', 2, 'tieu-thuyet', true, NOW()),
(9, 'Thơ', 'Thơ ca Việt Nam và thế giới', 2, 'tho', true, NOW()),
(10, 'Marketing', 'Sách về marketing và quảng cáo', 3, 'marketing', true, NOW());

-- Tạo authors
INSERT INTO `authors` (id, name, biography, birth_date, death_date, nationality, website, created_at) VALUES 
(1, 'Robert C. Martin', 'Lập trình viên và tác giả nổi tiếng với Clean Code', '1952-12-05', NULL, 'American', 'https://blog.cleancoder.com', NOW()),
(2, 'Martin Fowler', 'Kiến trúc sư phần mềm và tác giả', '1963-12-18', NULL, 'British', 'https://martinfowler.com', NOW()),
(3, 'Nguyễn Nhật Ánh', 'Nhà văn nổi tiếng Việt Nam', '1955-05-07', NULL, 'Vietnamese', NULL, NOW()),
(4, 'Tô Hoài', 'Nhà văn Việt Nam', '1920-09-27', '2014-07-06', 'Vietnamese', NULL, NOW()),
(5, 'Dale Carnegie', 'Nhà văn và diễn giả người Mỹ', '1888-11-24', '1955-11-01', 'American', NULL, NOW()),
(6, 'Napoleon Hill', 'Tác giả sách về thành công', '1883-10-26', '1970-11-08', 'American', NULL, NOW()),
(7, 'Yuval Noah Harari', 'Nhà sử học và tác giả người Israel', '1976-02-24', NULL, 'Israeli', 'https://www.ynharari.com', NOW()),
(8, 'Stephen Hawking', 'Nhà vật lý lý thuyết', '1942-01-08', '2018-03-14', 'British', NULL, NOW()),
(9, 'Elon Musk', 'Doanh nhân và kỹ sư', '1971-06-28', NULL, 'South African', 'https://twitter.com/elonmusk', NOW()),
(10, 'Tim Berners-Lee', 'Nhà phát minh World Wide Web', '1955-06-08', NULL, 'British', 'https://www.w3.org/People/Berners-Lee/', NOW());

-- Tạo publishers
INSERT INTO `publishers` (id, name, address, contact_info, website, email, established_year, created_at) VALUES 
(1, 'Pearson Education', '221 River Street, Hoboken, NJ 07030', '+1-201-236-7000', 'https://www.pearson.com', 'info@pearson.com', 1844, NOW()),
(2, 'O\'Reilly Media', '1005 Gravenstein Highway North, Sebastopol, CA 95472', '+1-707-827-7000', 'https://www.oreilly.com', 'info@oreilly.com', 1978, NOW()),
(3, 'NXB Trẻ', '161B Lý Chính Thắng, Phường 7, Quận 3, TP.HCM', '028-39316289', 'https://nxbtre.com.vn', 'nxbtre@nxbtre.com.vn', 1981, NOW()),
(4, 'NXB Kim Đồng', '55 Quang Trung, Hai Bà Trưng, Hà Nội', '024-39434730', 'https://nxbkimdong.com.vn', 'info@nxbkimdong.com.vn', 1957, NOW()),
(5, 'Simon & Schuster', '1230 Avenue of the Americas, New York, NY 10020', '+1-212-698-7000', 'https://www.simonandschuster.com', 'info@simonandschuster.com', 1924, NOW()),
(6, 'Penguin Random House', '1745 Broadway, New York, NY 10019', '+1-212-782-9000', 'https://www.penguinrandomhouse.com', 'info@penguinrandomhouse.com', 1927, NOW()),
(7, 'McGraw-Hill Education', '2 Penn Plaza, New York, NY 10121', '+1-212-904-2000', 'https://www.mheducation.com', 'info@mheducation.com', 1888, NOW()),
(8, 'NXB Thông tin và Truyền thông', '115 Trần Duy Hưng, Cầu Giấy, Hà Nội', '024-35532207', 'https://mic.gov.vn', 'mic@mic.gov.vn', 1993, NOW()),
(9, 'Addison-Wesley', '75 Arlington Street, Boston, MA 02116', '+1-617-848-6000', 'https://www.addison-wesley.com', 'info@addison-wesley.com', 1942, NOW()),
(10, 'NXB Lao Động', '175 Giảng Võ, Đống Đa, Hà Nội', '024-37324919', 'https://nxblaodong.com.vn', 'nxblaodong@nxblaodong.com.vn', 1958, NOW());

-- Tạo books
INSERT INTO `books` (id, title, isbn, publication_year, description, cover_image_url, language, number_of_pages, edition, total_copies_for_loan, available_copies_for_loan, is_lendable, price, stock_for_sale, is_sellable, category_id, publisher_id, created_at, updated_at) VALUES 
(1, 'Clean Code: A Handbook of Agile Software Craftsmanship', '978-0132350884', 2008, 'Cuốn sách hướng dẫn viết code sạch và dễ bảo trì', 'https://images.amazon.com/images/P/0132350882.01.L.jpg', 'en', 464, '1st Edition', 10, 8, true, 850000, 20, true, 6, 1, NOW(), NOW()),
(2, 'Refactoring: Improving the Design of Existing Code', '978-0134757599', 2018, 'Kỹ thuật cải thiện thiết kế code hiện có', 'https://images.amazon.com/images/P/0134757599.01.L.jpg', 'en', 448, '2nd Edition', 8, 6, true, 920000, 15, true, 6, 9, NOW(), NOW()),
(3, 'Tôi thấy hoa vàng trên cỏ xanh', '978-6041215123', 2010, 'Tiểu thuyết nổi tiếng của Nguyễn Nhật Ánh', 'https://salt.tikicdn.com/cache/w1200/ts/product/5e/18/24/2a6154ba08df6ce6161c13f4303fa19e.jpg', 'vi', 544, 'Tái bản lần 20', 12, 10, true, 120000, 50, true, 8, 3, NOW(), NOW()),
(4, 'Dế Mèn phiêu lưu ký', '978-6041000100', 1941, 'Tác phẩm kinh điển dành cho thiếu nhi', 'https://salt.tikicdn.com/cache/w1200/ts/product/ee/8f/d0/29c5a3e5a1b3c8f2e1f8b5d6f7e8a9b0.jpg', 'vi', 200, 'Tái bản lần 50', 15, 12, true, 85000, 40, true, 8, 4, NOW(), NOW()),
(5, 'How to Win Friends and Influence People', '978-0671027032', 1936, 'Cuốn sách về kỹ năng giao tiếp và ảnh hưởng', 'https://images.amazon.com/images/P/0671027034.01.L.jpg', 'en', 288, 'Revised Edition', 6, 4, true, 320000, 25, true, 10, 5, NOW(), NOW()),
(6, 'Think and Grow Rich', '978-1585424337', 1937, 'Cuốn sách về tư duy và thành công', 'https://images.amazon.com/images/P/1585424331.01.L.jpg', 'en', 320, 'Original Edition', 8, 6, true, 280000, 30, true, 10, 6, NOW(), NOW()),
(7, 'Sapiens: A Brief History of Humankind', '978-0062316097', 2014, 'Lịch sử loài người từ thời nguyên thủy', 'https://images.amazon.com/images/P/0062316095.01.L.jpg', 'en', 464, '1st Edition', 10, 8, true, 390000, 35, true, 5, 6, NOW(), NOW()),
(8, 'A Brief History of Time', '978-0553380163', 1988, 'Cuốn sách về vũ trụ học phổ thông', 'https://images.amazon.com/images/P/0553380168.01.L.jpg', 'en', 256, 'Updated Edition', 5, 3, true, 450000, 20, true, 4, 6, NOW(), NOW()),
(9, 'Database System Concepts', '978-0078022159', 2019, 'Giáo trình về hệ thống cơ sở dữ liệu', 'https://images.amazon.com/images/P/0078022150.01.L.jpg', 'en', 1376, '7th Edition', 12, 10, true, 1200000, 15, true, 7, 7, NOW(), NOW()),
(10, 'Learning Python', '978-1449355739', 2013, 'Học Python từ cơ bản đến nâng cao', 'https://images.amazon.com/images/P/1449355730.01.L.jpg', 'en', 1648, '5th Edition', 15, 12, true, 950000, 25, true, 6, 2, NOW(), NOW()),
(11, 'The Pragmatic Programmer', '978-0135957059', 2019, 'Hướng dẫn trở thành lập trình viên chuyên nghiệp', 'https://images.amazon.com/images/P/0135957052.01.L.jpg', 'en', 352, '2nd Edition', 8, 6, true, 750000, 18, true, 6, 9, NOW(), NOW()),
(12, 'Design Patterns', '978-0201633612', 1994, 'Các mẫu thiết kế phần mềm', 'https://images.amazon.com/images/P/0201633610.01.L.jpg', 'en', 395, '1st Edition', 6, 4, true, 880000, 12, true, 6, 9, NOW(), NOW()),
(13, 'Effective Java', '978-0134685991', 2018, 'Lập trình Java hiệu quả', 'https://images.amazon.com/images/P/0134685997.01.L.jpg', 'en', 416, '3rd Edition', 10, 8, true, 820000, 20, true, 6, 9, NOW(), NOW()),
(14, 'Head First Design Patterns', '978-0596007126', 2004, 'Học Design Patterns một cách dễ hiểu', 'https://images.amazon.com/images/P/0596007124.01.L.jpg', 'en', 694, '1st Edition', 7, 5, true, 690000, 15, true, 6, 2, NOW(), NOW()),
(15, 'Artificial Intelligence: A Modern Approach', '978-0134610993', 2020, 'Giáo trình về trí tuệ nhân tạo', 'https://images.amazon.com/images/P/0134610997.01.L.jpg', 'en', 1152, '4th Edition', 8, 6, true, 1350000, 10, true, 1, 1, NOW(), NOW());

-- Tạo book_authors (nhiều tác giả cho một cuốn sách)
INSERT INTO `book_authors` (book_id, author_id, author_role) VALUES 
(1, 1, 'AUTHOR'),
(2, 2, 'AUTHOR'),
(3, 3, 'AUTHOR'),
(4, 4, 'AUTHOR'),
(5, 5, 'AUTHOR'),
(6, 6, 'AUTHOR'),
(7, 7, 'AUTHOR'),
(8, 8, 'AUTHOR'),
(9, 1, 'CO_AUTHOR'),
(10, 2, 'AUTHOR'),
(11, 1, 'CO_AUTHOR'),
(11, 2, 'CO_AUTHOR'),
(12, 2, 'AUTHOR'),
(13, 1, 'AUTHOR'),
(14, 2, 'AUTHOR'),
(15, 7, 'AUTHOR');

-- Tạo cart_items (giỏ hàng của user) - user_id tham chiếu đến Authentication Service
-- Lưu ý: user_id ở đây là BIGINT và sẽ được map với user_id từ JWT token
INSERT INTO `cart_items` (id, user_id, book_id, quantity, unit_price, created_at, updated_at) VALUES 
(1, 1, 1, 2, 850000, NOW(), NOW()),     -- user_id 1 maps to user-001 từ auth service
(2, 1, 3, 1, 120000, NOW(), NOW()),
(3, 1, 5, 1, 320000, NOW(), NOW()),
(4, 2, 2, 1, 920000, NOW(), NOW()),     -- user_id 2 maps to user-002 từ auth service
(5, 2, 4, 2, 85000, NOW(), NOW()),
(6, 2, 6, 1, 280000, NOW(), NOW()),
(7, 3, 7, 1, 390000, NOW(), NOW()),     -- user_id 3 maps to user-003 từ auth service
(8, 3, 8, 1, 450000, NOW(), NOW()),
(9, 3, 9, 1, 1200000, NOW(), NOW());

-- Tạo orders
INSERT INTO `orders` (id, user_id, order_code, order_date, sub_total_amount, shipping_fee, discount_amount, tax_amount, total_amount, status, payment_status, payment_method, shipping_address_line1, shipping_address_line2, shipping_city, shipping_postal_code, shipping_country, customer_note, created_at, updated_at) VALUES 
(1, 1, 'ORDER-001-2024', '2024-01-15 10:30:00', 1290000, 50000, 0, 129000, 1469000, 'DELIVERED', 'PAID', 'CREDIT_CARD', '123 Main St', 'Apartment 4B', 'Ho Chi Minh City', '700000', 'Vietnam', 'Giao hàng buổi sáng', NOW() - INTERVAL 30 DAY, NOW() - INTERVAL 25 DAY),
(2, 2, 'ORDER-002-2024', '2024-02-20 14:15:00', 1090000, 50000, 50000, 104000, 1194000, 'SHIPPED', 'PAID', 'VNPAY', '456 Oak Ave', NULL, 'Hanoi', '100000', 'Vietnam', 'Gọi trước khi giao', NOW() - INTERVAL 15 DAY, NOW() - INTERVAL 10 DAY),
(3, 3, 'ORDER-003-2024', '2024-03-10 09:45:00', 2040000, 0, 100000, 194000, 2134000, 'PROCESSING', 'PAID', 'BANK_TRANSFER', '789 Pine Rd', 'Floor 2', 'Da Nang', '550000', 'Vietnam', 'Miễn phí vận chuyển', NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 3 DAY),
(4, 1, 'ORDER-004-2024', '2024-04-05 16:20:00', 970000, 50000, 0, 102000, 1122000, 'PENDING_PAYMENT', 'UNPAID', 'COD', '123 Main St', 'Apartment 4B', 'Ho Chi Minh City', '700000', 'Vietnam', 'Thanh toán khi nhận hàng', NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 1 DAY),
(5, 2, 'ORDER-005-2024', '2024-05-12 11:10:00', 750000, 50000, 25000, 77500, 852500, 'CANCELLED', 'REFUNDED', 'CREDIT_CARD', '456 Oak Ave', NULL, 'Hanoi', '100000', 'Vietnam', 'Hủy do thay đổi ý định', NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 8 DAY);

-- Tạo order_items
INSERT INTO `order_items` (id, order_id, book_id, quantity, price_per_unit, item_total_price, book_title, book_isbn) VALUES 
(1, 1, 1, 1, 850000, 850000, 'Clean Code: A Handbook of Agile Software Craftsmanship', '978-0132350884'),
(2, 1, 3, 2, 120000, 240000, 'Tôi thấy hoa vàng trên cỏ xanh', '978-6041215123'),
(3, 1, 5, 1, 320000, 320000, 'How to Win Friends and Influence People', '978-0671027032'),
(4, 2, 2, 1, 920000, 920000, 'Refactoring: Improving the Design of Existing Code', '978-0134757599'),
(5, 2, 4, 2, 85000, 170000, 'Dế Mèn phiêu lưu ký', '978-6041000100'),
(6, 3, 7, 1, 390000, 390000, 'Sapiens: A Brief History of Humankind', '978-0062316097'),
(7, 3, 8, 1, 450000, 450000, 'A Brief History of Time', '978-0553380163'),
(8, 3, 9, 1, 1200000, 1200000, 'Database System Concepts', '978-0078022159'),
(9, 4, 10, 1, 950000, 950000, 'Learning Python', '978-1449355739'),
(10, 5, 11, 1, 750000, 750000, 'The Pragmatic Programmer', '978-0135957059');

-- Tạo loans
INSERT INTO `loans` (id, user_id, book_id, loan_date, due_date, return_date, status, fine_amount, fine_paid, notes_by_librarian, user_notes, approved_by, approved_at, returned_to, created_at, updated_at) VALUES 
(1, 1, 1, '2024-01-10 09:00:00', '2024-01-24 23:59:59', '2024-01-22 14:30:00', 'RETURNED', 0, true, 'Sách trả đúng hạn, tình trạng tốt', 'Sách rất hữu ích', 4, '2024-01-10 09:30:00', 4, NOW() - INTERVAL 45 DAY, NOW() - INTERVAL 30 DAY),
(2, 2, 3, '2024-02-15 10:15:00', '2024-03-01 23:59:59', '2024-03-05 16:45:00', 'RETURNED', 20000, true, 'Trả trễ 4 ngày', 'Xin lỗi vì trả trễ', 4, '2024-02-15 10:45:00', 4, NOW() - INTERVAL 35 DAY, NOW() - INTERVAL 20 DAY),
(3, 3, 7, '2024-03-20 11:30:00', '2024-04-03 23:59:59', NULL, 'BORROWED', 0, false, 'Đang mượn', 'Sách hay, cần thêm thời gian đọc', 4, '2024-03-20 12:00:00', NULL, NOW() - INTERVAL 25 DAY, NOW() - INTERVAL 20 DAY),
(4, 1, 10, '2024-04-10 14:20:00', '2024-04-24 23:59:59', NULL, 'BORROWED', 0, false, 'Đang mượn', 'Học Python', 4, '2024-04-10 14:50:00', NULL, NOW() - INTERVAL 15 DAY, NOW() - INTERVAL 10 DAY),
(5, 2, 2, '2024-05-05 08:45:00', '2024-05-19 23:59:59', NULL, 'OVERDUE', 15000, false, 'Quá hạn 3 ngày', 'Sẽ trả trong tuần này', 4, '2024-05-05 09:15:00', NULL, NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 5 DAY),
(6, 3, 11, '2024-05-15 16:10:00', '2024-05-29 23:59:59', NULL, 'REQUESTED', 0, false, NULL, 'Cần để nghiên cứu', NULL, NULL, NULL, NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 3 DAY),
(7, 1, 12, '2024-06-01 09:30:00', '2024-06-15 23:59:59', NULL, 'APPROVED', 0, false, 'Đã duyệt, chờ nhận sách', 'Cảm ơn', 4, '2024-06-01 10:00:00', NULL, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 2 DAY);

-- Tạo notifications - user_id tham chiếu đến Authentication Service thông qua JWT
INSERT INTO `notifications` (id, user_id, type, title, message, status, reference_id, reference_type, action_url, priority, is_email_sent, email_sent_at, read_at, expires_at, created_at, updated_at) VALUES 
(1, 'user-001', 'LOAN_APPROVED', 'Yêu cầu mượn sách đã được duyệt', 'Yêu cầu mượn sách "Clean Code" của bạn đã được duyệt. Vui lòng đến thư viện để nhận sách.', 'READ', 1, 'LOAN', '/loans/1', 2, true, NOW() - INTERVAL 45 DAY, NOW() - INTERVAL 44 DAY, NOW() + INTERVAL 30 DAY, NOW() - INTERVAL 45 DAY, NOW() - INTERVAL 44 DAY),
(2, 'user-001', 'LOAN_DUE_SOON', 'Sách sắp đến hạn trả', 'Sách "Clean Code" sẽ đến hạn trả vào ngày 24/01/2024. Vui lòng trả sách đúng hạn.', 'READ', 1, 'LOAN', '/loans/1', 3, true, NOW() - INTERVAL 32 DAY, NOW() - INTERVAL 31 DAY, NOW() + INTERVAL 15 DAY, NOW() - INTERVAL 32 DAY, NOW() - INTERVAL 31 DAY),
(3, 'user-002', 'LOAN_OVERDUE', 'Sách quá hạn trả', 'Sách "Refactoring" đã quá hạn trả. Vui lòng trả sách và thanh toán phí phạt 15,000 VND.', 'UNREAD', 5, 'LOAN', '/loans/5', 3, true, NOW() - INTERVAL 3 DAY, NULL, NOW() + INTERVAL 7 DAY, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY),
(4, 'user-001', 'ORDER_CONFIRMED', 'Đơn hàng đã được xác nhận', 'Đơn hàng ORDER-001-2024 của bạn đã được xác nhận và đang được xử lý.', 'READ', 1, 'ORDER', '/orders/ORDER-001-2024', 2, true, NOW() - INTERVAL 30 DAY, NOW() - INTERVAL 29 DAY, NOW() + INTERVAL 30 DAY, NOW() - INTERVAL 30 DAY, NOW() - INTERVAL 29 DAY),
(5, 'user-001', 'ORDER_SHIPPED', 'Đơn hàng đã được giao cho đơn vị vận chuyển', 'Đơn hàng ORDER-001-2024 đã được giao cho đơn vị vận chuyển. Mã vận đơn: VN123456789', 'READ', 1, 'ORDER', '/orders/ORDER-001-2024/track', 2, true, NOW() - INTERVAL 28 DAY, NOW() - INTERVAL 27 DAY, NOW() + INTERVAL 30 DAY, NOW() - INTERVAL 28 DAY, NOW() - INTERVAL 27 DAY),
(6, 'user-002', 'ORDER_DELIVERED', 'Đơn hàng đã được giao thành công', 'Đơn hàng ORDER-002-2024 đã được giao thành công. Cảm ơn bạn đã mua hàng!', 'UNREAD', 2, 'ORDER', '/orders/ORDER-002-2024', 1, true, NOW() - INTERVAL 10 DAY, NULL, NOW() + INTERVAL 30 DAY, NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 10 DAY),
(7, 'user-003', 'SYSTEM_MAINTENANCE', 'Bảo trì hệ thống', 'Hệ thống sẽ bảo trì từ 2:00 - 4:00 sáng ngày mai. Vui lòng không sử dụng trong thời gian này.', 'UNREAD', NULL, 'SYSTEM', NULL, 1, false, NULL, NULL, NOW() + INTERVAL 2 DAY, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY),
(8, 'user-001', 'BOOK_AVAILABLE', 'Sách bạn quan tâm đã có sẵn', 'Sách "Design Patterns" mà bạn đang chờ đã có sẵn. Vui lòng đặt mượn sớm.', 'UNREAD', 12, 'BOOK', '/books/12', 2, false, NULL, NULL, NOW() + INTERVAL 7 DAY, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY),
(9, 'user-002', 'FINE_PAYMENT', 'Thông báo thanh toán phí phạt', 'Bạn có phí phạt 20,000 VND cần thanh toán cho việc trả sách trễ. Vui lòng thanh toán tại quầy.', 'READ', 2, 'LOAN', '/loans/2', 3, true, NOW() - INTERVAL 20 DAY, NOW() - INTERVAL 19 DAY, NOW() + INTERVAL 30 DAY, NOW() - INTERVAL 20 DAY, NOW() - INTERVAL 19 DAY),
(10, 'user-003', 'PROMOTION', 'Khuyến mãi đặc biệt', 'Giảm 20% cho tất cả sách công nghệ trong tháng này. Mã giảm giá: TECH20', 'UNREAD', NULL, 'PROMOTION', '/books?category=technology', 1, false, NULL, NULL, NOW() + INTERVAL 25 DAY, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY),
(11, 'user-001', 'LOAN_REMINDER', 'Nhắc nhở trả sách', 'Bạn còn 3 ngày để trả sách "Learning Python". Vui lòng trả đúng hạn.', 'UNREAD', 4, 'LOAN', '/loans/4', 2, true, NOW() - INTERVAL 1 DAY, NULL, NOW() + INTERVAL 5 DAY, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY),
(12, 'user-002', 'ACCOUNT_SECURITY', 'Thông báo bảo mật tài khoản', 'Tài khoản của bạn đã đăng nhập từ thiết bị mới. Nếu không phải bạn, vui lòng đổi mật khẩu ngay.', 'READ', NULL, 'SECURITY', '/account/security', 3, true, NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 4 DAY, NOW() + INTERVAL 30 DAY, NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 4 DAY);

-- Tạo documents
INSERT INTO `documents` (id, title, description, file_name, original_file_name, file_type, file_size, mime_type, bucket_name, object_key, access_level, book_id, uploaded_by, download_count, is_active, created_at, updated_at) VALUES 
(1, 'Clean Code - Sample Chapter', 'Chương mẫu từ cuốn Clean Code', 'clean_code_sample_chapter.pdf', 'Clean Code - Chapter 1.pdf', 'PDF', 2048576, 'application/pdf', 'library-documents', 'books/clean-code/sample-chapter.pdf', 'PUBLIC', 1, 'user-004', 127, true, NOW() - INTERVAL 30 DAY, NOW() - INTERVAL 25 DAY),
(2, 'Refactoring - Code Examples', 'Ví dụ code từ cuốn Refactoring', 'refactoring_examples.zip', 'Refactoring Examples.zip', 'ZIP', 5242880, 'application/zip', 'library-documents', 'books/refactoring/code-examples.zip', 'RESTRICTED', 2, 'user-004', 45, true, NOW() - INTERVAL 25 DAY, NOW() - INTERVAL 20 DAY),
(3, 'Database Concepts - Exercise Solutions', 'Đáp án bài tập Database Concepts', 'db_concepts_solutions.pdf', 'Database Solutions.pdf', 'PDF', 1536000, 'application/pdf', 'library-documents', 'books/database-concepts/solutions.pdf', 'PRIVATE', 9, 'user-004', 23, true, NOW() - INTERVAL 20 DAY, NOW() - INTERVAL 15 DAY),
(4, 'Python Learning Resources', 'Tài liệu học Python bổ sung', 'python_resources.pdf', 'Python Learning Guide.pdf', 'PDF', 3072000, 'application/pdf', 'library-documents', 'books/python/learning-resources.pdf', 'PUBLIC', 10, 'user-004', 89, true, NOW() - INTERVAL 15 DAY, NOW() - INTERVAL 10 DAY),
(5, 'Design Patterns - UML Diagrams', 'Sơ đồ UML cho các Design Patterns', 'design_patterns_uml.pdf', 'Design Patterns UML.pdf', 'PDF', 4096000, 'application/pdf', 'library-documents', 'books/design-patterns/uml-diagrams.pdf', 'RESTRICTED', 12, 'user-004', 67, true, NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 5 DAY),
(6, 'Library Usage Guide', 'Hướng dẫn sử dụng thư viện', 'library_usage_guide.pdf', 'Library Guide.pdf', 'PDF', 1024000, 'application/pdf', 'library-documents', 'general/library-usage-guide.pdf', 'PUBLIC', NULL, 'user-004', 234, true, NOW() - INTERVAL 60 DAY, NOW() - INTERVAL 55 DAY),
(7, 'Book Borrowing Policies', 'Chính sách mượn sách', 'borrowing_policies.pdf', 'Borrowing Policies.pdf', 'PDF', 512000, 'application/pdf', 'library-documents', 'policies/borrowing-policies.pdf', 'PUBLIC', NULL, 'user-005', 156, true, NOW() - INTERVAL 50 DAY, NOW() - INTERVAL 45 DAY),
(8, 'Annual Report 2023', 'Báo cáo thường niên 2023', 'annual_report_2023.pdf', 'Annual Report 2023.pdf', 'PDF', 8192000, 'application/pdf', 'library-documents', 'reports/annual-report-2023.pdf', 'PRIVATE', NULL, 'user-005', 12, true, NOW() - INTERVAL 40 DAY, NOW() - INTERVAL 35 DAY);

-- Tạo document_access_logs
INSERT INTO `document_access_logs` (id, document_id, user_id, access_type, ip_address, user_agent, accessed_at) VALUES 
(1, 1, 'user-001', 'DOWNLOAD', '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', NOW() - INTERVAL 5 DAY),
(2, 1, 'user-002', 'VIEW', '192.168.1.101', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15', NOW() - INTERVAL 4 DAY),
(3, 2, 'user-001', 'DOWNLOAD', '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', NOW() - INTERVAL 3 DAY),
(4, 4, 'user-003', 'VIEW', '192.168.1.102', 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36', NOW() - INTERVAL 2 DAY),
(5, 6, 'user-001', 'DOWNLOAD', '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', NOW() - INTERVAL 1 DAY),
(6, 6, 'user-002', 'VIEW', '192.168.1.101', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15', NOW() - INTERVAL 1 DAY),
(7, 7, 'user-003', 'DOWNLOAD', '192.168.1.102', 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36', NOW() - INTERVAL 6 HOUR),
(8, 1, 'user-003', 'VIEW', '192.168.1.102', 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36', NOW() - INTERVAL 3 HOUR),
(9, 4, 'user-001', 'DOWNLOAD', '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', NOW() - INTERVAL 2 HOUR),
(10, 5, 'user-002', 'VIEW', '192.168.1.101', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15', NOW() - INTERVAL 1 HOUR);

-- Bật lại foreign key checks và safe update mode
SET FOREIGN_KEY_CHECKS = 1;
SET SQL_SAFE_UPDATES = 1;

-- =====================================================
-- USER ID MAPPING REFERENCE
-- =====================================================
/*
Mapping giữa Authentication Service và Library Project:

Authentication Service (user table):
- user-001 -> Nguyen Van A (user1)
- user-002 -> Tran Thi B (user2)  
- user-003 -> Le Van C (user3)
- user-004 -> Pham Thi D (librarian)
- user-005 -> Hoang Van E (admin)

Library Project (numeric user_id):
- 1 -> maps to user-001 (user1)
- 2 -> maps to user-002 (user2)
- 3 -> maps to user-003 (user3)
- 4 -> maps to user-004 (librarian)
- 5 -> maps to user-005 (admin)

Notifications và Documents sử dụng user_id từ JWT token (user-001, user-002, etc.)
Cart, Orders, Loans sử dụng numeric user_id (1, 2, 3, etc.)
*/

-- =====================================================
-- KIỂM TRA DỮ LIỆU
-- =====================================================

SELECT 'Library Project test data inserted successfully!' as message;
SELECT 'Total categories: ' as info, COUNT(*) as count FROM categories;
SELECT 'Total authors: ' as info, COUNT(*) as count FROM authors;
SELECT 'Total publishers: ' as info, COUNT(*) as count FROM publishers;
SELECT 'Total books: ' as info, COUNT(*) as count FROM books;
SELECT 'Total cart items: ' as info, COUNT(*) as count FROM cart_items;
SELECT 'Total orders: ' as info, COUNT(*) as count FROM orders;
SELECT 'Total loans: ' as info, COUNT(*) as count FROM loans;
SELECT 'Total notifications: ' as info, COUNT(*) as count FROM notifications;
SELECT 'Total documents: ' as info, COUNT(*) as count FROM documents;
SELECT 'Total document access logs: ' as info, COUNT(*) as count FROM document_access_logs;

-- =====================================================
-- SCRIPT HOÀN THÀNH
-- =====================================================