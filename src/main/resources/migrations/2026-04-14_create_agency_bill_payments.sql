CREATE TABLE agency_bill_payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6) NULL,
    updated_at DATETIME(6) NOT NULL,
    agency_id BIGINT NOT NULL,
    cash_account_id BIGINT NULL,
    cash_account_name VARCHAR(255) NULL,
    payment_method_id BIGINT NULL,
    payment_method_name VARCHAR(255) NULL,
    connection_id BIGINT NULL,
    connection_account_number VARCHAR(50) NULL,
    member_name VARCHAR(255) NULL,
    reference_text VARCHAR(255) NOT NULL,
    paid_date DATE NOT NULL,
    bill_amount DECIMAL(14,2) NOT NULL,
    service_charge_amount DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    total_amount DECIMAL(14,2) NOT NULL,
    CONSTRAINT fk_agency_bill_payments_agency FOREIGN KEY (agency_id) REFERENCES agencies (id)
);

CREATE INDEX idx_agency_bill_payments_agency_created ON agency_bill_payments (agency_id, created_at DESC);

CREATE TABLE agency_bill_payment_settlements (
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

CREATE INDEX idx_agency_bill_payment_settlements_payment_created ON agency_bill_payment_settlements (agency_bill_payment_id, created_at ASC);
