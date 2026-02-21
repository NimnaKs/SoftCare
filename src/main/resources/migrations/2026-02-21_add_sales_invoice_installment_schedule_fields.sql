ALTER TABLE sales_invoices
    ADD COLUMN down_payment_date DATE NULL,
    ADD COLUMN installment_start_year INT NULL,
    ADD COLUMN installment_start_month INT NULL;
