-- Retire unchanged credentials from the original coursework seed only.
UPDATE client SET password_hash='BOOTSTRAP_REQUIRED', password_change_required=true, credential_version=credential_version+1
WHERE id IN ('10000000-0000-0000-0000-000000000001','10000000-0000-0000-0000-000000000002',
             '10000000-0000-0000-0000-000000000003','10000000-0000-0000-0000-000000000004',
             '10000000-0000-0000-0000-000000000005') AND password_hash=crypt('password',password_hash);
DELETE FROM refresh_session WHERE client_id IN (SELECT id FROM client WHERE password_hash='BOOTSTRAP_REQUIRED');
