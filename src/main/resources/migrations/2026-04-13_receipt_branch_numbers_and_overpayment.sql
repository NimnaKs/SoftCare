-- Reset receipt data before switching numbering and settlement semantics
DELETE FROM receipt_settlements;
DELETE FROM unrecognized_receipts;
DELETE FROM cheque_tracking;
DELETE FROM receipt_audit_logs;
DELETE FROM receipts;

ALTER TABLE receipts
    DROP INDEX IF EXISTS uq_receipt_no;

ALTER TABLE receipts
    DROP COLUMN IF EXISTS branch_receipt_no;

ALTER TABLE receipts
    ADD UNIQUE KEY uq_receipt_org_receipt_no (org_unit_id, receipt_no);

ALTER TABLE receipt_settlements
    ADD COLUMN IF NOT EXISTS settlement_type ENUM('INVOICE','OVERPAYMENT','LIABILITY') NOT NULL DEFAULT 'INVOICE' AFTER settled_amount,
    ADD COLUMN IF NOT EXISTS liability_account_id BIGINT UNSIGNED NULL AFTER settlement_type,
    ADD COLUMN IF NOT EXISTS reference_no VARCHAR(50) NULL AFTER liability_account_id;

ALTER TABLE receipt_settlements
    ADD INDEX IF NOT EXISTS idx_rc_set_reference_no (reference_no);

ALTER TABLE receipt_settlements
    ADD INDEX IF NOT EXISTS idx_rc_set_liability_account (liability_account_id);

INSERT IGNORE INTO liability_accounts (
    id,
    main_category_id,
    account_number,
    name,
    description,
    is_default,
    function_key,
    is_system,
    is_active
) VALUES
(3, 1, 'LIA-0002', 'Unrecognized Receipts', 'Unrecognized Receipts', 1, 'UNRECOGNIZED_RECEIPTS', 1, 1),
(4, 1, 'LIA-0003', 'Over Payment', 'Over Payment', 0, 'OVER_PAYMENT', 1, 1);

ALTER TABLE liability_accounts AUTO_INCREMENT = 5;
