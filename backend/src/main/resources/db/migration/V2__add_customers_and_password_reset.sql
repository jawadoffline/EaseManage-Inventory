-- =============================================
-- V2: Add customers and password_reset_tokens tables
-- =============================================

-- Customers table
CREATE TABLE IF NOT EXISTS customers (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    email           VARCHAR(255),
    phone           VARCHAR(50),
    address         VARCHAR(500),
    city            VARCHAR(100),
    country         VARCHAR(100),
    contact_person  VARCHAR(200),
    notes           TEXT,
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version         BIGINT       DEFAULT 0
);

-- Password reset tokens table
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token       VARCHAR(500) NOT NULL UNIQUE,
    expires_at  TIMESTAMP    NOT NULL,
    used        BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_password_reset_tokens_token ON password_reset_tokens(token);

-- Add version columns to tables that got @Version but don't have it in V1
ALTER TABLE users ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
ALTER TABLE products ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
ALTER TABLE categories ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
ALTER TABLE warehouses ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
ALTER TABLE inventory ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
ALTER TABLE purchase_orders ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
ALTER TABLE sales_orders ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
