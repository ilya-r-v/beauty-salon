CREATE TABLE account_role (code varchar(20) PRIMARY KEY);
INSERT INTO account_role VALUES ('CLIENT'), ('MASTER'), ('ADMIN');
ALTER TABLE client ADD FOREIGN KEY(role) REFERENCES account_role(code);
ALTER TABLE service_type ADD COLUMN active boolean NOT NULL DEFAULT true;
ALTER TABLE booking ADD COLUMN external_code varchar(80);
UPDATE booking SET external_code = booking_number::text;
ALTER TABLE booking ALTER COLUMN external_code SET NOT NULL;
ALTER TABLE booking ADD UNIQUE (external_code);
ALTER TABLE booking ADD COLUMN priority varchar(10) GENERATED ALWAYS AS
  (CASE WHEN status = 'PENDING' AND NOT prepaid THEN 'HIGH' ELSE 'NORMAL' END) STORED;
CREATE INDEX booking_filter ON booking (archived,status,booked_price);
CREATE TABLE booking_note (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), booking_id uuid NOT NULL REFERENCES booking ON DELETE CASCADE,
 author_id uuid NOT NULL REFERENCES client, body varchar(2000) NOT NULL, pinned boolean NOT NULL,
 created_at timestamptz NOT NULL DEFAULT now(), updated_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX notes_history ON booking_note(booking_id,pinned,created_at);
CREATE TABLE audit_event (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), actor uuid, event text NOT NULL, entity text NOT NULL,
 entity_id uuid, occurred_at timestamptz NOT NULL DEFAULT now(), description text NOT NULL, correlation_id text
);
CREATE INDEX audit_time ON audit_event(occurred_at DESC);
CREATE TABLE login_attempt (phone varchar(20) PRIMARY KEY, failures int NOT NULL, since timestamptz NOT NULL);
CREATE FUNCTION audit_change() RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE row_data jsonb; target uuid;
BEGIN
 row_data := CASE WHEN TG_OP = 'DELETE' THEN to_jsonb(OLD) ELSE to_jsonb(NEW) END;
 target := COALESCE(row_data->>'id',row_data->>'booking_id',row_data->>'client_id')::uuid;
 INSERT INTO audit_event(actor,event,entity,entity_id,description,correlation_id)
 VALUES(nullif(current_setting('salon.actor', true),'')::uuid,TG_OP,TG_TABLE_NAME,target,
        TG_OP || ' ' || TG_TABLE_NAME,current_setting('salon.correlation',true));
 RETURN NULL;
END $$;
DO $$ DECLARE t text; BEGIN
 FOREACH t IN ARRAY ARRAY['booking','booking_note','booking_tag','client','service_type','service','service_variant','master_profile','review'] LOOP
 EXECUTE format('CREATE TRIGGER record_change AFTER INSERT OR UPDATE OR DELETE ON %I FOR EACH ROW EXECUTE FUNCTION audit_change()',t);
 END LOOP;
END $$;
CREATE TABLE import_preview (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), actor uuid NOT NULL REFERENCES client ON DELETE CASCADE,
 csv text NOT NULL, mode text NOT NULL, expires_at timestamptz NOT NULL DEFAULT now()+interval '15 minutes'
);
ALTER TABLE client ADD COLUMN credential_version bigint NOT NULL DEFAULT 0;
