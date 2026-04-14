ALTER TABLE agency_bill_payments
    ADD COLUMN IF NOT EXISTS connection_id BIGINT NULL AFTER payment_method_name,
    ADD COLUMN IF NOT EXISTS connection_account_number VARCHAR(50) NULL AFTER connection_id,
    ADD COLUMN IF NOT EXISTS member_name VARCHAR(255) NULL AFTER connection_account_number;

CREATE TABLE IF NOT EXISTS agency_bill_payment_settlements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6) NULL,
    updated_at DATETIME(6) NOT NULL,
    agency_bill_payment_id BIGINT NOT NULL,
    invoice_id BIGINT NULL,
    installment_id BIGINT NULL,
    settled_amount DECIMAL(14,2) NOT NULL,
    reference_no VARCHAR(50) NULL,
    CONSTRAINT fk_agency_bill_payment_settlements_payment FOREIGN KEY (agency_bill_payment_id) REFERENCES agency_bill_payments (id)
);
