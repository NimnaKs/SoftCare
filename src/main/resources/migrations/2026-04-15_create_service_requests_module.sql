ALTER TABLE service_requests
    ADD COLUMN contact_mobile_number VARCHAR(20) NULL,
    ADD COLUMN connection_tariff_id BIGINT UNSIGNED NULL;

ALTER TABLE service_request_solutions
    ADD COLUMN before_connection_status VARCHAR(30) NULL,
    ADD COLUMN after_connection_status VARCHAR(30) NULL,
    ADD COLUMN before_meter_status VARCHAR(30) NULL,
    ADD COLUMN after_meter_status VARCHAR(30) NULL,
    ADD COLUMN meter_status VARCHAR(30) NULL,
    ADD COLUMN system_action VARCHAR(255) NULL,
    ADD COLUMN description VARCHAR(1000) NULL,
    ADD COLUMN bill_is_open TINYINT(1) NOT NULL DEFAULT 1,
    ADD COLUMN pending_account_update TINYINT(1) NOT NULL DEFAULT 0,
    ADD COLUMN reconnection_fee DECIMAL(14,2) NULL;

ALTER TABLE service_request_material_consumptions
    ADD COLUMN has_mcn_form TINYINT(1) NOT NULL DEFAULT 0,
    ADD COLUMN mcn_reference VARCHAR(100) NULL;

