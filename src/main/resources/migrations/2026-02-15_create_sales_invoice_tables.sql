CREATE TABLE IF NOT EXISTS sales_invoices (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP NULL,
    invoice_no VARCHAR(50) NOT NULL UNIQUE,
    sale_type VARCHAR(30) NOT NULL,
    billing_method VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    org_unit_id BIGINT NULL,
    billing_zone_id BIGINT NULL,
    customer_name VARCHAR(255) NULL,
    customer_nic VARCHAR(255) NULL,
    customer_address VARCHAR(500) NULL,
    customer_mobile VARCHAR(255) NULL,
    revenue_total DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    sales_expense_total DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    consumption_expense_total DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    grand_total_payable DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    has_inventory_issue BIT NOT NULL DEFAULT 0,
    is_recurring BIT NOT NULL DEFAULT 0,
    recurring_enabled BIT NOT NULL DEFAULT 0,
    down_payment DECIMAL(14,2) NULL,
    number_of_installments INT NULL,
    INDEX idx_sales_invoices_invoice_no (invoice_no),
    INDEX idx_sales_invoices_billing_zone_id (billing_zone_id),
    INDEX idx_sales_invoices_status (status)
);

CREATE TABLE IF NOT EXISTS sales_invoice_revenue_lines (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP NULL,
    invoice_id BIGINT NOT NULL,
    revenue_account_id BIGINT NOT NULL,
    type_label VARCHAR(255) NOT NULL,
    description VARCHAR(500) NULL,
    amount DECIMAL(14,2) NOT NULL,
    reference_no VARCHAR(100) NULL,
    CONSTRAINT fk_sales_rev_line_invoice FOREIGN KEY (invoice_id) REFERENCES sales_invoices(id),
    CONSTRAINT fk_sales_rev_line_account FOREIGN KEY (revenue_account_id) REFERENCES revenue_accounts(id),
    INDEX idx_sales_inv_rev_line_account_id (revenue_account_id)
);

CREATE TABLE IF NOT EXISTS sales_invoice_inventory_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP NULL,
    invoice_id BIGINT NOT NULL,
    category1 VARCHAR(255) NULL,
    category2 VARCHAR(255) NULL,
    category3 VARCHAR(255) NULL,
    description_spec VARCHAR(500) NULL,
    qty DECIMAL(14,3) NOT NULL,
    unit VARCHAR(50) NULL,
    unit_cost DECIMAL(14,2) NOT NULL,
    amount DECIMAL(14,2) NOT NULL,
    CONSTRAINT fk_sales_inventory_item_invoice FOREIGN KEY (invoice_id) REFERENCES sales_invoices(id)
);

CREATE TABLE IF NOT EXISTS sales_invoice_inventory_policy (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP NULL,
    invoice_id BIGINT NOT NULL UNIQUE,
    record_as VARCHAR(40) NOT NULL,
    charged_from_customer BIT NOT NULL DEFAULT 0,
    CONSTRAINT fk_sales_inventory_policy_invoice FOREIGN KEY (invoice_id) REFERENCES sales_invoices(id)
);

CREATE TABLE IF NOT EXISTS sales_invoice_installments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP NULL,
    invoice_id BIGINT NOT NULL,
    installment_no INT NOT NULL,
    label VARCHAR(255) NOT NULL,
    amount DECIMAL(14,2) NOT NULL,
    due_date DATE NULL,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT fk_sales_installment_invoice FOREIGN KEY (invoice_id) REFERENCES sales_invoices(id)
);

CREATE TABLE IF NOT EXISTS sales_invoice_connections (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP NULL,
    invoice_id BIGINT NOT NULL,
    connection_id BIGINT NOT NULL,
    billing_zone_id BIGINT NULL,
    premises_no VARCHAR(255) NOT NULL,
    connection_no VARCHAR(255) NOT NULL,
    account_number VARCHAR(255) NOT NULL,
    customer_name VARCHAR(255) NOT NULL,
    CONSTRAINT fk_sales_connections_invoice FOREIGN KEY (invoice_id) REFERENCES sales_invoices(id),
    INDEX idx_sales_inv_connections_account_no (account_number),
    INDEX idx_sales_inv_connections_connection_id (connection_id)
);
