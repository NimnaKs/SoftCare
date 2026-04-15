ALTER TABLE agency_bill_payments
    MODIFY cash_account_id BIGINT NULL,
    MODIFY cash_account_name VARCHAR(255) NULL,
    MODIFY payment_method_id BIGINT NULL,
    MODIFY payment_method_name VARCHAR(255) NULL,
    MODIFY reference_text VARCHAR(255) NULL;
