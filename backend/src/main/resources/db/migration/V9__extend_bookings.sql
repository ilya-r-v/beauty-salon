ALTER TABLE booking
    ADD COLUMN booking_number BIGINT GENERATED ALWAYS AS IDENTITY,
    ADD CONSTRAINT uq_booking_number UNIQUE (booking_number),
    ADD COLUMN booked_price NUMERIC(10,2) CHECK (booked_price > 0),
    ADD COLUMN comment VARCHAR(500),
    ADD COLUMN prepaid BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE booking AS b
SET booked_price = v.price
FROM service_variant AS v
WHERE v.id = b.service_variant_id;

ALTER TABLE booking ALTER COLUMN booked_price SET NOT NULL;

CREATE TABLE booking_tag (
    booking_id UUID NOT NULL REFERENCES booking(id) ON DELETE CASCADE,
    tag VARCHAR(50) NOT NULL CHECK (btrim(tag) <> ''),
    PRIMARY KEY (booking_id, tag)
);

CREATE INDEX idx_booking_tag_tag ON booking_tag (tag, booking_id);
