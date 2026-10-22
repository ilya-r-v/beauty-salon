CREATE TABLE time_slot (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    master_id  UUID NOT NULL
        REFERENCES master_profile(client_id) ON DELETE CASCADE,
    start_time TIMESTAMP NOT NULL,
    end_time   TIMESTAMP NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_time_slot_range
        CHECK (end_time > start_time),
    CONSTRAINT uq_time_slot_master_start
        UNIQUE (master_id, start_time)
);