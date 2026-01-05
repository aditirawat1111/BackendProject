-- Insert Categories
INSERT INTO category (id, name, description, created_at, last_modified, is_deleted)
VALUES
    (1, 'Electronics', 'Electronic devices and accessories', NOW(), NOW(), b'0'),
    (2, 'Clothing', 'Fashion and apparel', NOW(), NOW(), b'0'),
    (3, 'Books', 'Books and educational materials', NOW(), NOW(), b'0'),
    (4, 'Home & Kitchen', 'Home appliances and kitchen items', NOW(), NOW(), b'0'),
    (5, 'Sports & Fitness', 'Sports equipment and fitness gear', NOW(), NOW(), b'0');

-- Insert Products for Electronics Category
INSERT INTO product (id, name, description, price, image_url, category_id, created_at, last_modified, is_deleted)
VALUES
    (1, 'Wireless Bluetooth Headphones', 'Premium noise-cancelling headphones with 30-hour battery life', 79.99, 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e', 1, NOW(), NOW(), b'0'),
    (2, 'Smart Watch Pro', 'Fitness tracker with heart rate monitor and GPS', 249.99, 'https://images.unsplash.com/photo-1523275335684-37898b6baf30', 1, NOW(), NOW(), b'0'),
    (3, 'Wireless Mouse', 'Ergonomic wireless mouse with precision tracking', 29.99, 'https://images.unsplash.com/photo-1527814050087-3793815479db', 1, NOW(), NOW(), b'0'),
    (4, 'USB-C Hub Adapter', '7-in-1 USB-C hub with HDMI and SD card reader', 39.99, 'https://images.unsplash.com/photo-1625948515291-69613efd103f', 1, NOW(), NOW(), b'0'),
    (5, 'Portable Bluetooth Speaker', 'Waterproof speaker with 360-degree sound', 59.99, 'https://images.unsplash.com/photo-1608043152269-423dbba4e7e1', 1, NOW(), NOW(), b'0'),
    (6, 'Mechanical Keyboard RGB', 'Gaming keyboard with customizable RGB lighting', 89.99, 'https://images.unsplash.com/photo-1595225476474-87563907a212', 1, NOW(), NOW(), b'0'),
    (7, 'Webcam HD 1080p', 'High-definition webcam with built-in microphone', 69.99, 'https://images.unsplash.com/photo-1632514974936-aa3e8e52fc3e', 1, NOW(), NOW(), b'0'),
    (8, 'Phone Stand Adjustable', 'Universal phone holder for desk', 19.99, 'https://images.unsplash.com/photo-1601784551446-20c9e07cdbdb', 1, NOW(), NOW(), b'0');

-- Insert Products for Clothing Category
INSERT INTO product (id, name, description, price, image_url, category_id, created_at, last_modified, is_deleted)
VALUES
    (9, 'Classic Cotton T-Shirt', 'Comfortable cotton t-shirt in multiple colors', 24.99, 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab', 2, NOW(), NOW(), b'0'),
    (10, 'Denim Jeans', 'Slim fit denim jeans with stretch fabric', 49.99, 'https://images.unsplash.com/photo-1542272604-787c3835535d', 2, NOW(), NOW(), b'0'),
    (11, 'Hoodie Sweatshirt', 'Warm pullover hoodie with front pocket', 39.99, 'https://images.unsplash.com/photo-1556821840-3a63f95609a7', 2, NOW(), NOW(), b'0'),
    (12, 'Running Shoes', 'Lightweight running shoes with cushioned sole', 89.99, 'https://images.unsplash.com/photo-1542291026-7eec264c27ff', 2, NOW(), NOW(), b'0'),
    (13, 'Leather Jacket', 'Genuine leather jacket with zipper closure', 199.99, 'https://images.unsplash.com/photo-1551028719-00167b16eac5', 2, NOW(), NOW(), b'0'),
    (14, 'Summer Dress', 'Floral print summer dress for women', 59.99, 'https://images.unsplash.com/photo-1595777457583-95e059d581b8', 2, NOW(), NOW(), b'0'),
    (15, 'Wool Scarf', 'Soft wool scarf for winter', 29.99, 'https://images.unsplash.com/photo-1520903920243-00d872a2d1c9', 2, NOW(), NOW(), b'0');

-- Insert Products for Books Category
INSERT INTO product (id, name, description, price, image_url, category_id, created_at, last_modified, is_deleted)
VALUES
    (16, 'The Art of Programming', 'Comprehensive guide to software development', 45.99, 'https://images.unsplash.com/photo-1532012197267-da84d127e765', 3, NOW(), NOW(), b'0'),
    (17, 'Digital Marketing Handbook', 'Complete guide to modern digital marketing', 34.99, 'https://images.unsplash.com/photo-1543002588-bfa74002ed7e', 3, NOW(), NOW(), b'0'),
    (18, 'Python for Beginners', 'Learn Python programming from scratch', 29.99, 'https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5', 3, NOW(), NOW(), b'0'),
    (19, 'Data Science Essentials', 'Introduction to data science and analytics', 49.99, 'https://images.unsplash.com/photo-1551288049-bebda4e38f71', 3, NOW(), NOW(), b'0'),
    (20, 'Creative Writing Guide', 'Master the art of storytelling', 24.99, 'https://images.unsplash.com/photo-1455390582262-044cdead277a', 3, NOW(), NOW(), b'0'),
    (21, 'Business Strategy', 'Strategic planning for modern businesses', 54.99, 'https://images.unsplash.com/photo-1507842217343-583bb7270b66', 3, NOW(), NOW(), b'0');

-- Insert Products for Home & Kitchen Category
INSERT INTO product (id, name, description, price, image_url, category_id, created_at, last_modified, is_deleted)
VALUES
    (22, 'Stainless Steel Cookware Set', '10-piece non-stick cookware set', 149.99, 'https://images.unsplash.com/photo-1584990347449-39f4c5d2d0ed', 4, NOW(), NOW(), b'0'),
    (23, 'Electric Kettle', 'Fast-boiling electric kettle with auto shut-off', 34.99, 'https://images.unsplash.com/photo-1587824267265-f08e1bcf0c8a', 4, NOW(), NOW(), b'0'),
    (24, 'Coffee Maker', 'Programmable coffee maker with thermal carafe', 79.99, 'https://images.unsplash.com/photo-1517668808822-9ebb02f2a0e6', 4, NOW(), NOW(), b'0'),
    (25, 'Kitchen Knife Set', 'Professional chef knife set with block', 99.99, 'https://images.unsplash.com/photo-1593618998160-e34014e67546', 4, NOW(), NOW(), b'0'),
    (26, 'Vacuum Cleaner Robot', 'Smart robot vacuum with app control', 299.99, 'https://images.unsplash.com/photo-1558317374-067fb44f3cb7', 4, NOW(), NOW(), b'0'),
    (27, 'Air Purifier', 'HEPA air purifier for large rooms', 159.99, 'https://images.unsplash.com/photo-1585771724684-38269d6639fd', 4, NOW(), NOW(), b'0'),
    (28, 'Bed Sheet Set', 'Premium cotton bed sheet set queen size', 59.99, 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304', 4, NOW(), NOW(), b'0'),
    (29, 'Decorative Throw Pillows', 'Set of 4 decorative cushion covers', 39.99, 'https://images.unsplash.com/photo-1584100936595-c0654b55a2e2', 4, NOW(), NOW(), b'0');

-- Insert Products for Sports & Fitness Category
INSERT INTO product (id, name, description, price, image_url, category_id, created_at, last_modified, is_deleted)
VALUES
    (30, 'Yoga Mat Premium', 'Non-slip yoga mat with carrying strap', 34.99, 'https://images.unsplash.com/photo-1601925260368-ae2f83cf8b7f', 5, NOW(), NOW(), b'0'),
    (31, 'Adjustable Dumbbells', 'Set of adjustable weights 5-25 lbs', 129.99, 'https://images.unsplash.com/photo-1517836357463-d25dfeac3438', 5, NOW(), NOW(), b'0'),
    (32, 'Resistance Bands Set', 'Set of 5 resistance bands with handles', 24.99, 'https://images.unsplash.com/photo-1598289431512-b97b0917affc', 5, NOW(), NOW(), b'0'),
    (33, 'Fitness Tracker Band', 'Activity tracker with sleep monitoring', 49.99, 'https://images.unsplash.com/photo-1575311373937-040b8e1fd5b6', 5, NOW(), NOW(), b'0'),
    (34, 'Tennis Racket', 'Professional tennis racket with cover', 89.99, 'https://images.unsplash.com/photo-1617083278551-83d7b4bc2382', 5, NOW(), NOW(), b'0'),
    (35, 'Basketball Official Size', 'Official size basketball indoor/outdoor', 29.99, 'https://images.unsplash.com/photo-1546519638-68e109498ffc', 5, NOW(), NOW(), b'0'),
    (36, 'Camping Tent 4-Person', 'Waterproof camping tent with carry bag', 139.99, 'https://images.unsplash.com/photo-1504280390367-361c6d9f38f4', 5, NOW(), NOW(), b'0'),
    (37, 'Water Bottle Insulated', 'Stainless steel water bottle 32oz', 24.99, 'https://images.unsplash.com/photo-1602143407151-7111542de6e8', 5, NOW(), NOW(), b'0');

-- Insert Test Users
-- Password: Password123! (BCrypt hash)
-- Note: If login fails, the database may need to be recreated after table name fix (user -> users)
INSERT INTO `users` (id, name, email, password, phone_number, address, role, created_at, last_modified, is_deleted)
VALUES
    (1, 'John Doe', 'john.doe@example.com', '$2a$10$XptfskLsT9pLz.eN2vDLKOnV8GVjJVGXjZz3TLmp/N5E0vUWK.Wz2', '+1234567890', '123 Main St, New York, NY 10001', 'USER', NOW(), NOW(), b'0'),
    (2, 'Jane Smith', 'jane.smith@example.com', '$2a$10$XptfskLsT9pLz.eN2vDLKOnV8GVjJVGXjZz3TLmp/N5E0vUWK.Wz2', '+1234567891', '456 Oak Ave, Los Angeles, CA 90001', 'USER', NOW(), NOW(), b'0'),
    (3, 'Admin User', 'admin@example.com', '$2a$10$pzvNZfLyqnBzakXHg7a/buYzlhzhPSIYUN15ypUIqm5Ak/LdKI5L.', '+1234567892', '789 Admin Blvd, Chicago, IL 60601', 'ADMIN', NOW(), NOW(), b'0')
ON DUPLICATE KEY UPDATE 
    password = VALUES(password),
    last_modified = NOW();

