-- Allow converted unrecognized receipts to persist their final state
ALTER TABLE unrecognized_receipts
    ADD COLUMN IF NOT EXISTS allocated_amount DECIMAL(14,2) NOT NULL DEFAULT 0 AFTER liability_account_id,
    MODIFY status ENUM('OPEN','REFUNDED','SETTLED_AS_CUSTOMER','SETTLED_AS_INCOME','SETTLED_AS_NON_CUSTOMER','REVERSED') NOT NULL DEFAULT 'OPEN';
