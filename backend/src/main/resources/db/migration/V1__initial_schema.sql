-- =============================================
-- EaseManage Inventory - Initial Database Schema
-- =============================================

-- Users table
CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(50)  NOT NULL UNIQUE,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    role            VARCHAR(20)  NOT NULL DEFAULT 'VIEWER',
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    avatar_url      VARCHAR(500),
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Refresh tokens table
CREATE TABLE refresh_tokens (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token       VARCHAR(500) NOT NULL UNIQUE,
    expires_at  TIMESTAMP    NOT NULL,
    revoked     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);

-- Categories table (hierarchical)
CREATE TABLE categories (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    parent_id   BIGINT       REFERENCES categories(id) ON DELETE SET NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Products table
CREATE TABLE products (
    id              BIGSERIAL PRIMARY KEY,
    sku             VARCHAR(50)    NOT NULL UNIQUE,
    name            VARCHAR(255)   NOT NULL,
    description     TEXT,
    category_id     BIGINT         REFERENCES categories(id) ON DELETE SET NULL,
    unit_of_measure VARCHAR(20)    NOT NULL DEFAULT 'PCS',
    min_stock_level INTEGER        NOT NULL DEFAULT 0,
    max_stock_level INTEGER,
    reorder_point   INTEGER        NOT NULL DEFAULT 0,
    cost_price      DECIMAL(12,2)  NOT NULL DEFAULT 0,
    selling_price   DECIMAL(12,2)  NOT NULL DEFAULT 0,
    barcode         VARCHAR(100),
    image_url       VARCHAR(500),
    is_active       BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_products_sku ON products(sku);
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_barcode ON products(barcode);

-- Warehouses table
CREATE TABLE warehouses (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    code        VARCHAR(20)  NOT NULL UNIQUE,
    address     VARCHAR(500),
    city        VARCHAR(100),
    state       VARCHAR(100),
    country     VARCHAR(100),
    capacity    INTEGER,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Inventory (stock per product per warehouse)
CREATE TABLE inventory (
    id                  BIGSERIAL PRIMARY KEY,
    product_id          BIGINT  NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    warehouse_id        BIGINT  NOT NULL REFERENCES warehouses(id) ON DELETE CASCADE,
    quantity            INTEGER NOT NULL DEFAULT 0,
    reserved_quantity   INTEGER NOT NULL DEFAULT 0,
    last_counted_at     TIMESTAMP,
    UNIQUE(product_id, warehouse_id)
);

CREATE INDEX idx_inventory_product ON inventory(product_id);
CREATE INDEX idx_inventory_warehouse ON inventory(warehouse_id);

-- Suppliers table
CREATE TABLE suppliers (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    email           VARCHAR(255),
    phone           VARCHAR(50),
    address         VARCHAR(500),
    city            VARCHAR(100),
    country         VARCHAR(100),
    contact_person  VARCHAR(200),
    payment_terms   VARCHAR(100),
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Supplier-Product relationship
CREATE TABLE supplier_products (
    id            BIGSERIAL PRIMARY KEY,
    supplier_id   BIGINT        NOT NULL REFERENCES suppliers(id) ON DELETE CASCADE,
    product_id    BIGINT        NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    supplier_sku  VARCHAR(50),
    lead_time_days INTEGER,
    unit_cost     DECIMAL(12,2),
    UNIQUE(supplier_id, product_id)
);

-- Purchase Orders
CREATE TABLE purchase_orders (
    id                  BIGSERIAL PRIMARY KEY,
    order_number        VARCHAR(50)   NOT NULL UNIQUE,
    supplier_id         BIGINT        NOT NULL REFERENCES suppliers(id),
    warehouse_id        BIGINT        NOT NULL REFERENCES warehouses(id),
    status              VARCHAR(20)   NOT NULL DEFAULT 'DRAFT',
    total_amount        DECIMAL(14,2) NOT NULL DEFAULT 0,
    expected_delivery   DATE,
    notes               TEXT,
    created_by          BIGINT        REFERENCES users(id),
    created_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE purchase_order_items (
    id                  BIGSERIAL PRIMARY KEY,
    purchase_order_id   BIGINT        NOT NULL REFERENCES purchase_orders(id) ON DELETE CASCADE,
    product_id          BIGINT        NOT NULL REFERENCES products(id),
    quantity            INTEGER       NOT NULL,
    unit_cost           DECIMAL(12,2) NOT NULL,
    received_quantity   INTEGER       NOT NULL DEFAULT 0
);

-- Sales Orders
CREATE TABLE sales_orders (
    id                  BIGSERIAL PRIMARY KEY,
    order_number        VARCHAR(50)   NOT NULL UNIQUE,
    customer_name       VARCHAR(255)  NOT NULL,
    warehouse_id        BIGINT        NOT NULL REFERENCES warehouses(id),
    status              VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    total_amount        DECIMAL(14,2) NOT NULL DEFAULT 0,
    shipping_address    TEXT,
    created_by          BIGINT        REFERENCES users(id),
    created_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sales_order_items (
    id              BIGSERIAL PRIMARY KEY,
    sales_order_id  BIGINT        NOT NULL REFERENCES sales_orders(id) ON DELETE CASCADE,
    product_id      BIGINT        NOT NULL REFERENCES products(id),
    quantity        INTEGER       NOT NULL,
    unit_price      DECIMAL(12,2) NOT NULL
);

-- Stock Movements
CREATE TABLE stock_movements (
    id                  BIGSERIAL PRIMARY KEY,
    product_id          BIGINT      NOT NULL REFERENCES products(id),
    from_warehouse_id   BIGINT      REFERENCES warehouses(id),
    to_warehouse_id     BIGINT      REFERENCES warehouses(id),
    quantity            INTEGER     NOT NULL,
    movement_type       VARCHAR(20) NOT NULL,
    reference_type      VARCHAR(50),
    reference_id        BIGINT,
    reason              TEXT,
    created_by          BIGINT      REFERENCES users(id),
    created_at          TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_stock_movements_product ON stock_movements(product_id);
CREATE INDEX idx_stock_movements_type ON stock_movements(movement_type);

-- Audit Log
CREATE TABLE audit_log (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT       REFERENCES users(id),
    entity_type VARCHAR(50)  NOT NULL,
    entity_id   BIGINT       NOT NULL,
    action      VARCHAR(20)  NOT NULL,
    old_values  JSONB,
    new_values  JSONB,
    ip_address  VARCHAR(45),
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_log_entity ON audit_log(entity_type, entity_id);
CREATE INDEX idx_audit_log_user ON audit_log(user_id);
CREATE INDEX idx_audit_log_created ON audit_log(created_at);

-- Notifications
CREATE TABLE notifications (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title       VARCHAR(255) NOT NULL,
    message     TEXT         NOT NULL,
    type        VARCHAR(30)  NOT NULL,
    is_read     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notifications_user ON notifications(user_id, is_read);

-- Seed default admin user (password: admin123)
INSERT INTO users (username, email, password_hash, first_name, last_name, role, status)
VALUES ('admin', 'admin@easemanage.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'System', 'Admin', 'ADMIN', 'ACTIVE');
