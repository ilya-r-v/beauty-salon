CREATE INDEX idx_service_master_id
    ON service(master_id);

CREATE INDEX idx_service_service_type_id
    ON service(service_type_id);

CREATE INDEX idx_service_variant_service_id
    ON service_variant(service_id);

CREATE INDEX idx_booking_service_variant_id
    ON booking(service_variant_id);

CREATE INDEX idx_booking_client_id
    ON booking(client_id);

CREATE INDEX idx_booking_slot_id
    ON booking(slot_id);

CREATE INDEX idx_time_slot_start_time 
    ON time_slot(start_time);

CREATE INDEX idx_booking_status
    ON booking(status);

CREATE INDEX idx_review_author_id
    ON review(author_id);

CREATE INDEX idx_review_master_id
    ON review(master_id);