ALTER TABLE booking ADD COLUMN archived boolean NOT NULL DEFAULT false;
ALTER TABLE booking ADD COLUMN version bigint NOT NULL DEFAULT 0;
CREATE INDEX idx_booking_archived ON booking(archived);
