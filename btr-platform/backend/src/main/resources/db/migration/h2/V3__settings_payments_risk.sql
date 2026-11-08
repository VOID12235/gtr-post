ALTER TABLE contest ADD COLUMN fee_enabled BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE contest ADD COLUMN fee_amount DECIMAL(10,2);

CREATE TABLE sys_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    config_group VARCHAR(32) NOT NULL,
    config_key VARCHAR(128) NOT NULL,
    config_value VARCHAR(4000),
    description VARCHAR(255),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_sys_config UNIQUE (tenant_id, config_key)
);

CREATE TABLE payment_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    contest_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(32) NOT NULL,
    method VARCHAR(32) NOT NULL,
    reference VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    paid_at TIMESTAMP
);
CREATE INDEX idx_payment_order_user ON payment_order(tenant_id, user_id, status);
CREATE INDEX idx_payment_order_contest ON payment_order(tenant_id, contest_id, status);

CREATE TABLE prize_pool (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    contest_id BIGINT NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL,
    funded_amount DECIMAL(12,2) NOT NULL,
    status VARCHAR(32) NOT NULL,
    note VARCHAR(500),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_prize_pool_contest UNIQUE (tenant_id, contest_id)
);

CREATE TABLE insurance_policy (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    contest_id BIGINT NOT NULL,
    type VARCHAR(64) NOT NULL,
    insurer VARCHAR(128),
    policy_no VARCHAR(128),
    coverage VARCHAR(500),
    valid_from TIMESTAMP,
    valid_to TIMESTAMP,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_insurance_contest ON insurance_policy(tenant_id, contest_id);

CREATE TABLE safety_plan (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    contest_id BIGINT NOT NULL,
    level VARCHAR(16) NOT NULL,
    venue VARCHAR(255),
    capacity INT,
    plan VARCHAR(2000),
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_safety_plan_contest UNIQUE (tenant_id, contest_id)
);

CREATE TABLE upload_file (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    stored_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(128),
    size BIGINT NOT NULL,
    url VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_upload_file_tenant ON upload_file(tenant_id, created_at);
