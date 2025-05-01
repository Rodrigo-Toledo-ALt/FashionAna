-- FashionDAM Clothing Store Database Schema

-- Truncar todas las tablas en el orden correcto para evitar problemas de dependencia
TRUNCATE TABLE
    inventory_transactions,
    return_items,
    returns,
    transactions,
    order_status_history,
    order_items,
    orders,
    product_variants,
    products,
    categories,
    payment_methods,
    addresses,
    customers,
    employees
RESTART IDENTITY CASCADE;




create schema BBDD_FashionDAM;
CREATE SCHEMA IF NOT EXISTS BBDD_FashionDAM;
SET search_path TO BBDD_FashionDAM;


-- Category table for product classification
CREATE TABLE categories (
    category_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT
);

-- Recrear la tabla de productos con el campo imageUrl
CREATE TABLE products (
                          product_id SERIAL PRIMARY KEY,
                          name VARCHAR(200) NOT NULL,
                          description TEXT,
                          base_price DECIMAL(10, 2) NOT NULL,
                          category_id INTEGER REFERENCES categories(category_id),
                          image_url TEXT,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Product variants (size, color combinations)
CREATE TABLE product_variants (
    variant_id SERIAL PRIMARY KEY,
    product_id INTEGER REFERENCES products(product_id) ON DELETE CASCADE,
    size VARCHAR(10) NOT NULL, -- S, M, L, XL, etc.
    color VARCHAR(50) NOT NULL,
    stock_quantity INTEGER NOT NULL DEFAULT 0,
    price DECIMAL(10, 2) NOT NULL, -- This can differ from base_price for specific variants
    sku VARCHAR(50) UNIQUE NOT NULL -- Stock Keeping Unit - unique identifier
);

-- Customer table
CREATE TABLE customers (
    customer_id SERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL, -- Hashed password
    birth_date DATE NOT NULL CHECK (birth_date <= CURRENT_DATE - INTERVAL '18 years'),
    phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Address table (to allow customers to have multiple addresses)
CREATE TABLE addresses (
    address_id SERIAL PRIMARY KEY,
    customer_id INTEGER REFERENCES customers(customer_id) ON DELETE CASCADE,
    street VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    state_province VARCHAR(100),
    country VARCHAR(100) NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    is_billing BOOLEAN DEFAULT FALSE
);

-- Payment information
CREATE TABLE payment_methods (
    payment_id SERIAL PRIMARY KEY,
    customer_id INTEGER REFERENCES customers(customer_id) ON DELETE CASCADE,
    payment_type VARCHAR(50) NOT NULL, -- Credit card, PayPal, etc.
    provider VARCHAR(100), -- Visa, MasterCard, etc.
    account_number VARCHAR(255), -- Encrypted
    expiry_date VARCHAR(10), -- Encrypted
    is_default BOOLEAN DEFAULT FALSE
);

-- Employee table for store staff
CREATE TABLE employees (
    employee_id SERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL, -- Hashed password
    role VARCHAR(50) NOT NULL, -- Manager, Sales Associate, etc.
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Order status enum type
CREATE TYPE order_status AS ENUM (
    'created',
    'confirmed',
    'preparing',
    'ready_to_pickup',
    'shipped',
    'delivered',
    'cancelled',
    'returned'
);

-- Order delivery type
CREATE TYPE delivery_type AS ENUM (
    'pickup',
    'delivery'
);

-- Orders table
CREATE TABLE orders (
    order_id SERIAL PRIMARY KEY,
    customer_id INTEGER REFERENCES customers(customer_id),
    employee_id INTEGER REFERENCES employees(employee_id),
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status order_status DEFAULT 'created',
    delivery_type delivery_type NOT NULL,
    shipping_address_id INTEGER REFERENCES addresses(address_id),
    billing_address_id INTEGER REFERENCES addresses(address_id),
    payment_id INTEGER REFERENCES payment_methods(payment_id),
    total_amount DECIMAL(10, 2) NOT NULL DEFAULT 0,
    tracking_code VARCHAR(100),
    expected_delivery_date DATE,
    notes TEXT
);

-- Order items (products in an order)
CREATE TABLE order_items (
    order_item_id SERIAL PRIMARY KEY,
    order_id INTEGER REFERENCES orders(order_id) ON DELETE CASCADE,
    variant_id INTEGER REFERENCES product_variants(variant_id),
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(10, 2) NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL
);

-- In-store sales/transactions
CREATE TABLE transactions (
    transaction_id SERIAL PRIMARY KEY,
    order_id INTEGER REFERENCES orders(order_id),
    employee_id INTEGER REFERENCES employees(employee_id),
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    payment_method VARCHAR(50) NOT NULL, -- Cash, Credit Card, etc.
    amount DECIMAL(10, 2) NOT NULL,
    transaction_type VARCHAR(20) NOT NULL -- Sale, Return, Refund
);

-- Return/refund records
CREATE TABLE returns (
    return_id SERIAL PRIMARY KEY,
    order_id INTEGER REFERENCES orders(order_id),
    customer_id INTEGER REFERENCES customers(customer_id),
    employee_id INTEGER REFERENCES employees(employee_id),
    return_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reason TEXT,
    refund_amount DECIMAL(10, 2) DEFAULT 0,
    status VARCHAR(50) DEFAULT 'pending' -- pending, approved, rejected, completed
);

-- Return items
CREATE TABLE return_items (
    return_item_id SERIAL PRIMARY KEY,
    return_id INTEGER REFERENCES returns(return_id) ON DELETE CASCADE,
    order_item_id INTEGER REFERENCES order_items(order_item_id),
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(10, 2) NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL
);

-- Order status history for tracking changes
CREATE TABLE order_status_history (
    history_id SERIAL PRIMARY KEY,
    order_id INTEGER REFERENCES orders(order_id) ON DELETE CASCADE,
    status order_status NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by INTEGER REFERENCES employees(employee_id),
    notes TEXT
);

-- Inventory transactions for tracking stock changes
CREATE TABLE inventory_transactions (
    inventory_transaction_id SERIAL PRIMARY KEY,
    variant_id INTEGER REFERENCES product_variants(variant_id),
    quantity INTEGER NOT NULL, -- Can be positive (restock) or negative (sale)
    transaction_type VARCHAR(50) NOT NULL, -- Purchase, Sale, Return, Adjustment
    reference_id INTEGER, -- ID of related transaction (order_id, return_id, etc.)
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    employee_id INTEGER REFERENCES employees(employee_id),
    notes TEXT
);

-- Indexes for performance optimization
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_variants_product ON product_variants(product_id);
CREATE INDEX idx_order_items_order ON order_items(order_id);
CREATE INDEX idx_order_items_variant ON order_items(variant_id);
CREATE INDEX idx_orders_customer ON orders(customer_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_transactions_order ON transactions(order_id);
CREATE INDEX idx_return_items_return ON return_items(return_id);
CREATE INDEX idx_inventory_transactions_variant ON inventory_transactions(variant_id);

-- Datos de ejemplo

-- Datos de Muestra para FashionAna

-- Datos de Muestra para FashionAna

-- Insertar datos en categorías
INSERT INTO categories (name, description) VALUES
    ('Camisetas', 'Camisetas casuales y cómodas para uso diario'),
    ('Vaqueros', 'Pantalones vaqueros en varios estilos y lavados'),
    ('Vestidos', 'Vestidos elegantes para diversas ocasiones'),
    ('Ropa de abrigo', 'Chaquetas, abrigos y otras prendas de abrigo'),
    ('Accesorios', 'Cinturones, sombreros, bufandas y otros accesorios de moda'),
    ('Calzado', 'Zapatos, botas y sandalias para todas las temporadas'),
    ('Ropa deportiva', 'Ropa atlética y orientada a deportes');

-- Insertar datos en productos
INSERT INTO products (name, description, base_price, category_id) VALUES
    ('Camiseta Cuello Redondo Clásica', 'Camiseta suave de algodón con cuello redondo en varios colores', 19.99, 1),
    ('Camiseta Cuello V', 'Camiseta cómoda de algodón con cuello en V', 19.99, 1),
    ('Camiseta con Estampado Gráfico', 'Camiseta de algodón con diseños gráficos modernos', 24.99, 1),
    ('Vaqueros Slim Fit', 'Vaqueros modernos slim fit con comodidad elástica', 49.99, 2),
    ('Vaqueros Regular Fit', 'Vaqueros clásicos regular fit cómodos', 45.99, 2),
    ('Vaqueros Desgastados', 'Vaqueros de moda desgastados', 59.99, 2),
    ('Vestido Floral de Verano', 'Vestido ligero con patrón floral perfecto para verano', 69.99, 3),
    ('Vestido de Cóctel', 'Vestido elegante para ocasiones especiales', 89.99, 3),
    ('Vestido Maxi', 'Vestido largo y cómodo para uso casual', 79.99, 3),
    ('Chaqueta de Cuero', 'Chaqueta clásica de cuero con forro cálido', 149.99, 4),
    ('Abrigo Acolchado', 'Abrigo cálido acolchado para clima invernal', 129.99, 4),
    ('Chaqueta Vaquera', 'Chaqueta vaquera versátil para conjuntos casuales', 79.99, 4),
    ('Cinturón de Cuero', 'Cinturón de cuero genuino con hebilla clásica', 29.99, 5),
    ('Bufanda de Invierno', 'Bufanda tejida cálida para invierno', 24.99, 5),
    ('Gorra de Béisbol', 'Gorra ajustable de algodón con diferentes diseños', 19.99, 5),
    ('Zapatillas Deportivas', 'Zapatillas cómodas para correr y entrenar', 89.99, 6),
    ('Botas de Cuero', 'Botas resistentes de cuero para temporada fría', 119.99, 6),
    ('Sandalias de Verano', 'Sandalias ligeras y cómodas para la playa', 39.99, 6),
    ('Conjunto Deportivo', 'Conjunto de pantalón y sudadera para actividades deportivas', 79.99, 7),
    ('Leggings Deportivos', 'Leggings elásticos para fitness y yoga', 44.99, 7);

-- Insertar variantes de productos
INSERT INTO product_variants (product_id, size, color, stock_quantity, price, sku) VALUES
    (1, 'S', 'Negro', 50, 19.99, 'CRCN-S-NEG'),
    (1, 'M', 'Negro', 75, 19.99, 'CRCN-M-NEG'),
    (1, 'L', 'Negro', 60, 19.99, 'CRCN-L-NEG'),
    (1, 'XL', 'Negro', 40, 19.99, 'CRCN-XL-NEG'),
    (1, 'S', 'Blanco', 45, 19.99, 'CRCN-S-BLA'),
    (1, 'M', 'Blanco', 70, 19.99, 'CRCN-M-BLA'),
    (1, 'L', 'Blanco', 65, 19.99, 'CRCN-L-BLA'),
    (1, 'XL', 'Blanco', 35, 19.99, 'CRCN-XL-BLA'),
    (1, 'S', 'Azul', 30, 19.99, 'CRCN-S-AZU'),
    (1, 'M', 'Azul', 45, 19.99, 'CRCN-M-AZU'),
    (1, 'L', 'Azul', 50, 19.99, 'CRCN-L-AZU'),
    (1, 'XL', 'Azul', 25, 19.99, 'CRCN-XL-AZU'),
    (2, 'S', 'Negro', 40, 19.99, 'CCV-S-NEG'),
    (2, 'M', 'Negro', 60, 19.99, 'CCV-M-NEG'),
    (2, 'L', 'Negro', 55, 19.99, 'CCV-L-NEG'),
    (2, 'XL', 'Negro', 30, 19.99, 'CCV-XL-NEG'),
    (2, 'S', 'Rojo', 25, 19.99, 'CCV-S-ROJ'),
    (2, 'M', 'Rojo', 35, 19.99, 'CCV-M-ROJ'),
    (2, 'L', 'Rojo', 40, 19.99, 'CCV-L-ROJ'),
    (2, 'XL', 'Rojo', 20, 19.99, 'CCV-XL-ROJ'),
    (3, 'S', 'Gris', 30, 24.99, 'CEG-S-GRI'),
    (3, 'M', 'Gris', 45, 24.99, 'CEG-M-GRI'),
    (3, 'L', 'Gris', 50, 24.99, 'CEG-L-GRI'),
    (3, 'XL', 'Gris', 25, 24.99, 'CEG-XL-GRI'),
    (4, '30', 'Azul oscuro', 25, 49.99, 'VSF-30-AZO'),
    (4, '32', 'Azul oscuro', 40, 49.99, 'VSF-32-AZO'),
    (4, '34', 'Azul oscuro', 45, 49.99, 'VSF-34-AZO'),
    (4, '36', 'Azul oscuro', 30, 49.99, 'VSF-36-AZO'),
    (4, '38', 'Azul oscuro', 20, 49.99, 'VSF-38-AZO'),
    (4, '30', 'Negro', 25, 49.99, 'VSF-30-NEG'),
    (4, '32', 'Negro', 35, 49.99, 'VSF-32-NEG'),
    (4, '34', 'Negro', 40, 49.99, 'VSF-34-NEG'),
    (4, '36', 'Negro', 30, 49.99, 'VSF-36-NEG'),
    (4, '38', 'Negro', 20, 49.99, 'VSF-38-NEG');

-- Insertar datos de empleados
INSERT INTO employees (first_name, last_name, email, password, role) VALUES
    ('Carlos', 'Rodríguez', 'carlos.rodriguez@fashionana.com', '$2a$10$vDG7.WQV9F4HRqGUQONFTua3VFmMqXK4Gu0hRr9NmZlTmS5YA9pxS', 'Gerente'), -- contraseña: admin123
    ('María', 'López', 'maria.lopez@fashionana.com', '$2a$10$LnGE7JpX1zR7YxLA.9vFb.xHVK0vx0YDV2B/PXlJnkdNwZkX0C0Pe', 'Vendedor'), -- contraseña: vendedor123
    ('Juan', 'García', 'juan.garcia@fashionana.com', '$2a$10$FVJ5LbSQxBaXB/r8AHfX5OdZHHnGUhaEJ4q4X8j5nAbVpW2IEdMGi', 'Vendedor'), -- contraseña: vendedor123
    ('Ana', 'Martínez', 'ana.martinez@fashionana.com', '$2a$10$q0OV5kZnYKERD1r6Zz.5z.Hv0yCM9aYIQMB3Bw/MxH0dKqBBnVWJi', 'Almacén'); -- contraseña: almacen123

-- Insertar datos de clientes
INSERT INTO customers (first_name, last_name, email, password, birth_date, phone) VALUES
    ('Pedro', 'Sánchez', 'pedro.sanchez@email.com', '$2a$10$5YdZ5qCLMUwrS7KP9L.3/.6DVeVXYZbXkN9J01WKz8m4EGo5z2FS2', '1985-05-15', '600123456'), -- contraseña: cliente123
    ('Laura', 'Fernández', 'laura.fernandez@email.com', '$2a$10$7c.JrWZxBXY0HV1Fc49Qh.MQGw5HYjjg/HS/q5rJKYrRrw.s7j3Em', '1990-10-20', '600654321'), -- contraseña: cliente123
    ('Miguel', 'González', 'miguel.gonzalez@email.com', '$2a$10$DXY.ZZQPzQFrY4Zk2j5qDuXp4X6T7nVBi5Qm7r7z5E4UQUEjJPggy', '1978-03-25', '600987654'), -- contraseña: cliente123
    ('Carmen', 'Ruiz', 'carmen.ruiz@email.com', '$2a$10$1JvjvMaWM1CfQ3foDj6ub.QUByNwzm9mpN5lLXcwLpj1mD5MzdB26', '1995-12-10', '600456789'), -- contraseña: cliente123
    ('Javier', 'Díaz', 'javier.diaz@email.com', '$2a$10$gMKnEHQl/I7GQTjDxuQZp.nRtbS.ZVfAEuUTQMNc7XHJg.h.4izqC', '1982-07-30', '600789123'); -- contraseña: cliente123

-- Insertar direcciones
INSERT INTO addresses (customer_id, street, city, postal_code, state_province, country, is_default, is_billing) VALUES
    (1, 'Calle Mayor 15, 3ºA', 'Madrid', '28001', 'Madrid', 'España', true, true),
    (2, 'Avenida Diagonal 423, 5ºB', 'Barcelona', '08008', 'Cataluña', 'España', true, true),
    (2, 'Calle Trabajo 7, 1ºC', 'Barcelona', '08019', 'Cataluña', 'España', false, false),
    (3, 'Calle Gran Vía 56, 4ºD', 'Madrid', '28013', 'Madrid', 'España', true, true),
    (4, 'Calle Sierpes 23, 2ºE', 'Sevilla', '41004', 'Andalucía', 'España', true, true),
    (5, 'Calle Colón 32, 7ºF', 'Valencia', '46004', 'Valencia', 'España', true, true);

-- Insertar métodos de pago
INSERT INTO payment_methods (customer_id, payment_type, provider, account_number, expiry_date, is_default) VALUES
    (1, 'Tarjeta de crédito', 'Visa', 'XXXXXXXXXXXX4321', '12/25', true),
    (2, 'Tarjeta de crédito', 'MasterCard', 'XXXXXXXXXXXX1234', '10/26', true),
    (2, 'PayPal', 'PayPal', 'laura.fernandez@email.com', NULL, false),
    (3, 'Tarjeta de débito', 'Visa Electron', 'XXXXXXXXXXXX7890', '03/24', true),
    (4, 'Tarjeta de crédito', 'American Express', 'XXXXXXXXXXXX5678', '09/25', true),
    (5, 'PayPal', 'PayPal', 'javier.diaz@email.com', NULL, true);

-- Insertar pedidos
INSERT INTO orders (customer_id, employee_id, order_date, status, delivery_type, shipping_address_id, billing_address_id, payment_id, total_amount, tracking_code, expected_delivery_date, notes) VALUES
    (1, 2, '2024-04-10 10:30:00', 'delivered', 'delivery', 1, 1, 1, 79.96, 'TRK123456789', '2024-04-15', 'Entregar por la tarde'),
    (2, 3, '2024-04-12 15:45:00', 'confirmed', 'delivery', 2, 2, 2, 149.97, 'TRK987654321', '2024-04-20', 'Llamar antes de entregar'),
    (3, 2, '2024-04-14 09:15:00', 'shipped', 'delivery', 4, 4, 4, 129.99, 'TRK456123789', '2024-04-22', NULL),
    (4, 3, '2024-04-15 14:20:00', 'delivered', 'pickup', 5, 5, 5, 124.97, NULL, NULL, 'Recogida en tienda'),
    (5, 2, '2024-04-18 11:30:00', 'preparing', 'delivery', 6, 6, 6, 189.98, 'TRK789456123', '2024-04-25', 'Empaquetar como regalo');

-- Insertar elementos de pedidos
INSERT INTO order_items (order_id, variant_id, quantity, unit_price, subtotal) VALUES
    (1, 1, 2, 19.99, 39.98),
    (1, 13, 2, 19.99, 39.98),
    (2, 22, 1, 24.99, 24.99),
    (2, 25, 1, 49.99, 49.99),
    (2, 30, 1, 49.99, 49.99),
    (2, 7, 1, 24.99, 24.99),
    (3, 10, 1, 129.99, 129.99),
    (4, 3, 1, 19.99, 19.99),
    (4, 24, 1, 24.99, 24.99),
    (4, 28, 1, 79.99, 79.99),
    (5, 5, 2, 19.99, 39.98),
    (5, 26, 3, 49.99, 149.99);

-- Insertar historial de estados de pedidos
INSERT INTO order_status_history (order_id, status, updated_at, updated_by, notes) VALUES
    (1, 'created', '2024-04-10 10:30:00', 2, 'Pedido creado'),
    (1, 'confirmed', '2024-04-10 10:45:00', 1, 'Pago confirmado'),
    (1, 'preparing', '2024-04-10 11:30:00', 2, 'Preparando pedido'),
    (1, 'shipped', '2024-04-11 09:15:00', 1, 'Pedido enviado'),
    (1, 'delivered', '2024-04-15 16:30:00', 2, 'Entregado al cliente'),
    (2, 'created', '2024-04-12 15:45:00', 3, 'Pedido creado'),
    (2, 'confirmed', '2024-04-12 16:00:00', 1, 'Pago confirmado'),
    (3, 'created', '2024-04-14 09:15:00', 2, 'Pedido creado'),
    (3, 'confirmed', '2024-04-14 09:30:00', 1, 'Pago confirmado'),
    (3, 'preparing', '2024-04-14 10:45:00', 4, 'Preparando pedido'),
    (3, 'shipped', '2024-04-15 11:30:00', 4, 'Pedido enviado'),
    (4, 'created', '2024-04-15 14:20:00', 3, 'Pedido creado'),
    (4, 'confirmed', '2024-04-15 14:35:00', 1, 'Pago confirmado'),
    (4, 'preparing', '2024-04-15 15:30:00', 4, 'Preparando pedido'),
    (4, 'ready_to_pickup', '2024-04-16 10:15:00', 4, 'Listo para recoger'),
    (4, 'delivered', '2024-04-16 17:45:00', 3, 'Recogido en tienda'),
    (5, 'created', '2024-04-18 11:30:00', 2, 'Pedido creado'),
    (5, 'confirmed', '2024-04-18 11:45:00', 1, 'Pago confirmado'),
    (5, 'preparing', '2024-04-18 14:30:00', 4, 'Preparando pedido');

-- Insertar transacciones
INSERT INTO transactions (order_id, employee_id, transaction_date, payment_method, amount, transaction_type) VALUES
    (1, 2, '2024-04-10 10:40:00', 'Tarjeta de crédito', 79.96, 'Sale'),
    (2, 3, '2024-04-12 15:55:00', 'Tarjeta de crédito', 149.97, 'Sale'),
    (3, 2, '2024-04-14 09:25:00', 'Tarjeta de débito', 129.99, 'Sale'),
    (4, 3, '2024-04-15 14:30:00', 'Tarjeta de crédito', 124.97, 'Sale'),
    (5, 2, '2024-04-18 11:40:00', 'PayPal', 189.98, 'Sale');

-- Insertar devoluciones
INSERT INTO returns (order_id, customer_id, employee_id, return_date, reason, refund_amount, status) VALUES
    (1, 1, 2, '2024-04-18 11:15:00', 'Talla incorrecta', 19.99, 'completed'),
    (4, 4, 3, '2024-04-19 16:30:00', 'Defectuoso', 24.99, 'completed');

-- Insertar elementos de devolución
INSERT INTO return_items (return_id, order_item_id, quantity, unit_price, subtotal) VALUES
    (1, 1, 1, 19.99, 19.99),
    (2, 9, 1, 24.99, 24.99);

-- Insertar transacciones de inventario
INSERT INTO inventory_transactions (variant_id, quantity, transaction_type, reference_id, transaction_date, employee_id, notes) VALUES
    (1, -2, 'Sale', 1, '2024-04-10 10:30:00', 4, 'Venta de camisetas'),
    (13, -2, 'Sale', 1, '2024-04-10 10:30:00', 4, 'Venta de camisetas'),
    (22, -1, 'Sale', 2, '2024-04-12 15:45:00', 4, 'Venta de camiseta estampada'),
    (25, -1, 'Sale', 2, '2024-04-12 15:45:00', 4, 'Venta de vaqueros'),
    (30, -1, 'Sale', 2, '2024-04-12 15:45:00', 4, 'Venta de vaqueros'),
    (7, -1, 'Sale', 2, '2024-04-12 15:45:00', 4, 'Venta de camiseta'),
    (10, -1, 'Sale', 3, '2024-04-14 09:15:00', 4, 'Venta de abrigo'),
    (3, -1, 'Sale', 4, '2024-04-15 14:20:00', 4, 'Venta de camiseta'),
    (24, -1, 'Sale', 4, '2024-04-15 14:20:00', 4, 'Venta de camiseta estampada'),
    (28, -1, 'Sale', 4, '2024-04-15 14:20:00', 4, 'Venta de chaqueta'),
    (5, -2, 'Sale', 5, '2024-04-18 11:30:00', 4, 'Venta de camisetas'),
    (26, -3, 'Sale', 5, '2024-04-18 11:30:00', 4, 'Venta de vaqueros'),
    (1, 1, 'Return', 1, '2024-04-18 11:15:00', 4, 'Devolución de camiseta'),
    (24, 1, 'Return', 2, '2024-04-19 16:30:00', 4, 'Devolución de camiseta'),
    (1, 100, 'Purchase', NULL, '2024-04-01 08:30:00', 4, 'Reposición de stock'),
    (5, 100, 'Purchase', NULL, '2024-04-01 08:30:00', 4, 'Reposición de stock'),
    (10, 100, 'Purchase', NULL, '2024-04-01 08:30:00', 4, 'Reposición de stock'),
    (15, 100, 'Purchase', NULL, '2024-04-01 08:30:00', 4, 'Reposición de stock'),
    (20, 100, 'Purchase', NULL, '2024-04-01 08:30:00', 4, 'Reposición de stock'),
    (25, 100, 'Purchase', NULL, '2024-04-01 08:30:00', 4, 'Reposición de stock'),
    (30, 100, 'Purchase', NULL, '2024-04-01 08:30:00', 4, 'Reposición de stock');


