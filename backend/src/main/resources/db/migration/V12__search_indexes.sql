CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE INDEX client_name_search ON client USING gin (lower(name) gin_trgm_ops);
CREATE INDEX variant_title_search ON service_variant USING gin (lower(title) gin_trgm_ops);
CREATE INDEX booking_comment_search ON booking USING gin (lower(comment) gin_trgm_ops);
CREATE INDEX booking_code_search ON booking USING gin (lower(external_code) gin_trgm_ops);
CREATE INDEX booking_priority_search ON booking(priority);
CREATE INDEX login_attempt_expiry ON login_attempt(since);
CREATE INDEX import_preview_expiry ON import_preview(expires_at);
