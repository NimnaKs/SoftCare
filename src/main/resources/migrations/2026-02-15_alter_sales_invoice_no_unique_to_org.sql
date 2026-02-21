ALTER TABLE sales_invoices
    DROP INDEX invoice_no;

ALTER TABLE sales_invoices
    ADD CONSTRAINT uq_sales_invoices_org_invoice_no UNIQUE (org_unit_id, invoice_no);
