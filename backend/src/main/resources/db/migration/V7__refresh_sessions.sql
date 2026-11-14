ALTER TABLE client ADD COLUMN active boolean NOT NULL DEFAULT true;
ALTER TABLE client ADD COLUMN password_change_required boolean NOT NULL DEFAULT false;
CREATE TABLE refresh_session (
 token_hash varchar(64) PRIMARY KEY,
 client_id uuid NOT NULL REFERENCES client(id) ON DELETE CASCADE,
 expires_at timestamptz NOT NULL,
 created_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_refresh_client ON refresh_session(client_id);