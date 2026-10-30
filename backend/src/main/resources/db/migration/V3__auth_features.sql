-- V3__auth_features.sql

-- Table for storing Refresh Tokens
CREATE TABLE refresh_token (
                               id BIGSERIAL PRIMARY KEY,
                               token VARCHAR(255) NOT NULL UNIQUE,
                               user_id BIGINT NOT NULL,
                               expiry_date TIMESTAMP WITH TIME ZONE NOT NULL,
                               CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE CASCADE
);

-- Add columns to app_user for Password Reset
ALTER TABLE app_user ADD COLUMN reset_password_token VARCHAR(255);
ALTER TABLE app_user ADD COLUMN reset_password_token_expiry TIMESTAMP WITH TIME ZONE;