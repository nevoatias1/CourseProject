CREATE TABLE users (
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20),
    address VARCHAR(255),
    username VARCHAR(255),
    password VARCHAR(255) NOT NULL,
    role VARCHAR(255) DEFAULT 'USER',
    PRIMARY KEY (username)
);

INSERT INTO users (first_name, last_name, email, phone, address, username, password, role) VALUES
('Nevo', 'Atias', 'nevo@gmail.com', '0500000000', 'Haifa', 'nevoatias1', '$2a$10$OqPr0on8o5C6bfG30p0YzuZ6sK0nq0JKCkNgmsiSQZpf/GgM9GmfS', 'ADMIN');

CREATE TABLE categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    price DECIMAL(10,2) NOT NULL,
    image_url VARCHAR(1000),
    stock INT NOT NULL,
    category_id INT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (category_id) REFERENCES categories(id)
);

CREATE TABLE product_images (
    product_id INT PRIMARY KEY,
    content_type VARCHAR(255) NOT NULL,
    data BLOB NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE TABLE favorites (
    username VARCHAR(255) NOT NULL,
    product_id INT NOT NULL,
    PRIMARY KEY (username, product_id),
    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE TABLE cart_items (
    username VARCHAR(255) NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    PRIMARY KEY (username, product_id),
    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE TABLE orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    total DECIMAL(10,2) NOT NULL,
    status VARCHAR(255) NOT NULL,
    shipping_address VARCHAR(255),
    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE
);

CREATE TABLE order_items (
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (order_id, product_id),
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id)
);

INSERT INTO categories (name) VALUES
('Smartphones'),
('Laptops'),
('Audio'),
('Smartwatches'),
('Gaming'),
('Accessories');

INSERT INTO products (name, description, price, image_url, stock, category_id) VALUES
('iPhone 17 Pro', 'Apple flagship smartphone with a pro camera system, bright display, and fast performance.', 1099.00, '/images/products/iphone-17-pro.jpg', 18, 1),
('Samsung Galaxy S25 Ultra', 'Premium Android phone with a large display, versatile cameras, and an integrated S Pen.', 1299.00, '/images/products/samsung-galaxy-s25-ultra.jpg', 12, 1),
('Google Pixel 10 Pro', 'Google smartphone with advanced computational photography and a clean Android experience.', 999.00, '/images/products/google-pixel-10-pro.jpg', 24, 1),
('Samsung Galaxy Z Flip7', 'Compact foldable smartphone that opens into a full-size display with a pocket-friendly design.', 1099.00, '/images/products/samsung-galaxy-z-flip7.jpg', 9, 1),
('MacBook Air 13" M4', 'Thin, quiet Apple laptop with the M4 chip, a sharp display, and all-day battery life.', 999.00, '/images/products/macbook-air-13-m4.jpg', 21, 2),
('MacBook Pro 14" M5', 'Powerful 14-inch MacBook Pro for demanding creative workflows and professional apps.', 1599.00, '/images/products/macbook-pro-14-m5.jpg', 8, 2),
('Lenovo ThinkPad X1 Carbon Gen 13', 'Lightweight business laptop with a durable carbon-fiber design and excellent keyboard.', 1899.00, '/images/products/lenovo-thinkpad-x1-carbon-gen-13.jpg', 14, 2),
('ASUS ROG Zephyrus G14', 'Compact gaming laptop pairing a high-refresh display with dedicated graphics performance.', 1999.00, '/images/products/asus-rog-zephyrus-g14.jpg', 7, 2),
('AirPods Pro 3', 'Wireless earbuds with active noise cancellation, a compact charging case, and adaptive audio.', 249.00, '/images/products/airpods-pro-3.jpg', 35, 3),
('Sony WH-1000XM6', 'Over-ear wireless headphones with class-leading noise cancellation and rich detailed sound.', 449.00, '/images/products/sony-wh-1000xm6.jpg', 16, 3),
('Samsung Galaxy Buds3 Pro', 'Premium wireless earbuds with intelligent noise control and a comfortable in-ear fit.', 249.00, '/images/products/samsung-galaxy-buds3-pro.jpg', 28, 3),
('JBL Flip 7', 'Portable waterproof Bluetooth speaker with bold sound for rooms, trips, and outdoor listening.', 149.00, '/images/products/jbl-flip-7.jpg', 32, 3),
('Apple Watch Series 11', 'Everyday Apple smartwatch for fitness tracking, notifications, and health insights.', 399.00, '/images/products/apple-watch-series-11.jpg', 20, 4),
('Apple Watch Ultra 3', 'Rugged Apple smartwatch with a large display, advanced workouts, and extended battery life.', 799.00, '/images/products/apple-watch-ultra-3.jpg', 6, 4),
('Samsung Galaxy Watch8', 'Elegant Android smartwatch with wellness tracking, notifications, and a vivid AMOLED display.', 349.00, '/images/products/samsung-galaxy-watch8.jpg', 18, 4),
('Garmin Fenix 8', 'Adventure-focused multisport watch with mapping, training tools, and long battery endurance.', 999.00, '/images/products/garmin-fenix-8.jpg', 11, 4),
('PlayStation 5 Pro', 'High-performance console with enhanced graphics, fast loading, and a large game library.', 749.00, '/images/products/playstation-5-pro.jpg', 10, 5),
('Nintendo Switch 2', 'Flexible hybrid console for handheld or TV play with a broad collection of Nintendo games.', 449.00, '/images/products/nintendo-switch-2.jpg', 17, 5),
('Xbox Series X', 'Powerful 4K gaming console with fast loading and backward compatibility.', 599.00, '/images/products/xbox-series-x.jpg', 0, 5),
('Steam Deck OLED', 'Handheld PC gaming device with an OLED display, comfortable controls, and a huge Steam library.', 549.00, '/images/products/steam-deck-oled.jpg', 13, 5),
('Logitech MX Master 3S', 'Comfortable precision wireless mouse with quiet clicks, advanced scrolling, and multi-device support.', 99.00, '/images/products/logitech-mx-master-3s.jpg', 30, 6),
('Logitech MX Keys S', 'Low-profile wireless keyboard with quiet typing, smart illumination, and multi-device pairing.', 109.00, '/images/products/logitech-mx-keys-s.jpg', 22, 6),
('Samsung T7 Shield 1TB', 'Rugged portable SSD with fast USB-C transfers and protection for work on the move.', 109.00, '/images/products/samsung-t7-shield-1tb.jpg', 26, 6),
('Anker 737 Power Bank', 'High-capacity portable charger with USB-C Power Delivery for phones, tablets, and laptops.', 99.00, '/images/products/anker-737-power-bank.jpg', 19, 6);
