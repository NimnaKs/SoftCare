ALTER TABLE service_requests
    ADD COLUMN ticket_no VARCHAR(50) NULL AFTER id,
    ADD COLUMN account_number VARCHAR(100) NULL AFTER connection_id,
    ADD COLUMN customer_name_snapshot VARCHAR(255) NULL AFTER account_number,
    ADD COLUMN current_stage VARCHAR(40) NOT NULL DEFAULT 'REQUEST' AFTER status,
    ADD COLUMN submitted_at DATETIME NULL AFTER saved_at,
    ADD COLUMN last_paused_at DATETIME NULL AFTER submitted_at,
    ADD COLUMN expiry_at DATETIME NULL AFTER closed_at,
    ADD COLUMN total_paused_minutes BIGINT NOT NULL DEFAULT 0 AFTER expiry_at;

UPDATE service_requests
SET account_number = COALESCE(account_number, NULL),
    current_stage = CASE
        WHEN status = 'DRAFT' THEN 'REQUEST'
        WHEN status = 'CLOSED' THEN 'CLOSED'
        ELSE 'WORK_ORDER'
        END,
    submitted_at = CASE
        WHEN status = 'DRAFT' THEN NULL
        ELSE saved_at
        END,
    expiry_at = CASE
        WHEN status = 'DRAFT' THEN NULL
        ELSE DATE_ADD(saved_at, INTERVAL 7 DAY)
        END,
    ticket_no = CONCAT('SR-', LPAD(id, 6, '0'))
WHERE ticket_no IS NULL;

ALTER TABLE service_requests
    MODIFY COLUMN ticket_no VARCHAR(50) NOT NULL,
    MODIFY COLUMN request_group ENUM('CUSTOMER_COMPLAINT','CONNECTION_METER_SERVICE','DISTRIBUTION_LINE_ISSUE','OTHER_ISSUES') NOT NULL,
    MODIFY COLUMN category ENUM(
        'WATER_SUPPLY_PRESSURE_ISSUES',
        'WATER_QUALITY_SOURCE_ISSUES',
        'WATER_BILLING_USAGE_METER_READING_ISSUES',
        'CONNECTION_INSTALLATION_DISCONNECTION_RECONNECTION',
        'OWNERSHIP_PREMISES_TARIFF_TRANSFERS',
        'BULK_VALVE_BULK_METER_FAULTS_DAMAGES',
        'SERVICE_PIPELINE_LEAKS_DAMAGES_BLOCKAGES',
        'MAIN_PIPELINE_LEAKS_DAMAGES_BLOCKAGES',
        'SERVICE_VALVE_SERVICE_METER_FAULTS_DAMAGES_BLOCKAGES',
        'PIPE_BURSTS_WATER_LEAKAGES',
        'TANK_AERATOR_FILTRATION_SYSTEM_ISSUES',
        'WATER_PUMP_PUMP_HOUSE_FAULTS',
        'ELECTRICAL_SYSTEM_GENERATOR_FAULTS',
        'COMPUTER_INTERNET_CONNECTIVITY_FAULTS',
        'ILLEGAL_WATER_SUPPLY_COMPLAINTS',
        'STAFF_EMPLOYEE_CONDUCT_COMPLAINTS',
        'DOCUMENT_REQUESTS_GENERAL_INQUIRIES',
        'SUGGESTIONS_COMPLAINT_PROGRESS_INQUIRIES',
        'BILLING_PAYMENT_CENTER_COMPLAINTS',
        'OTHER'
    ) NOT NULL,
    MODIFY COLUMN status ENUM('DRAFT','SUBMITTED','IN_PROGRESS','PAUSED','RESOLVED','CLOSED','CANCELLED') NOT NULL DEFAULT 'DRAFT';

ALTER TABLE service_request_work_orders
    MODIFY COLUMN action_type ENUM(
        'FORWARDED_TO_MAINTENANCE_DEPARTMENT',
        'SUBMITTED_TO_EXECUTIVE_COMMITTEE_FOR_APPROVAL',
        'ASSIGNED_TO_COMPUTER_OPERATOR',
        'FORWARDED_TO_ACCOUNTS_CLERK',
        'SENT_FOR_INFORMATION_VERIFICATION',
        'OTHER_ACTION'
    ) NOT NULL,
    ADD COLUMN created_by BIGINT UNSIGNED NULL AFTER status,
    ADD COLUMN updated_by BIGINT UNSIGNED NULL AFTER created_by;

ALTER TABLE service_request_work_order_employees
    ADD COLUMN assigned_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER employee_id;

ALTER TABLE service_request_solutions
    MODIFY COLUMN resolution_type ENUM(
        'NEW_SERVICE_CONNECTION_INSTALLED',
        'SERVICE_DISCONNECTED_DUE_TO_NON_PAYMENT',
        'SERVICE_DISCONNECTED_UPON_CUSTOMER_REQUEST',
        'SERVICE_RECONNECTED',
        'NEW_METER_REPLACED',
        'METER_REPAIRED',
        'METER_READING_ADJUSTED',
        'SERVICE_LINE_REPAIRED',
        'MAIN_LINE_REPAIRED',
        'OTHER_RESOLUTION'
    ) NOT NULL,
    DROP COLUMN other_description,
    DROP COLUMN meter_status,
    DROP COLUMN system_action,
    DROP COLUMN bill_is_open,
    DROP COLUMN pending_account_update,
    DROP COLUMN reconnection_fee,
    ADD COLUMN meter_action VARCHAR(30) NULL AFTER after_meter_status,
    ADD COLUMN application_status ENUM('DRAFT','PENDING_BILL_CLOSE','READY_TO_APPLY','APPLIED','FAILED') NOT NULL DEFAULT 'DRAFT' AFTER meter_action,
    ADD COLUMN bill_status_at_resolution ENUM('OPEN','CLOSED','NOT_APPLICABLE') NOT NULL DEFAULT 'NOT_APPLICABLE' AFTER application_status,
    ADD COLUMN requires_reconnection_fee TINYINT(1) NOT NULL DEFAULT 0 AFTER bill_status_at_resolution,
    ADD COLUMN reconnection_fee_amount DECIMAL(14,2) NULL AFTER requires_reconnection_fee,
    ADD COLUMN tariff_id BIGINT UNSIGNED NULL AFTER reconnection_fee_amount,
    ADD COLUMN invoice_id BIGINT UNSIGNED NULL AFTER tariff_id,
    ADD COLUMN applied_at DATETIME NULL AFTER invoice_id,
    ADD COLUMN applied_by BIGINT UNSIGNED NULL AFTER applied_at,
    ADD COLUMN created_by BIGINT UNSIGNED NULL AFTER applied_by,
    ADD COLUMN updated_by BIGINT UNSIGNED NULL AFTER created_by;

UPDATE service_request_solutions
SET application_status = CASE
    WHEN status = 'APPLIED' THEN 'APPLIED'
    ELSE 'PENDING_BILL_CLOSE'
    END,
    bill_status_at_resolution = 'NOT_APPLICABLE',
    requires_reconnection_fee = 0;

ALTER TABLE service_request_solutions
    DROP COLUMN status;

ALTER TABLE service_request_material_consumptions
    DROP COLUMN has_mcn_form,
    DROP COLUMN mcn_reference,
    DROP COLUMN import_from_mcn,
    DROP COLUMN maintain_charge_amount,
    DROP COLUMN created_at,
    ADD COLUMN maintain_charge DECIMAL(14,2) NULL AFTER description,
    ADD COLUMN invoice_id BIGINT UNSIGNED NULL AFTER maintain_charge,
    ADD COLUMN status ENUM('DRAFT','INVOICED','SKIPPED') NOT NULL DEFAULT 'DRAFT' AFTER invoice_id,
    ADD COLUMN created_by BIGINT UNSIGNED NULL AFTER status,
    ADD COLUMN updated_by BIGINT UNSIGNED NULL AFTER created_by,
    ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER updated_by,
    ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER created_at;

ALTER TABLE service_request_feedback
    MODIFY COLUMN final_response ENUM('CONTACTED_AND_INFORMED','CONTACTED_BUT_COULD_NOT_INFORM','BUSY','NO_ANSWER','NOT_RESPONDING') NOT NULL,
    DROP COLUMN updated_at,
    ADD COLUMN remarks VARCHAR(1000) NULL AFTER final_response,
    ADD COLUMN updated_by BIGINT UNSIGNED NULL AFTER remarks,
    ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER updated_by,
    ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER created_at;

CREATE TABLE service_request_stages (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    service_request_id BIGINT UNSIGNED NOT NULL,
    stage_type ENUM('REQUEST','WORK_ORDER','SOLUTION','MATERIAL_CONSUMPTION','FEEDBACK','CLOSED') NOT NULL,
    status ENUM('NOT_STARTED','IN_PROGRESS','PAUSED','COMPLETED','SKIPPED') NOT NULL DEFAULT 'NOT_STARTED',
    started_at DATETIME NULL,
    paused_at DATETIME NULL,
    completed_at DATETIME NULL,
    last_updated_by BIGINT UNSIGNED NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    UNIQUE KEY uq_service_request_stage (service_request_id, stage_type),
    CONSTRAINT fk_sr_stage_request FOREIGN KEY (service_request_id) REFERENCES service_requests(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE service_request_timeline_events (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    service_request_id BIGINT UNSIGNED NOT NULL,
    stage_type ENUM('REQUEST','WORK_ORDER','SOLUTION','MATERIAL_CONSUMPTION','FEEDBACK','CLOSED') NULL,
    event_type ENUM(
        'CREATED',
        'UPDATED',
        'SUBMITTED',
        'PAUSED',
        'RESUMED',
        'MOBILE_UPDATED',
        'WORK_ORDER_SAVED',
        'WORK_ORDER_COMPLETED',
        'SOLUTION_SAVED',
        'SOLUTION_APPLIED',
        'SOLUTION_PENDING',
        'MATERIAL_CONSUMPTION_SAVED',
        'INVOICE_CREATED',
        'FEEDBACK_SAVED',
        'RESOLVED',
        'CLOSED',
        'CANCELLED'
    ) NOT NULL,
    notes VARCHAR(1000) NULL,
    payload_json VARCHAR(4000) NULL,
    created_by BIGINT UNSIGNED NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    INDEX idx_sr_timeline_request (service_request_id, created_at),
    CONSTRAINT fk_sr_timeline_request FOREIGN KEY (service_request_id) REFERENCES service_requests(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
