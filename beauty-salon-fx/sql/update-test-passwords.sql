-- psql -d beauty_salon -f sql/update-test-passwords.sql

UPDATE client SET password_hash = 'pbkdf2$65536$HfY5CouWR241jBsXRti9ug==$JxFyYSZRXTm4XCzr3qkh6/0zNS159Mcmi3OjcV0AklQ=' WHERE id = 'a1111111-0000-0000-0000-000000000001';
UPDATE client SET password_hash = 'pbkdf2$65536$m3tNYaj51D7E82ZyuQo0PQ==$zPRqEep04bILfbq61ikv+3q6DghaDNFiBIMM31RrByQ=' WHERE id = 'a1111111-0000-0000-0000-000000000002';
UPDATE client SET password_hash = 'pbkdf2$65536$2MyOB8aYbXMP5L28cbOu9A==$CbUws66IvhR+jenHjHN6YrfMAMM6WXG1pg1K6mVZTMI=' WHERE id = 'a1111111-0000-0000-0000-000000000003';
UPDATE client SET password_hash = 'pbkdf2$65536$BDjKW97A2moMV6PRxSGNrw==$SP02ni0Or+xoMFKOaNnWojDlFpDm1SOnx2nrEGXSU3Y=' WHERE id = 'a1111111-0000-0000-0000-000000000004';
UPDATE client SET password_hash = 'pbkdf2$65536$UnlrvZlCLo+0OTF0F87DLw==$F5HZJRB8MkNUxgtPmE1PXAwKtUTs6+Z4TWtr0JQIHGw=' WHERE id = 'a1111111-0000-0000-0000-000000000005';

UPDATE client SET password_hash = 'pbkdf2$65536$kpPlISjGlZzSAm7mzx55iQ==$Ycqp5QdR4ZoHvMw9ZrJikrTR+0Jbn9zIdEmhCw8c9FU='
WHERE password_hash NOT LIKE 'pbkdf2$%';
