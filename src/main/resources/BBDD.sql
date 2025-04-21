-- FashionDAM Clothing Store Database Schema

create schema BBDD_FashionDAM;
CREATE SCHEMA IF NOT EXISTS BBDD_FashionDAM;
SET search_path TO BBDD_FashionDAM;


-- Category table for product classification
CREATE TABLE categories (
    category_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT
);

-- Product table
CREATE TABLE products (
    product_id SERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    base_price DECIMAL(10, 2) NOT NULL,
    category_id INTEGER REFERENCES categories(category_id),
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