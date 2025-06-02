-- V1__Initial_Schema.sql
-- Library Management System Database Schema
-- PostgreSQL Database

-- Enable UUID extension if needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Function for updating updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 1. Roles table
CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(20) UNIQUE NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_role_name CHECK (name IN ('ROLE_USER', 'ROLE_LIBRARIAN', 'ROLE_ADMIN'))
);

-- Insert default roles
INSERT INTO roles (name, description) VALUES
('ROLE_USER', 'Regular user - can borrow and purchase books'),
('ROLE_LIBRARIAN', 'Librarian - manage books, documents, loans'),
('ROLE_ADMIN', 'Administrator - full system access');

-- 2. Users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    full_name VARCHAR(100),
    phone_number VARCHAR(20),
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT true,
    last_login TIMESTAMP,
    
    CONSTRAINT chk_username_length CHECK (LENGTH(username) >= 3),
    CONSTRAINT chk_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

-- Trigger for users updated_at
CREATE TRIGGER update_users_updated_at 
    BEFORE UPDATE ON users
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- 3. User roles junction table
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id INT NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_by BIGINT,
    
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_by) REFERENCES users(id) ON DELETE SET NULL
);

-- 4. Categories table
CREATE TABLE categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    parent_category_id INT,
    slug VARCHAR(120) UNIQUE,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (parent_category_id) REFERENCES categories(id) ON DELETE SET NULL,
    CONSTRAINT chk_category_name_length CHECK (LENGTH(name) >= 2)
);

-- Function to generate category slug
CREATE OR REPLACE FUNCTION generate_category_slug()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.slug IS NULL OR NEW.slug = '' THEN
        NEW.slug = LOWER(REPLACE(REPLACE(NEW.name, ' ', '-'), '&', 'and'));
        NEW.slug = REGEXP_REPLACE(NEW.slug, '[^a-z0-9\-]', '', 'g');
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER category_slug_trigger
    BEFORE INSERT OR UPDATE ON categories
    FOR EACH ROW
    EXECUTE FUNCTION generate_category_slug();

-- 5. Authors table
CREATE TABLE authors (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    biography TEXT,
    birth_date DATE,
    death_date DATE,
    nationality VARCHAR(50),
    website VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_author_name_length CHECK (LENGTH(name) >= 2),
    CONSTRAINT chk_death_after_birth CHECK (death_date IS NULL OR death_date > birth_date)
);

-- Index for author search
CREATE INDEX idx_authors_name_fulltext ON authors USING gin(to_tsvector('english', name));

-- 6. Publishers table
CREATE TABLE publishers (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address TEXT,
    contact_info VARCHAR(255),
    website VARCHAR(255),
    email VARCHAR(100),
    established_year INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_publisher_name_length CHECK (LENGTH(name) >= 2),
    CONSTRAINT chk_established_year CHECK (established_year IS NULL OR 
        (established_year > 1400 AND established_year <= EXTRACT(YEAR FROM CURRENT_DATE)))
);

-- 7. Books table (main table)
CREATE TABLE books (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    isbn VARCHAR(20) UNIQUE NOT NULL,
    publication_year INT,
    description TEXT,
    cover_image_url VARCHAR(500),
    language VARCHAR(10) DEFAULT 'en',
    number_of_pages INT,
    edition VARCHAR(50),
    
    -- Loan management
    total_copies_for_loan INT NOT NULL DEFAULT 0,
    available_copies_for_loan INT NOT NULL DEFAULT 0,
    is_lendable BOOLEAN DEFAULT true,
    
    -- Sales management  
    price DECIMAL(12,2),
    stock_for_sale INT DEFAULT 0,
    is_sellable BOOLEAN DEFAULT false,
    
    -- Relationships
    category_id INT,
    publisher_id INT,
    
    -- Metadata
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign Keys
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    FOREIGN KEY (publisher_id) REFERENCES publishers(id) ON DELETE SET NULL,
    
    -- Constraints
    CONSTRAINT chk_book_title_length CHECK (LENGTH(title) >= 1),
    CONSTRAINT chk_isbn_format CHECK (isbn ~* '^[0-9\-X]{10,17}$'),
    CONSTRAINT chk_publication_year CHECK (publication_year IS NULL OR 
        (publication_year > 0 AND publication_year <= EXTRACT(YEAR FROM CURRENT_DATE) + 1)),
    CONSTRAINT chk_number_of_pages CHECK (number_of_pages IS NULL OR number_of_pages > 0),
    CONSTRAINT chk_price CHECK (price IS NULL OR price >= 0),
    CONSTRAINT chk_stock_for_sale CHECK (stock_for_sale >= 0),
    CONSTRAINT chk_loan_copies CHECK (available_copies_for_loan <= total_copies_for_loan),
    CONSTRAINT chk_loan_copies_non_negative CHECK (available_copies_for_loan >= 0),
    CONSTRAINT chk_sellable_price CHECK (
        (is_sellable = false) OR 
        (is_sellable = true AND price IS NOT NULL AND price > 0)
    )
);

-- Trigger for books updated_at
CREATE TRIGGER update_books_updated_at 
    BEFORE UPDATE ON books
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- Full-text search indexes
CREATE INDEX idx_books_title_fulltext ON books USING gin(to_tsvector('english', title));
CREATE INDEX idx_books_description_fulltext ON books USING gin(to_tsvector('english', description));

-- Performance indexes
CREATE INDEX idx_books_category ON books(category_id);
CREATE INDEX idx_books_publisher ON books(publisher_id);
CREATE INDEX idx_books_isbn ON books(isbn);
CREATE INDEX idx_books_sellable ON books(is_sellable) WHERE is_sellable = true;
CREATE INDEX idx_books_lendable ON books(is_lendable) WHERE is_lendable = true;
CREATE INDEX idx_books_available_loan ON books(available_copies_for_loan) WHERE available_copies_for_loan > 0;
CREATE INDEX idx_books_in_stock ON books(stock_for_sale) WHERE stock_for_sale > 0;

-- 8. Book authors junction table
CREATE TABLE book_authors (
    book_id BIGINT NOT NULL,
    author_id INT NOT NULL,
    author_role VARCHAR(50) DEFAULT 'AUTHOR',
    
    PRIMARY KEY (book_id, author_id),
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES authors(id) ON DELETE CASCADE,
    
    CONSTRAINT chk_author_role CHECK (author_role IN ('AUTHOR', 'CO_AUTHOR', 'EDITOR', 'TRANSLATOR'))
);

-- Index for finding books by author
CREATE INDEX idx_book_authors_author ON book_authors(author_id);

-- 9. Loans table
CREATE TABLE loans (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    
    -- Loan timeline
    loan_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    due_date TIMESTAMP NOT NULL,
    return_date TIMESTAMP,
    
    -- Status management
    status VARCHAR(20) NOT NULL DEFAULT 'REQUESTED',
    
    -- Fine management
    fine_amount DECIMAL(10,2) DEFAULT 0,
    fine_paid BOOLEAN DEFAULT false,
    
    -- Notes
    notes_by_librarian TEXT,
    user_notes TEXT,
    
    -- Processing info
    approved_by BIGINT,
    approved_at TIMESTAMP,
    returned_to BIGINT,
    
    -- Metadata
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign Keys
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (returned_to) REFERENCES users(id) ON DELETE SET NULL,
    
    -- Constraints
    CONSTRAINT chk_loan_status CHECK (status IN (
        'REQUESTED', 'APPROVED', 'BORROWED', 'RETURNED', 'OVERDUE', 'CANCELLED'
    )),
    CONSTRAINT chk_due_date_after_loan CHECK (due_date > loan_date),
    CONSTRAINT chk_return_date_after_loan CHECK (return_date IS NULL OR return_date >= loan_date),
    CONSTRAINT chk_fine_amount CHECK (fine_amount >= 0),
    CONSTRAINT chk_approved_status CHECK (
        (status IN ('REQUESTED', 'CANCELLED') AND approved_by IS NULL) OR
        (status NOT IN ('REQUESTED', 'CANCELLED') AND approved_by IS NOT NULL)
    )
);

-- Trigger for loans updated_at
CREATE TRIGGER update_loans_updated_at 
    BEFORE UPDATE ON loans
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- Indexes for loans
CREATE INDEX idx_loans_user_status ON loans(user_id, status);
CREATE INDEX idx_loans_book_status ON loans(book_id, status);
CREATE INDEX idx_loans_due_date ON loans(due_date);
CREATE INDEX idx_loans_status ON loans(status);
CREATE INDEX idx_loans_overdue ON loans(due_date) WHERE status = 'BORROWED' AND due_date < CURRENT_TIMESTAMP;

-- 10. Order sequence for generating order codes
CREATE SEQUENCE order_code_seq START 1;

-- 11. Orders table
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    order_code VARCHAR(50) UNIQUE NOT NULL,
    
    -- Order timeline
    order_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Financial info
    sub_total_amount DECIMAL(12,2) NOT NULL,
    shipping_fee DECIMAL(10,2) DEFAULT 0,
    discount_amount DECIMAL(10,2) DEFAULT 0,
    tax_amount DECIMAL(10,2) DEFAULT 0,
    total_amount DECIMAL(12,2) NOT NULL,
    
    -- Status management
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING_PAYMENT',
    payment_status VARCHAR(30) DEFAULT 'UNPAID',
    payment_method VARCHAR(50),
    payment_transaction_id VARCHAR(100),
    
    -- Shipping info
    shipping_address_line1 VARCHAR(255),
    shipping_address_line2 VARCHAR(255),
    shipping_city VARCHAR(100),
    shipping_postal_code VARCHAR(20),
    shipping_country VARCHAR(50) DEFAULT 'Vietnam',
    shipping_date TIMESTAMP,
    delivery_date TIMESTAMP,
    
    -- Notes
    customer_note TEXT,
    admin_notes TEXT,
    
    -- Metadata
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign Keys
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    -- Constraints
    CONSTRAINT chk_order_status CHECK (status IN (
        'PENDING_PAYMENT', 'PAID', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED', 'REFUNDED'
    )),
    CONSTRAINT chk_payment_status CHECK (payment_status IN (
        'UNPAID', 'PAID', 'FAILED', 'REFUNDED', 'PARTIALLY_REFUNDED'
    )),
    CONSTRAINT chk_amounts CHECK (
        sub_total_amount >= 0 AND 
        shipping_fee >= 0 AND 
        discount_amount >= 0 AND
        tax_amount >= 0 AND
        total_amount >= 0
    ),
    CONSTRAINT chk_total_calculation CHECK (
        total_amount = sub_total_amount + shipping_fee + tax_amount - discount_amount
    )
);

-- Function to generate order code
CREATE OR REPLACE FUNCTION generate_order_code()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.order_code IS NULL OR NEW.order_code = '' THEN
        NEW.order_code = 'ORD-' || TO_CHAR(NEW.order_date, 'YYYY') || '-' || 
                        LPAD(nextval('order_code_seq')::TEXT, 6, '0');
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER generate_order_code_trigger
    BEFORE INSERT ON orders
    FOR EACH ROW
    EXECUTE FUNCTION generate_order_code();

CREATE TRIGGER update_orders_updated_at 
    BEFORE UPDATE ON orders
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- Indexes for orders
CREATE INDEX idx_orders_user ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_payment_status ON orders(payment_status);
CREATE INDEX idx_orders_order_date ON orders(order_date);
CREATE INDEX idx_orders_code ON orders(order_code);

-- 12. Order items table
CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    
    -- Quantity and pricing
    quantity INT NOT NULL,
    price_per_unit DECIMAL(12,2) NOT NULL,
    item_total_price DECIMAL(12,2) NOT NULL,
    
    -- Snapshot data (at time of order)
    book_title VARCHAR(255) NOT NULL,
    book_isbn VARCHAR(20) NOT NULL,
    
    -- Foreign Keys
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE RESTRICT,
    
    -- Constraints
    CONSTRAINT chk_quantity CHECK (quantity > 0),
    CONSTRAINT chk_price_per_unit CHECK (price_per_unit >= 0),
    CONSTRAINT chk_item_total_price CHECK (item_total_price >= 0),
    CONSTRAINT chk_item_total_calculation CHECK (item_total_price = quantity * price_per_unit),
    
    -- Unique constraint per order
    UNIQUE(order_id, book_id)
);

-- Indexes for order items
CREATE INDEX idx_order_items_order ON order_items(order_id);
CREATE INDEX idx_order_items_book ON order_items(book_id);

-- 13. Cart items table
CREATE TABLE cart_items (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign Keys
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    
    -- Constraints
    CONSTRAINT chk_cart_quantity CHECK (quantity > 0),
    
    -- Unique constraint per user/book
    UNIQUE(user_id, book_id)
);

-- Trigger for cart items updated_at
CREATE TRIGGER update_cart_items_updated_at 
    BEFORE UPDATE ON cart_items
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- Indexes for cart items
CREATE INDEX idx_cart_items_user ON cart_items(user_id);
CREATE INDEX idx_cart_items_book ON cart_items(book_id);

-- 14. Notifications table
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT false,
    read_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign Keys
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    -- Constraints
    CONSTRAINT chk_notification_type CHECK (type IN (
        'LOAN_APPROVED', 'LOAN_DUE_SOON', 'LOAN_OVERDUE', 
        'ORDER_CONFIRMED', 'ORDER_SHIPPED', 'ORDER_DELIVERED',
        'SYSTEM_ANNOUNCEMENT', 'NEW_BOOK_AVAILABLE'
    ))
);

-- Indexes for notifications
CREATE INDEX idx_notifications_user_unread ON notifications(user_id) WHERE is_read = false;
CREATE INDEX idx_notifications_created_at ON notifications(created_at);

-- 15. Documents table
CREATE TABLE documents (
    id BIGSERIAL PRIMARY KEY,
    book_id BIGINT NOT NULL,
    document_type VARCHAR(50) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    minio_object_name VARCHAR(500) NOT NULL,
    minio_bucket_name VARCHAR(255) NOT NULL,
    access_level VARCHAR(30) DEFAULT 'RESTRICTED',
    description TEXT,
    uploaded_by BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign Keys
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE SET NULL,
    
    -- Constraints
    CONSTRAINT chk_document_type CHECK (document_type IN (
        'PDF', 'EPUB', 'PREVIEW', 'AUDIO', 'VIDEO', 'OTHER'
    )),
    CONSTRAINT chk_access_level CHECK (access_level IN (
        'PUBLIC', 'RESTRICTED', 'PREMIUM', 'ADMIN_ONLY'
    )),
    CONSTRAINT chk_file_size CHECK (file_size > 0)
);

-- Trigger for documents updated_at
CREATE TRIGGER update_documents_updated_at 
    BEFORE UPDATE ON documents
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- Indexes for documents
CREATE INDEX idx_documents_book ON documents(book_id);
CREATE INDEX idx_documents_type ON documents(document_type);
CREATE INDEX idx_documents_access_level ON documents(access_level);

-- 16. Refresh tokens table for JWT
CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(500) UNIQUE NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign Keys
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Indexes for refresh tokens
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_tokens_expires ON refresh_tokens(expires_at);

-- Clean up expired tokens periodically
CREATE INDEX idx_refresh_tokens_cleanup ON refresh_tokens(expires_at) WHERE expires_at < CURRENT_TIMESTAMP;