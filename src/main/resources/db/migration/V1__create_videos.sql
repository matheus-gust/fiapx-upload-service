CREATE TABLE videos (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_email        VARCHAR(255) NOT NULL,
    original_filename VARCHAR(500) NOT NULL,
    s3_key            VARCHAR(1000) NOT NULL,
    zip_s3_key        VARCHAR(1000),
    status            VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    error_message     TEXT,
    created_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_videos_user_email ON videos (user_email);
