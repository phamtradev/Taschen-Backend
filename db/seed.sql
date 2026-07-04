-- ============================================================
-- Seed data for Taschen Backend (PostgreSQL / Neon)
-- ============================================================
-- WHEN TO RUN: after the app has started at least once, so Hibernate
--   (ddl-auto=update) has already created the tables. Then run this file
--   against your Neon database — via the Neon SQL Editor (paste & Run) or:
--     psql "postgresql://<user>:<pwd>@<host>/<db>?sslmode=require" -f db/seed.sql
--
-- SAFE TO RE-RUN: every statement is idempotent (INSERT ... WHERE NOT EXISTS
--   or ON CONFLICT DO NOTHING), so running twice will not create duplicates.
--
-- IDs are NOT hardcoded (columns are IDENTITY/serial); rows are linked by
-- natural keys (code / email / title) via sub-selects.
-- ============================================================


-- ------------------------------------------------------------
-- 1) ROLES  (register requires the 'USER' role to exist)
-- ------------------------------------------------------------
INSERT INTO roles (code, name)
SELECT 'USER', 'Khách hàng'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE code = 'USER');

INSERT INTO roles (code, name)
SELECT 'ADMIN', 'Quản trị viên'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE code = 'ADMIN');

INSERT INTO roles (code, name)
SELECT 'SELLER', 'Nhân viên bán hàng'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE code = 'SELLER');

INSERT INTO roles (code, name)
SELECT 'WAREHOUSE_STAFF', 'Nhân viên kho'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE code = 'WAREHOUSE_STAFF');


-- ------------------------------------------------------------
-- 2) PERMISSIONS  (checked by PermissionFilter for non-skip-listed paths)
--    Pattern '/api/**' matches every path under /api/ (matchPath: startsWith).
--    These give ADMIN full access. Customer endpoints (orders/cart/…) are in
--    the filter skip-list, so the USER role needs no permission rows.
-- ------------------------------------------------------------
INSERT INTO permissions (code, http_method, path_pattern, active) VALUES
    ('ADMIN_ALL_GET',    'GET',    '/api/**', true),
    ('ADMIN_ALL_POST',   'POST',   '/api/**', true),
    ('ADMIN_ALL_PUT',    'PUT',    '/api/**', true),
    ('ADMIN_ALL_PATCH',  'PATCH',  '/api/**', true),
    ('ADMIN_ALL_DELETE', 'DELETE', '/api/**', true)
ON CONFLICT (code) DO NOTHING;

-- Link the 5 permissions to the ADMIN role.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN
    ('ADMIN_ALL_GET','ADMIN_ALL_POST','ADMIN_ALL_PUT','ADMIN_ALL_PATCH','ADMIN_ALL_DELETE')
WHERE r.code = 'ADMIN'
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp
      WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );


-- ------------------------------------------------------------
-- 3) ADMIN USER
-- ------------------------------------------------------------
-- Password below is the canonical Spring Security BCrypt hash for the
-- plaintext:  password
-- The app uses BCryptPasswordEncoder, so this value works as-is.
--
-- >> If admin login fails, DO NOT trust this hash: instead register a user via
--    POST /api/auth/register, then run at the bottom of this file the block
--    "PROMOTE A REGISTERED USER TO ADMIN". Also change this password after first login.
INSERT INTO users (first_name, last_name, gender, email, phone_number, is_active, password)
SELECT 'Admin', 'Taschen', 'OTHER', 'admin@taschen.local', '0900000000', true,
       '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@taschen.local');

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.code = 'ADMIN'
WHERE u.email = 'admin@taschen.local'
  AND NOT EXISTS (
      SELECT 1 FROM user_roles ur WHERE ur.user_id = u.id AND ur.role_id = r.id
  );


-- ------------------------------------------------------------
-- 4) CATEGORIES  (code is unique)
-- ------------------------------------------------------------
INSERT INTO categories (code, name) VALUES
    ('FICTION',  'Tiểu thuyết'),
    ('SCIENCE',  'Khoa học'),
    ('BUSINESS', 'Kinh doanh'),
    ('CHILDREN', 'Thiếu nhi')
ON CONFLICT (code) DO NOTHING;


-- ------------------------------------------------------------
-- 5) VARIANTS (book formats)
-- ------------------------------------------------------------
INSERT INTO variants (format_code, format_name)
SELECT 'PAPERBACK', 'Bìa mềm'
WHERE NOT EXISTS (SELECT 1 FROM variants WHERE format_code = 'PAPERBACK');

INSERT INTO variants (format_code, format_name)
SELECT 'HARDCOVER', 'Bìa cứng'
WHERE NOT EXISTS (SELECT 1 FROM variants WHERE format_code = 'HARDCOVER');


-- ------------------------------------------------------------
-- 6) SUPPLIERS
-- ------------------------------------------------------------
INSERT INTO suppliers (name, email, phone, address, is_active)
SELECT 'NXB Trẻ', 'contact@nxbtre.vn', '02839316289', '161B Lý Chính Thắng, Q.3, TP.HCM', true
WHERE NOT EXISTS (SELECT 1 FROM suppliers WHERE name = 'NXB Trẻ');

INSERT INTO suppliers (name, email, phone, address, is_active)
SELECT 'NXB Kim Đồng', 'info@nxbkimdong.com.vn', '02439434730', '55 Quang Trung, Hai Bà Trưng, Hà Nội', true
WHERE NOT EXISTS (SELECT 1 FROM suppliers WHERE name = 'NXB Kim Đồng');


-- ------------------------------------------------------------
-- 7) BOOKS  (supplier linked by sub-select)
-- ------------------------------------------------------------
INSERT INTO books (title, author, description, publication_year, weight_grams, page_count,
                   price, stock_quantity, image_url, is_active, supplier_id)
SELECT 'Nhà Giả Kim', 'Paulo Coelho',
       'Câu chuyện về cậu bé chăn cừu Santiago đi tìm kho báu và ý nghĩa cuộc đời.',
       2020, 250, 228, 79000, 100,
       'https://res.cloudinary.com/demo/image/upload/nha-gia-kim.jpg', true,
       (SELECT id FROM suppliers WHERE name = 'NXB Trẻ' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = 'Nhà Giả Kim');

INSERT INTO books (title, author, description, publication_year, weight_grams, page_count,
                   price, stock_quantity, image_url, is_active, supplier_id)
SELECT 'Đắc Nhân Tâm', 'Dale Carnegie',
       'Nghệ thuật thu phục lòng người và giao tiếp hiệu quả.',
       2019, 320, 320, 88000, 80,
       'https://res.cloudinary.com/demo/image/upload/dac-nhan-tam.jpg', true,
       (SELECT id FROM suppliers WHERE name = 'NXB Trẻ' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = 'Đắc Nhân Tâm');

INSERT INTO books (title, author, description, publication_year, weight_grams, page_count,
                   price, stock_quantity, image_url, is_active, supplier_id)
SELECT 'Dế Mèn Phiêu Lưu Ký', 'Tô Hoài',
       'Tác phẩm thiếu nhi kinh điển của văn học Việt Nam.',
       2018, 200, 180, 55000, 120,
       'https://res.cloudinary.com/demo/image/upload/de-men.jpg', true,
       (SELECT id FROM suppliers WHERE name = 'NXB Kim Đồng' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = 'Dế Mèn Phiêu Lưu Ký');

INSERT INTO books (title, author, description, publication_year, weight_grams, page_count,
                   price, stock_quantity, image_url, is_active, supplier_id)
SELECT 'Sapiens: Lược Sử Loài Người', 'Yuval Noah Harari',
       'Hành trình tiến hóa và phát triển của loài người.',
       2021, 500, 560, 189000, 60,
       'https://res.cloudinary.com/demo/image/upload/sapiens.jpg', true,
       (SELECT id FROM suppliers WHERE name = 'NXB Trẻ' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = 'Sapiens: Lược Sử Loài Người');


-- ------------------------------------------------------------
-- 8) BOOK ↔ CATEGORY links (book_categories)
-- ------------------------------------------------------------
INSERT INTO book_categories (book_id, category_id)
SELECT b.id, c.id
FROM (VALUES
        ('Nhà Giả Kim',                 'FICTION'),
        ('Đắc Nhân Tâm',                'BUSINESS'),
        ('Dế Mèn Phiêu Lưu Ký',         'CHILDREN'),
        ('Sapiens: Lược Sử Loài Người', 'SCIENCE')
     ) AS x(title, cat_code)
JOIN books b      ON b.title = x.title
JOIN categories c ON c.code  = x.cat_code
WHERE NOT EXISTS (
    SELECT 1 FROM book_categories bc WHERE bc.book_id = b.id AND bc.category_id = c.id
);


-- ------------------------------------------------------------
-- 9) BOOK VARIANTS  (one PAPERBACK per seed book, price/stock copied from the book)
-- ------------------------------------------------------------
INSERT INTO book_variants (book_id, variant_id, price, stock_quantity)
SELECT b.id, v.id, b.price, b.stock_quantity
FROM books b
CROSS JOIN variants v
WHERE v.format_code = 'PAPERBACK'
  AND b.title IN ('Nhà Giả Kim', 'Đắc Nhân Tâm', 'Dế Mèn Phiêu Lưu Ký', 'Sapiens: Lược Sử Loài Người')
  AND NOT EXISTS (
      SELECT 1 FROM book_variants bv WHERE bv.book_id = b.id AND bv.variant_id = v.id
  );


-- ------------------------------------------------------------
-- 10) BANNERS
-- ------------------------------------------------------------
INSERT INTO banners (name, image_url)
SELECT 'Khuyến mãi mùa hè', 'https://res.cloudinary.com/demo/image/upload/banner-summer.jpg'
WHERE NOT EXISTS (SELECT 1 FROM banners WHERE name = 'Khuyến mãi mùa hè');

INSERT INTO banners (name, image_url)
SELECT 'Sách mới về', 'https://res.cloudinary.com/demo/image/upload/banner-newbooks.jpg'
WHERE NOT EXISTS (SELECT 1 FROM banners WHERE name = 'Sách mới về');


-- ============================================================
-- OPTIONAL: PROMOTE A REGISTERED USER TO ADMIN
-- ------------------------------------------------------------
-- Use this if the seeded admin hash does not work, or to make a real
-- registered account an admin. Register via POST /api/auth/register first,
-- then replace the email below and run:
--
-- UPDATE users SET is_active = true WHERE email = 'you@example.com';
-- INSERT INTO user_roles (user_id, role_id)
-- SELECT u.id, r.id FROM users u JOIN roles r ON r.code = 'ADMIN'
-- WHERE u.email = 'you@example.com'
--   AND NOT EXISTS (SELECT 1 FROM user_roles ur WHERE ur.user_id = u.id AND ur.role_id = r.id);
-- ============================================================
