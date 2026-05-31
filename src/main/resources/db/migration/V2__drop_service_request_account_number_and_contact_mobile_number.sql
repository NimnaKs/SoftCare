ALTER TABLE service_requests
    DROP COLUMN IF EXISTS account_number;

ALTER TABLE service_requests
    DROP COLUMN IF EXISTS contact_mobile_number;
