-- V4: Change audit_log JSONB columns to TEXT for compatibility
-- The audit service stores plain string values, not JSON
ALTER TABLE audit_log ALTER COLUMN old_values TYPE TEXT;
ALTER TABLE audit_log ALTER COLUMN new_values TYPE TEXT;
