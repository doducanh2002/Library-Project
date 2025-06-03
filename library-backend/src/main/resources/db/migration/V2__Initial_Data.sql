-- V2__Initial_Data.sql
-- Initial sample data for development and testing

-- Sample categories
INSERT INTO categories (name, description, parent_category_id) VALUES
('Fiction', 'Fictional works including novels and short stories', NULL),
('Non-Fiction', 'Factual and educational content', NULL),
('Science Fiction', 'Science fiction and futuristic stories', 1),
('Fantasy', 'Fantasy and magical stories', 1),
('History', 'Historical books and documents', 2),
('Science', 'Scientific books and research', 2),
('Technology', 'Technology and computer science', 2),
('Self-Help', 'Personal development and self-improvement', 2);

-- Sample publishers
INSERT INTO publishers (name, address, contact_info, website, email, established_year) VALUES
('Penguin Random House', '1745 Broadway, New York, NY 10019', '+1-212-782-9000', 'https://www.penguinrandomhouse.com', 'info@penguinrandomhouse.com', 1927),
('HarperCollins', '195 Broadway, New York, NY 10007', '+1-212-207-7000', 'https://www.harpercollins.com', 'info@harpercollins.com', 1989),
('O''Reilly Media', '1005 Gravenstein Highway North, Sebastopol, CA 95472', '+1-707-827-7000', 'https://www.oreilly.com', 'info@oreilly.com', 1978);

-- Sample authors
INSERT INTO authors (name, biography, birth_date, nationality) VALUES
('J.K. Rowling', 'British author best known for the Harry Potter fantasy series', '1965-07-31', 'British'),
('Stephen King', 'American author of horror, supernatural fiction, suspense, and fantasy novels', '1947-09-21', 'American'),
('Malcolm Gladwell', 'Canadian journalist, author, and public speaker', '1963-09-03', 'Canadian'),
('Yuval Noah Harari', 'Israeli public intellectual, historian and professor', '1976-02-24', 'Israeli');

-- Create a default admin user (password: admin123)
INSERT INTO users (username, password, email, full_name, phone_number, address)
VALUES ('admin', '$2a$10$YHVRGOscVYeMbIjkf5qRg.lYqB43jrIh1baf0mlk5wOa5419w1Dmu', 'admin@library.com', 'System Administrator', '0123456789', '123 Admin Street');

-- Create a default librarian user (password: librarian123)
INSERT INTO users (username, password, email, full_name, phone_number, address)
VALUES ('librarian', '$2a$10$mTrN5Y4TTlpmJPkEIrvj8OKQdRfzqP5rUz5CAj4lLdrenCmJceLni', 'librarian@library.com', 'Default Librarian', '0987654321', '456 Library Avenue');

-- Create a default test user (password: user123)
INSERT INTO users (username, password, email, full_name, phone_number, address)
VALUES ('testuser', '$2a$10$RBfNkwYH0f1GcC3o9v9xCO5fM9E2z5IfCsVKvJMVLOIO6Q5Y6xbLa', 'user@library.com', 'Test User', '0111222333', '789 User Road');

-- Assign roles
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r 
WHERE u.username = 'admin' AND r.name = 'ROLE_ADMIN';

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r 
WHERE u.username = 'librarian' AND r.name = 'ROLE_LIBRARIAN';

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r 
WHERE u.username = 'testuser' AND r.name = 'ROLE_USER';

-- Sample books
INSERT INTO books (title, isbn, publication_year, description, language, number_of_pages, 
                  total_copies_for_loan, available_copies_for_loan, is_lendable, 
                  price, stock_for_sale, is_sellable, category_id, publisher_id)
VALUES 
('Harry Potter and the Philosopher''s Stone', '978-0747532699', 1997, 
 'The first novel in the Harry Potter series and Rowling''s debut novel', 'en', 223,
 5, 5, true, 19.99, 20, true, 
 (SELECT id FROM categories WHERE name = 'Fantasy'),
 (SELECT id FROM publishers WHERE name = 'Penguin Random House')),
 
('The Shining', '978-0307743657', 1977,
 'A horror novel by American author Stephen King', 'en', 447,
 3, 3, true, 24.99, 15, true,
 (SELECT id FROM categories WHERE name = 'Fiction'),
 (SELECT id FROM publishers WHERE name = 'HarperCollins')),
 
('Outliers', '978-0316017930', 2008,
 'The Story of Success examines the factors that contribute to high levels of success', 'en', 309,
 2, 2, true, 29.99, 10, true,
 (SELECT id FROM categories WHERE name = 'Non-Fiction'),
 (SELECT id FROM publishers WHERE name = 'Penguin Random House')),
 
('Sapiens: A Brief History of Humankind', '978-0062316097', 2011,
 'A book that explores the history of humankind from the Stone Age to the modern day', 'en', 443,
 4, 4, true, 34.99, 25, true,
 (SELECT id FROM categories WHERE name = 'History'),
 (SELECT id FROM publishers WHERE name = 'HarperCollins'));

-- Link books to authors
INSERT INTO book_authors (book_id, author_id, author_role)
SELECT b.id, a.id, 'AUTHOR' FROM books b, authors a 
WHERE b.title LIKE 'Harry Potter%' AND a.name = 'J.K. Rowling';

INSERT INTO book_authors (book_id, author_id, author_role)
SELECT b.id, a.id, 'AUTHOR' FROM books b, authors a 
WHERE b.title = 'The Shining' AND a.name = 'Stephen King';

INSERT INTO book_authors (book_id, author_id, author_role)
SELECT b.id, a.id, 'AUTHOR' FROM books b, authors a 
WHERE b.title = 'Outliers' AND a.name = 'Malcolm Gladwell';

INSERT INTO book_authors (book_id, author_id, author_role)
SELECT b.id, a.id, 'AUTHOR' FROM books b, authors a 
WHERE b.title LIKE 'Sapiens%' AND a.name = 'Yuval Noah Harari';