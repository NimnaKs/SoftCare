ALTER TABLE sales_invoice_inventory_items
    ADD COLUMN inventory_template_id BIGINT NULL AFTER invoice_id,
    ADD COLUMN batch_no VARCHAR(50) NULL AFTER inventory_template_id;

CREATE INDEX idx_sales_inv_item_template_id ON sales_invoice_inventory_items (inventory_template_id);
CREATE INDEX idx_sales_inv_item_batch_no ON sales_invoice_inventory_items (batch_no);
