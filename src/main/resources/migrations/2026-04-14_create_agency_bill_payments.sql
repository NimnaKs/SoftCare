CREATE TABLE agency_bill_payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6) NULL,
    updated_at DATETIME(6) NOT NULL,
    agency_id BIGINT NOT NULL,
    cash_account_id BIGINT NOT NULL,
    cash_account_name VARCHAR(255) NOT NULL,
    payment_method_id BIGINT NOT NULL,
    payment_method_name VARCHAR(255) NOT NULL,
    reference_text VARCHAR(255) NOT NULL,
    paid_date DATE NOT NULL,
    bill_amount DECIMAL(14,2) NOT NULL,
    service_charge_amount DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    total_amount DECIMAL(14,2) NOT NULL,
    CONSTRAINT fk_agency_bill_payments_agency FOREIGN KEY (agency_id) REFERENCES agencies (id)
);

CREATE INDEX idx_agency_bill_payments_agency_created ON agency_bill_payments (agency_id, created_at DESC);
