-- V3: Fix audit_log and add missing columns for entity changes

-- Add username column to audit_log (entity has it, V1 schema didn't)
ALTER TABLE audit_log ADD COLUMN IF NOT EXISTS username VARCHAR(255);

-- Add version column to stock_movements if missing
ALTER TABLE stock_movements ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
