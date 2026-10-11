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
('Miki', 'Gabay', 'mikigabay@gmail.com', '0585236376', '45 Ben Eliezer St', 'mikigabay', '$2a$10$24P9JHWZJm8yRsJRpP4a.e11OvU9ynMvz6XAKJOrxl8Nhph7mojJ2', 'USER'),
('Amitay', 'Gabay', 'amitaygabay1@gmail.com', '0504380333', '38 Erez St', 'amitaygabay', '$2a$10$K78Qy75RrDNQcAolPojuM.sI.otXpP23xhZYJ7p2fXrIMoI.k2ehO', 'ADMIN');

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
    FOREIGN KEY (category_id) REFERENCES categories(id)
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
('Nova X1 Pro', '6.7-inch OLED smartphone with a 50 MP triple camera and all-day battery.', 899.99, 'https://placehold.co/600x400?text=Nova+X1+Pro', 18, 1),
('PixelView A5', 'Compact 5G smartphone with a bright display and fast wireless charging.', 549.00, 'https://placehold.co/600x400?text=PixelView+A5', 27, 1),
('Orbit S24', 'Flagship Android phone with titanium frame and advanced night photography.', 1099.00, 'https://placehold.co/600x400?text=Orbit+S24', 11, 1),
('Cobalt Mini', 'Pocket-friendly smartphone with dual cameras and expandable storage.', 329.50, 'https://placehold.co/600x400?text=Cobalt+Mini', 34, 1),
('AeroBook 14', '14-inch ultrabook with an Intel Core Ultra processor and 16 GB memory.', 1199.99, 'https://placehold.co/600x400?text=AeroBook+14', 14, 2),
('ForgeStation 16', '16-inch performance laptop built for creative work and demanding games.', 1799.00, 'https://placehold.co/600x400?text=ForgeStation+16', 9, 2),
('Campus Lite 13', 'Lightweight 13-inch laptop with long battery life for study and travel.', 699.00, 'https://placehold.co/600x400?text=Campus+Lite+13', 22, 2),
('StudioBook 15', '15-inch laptop with color-accurate display and dedicated graphics.', 1499.50, 'https://placehold.co/600x400?text=StudioBook+15', 12, 2),
('Pulse Buds Pro', 'Noise-cancelling true wireless earbuds with a charging case.', 179.99, 'https://placehold.co/600x400?text=Pulse+Buds+Pro', 40, 3),
('RoomWave 360', 'Portable Bluetooth speaker with 360-degree sound and IP67 protection.', 129.00, 'https://placehold.co/600x400?text=RoomWave+360', 31, 3),
('EchoFrame Max', 'Over-ear headphones with adaptive noise cancellation and 40-hour battery.', 299.99, 'https://placehold.co/600x400?text=EchoFrame+Max', 16, 3),
('StreamMic USB', 'Plug-and-play USB microphone for streaming, meetings, and podcasts.', 89.50, 'https://placehold.co/600x400?text=StreamMic+USB', 25, 3),
('ActiveTrack 2', 'Fitness smartwatch with GPS, heart-rate tracking, and a 10-day battery.', 249.00, 'https://placehold.co/600x400?text=ActiveTrack+2', 29, 4),
('Classic Steel', 'Stainless steel smartwatch with AMOLED display and wellness tracking.', 349.99, 'https://placehold.co/600x400?text=Classic+Steel', 15, 4),
('TrailWatch Go', 'Rugged outdoor smartwatch with offline maps and water resistance.', 399.00, 'https://placehold.co/600x400?text=TrailWatch+Go', 13, 4),
('MiniFit Band', 'Slim activity tracker with sleep monitoring and notification alerts.', 69.99, 'https://placehold.co/600x400?text=MiniFit+Band', 48, 4),
('Vector Console', 'Current-generation game console with fast SSD storage and 4K output.', 499.99, 'https://placehold.co/600x400?text=Vector+Console', 10, 5),
('Apex Controller', 'Wireless controller with customizable back buttons and USB-C charging.', 79.99, 'https://placehold.co/600x400?text=Apex+Controller', 36, 5),
('HyperKeys TKL', 'Tenkeyless mechanical keyboard with hot-swappable switches.', 139.00, 'https://placehold.co/600x400?text=HyperKeys+TKL', 20, 5),
('GlidePad XL', 'Extended gaming mouse pad with a smooth micro-weave surface.', 34.99, 'https://placehold.co/600x400?text=GlidePad+XL', 44, 5),
('VoltCharge 65W', 'Compact GaN USB-C charger with two USB-C ports and one USB-A port.', 54.99, 'https://placehold.co/600x400?text=VoltCharge+65W', 38, 6),
('LinkHub 7', 'Seven-port USB-C hub with HDMI, card readers, and power delivery.', 64.50, 'https://placehold.co/600x400?text=LinkHub+7', 24, 6),
('ShieldCase Mag', 'Magnetic protective phone case compatible with wireless chargers.', 29.99, 'https://placehold.co/600x400?text=ShieldCase+Mag', 57, 6),
('ViewStand Pro', 'Adjustable aluminum laptop stand for desks and hybrid workspaces.', 49.00, 'https://placehold.co/600x400?text=ViewStand+Pro', 33, 6);
