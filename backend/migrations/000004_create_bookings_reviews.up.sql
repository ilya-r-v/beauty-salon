CREATE TABLE booking (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    service_variant_id UUID NOT NULL
        REFERENCES service_variant(id),
    slot_id            UUID NOT NULL
        REFERENCES time_slot(id),
    status             VARCHAR(20) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING','CONFIRMED','COMPLETED','CANCELLED')),
    client_id          UUID NOT NULL
        REFERENCES client(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX uq_booking_active_slot
    ON booking(slot_id) WHERE status <> 'CANCELLED';

CREATE TABLE review (
    id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    author_id UUID NOT NULL
        REFERENCES client(id),
    master_id UUID NOT NULL
        REFERENCES master_profile(client_id),
    content   TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);