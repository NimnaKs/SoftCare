CREATE TABLE meter_reader_zone_assignments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6) NULL,
    org_unit_id BIGINT NOT NULL,
    reader_user_id BIGINT NOT NULL,
    billing_zone_id BIGINT NOT NULL,
    assigned_from DATE NOT NULL,
    assigned_to DATE NULL,
    note VARCHAR(500) NULL,
    CONSTRAINT FK_mrza_org_unit FOREIGN KEY (org_unit_id) REFERENCES org_units(id),
    CONSTRAINT FK_mrza_reader_user FOREIGN KEY (reader_user_id) REFERENCES users(id),
    CONSTRAINT FK_mrza_billing_zone FOREIGN KEY (billing_zone_id) REFERENCES billing_zones(id)
);
