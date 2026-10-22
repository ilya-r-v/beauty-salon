CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE client (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name          VARCHAR(100) NOT NULL,
    phone         VARCHAR(20) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20) NOT NULL DEFAULT 'CLIENT'
        CHECK (role IN ('CLIENT', 'MASTER', 'ADMIN')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE master_profile (
    client_id   UUID PRIMARY KEY
        REFERENCES client(id) ON DELETE CASCADE,
    avatar_url  VARCHAR(255),
    description TEXT,
    rating      REAL NOT NULL DEFAULT 0
        CHECK (rating >= 0 AND rating <= 5),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);