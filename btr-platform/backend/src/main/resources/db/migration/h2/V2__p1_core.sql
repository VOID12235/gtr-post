CREATE TABLE contest (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    title VARCHAR(160) NOT NULL,
    summary VARCHAR(1000),
    cover VARCHAR(500),
    mode VARCHAR(16),
    status VARCHAR(32) NOT NULL,
    signup_start TIMESTAMP,
    signup_end TIMESTAMP,
    submit_start TIMESTAMP,
    submit_end TIMESTAMP,
    review_start TIMESTAMP,
    review_end TIMESTAMP,
    published_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_contest_tenant_status ON contest(tenant_id, status);

CREATE TABLE contest_signup (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    contest_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    team_name VARCHAR(128),
    contact VARCHAR(128),
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_signup_contest_user UNIQUE (tenant_id, contest_id, user_id)
);

CREATE TABLE contest_work (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    contest_id BIGINT NOT NULL,
    signup_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    title VARCHAR(160) NOT NULL,
    description VARCHAR(2000),
    video_url VARCHAR(1000),
    file_url VARCHAR(1000),
    status VARCHAR(32) NOT NULL,
    total_score DECIMAL(8,2),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_work_signup UNIQUE (tenant_id, signup_id)
);
CREATE INDEX idx_work_contest ON contest_work(tenant_id, contest_id);

CREATE TABLE rubric (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    contest_id BIGINT NOT NULL,
    name VARCHAR(128) NOT NULL,
    mode VARCHAR(16) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE rubric_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    rubric_id BIGINT NOT NULL,
    item_key VARCHAR(64) NOT NULL,
    label VARCHAR(128) NOT NULL,
    weight DECIMAL(6,3) NOT NULL,
    min_score DECIMAL(6,2) NOT NULL,
    max_score DECIMAL(6,2) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_rubric_item_rubric ON rubric_item(tenant_id, rubric_id);

CREATE TABLE judge_score (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    contest_id BIGINT NOT NULL,
    work_id BIGINT NOT NULL,
    judge_id BIGINT NOT NULL,
    total_score DECIMAL(8,2) NOT NULL,
    comment VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_score_work_judge UNIQUE (tenant_id, work_id, judge_id)
);

CREATE TABLE certificate (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    contest_id BIGINT NOT NULL,
    user_id BIGINT,
    award VARCHAR(128) NOT NULL,
    code VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL,
    issued_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_certificate_code UNIQUE (code)
);

CREATE TABLE notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    type VARCHAR(32) NOT NULL,
    title VARCHAR(160) NOT NULL,
    body VARCHAR(1000),
    link VARCHAR(500),
    read_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_notification_user ON notification(tenant_id, user_id, read_at);

CREATE TABLE sponsor (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    industry VARCHAR(64),
    contact_name VARCHAR(64),
    contact_email VARCHAR(128),
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE campaign (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sponsor_id BIGINT NOT NULL,
    tenant_id BIGINT,
    name VARCHAR(160) NOT NULL,
    type VARCHAR(32) NOT NULL,
    budget DECIMAL(12,2),
    status VARCHAR(32) NOT NULL,
    creative_text VARCHAR(255),
    creative_url VARCHAR(500),
    start_at TIMESTAMP,
    end_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_campaign_status ON campaign(status, start_at, end_at);

CREATE TABLE campaign_placement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    campaign_id BIGINT NOT NULL,
    slot VARCHAR(64) NOT NULL,
    contest_id BIGINT,
    weight INT NOT NULL DEFAULT 100,
    start_at TIMESTAMP,
    end_at TIMESTAMP
);
CREATE INDEX idx_placement_slot ON campaign_placement(slot, contest_id);

CREATE TABLE ad_event (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    campaign_id BIGINT NOT NULL,
    placement_id BIGINT,
    event_type VARCHAR(16) NOT NULL,
    user_id BIGINT,
    ip VARCHAR(64),
    user_agent VARCHAR(500),
    occurred_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_ad_event_campaign ON ad_event(campaign_id, event_type, occurred_at);
