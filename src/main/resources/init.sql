CREATE SCHEMA IF NOT EXISTS generated;

CREATE TABLE IF NOT EXISTS chats (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS messages (
    id BIGSERIAL PRIMARY KEY,
    chat_id BIGINT NOT NULL REFERENCES chats(id),
    role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS features (
    id BIGSERIAL PRIMARY KEY,
    chat_id BIGINT NOT NULL REFERENCES chats(id),
    type VARCHAR(50) NOT NULL,
    description TEXT,
    content TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    parameters TEXT,
    results TEXT,
    error_message TEXT,
    linked_feature_id BIGINT,
    created_at TIMESTAMP NOT NULL
);

ALTER TABLE features ADD COLUMN IF NOT EXISTS linked_feature_id BIGINT;