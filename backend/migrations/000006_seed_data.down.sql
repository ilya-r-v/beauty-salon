DELETE FROM review
WHERE id::text LIKE '60000000-%'
   OR author_id::text LIKE '10000000-%'
   OR master_id::text LIKE '10000000-%';

DELETE FROM booking
WHERE id::text LIKE '50000000-%'
   OR slot_id::text LIKE '70000000-%'
   OR slot_id::text LIKE '71000000-%'
   OR service_variant_id::text LIKE '40000000-%'
   OR client_id::text LIKE '10000000-%';

DELETE FROM time_slot       WHERE id::text LIKE '70000000-%'
                               OR id::text LIKE '71000000-%';
DELETE FROM service_variant WHERE id::text         LIKE '40000000-%';
DELETE FROM service         WHERE id::text         LIKE '30000000-%';
DELETE FROM service_type    WHERE id::text         LIKE '20000000-%';
DELETE FROM master_profile  WHERE client_id::text  LIKE '10000000-%';
DELETE FROM client          WHERE id::text         LIKE '10000000-%';