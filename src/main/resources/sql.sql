CREATE DATABASE IF NOT EXISTS water_management
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE water_management;

CREATE TABLE roles (
                       id           BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                       name         VARCHAR(100) NOT NULL,
                       description  VARCHAR(255) NULL,
                       app_scope    ENUM('WEB', 'METER_APP', 'AGENCY_APP', 'AGENCY_WEB', 'ADMIN_PORTAL')
                                                 NOT NULL DEFAULT 'WEB',
                       UNIQUE KEY uq_roles_name (name)
) ENGINE=InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE water_projects (
                                id                     BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                name                   VARCHAR(255) NOT NULL,
                                status                 ENUM('REGISTERED', 'UNREGISTERED')
                                                                    NOT NULL DEFAULT 'UNREGISTERED',
                                department_org_name    VARCHAR(255) NULL,
                                registered_at          DATETIME NULL,
                                created_at             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                updated_at             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                                    ON UPDATE CURRENT_TIMESTAMP,

                                INDEX idx_wp_status (status),
                                INDEX idx_wp_department_name (department_org_name)
) ENGINE=InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE org_units (
                           id               BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                           name             VARCHAR(255) NOT NULL,
                           level            ENUM('NATIONAL', 'PROVINCE', 'DISTRICT', 'DIVISION', 'BRANCH')
                                                         NOT NULL,
                           parent_id        BIGINT UNSIGNED NULL,
                           water_project_id BIGINT UNSIGNED NULL,
                           created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                               ON UPDATE CURRENT_TIMESTAMP,
                           INDEX idx_org_units_parent (parent_id),
                           INDEX idx_org_units_level (level),
                           INDEX idx_org_units_water_project (water_project_id),
                           CONSTRAINT fk_org_units_parent
                               FOREIGN KEY (parent_id) REFERENCES org_units(id),
                           CONSTRAINT fk_org_units_water_project
                               FOREIGN KEY (water_project_id) REFERENCES water_projects(id)
) ENGINE=InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE organizations (
                               id                  BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                               org_unit_id         BIGINT UNSIGNED NOT NULL,
                               organization_code   VARCHAR(50) NOT NULL,
                               name_en             VARCHAR(255) NOT NULL,
                               name_si             VARCHAR(255) NULL,
                               name_ta             VARCHAR(255) NULL,
                               address_en          VARCHAR(500) NULL,
                               address_si          VARCHAR(500) NULL,
                               address_ta          VARCHAR(500) NULL,
                               postal_code         VARCHAR(20) NULL,
                               registration_number VARCHAR(100) NULL,
                               email               VARCHAR(255) NULL,
                               mobile_number       VARCHAR(20) NOT NULL,
                               telephone_number    VARCHAR(20) NULL,
                               logo_url            VARCHAR(500) NULL,
                               created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               updated_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                                   ON UPDATE CURRENT_TIMESTAMP,
                               UNIQUE KEY uq_org_org_unit (org_unit_id),
                               UNIQUE KEY uq_org_code (organization_code),
                               CONSTRAINT fk_organizations_org_unit
                                   FOREIGN KEY (org_unit_id) REFERENCES org_units(id)
) ENGINE=InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE authorized_officers (
                                     id                  BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                     organization_id     BIGINT UNSIGNED NOT NULL,
                                     designation         VARCHAR(100) NOT NULL,
                                     name                VARCHAR(255) NOT NULL,
                                     nic                 VARCHAR(20) NULL,
                                     mobile_number       VARCHAR(20) NOT NULL,
                                     stamp_photo_url     VARCHAR(500) NULL,
                                     signature_photo_url VARCHAR(500) NULL,
                                     is_active           TINYINT(1) NOT NULL DEFAULT 1,
                                     created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     INDEX idx_ao_org (organization_id),
                                     CONSTRAINT fk_auth_officer_org
                                         FOREIGN KEY (organization_id) REFERENCES organizations(id)
) ENGINE=InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE org_notification_contacts (
                                           id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                           organization_id BIGINT UNSIGNED NOT NULL,
                                           name            VARCHAR(255) NOT NULL,
                                           mobile_number   VARCHAR(20) NOT NULL,
                                           priority_order  INT NOT NULL,
                                           created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                           INDEX idx_onc_org (organization_id),
                                           CONSTRAINT fk_onc_org
                                               FOREIGN KEY (organization_id) REFERENCES organizations(id)
) ENGINE=InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE billing_zones (
                               id          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                               org_unit_id BIGINT UNSIGNED NOT NULL,
                               zone_name   VARCHAR(255) NOT NULL,
                               description VARCHAR(500) NULL,
                               zone_code   VARCHAR(10) NOT NULL,
                               sequence_number INT NOT NULL,
                               created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               UNIQUE KEY uq_bz_org_unit_code (org_unit_id, zone_code),
                               INDEX idx_bz_org_unit (org_unit_id),
                               CONSTRAINT fk_bz_org_unit
                                   FOREIGN KEY (org_unit_id) REFERENCES org_units(id)
) ENGINE=InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE users (
                       id                        BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                       username                  VARCHAR(100) NOT NULL,
                       password_hash             VARCHAR(255) NOT NULL,

                       nic                       VARCHAR(20) NOT NULL,
                       name                      VARCHAR(255) NOT NULL,
                       mobile_number             VARCHAR(20) NOT NULL,
                       secondary_contact_number  VARCHAR(20) NULL,
                       address                   VARCHAR(500) NULL,
                       profile_photo_url         VARCHAR(500) NULL,
                       status                    ENUM('ACTIVE','DEACTIVATED')
                                                              NOT NULL DEFAULT 'ACTIVE',
                       org_unit_id               BIGINT UNSIGNED NULL,
                       created_at                DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at                DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                           ON UPDATE CURRENT_TIMESTAMP,

                       UNIQUE KEY uq_users_username (username),
                       UNIQUE KEY uq_users_nic (nic),
                       INDEX idx_users_org_unit (org_unit_id),

                       CONSTRAINT fk_users_org_unit
                           FOREIGN KEY (org_unit_id) REFERENCES org_units(id)
) ENGINE=InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


CREATE TABLE user_roles (
                            user_id BIGINT UNSIGNED NOT NULL,
                            role_id BIGINT UNSIGNED NOT NULL,
                            PRIMARY KEY (user_id, role_id),
                            CONSTRAINT fk_user_roles_user
                                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                            CONSTRAINT fk_user_roles_role
                                FOREIGN KEY (role_id) REFERENCES roles(id)
) ENGINE=InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE meter_reader_zones (
                                    id               BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                    user_id          BIGINT UNSIGNED NOT NULL,
                                    billing_zone_id  BIGINT UNSIGNED NOT NULL,

                                    assigned_date    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    status           ENUM('ACTIVE', 'DEACTIVE', 'TRANSFER')
                                                                     NOT NULL DEFAULT 'ACTIVE',
                                    reason           VARCHAR(500) NULL,

                                    UNIQUE KEY uq_mrz_user_zone (user_id, billing_zone_id),
                                    INDEX idx_mrz_user (user_id),
                                    INDEX idx_mrz_zone (billing_zone_id),

                                    CONSTRAINT fk_mrz_user
                                        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

                                    CONSTRAINT fk_mrz_zone
                                        FOREIGN KEY (billing_zone_id) REFERENCES billing_zones(id) ON DELETE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE members (
                         id               BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                         membership_code  VARCHAR(100) NOT NULL UNIQUE,
                         org_unit_id      BIGINT UNSIGNED NOT NULL,
                         membership_type  ENUM('PERSONAL','CORPORATE') NOT NULL,
                         salutation       VARCHAR(50) NULL,
                         full_name        VARCHAR(255) NULL,
                         corporate_name   VARCHAR(255) NULL,
                         nic_old          VARCHAR(20) NULL,
                         nic_new          VARCHAR(20) NULL,
                         mobile_number    VARCHAR(20) NOT NULL,
                         dp_nic_front_url VARCHAR(500) NULL,
                         dp_nic_rear_url  VARCHAR(500) NULL,
                         signature_url    VARCHAR(500) NULL,
                         brc_document_url VARCHAR(500) NULL,
                         created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                             ON UPDATE CURRENT_TIMESTAMP,

                         UNIQUE KEY uq_members_nic_old (nic_old),
                         UNIQUE KEY uq_members_nic_new (nic_new),
                         INDEX idx_members_org_unit (org_unit_id),

                         CONSTRAINT fk_members_org_unit
                             FOREIGN KEY (org_unit_id) REFERENCES org_units(id)
) ENGINE=InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE employees (
                           id                        BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                           org_unit_id               BIGINT UNSIGNED NOT NULL,
                           name                      VARCHAR(255) NOT NULL,
                           nic                       VARCHAR(20) NOT NULL,
                           date_of_birth             DATE NOT NULL,
                           date_of_appointment       DATE NULL,
                           mobile_number             VARCHAR(20) NOT NULL,
                           secondary_contact_number  VARCHAR(20) NULL,
                           address                   VARCHAR(500) NULL,
                           designation               VARCHAR(255) NOT NULL,
                           profile_photo_url         VARCHAR(500) NULL,
                           status                    ENUM('ACTIVE','DEACTIVATED')
                                                                     NOT NULL DEFAULT 'ACTIVE',
                           created_at                DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at                DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                               ON UPDATE CURRENT_TIMESTAMP,
                           INDEX idx_employees_org_unit (org_unit_id),
                           UNIQUE KEY uq_employees_nic (nic),
                           CONSTRAINT fk_employees_org_unit
                               FOREIGN KEY (org_unit_id) REFERENCES org_units(id)
) ENGINE=InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE premises (
                          id               BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                          billing_zone_id  BIGINT UNSIGNED NOT NULL,

    -- Human readable code: A1, A1.1, A1.0.1 etc.
                          premises_code    VARCHAR(50) NOT NULL,

    -- Machine-friendly sortable path (e.g., "001.0001.0000.0001")
                          sort_path        VARCHAR(100) NOT NULL,

    -- Optional: parent premises for hierarchy navigation
                          parent_id        BIGINT UNSIGNED NULL,

                          created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- One code per zone
                          UNIQUE KEY uq_premises_zone_code (billing_zone_id, premises_code),

    -- Fast ordering within a zone
                          INDEX idx_premises_zone_sort (billing_zone_id, sort_path),

                          CONSTRAINT fk_premises_zone
                              FOREIGN KEY (billing_zone_id) REFERENCES billing_zones(id),

                          CONSTRAINT fk_premises_parent
                              FOREIGN KEY (parent_id) REFERENCES premises(id)
) ENGINE=InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- TODO : Customer Log

CREATE TABLE gn_divisions (
                              id          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                              name        VARCHAR(255) NOT NULL UNIQUE,
                              created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4;

CREATE TABLE valves (
                        id          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                        name        VARCHAR(255) NOT NULL UNIQUE,
                        created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4;

CREATE TABLE societies (
                           id          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                           name        VARCHAR(255) NOT NULL UNIQUE,
                           created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4;

CREATE TABLE clusters (
                          id          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                          name        VARCHAR(255) NOT NULL UNIQUE,
                          created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4;

CREATE TABLE tariffs (
                         id          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                         name        VARCHAR(255) NOT NULL UNIQUE,
                         description VARCHAR(500) NULL,
                         created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4;

CREATE TABLE address_lines (
                               id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                               org_unit_id     BIGINT UNSIGNED NOT NULL,
                               level           TINYINT NOT NULL, -- 1 to 4
                               name            VARCHAR(255) NOT NULL,
                               parent_line1_id BIGINT UNSIGNED NULL,
                               parent_line2_id BIGINT UNSIGNED NULL,
                               parent_line3_id BIGINT UNSIGNED NULL,

                               postal_code     VARCHAR(20) NULL,       -- only for Line 1
                               internal_code   VARCHAR(50) NULL,       -- auto-generated, internal only

                               created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                                   ON UPDATE CURRENT_TIMESTAMP,

    -- Prevent duplicate names under the SAME parent combo
                               UNIQUE KEY uq_address_line_hierarchy (
                                                                     org_unit_id, level, name, parent_line1_id, parent_line2_id, parent_line3_id
                                   ),

                               INDEX idx_addr_org_unit (org_unit_id),
                               INDEX idx_addr_line1 (parent_line1_id),
                               INDEX idx_addr_line2 (parent_line2_id),
                               INDEX idx_addr_line3 (parent_line3_id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE connections (
                             id                   BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                             member_id            BIGINT UNSIGNED NOT NULL,
                             premises_id          BIGINT UNSIGNED NOT NULL,
                             billing_zone_id      BIGINT UNSIGNED NOT NULL,
                             account_number       VARCHAR(50) NOT NULL UNIQUE,

                             status               ENUM('PENDING', 'CONNECTED', 'DISCONNECTED')
                                                                  NOT NULL DEFAULT 'PENDING',

    -- Address Hierarchy (Lines 1-4)
                             line1_id             BIGINT UNSIGNED NULL,
                             line2_id             BIGINT UNSIGNED NULL,
                             line3_id             BIGINT UNSIGNED NULL,
                             line4_id             BIGINT UNSIGNED NULL,

    -- House Fields
                             house_number         VARCHAR(100) NULL,
                             house_name           VARCHAR(255) NULL,
                             house_nickname       VARCHAR(255) NULL,

    -- Other Location Lists
                             gn_division_id       BIGINT UNSIGNED NULL,
                             valve_id             BIGINT UNSIGNED NULL,
                             society_id           BIGINT UNSIGNED NULL,
                             cluster_id           BIGINT UNSIGNED NULL,

    -- Contact Numbers
                             mobile_number        VARCHAR(20) NOT NULL,
                             secondary_number     VARCHAR(20) NULL,
                             fixed_line_number    VARCHAR(20) NULL,

    -- Tariff
                             tariff_id            BIGINT UNSIGNED NOT NULL,

                             created_at           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             updated_at           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                                 ON UPDATE CURRENT_TIMESTAMP,

                             INDEX idx_conn_member (member_id),
                             INDEX idx_conn_zone (billing_zone_id),
                             UNIQUE KEY uq_conn_premises (premises_id),

    -- Foreign Keys
                             CONSTRAINT fk_conn_member FOREIGN KEY (member_id) REFERENCES members(id),

                             CONSTRAINT fk_conn_premises FOREIGN KEY (premises_id) REFERENCES premises(id),

                             CONSTRAINT fk_conn_zone FOREIGN KEY (billing_zone_id) REFERENCES billing_zones(id),

                             CONSTRAINT fk_conn_tariff FOREIGN KEY (tariff_id) REFERENCES tariffs(id),

                             CONSTRAINT fk_conn_line1 FOREIGN KEY (line1_id) REFERENCES address_lines(id),
                             CONSTRAINT fk_conn_line2 FOREIGN KEY (line2_id) REFERENCES address_lines(id),
                             CONSTRAINT fk_conn_line3 FOREIGN KEY (line3_id) REFERENCES address_lines(id),
                             CONSTRAINT fk_conn_line4 FOREIGN KEY (line4_id) REFERENCES address_lines(id),

                             CONSTRAINT fk_conn_gn FOREIGN KEY (gn_division_id) REFERENCES gn_divisions(id),
                             CONSTRAINT fk_conn_valve FOREIGN KEY (valve_id) REFERENCES valves(id),
                             CONSTRAINT fk_conn_society FOREIGN KEY (society_id) REFERENCES societies(id),
                             CONSTRAINT fk_conn_cluster FOREIGN KEY (cluster_id) REFERENCES clusters(id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4;

CREATE TABLE connection_transfers (
                                      id                  BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                      connection_id       BIGINT UNSIGNED NOT NULL,

                                      old_member_id       BIGINT UNSIGNED NOT NULL,
                                      new_member_id       BIGINT UNSIGNED NOT NULL,

                                      old_premises_id     BIGINT UNSIGNED NOT NULL,
                                      new_premises_id     BIGINT UNSIGNED NOT NULL,

                                      old_billing_zone_id BIGINT UNSIGNED NOT NULL,
                                      new_billing_zone_id BIGINT UNSIGNED NOT NULL,

                                      reason              VARCHAR(500) NULL,
                                      transferred_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                      CONSTRAINT fk_ct_conn FOREIGN KEY (connection_id) REFERENCES connections(id),
                                      CONSTRAINT fk_ct_old_member FOREIGN KEY (old_member_id) REFERENCES members(id),
                                      CONSTRAINT fk_ct_new_member FOREIGN KEY (new_member_id) REFERENCES members(id),
                                      CONSTRAINT fk_ct_old_premises FOREIGN KEY (old_premises_id) REFERENCES premises(id),
                                      CONSTRAINT fk_ct_new_premises FOREIGN KEY (new_premises_id) REFERENCES premises(id),
                                      CONSTRAINT fk_ct_old_zone FOREIGN KEY (old_billing_zone_id) REFERENCES billing_zones(id),
                                      CONSTRAINT fk_ct_new_zone FOREIGN KEY (new_billing_zone_id) REFERENCES billing_zones(id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4;

-- TODO : Connection Transfer Members , premises and billing Zones  all 3 can be transfer need to handle that logic

CREATE TABLE revenue_main_categories (
                                         id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                         customer_type   ENUM('CUSTOMER', 'NON_CUSTOMER') NOT NULL,
                                         code            INT UNSIGNED NOT NULL,
                                         name            VARCHAR(255) NOT NULL,
                                         description     VARCHAR(500) NULL,
                                         is_system       TINYINT(1) NOT NULL DEFAULT 1,
                                         is_active       TINYINT(1) NOT NULL DEFAULT 1,
                                         created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                         updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                                            ON UPDATE CURRENT_TIMESTAMP,
                                         UNIQUE KEY uq_rev_main_code (code),
                                         UNIQUE KEY uq_rev_main_name (name),
                                         INDEX idx_rev_main_customer_type (customer_type)
) ENGINE=InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE revenue_accounts (
                                  id                 BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                  main_category_id   BIGINT UNSIGNED NOT NULL,
                                  account_number     VARCHAR(50) NOT NULL,
                                  name               VARCHAR(255) NOT NULL,
                                  description        VARCHAR(500) NULL,
                                  reference_prefix   VARCHAR(20) NULL,
                                  is_default         TINYINT(1) NOT NULL DEFAULT 0,
                                  function_key       VARCHAR(100) NULL,
                                  is_system          TINYINT(1) NOT NULL DEFAULT 0,
                                  is_active          TINYINT(1) NOT NULL DEFAULT 1,
                                  created_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  updated_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                                      ON UPDATE CURRENT_TIMESTAMP,

                                  UNIQUE KEY uq_rev_acc_number (account_number),
                                  UNIQUE KEY uq_rev_acc_main_name (main_category_id, name),
                                  INDEX idx_rev_main (main_category_id),
                                  INDEX idx_rev_default (main_category_id, is_default),
                                  INDEX idx_rev_function_key (function_key),

                                  CONSTRAINT fk_rev_accounts_main
                                      FOREIGN KEY (main_category_id) REFERENCES revenue_main_categories(id)
) ENGINE=InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;



CREATE TABLE expense_main_categories (
                                         id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                         expense_type    ENUM('OPERATING', 'NON_OPERATING') NOT NULL,
                                         code            INT UNSIGNED NOT NULL,
                                         name            VARCHAR(255) NOT NULL,
                                         description     VARCHAR(500) NULL,
                                         is_system       TINYINT(1) NOT NULL DEFAULT 1,
                                         is_active       TINYINT(1) NOT NULL DEFAULT 1,
                                         created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                         updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                                             ON UPDATE CURRENT_TIMESTAMP,
                                         UNIQUE KEY uq_exp_main_code (code),
                                         UNIQUE KEY uq_exp_main_name (name),
                                         INDEX idx_exp_main_type (expense_type)
) ENGINE=InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE expense_accounts (
                                  id                 BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                  main_category_id   BIGINT UNSIGNED NOT NULL,
                                  account_number     VARCHAR(50) NOT NULL,
                                  name               VARCHAR(255) NOT NULL,
                                  description        VARCHAR(500) NULL,
                                  is_default         TINYINT(1) NOT NULL DEFAULT 0,
                                  function_key       VARCHAR(100) NULL,
                                  is_system          TINYINT(1) NOT NULL DEFAULT 0,
                                  is_active          TINYINT(1) NOT NULL DEFAULT 1,
                                  created_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  updated_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                                      ON UPDATE CURRENT_TIMESTAMP,
                                  UNIQUE KEY uq_exp_acc_number (account_number),
                                  UNIQUE KEY uq_exp_acc_main_name (main_category_id, name),
                                  INDEX idx_exp_main (main_category_id),
                                  INDEX idx_exp_default (main_category_id, is_default),
                                  INDEX idx_exp_function_key (function_key),
                                  CONSTRAINT fk_exp_accounts_main
                                      FOREIGN KEY (main_category_id) REFERENCES expense_main_categories(id)
) ENGINE=InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE inventory_master_categories (
                                             id               BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                             parent_id        BIGINT UNSIGNED NULL,
                                             level            TINYINT NOT NULL,
                                             name             VARCHAR(255) NOT NULL,
                                             specification_01 VARCHAR(255) NULL,
                                             specification_02 VARCHAR(255) NULL,
                                             unit             VARCHAR(50) NULL,
                                             is_leaf          TINYINT(1) NOT NULL DEFAULT 0,
                                             is_system        TINYINT(1) NOT NULL DEFAULT 0,
                                             is_active        TINYINT(1) NOT NULL DEFAULT 1,
                                             created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                             updated_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                                                 ON UPDATE CURRENT_TIMESTAMP,
                                             UNIQUE KEY uq_inventory_hierarchy (level, name, parent_id),
                                             INDEX idx_inventory_parent (parent_id),
                                             INDEX idx_inventory_level (level),
                                             CONSTRAINT fk_inventory_parent
                                                 FOREIGN KEY (parent_id) REFERENCES inventory_master_categories(id)
) ENGINE=InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE branch_inventory_items (
                                        id             BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                        org_unit_id    BIGINT UNSIGNED NOT NULL,
                                        master_id      BIGINT UNSIGNED NOT NULL,
                                        custom_code    VARCHAR(50) NULL,
                                        is_active      TINYINT(1) NOT NULL DEFAULT 1,
                                        created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                        updated_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                                            ON UPDATE CURRENT_TIMESTAMP,
                                        UNIQUE KEY uq_branch_inventory (org_unit_id, master_id),
                                        INDEX idx_branch_inventory_org (org_unit_id),
                                        CONSTRAINT fk_branch_inventory_org
                                            FOREIGN KEY (org_unit_id) REFERENCES org_units(id),
                                        CONSTRAINT fk_branch_inventory_master
                                            FOREIGN KEY (master_id) REFERENCES inventory_master_categories(id)
) ENGINE=InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE fixed_asset_master_categories (
                                               id               BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                               parent_id        BIGINT UNSIGNED NULL,
                                               level            TINYINT NOT NULL,
                                               name             VARCHAR(255) NOT NULL,
                                               specification_01 VARCHAR(255) NULL,
                                               specification_02 VARCHAR(255) NULL,
                                               unit             VARCHAR(50) NULL,
                                               is_leaf          TINYINT(1) NOT NULL DEFAULT 0,
                                               is_system        TINYINT(1) NOT NULL DEFAULT 0,
                                               is_active        TINYINT(1) NOT NULL DEFAULT 1,
                                               created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                               updated_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                                                   ON UPDATE CURRENT_TIMESTAMP,
                                               UNIQUE KEY uq_fixed_asset_hierarchy (level, name, parent_id),
                                               INDEX idx_fixed_asset_parent (parent_id),
                                               INDEX idx_fixed_asset_level (level),
                                               CONSTRAINT fk_fixed_asset_parent
                                                   FOREIGN KEY (parent_id) REFERENCES fixed_asset_master_categories(id)
) ENGINE=InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE branch_fixed_assets (
                                     id             BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                     org_unit_id    BIGINT UNSIGNED NOT NULL,
                                     master_id      BIGINT UNSIGNED NOT NULL,
                                     asset_code     VARCHAR(50) NULL,
                                     is_active      TINYINT(1) NOT NULL DEFAULT 1,
                                     created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     updated_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                                         ON UPDATE CURRENT_TIMESTAMP,
                                     UNIQUE KEY uq_branch_fixed_asset (org_unit_id, master_id),
                                     INDEX idx_branch_fixed_asset_org (org_unit_id),
                                     CONSTRAINT fk_branch_fixed_asset_org
                                         FOREIGN KEY (org_unit_id) REFERENCES org_units(id),
                                     CONSTRAINT fk_branch_fixed_asset_master
                                         FOREIGN KEY (master_id) REFERENCES fixed_asset_master_categories(id)
) ENGINE=InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE liability_main_categories (
                                           id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                           liability_type  ENUM('CURRENT', 'NON_CURRENT') NOT NULL,
                                           code            INT UNSIGNED NOT NULL,
                                           name            VARCHAR(255) NOT NULL,
                                           description     VARCHAR(500) NULL,
                                           is_system       TINYINT(1) NOT NULL DEFAULT 1,
                                           is_active       TINYINT(1) NOT NULL DEFAULT 1,
                                           created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                           updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                                               ON UPDATE CURRENT_TIMESTAMP,
                                           UNIQUE KEY uq_liab_main_code (code),
                                           UNIQUE KEY uq_liab_main_name (name),
                                           INDEX idx_liab_main_type (liability_type)
) ENGINE=InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE liability_accounts (
                                    id                 BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                    main_category_id   BIGINT UNSIGNED NOT NULL,
                                    account_number     VARCHAR(50) NOT NULL,
                                    name               VARCHAR(255) NOT NULL,
                                    description        VARCHAR(500) NULL,
                                    is_default         TINYINT(1) NOT NULL DEFAULT 0,
                                    function_key       VARCHAR(100) NULL,
                                    is_system          TINYINT(1) NOT NULL DEFAULT 0,
                                    is_active          TINYINT(1) NOT NULL DEFAULT 1,
                                    created_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    updated_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                                        ON UPDATE CURRENT_TIMESTAMP,
                                    UNIQUE KEY uq_liab_acc_number (account_number),
                                    UNIQUE KEY uq_liab_acc_main_name (main_category_id, name),
                                    INDEX idx_liab_main (main_category_id),
                                    INDEX idx_liab_default (main_category_id, is_default),
                                    INDEX idx_liab_function_key (function_key),
                                    CONSTRAINT fk_liab_accounts_main
                                        FOREIGN KEY (main_category_id) REFERENCES liability_main_categories(id)
) ENGINE=InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE equity_accounts (
                                 id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                 account_number  VARCHAR(50) NOT NULL,
                                 name            VARCHAR(255) NOT NULL,
                                 description     VARCHAR(500) NULL,
                                 is_system       TINYINT(1) NOT NULL DEFAULT 1,
                                 is_active       TINYINT(1) NOT NULL DEFAULT 1,
                                 created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                                     ON UPDATE CURRENT_TIMESTAMP,
                                 UNIQUE KEY uq_equity_acc_number (account_number),
                                 UNIQUE KEY uq_equity_name (name)
) ENGINE=InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS suppliers (
                                        id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                        org_unit_id BIGINT UNSIGNED NULL,
                                        supplier_code VARCHAR(50) NULL,
                                        name VARCHAR(255) NOT NULL,
                                        address VARCHAR(500) NULL,
                                        brc_number VARCHAR(100) NULL,
                                        nic VARCHAR(20) NULL,
                                        mobile_number_1 VARCHAR(20) NULL,
                                        mobile_number_2 VARCHAR(20) NULL,
                                        telephone_number VARCHAR(20) NULL,
                                        email VARCHAR(255) NULL,
                                        liability_account_id BIGINT UNSIGNED NULL,
                                        is_active TINYINT(1) NOT NULL DEFAULT 1,
                                        created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                        updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                                            ON UPDATE CURRENT_TIMESTAMP,
                                        UNIQUE KEY uq_sup_code (supplier_code),
                                        UNIQUE KEY uq_sup_name (name),
                                        INDEX idx_sup_org (org_unit_id),
                                        INDEX idx_sup_liab_acc (liability_account_id),

                                        CONSTRAINT fk_sup_org FOREIGN KEY (org_unit_id)
                                            REFERENCES org_units(id),
                                        CONSTRAINT fk_sup_liability_account FOREIGN KEY (liability_account_id)
                                            REFERENCES liability_accounts(id)
) ENGINE=InnoDB
    DEFAULT CHARSET=utf8mb4
    COLLATE=utf8mb4_unicode_ci;

CREATE TABLE monetary_accounts (
                                   id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                   org_unit_id              BIGINT UNSIGNED NOT NULL,
                                   type                     ENUM('BANK','CASH_IN_HAND','CHEQUE_IN_HAND','CASHIER') NOT NULL,
                                   bank_account_type        ENUM('SAVINGS','CURRENT','FIXED_DEPOSIT') NULL,
                                   account_name             VARCHAR(255) NOT NULL,
                                   account_number           VARCHAR(100) NULL,
                                   bank_name                VARCHAR(255) NULL,
                                   branch_name              VARCHAR(255) NULL,
                                   branch_code              VARCHAR(50) NULL,
                                   branch_contact_number    VARCHAR(30) NULL,
                                   opening_balance          DECIMAL(14,2) NOT NULL DEFAULT 0.00,
                                   current_balance          DECIMAL(14,2) NOT NULL DEFAULT 0.00,
                                   description              VARCHAR(500) NULL,
                                   is_active                TINYINT(1) NOT NULL DEFAULT 1,
                                   created_at               DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   updated_at               DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                   UNIQUE KEY uq_monetary_account_org_name (org_unit_id, account_name),
                                   INDEX idx_monetary_account_org (org_unit_id),
                                   INDEX idx_monetary_account_type (type),
                                   CONSTRAINT fk_monetary_account_org
                                       FOREIGN KEY (org_unit_id) REFERENCES org_units(id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE TABLE payment_methods (
                                 id          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                 code        VARCHAR(50) NOT NULL,
                                 name        VARCHAR(255) NOT NULL,
                                 is_active   TINYINT(1) NOT NULL DEFAULT 1,
                                 created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                 UNIQUE KEY uq_payment_method_code (code),
                                 UNIQUE KEY uq_payment_method_name (name)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE monetary_account_payment_methods (
                                                  monetary_account_id  BIGINT UNSIGNED NOT NULL,
                                                  payment_method_id    BIGINT UNSIGNED NOT NULL,
                                                  PRIMARY KEY (monetary_account_id, payment_method_id),
                                                  CONSTRAINT fk_mapm_account
                                                      FOREIGN KEY (monetary_account_id) REFERENCES monetary_accounts(id) ON DELETE CASCADE,
                                                  CONSTRAINT fk_mapm_method
                                                      FOREIGN KEY (payment_method_id) REFERENCES payment_methods(id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE TABLE purchase_order_drafts (
                                       id             BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                       org_unit_id     BIGINT UNSIGNED NOT NULL,
                                       reference_no    VARCHAR(50) NOT NULL,
                                       status          ENUM('PENDING','ACCEPTED','CANCELLED') NOT NULL DEFAULT 'PENDING',
                                       created_by      BIGINT UNSIGNED NULL,
                                       created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                       UNIQUE KEY uq_po_draft_ref (reference_no),
                                       INDEX idx_po_draft_org (org_unit_id),
                                       INDEX idx_po_draft_status (status),
                                       CONSTRAINT fk_po_draft_org
                                           FOREIGN KEY (org_unit_id) REFERENCES org_units(id),
                                       CONSTRAINT fk_po_draft_user
                                           FOREIGN KEY (created_by) REFERENCES users(id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE TABLE purchase_order_draft_items (
                                            id                BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                            draft_id          BIGINT UNSIGNED NOT NULL,
                                            inventory_item_id BIGINT UNSIGNED NOT NULL,
                                            quantity          DECIMAL(14,3) NOT NULL,
                                            unit_cost         DECIMAL(14,2) NULL,
                                            total_amount      DECIMAL(14,2) NULL,
                                            created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                            updated_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                            UNIQUE KEY uq_po_draft_item (draft_id, inventory_item_id),
                                            INDEX idx_po_draft_item_draft (draft_id),
                                            CONSTRAINT fk_po_draft_item_draft
                                                FOREIGN KEY (draft_id) REFERENCES purchase_order_drafts(id) ON DELETE CASCADE,
                                            CONSTRAINT fk_po_draft_item_inventory
                                                FOREIGN KEY (inventory_item_id) REFERENCES branch_inventory_items(id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE TABLE purchase_orders (
                                 id                BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                 org_unit_id        BIGINT UNSIGNED NOT NULL,
                                 purchase_order_no  VARCHAR(50) NOT NULL,
                                 draft_id           BIGINT UNSIGNED NULL,
                                 supplier_id        BIGINT UNSIGNED NULL,
                                 status             ENUM('PENDING','REJECTED','CONVERTED_TO_GRN') NOT NULL DEFAULT 'PENDING',
                                 total_amount       DECIMAL(14,2) NOT NULL DEFAULT 0.00,
                                 created_by         BIGINT UNSIGNED NULL,
                                 created_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 updated_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                 UNIQUE KEY uq_po_no (purchase_order_no),
                                 INDEX idx_po_org (org_unit_id),
                                 INDEX idx_po_supplier (supplier_id),
                                 INDEX idx_po_status (status),
                                 CONSTRAINT fk_po_org
                                     FOREIGN KEY (org_unit_id) REFERENCES org_units(id),
                                 CONSTRAINT fk_po_draft
                                     FOREIGN KEY (draft_id) REFERENCES purchase_order_drafts(id),
                                 CONSTRAINT fk_po_supplier
                                     FOREIGN KEY (supplier_id) REFERENCES suppliers(id),
                                 CONSTRAINT fk_po_user
                                     FOREIGN KEY (created_by) REFERENCES users(id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE TABLE purchase_order_items (
                                      id                 BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                      purchase_order_id   BIGINT UNSIGNED NOT NULL,
                                      inventory_item_id   BIGINT UNSIGNED NOT NULL,
                                      quantity            DECIMAL(14,3) NOT NULL,
                                      unit_cost           DECIMAL(14,2) NOT NULL,
                                      total_amount        DECIMAL(14,2) NOT NULL,
                                      UNIQUE KEY uq_po_item (purchase_order_id, inventory_item_id),
                                      INDEX idx_po_item_po (purchase_order_id),
                                      CONSTRAINT fk_po_item_po
                                          FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(id) ON DELETE CASCADE,
                                      CONSTRAINT fk_po_item_inventory
                                          FOREIGN KEY (inventory_item_id) REFERENCES branch_inventory_items(id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE TABLE grn_invoices (
                              id                 BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                              org_unit_id         BIGINT UNSIGNED NOT NULL,
                              grn_no              VARCHAR(50) NOT NULL,
                              purchase_order_id   BIGINT UNSIGNED NULL,
                              supplier_id         BIGINT UNSIGNED NOT NULL,
                              grn_date            DATE NOT NULL,
                              total_amount        DECIMAL(14,2) NOT NULL DEFAULT 0.00,
                              status              ENUM('ACTIVE') NOT NULL DEFAULT 'ACTIVE',
                              created_by          BIGINT UNSIGNED NULL,
                              created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              updated_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                              UNIQUE KEY uq_grn_no (grn_no),
                              INDEX idx_grn_org (org_unit_id),
                              INDEX idx_grn_supplier (supplier_id),
                              CONSTRAINT fk_grn_org
                                  FOREIGN KEY (org_unit_id) REFERENCES org_units(id),
                              CONSTRAINT fk_grn_po
                                  FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(id),
                              CONSTRAINT fk_grn_supplier
                                  FOREIGN KEY (supplier_id) REFERENCES suppliers(id),
                              CONSTRAINT fk_grn_user
                                  FOREIGN KEY (created_by) REFERENCES users(id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE TABLE grn_invoice_items (
                                   id                 BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                   grn_id             BIGINT UNSIGNED NOT NULL,
                                   inventory_item_id  BIGINT UNSIGNED NOT NULL,
                                   batch_no           VARCHAR(50) NOT NULL,
                                   quantity           DECIMAL(14,3) NOT NULL,
                                   unit_cost          DECIMAL(14,2) NOT NULL,
                                   total_amount       DECIMAL(14,2) NOT NULL,
                                   UNIQUE KEY uq_grn_item_batch (grn_id, inventory_item_id, batch_no),
                                   INDEX idx_grn_item_grn (grn_id),
                                   INDEX idx_grn_item_inventory (inventory_item_id),
                                   CONSTRAINT fk_grn_item_grn
                                       FOREIGN KEY (grn_id) REFERENCES grn_invoices(id) ON DELETE CASCADE,
                                   CONSTRAINT fk_grn_item_inventory
                                       FOREIGN KEY (inventory_item_id) REFERENCES branch_inventory_items(id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE TABLE inventory_ledger (
                                  id                BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                  org_unit_id        BIGINT UNSIGNED NOT NULL,
                                  inventory_item_id  BIGINT UNSIGNED NOT NULL,
                                  batch_no           VARCHAR(50) NULL,
                                  movement_type      ENUM('OPENING','PURCHASE','SALE','CONSUMPTION','ADJUSTMENT','RETURN','REVERSAL') NOT NULL,
                                  reference_no       VARCHAR(50) NULL,
                                  movement_date      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  performed_by       BIGINT UNSIGNED NULL,
                                  description        VARCHAR(500) NULL,
                                  qty_in             DECIMAL(14,3) NOT NULL DEFAULT 0.000,
                                  qty_out            DECIMAL(14,3) NOT NULL DEFAULT 0.000,
                                  unit_cost          DECIMAL(14,2) NULL,
                                  created_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  INDEX idx_inv_led_org_item (org_unit_id, inventory_item_id),
                                  INDEX idx_inv_led_item_batch (inventory_item_id, batch_no),
                                  INDEX idx_inv_led_ref (reference_no),
                                  INDEX idx_inv_led_date (movement_date),
                                  CONSTRAINT fk_inv_led_org
                                      FOREIGN KEY (org_unit_id) REFERENCES org_units(id),
                                  CONSTRAINT fk_inv_led_item
                                      FOREIGN KEY (inventory_item_id) REFERENCES branch_inventory_items(id),
                                  CONSTRAINT fk_inv_led_user
                                      FOREIGN KEY (performed_by) REFERENCES users(id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE TABLE inventory_consumptions (
                                        id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                        org_unit_id      BIGINT UNSIGNED NOT NULL,
                                        reference_no     VARCHAR(50) NOT NULL,
                                        consumption_date DATE NOT NULL,
                                        reason           VARCHAR(500) NULL,
                                        created_by       BIGINT UNSIGNED NULL,
                                        created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                        updated_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                        UNIQUE KEY uq_inv_con_ref (reference_no),
                                        INDEX idx_inv_con_org (org_unit_id),
                                        CONSTRAINT fk_inv_con_org
                                            FOREIGN KEY (org_unit_id) REFERENCES org_units(id),
                                        CONSTRAINT fk_inv_con_user
                                            FOREIGN KEY (created_by) REFERENCES users(id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE TABLE inventory_consumption_items (
                                             id                BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                             consumption_id    BIGINT UNSIGNED NOT NULL,
                                             inventory_item_id BIGINT UNSIGNED NOT NULL,
                                             batch_no          VARCHAR(50) NULL,
                                             quantity          DECIMAL(14,3) NOT NULL,
                                             unit_cost         DECIMAL(14,2) NULL,
                                             total_amount      DECIMAL(14,2) NULL,
                                             INDEX idx_inv_con_item_con (consumption_id),
                                             CONSTRAINT fk_inv_con_item_con
                                                 FOREIGN KEY (consumption_id) REFERENCES inventory_consumptions(id) ON DELETE CASCADE,
                                             CONSTRAINT fk_inv_con_item_item
                                                 FOREIGN KEY (inventory_item_id) REFERENCES branch_inventory_items(id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE TABLE expense_voucher_drafts (
                                        id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                        org_unit_id      BIGINT UNSIGNED NOT NULL,
                                        supplier_id      BIGINT UNSIGNED NULL,
                                        reference_no     VARCHAR(50) NOT NULL,
                                        draft_date       DATE NOT NULL,
                                        status           ENUM('PENDING','ACCEPTED','DECLINED','CONVERTED') NOT NULL DEFAULT 'PENDING',
                                        created_by       BIGINT UNSIGNED NULL,
                                        created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                        updated_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                        UNIQUE KEY uq_exp_vd_ref (reference_no),
                                        INDEX idx_exp_vd_org (org_unit_id),
                                        INDEX idx_exp_vd_supplier (supplier_id),
                                        INDEX idx_exp_vd_status (status),
                                        CONSTRAINT fk_exp_vd_org
                                            FOREIGN KEY (org_unit_id) REFERENCES org_units(id),
                                        CONSTRAINT fk_exp_vd_supplier
                                            FOREIGN KEY (supplier_id) REFERENCES suppliers(id),
                                        CONSTRAINT fk_exp_vd_user
                                            FOREIGN KEY (created_by) REFERENCES users(id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE TABLE expense_voucher_draft_lines (
                                             id                 BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                             draft_id           BIGINT UNSIGNED NOT NULL,
                                             expense_account_id BIGINT UNSIGNED NOT NULL,
                                             description        VARCHAR(500) NULL,
                                             amount             DECIMAL(14,2) NOT NULL,
                                             INDEX idx_exp_vdl_draft (draft_id),
                                             INDEX idx_exp_vdl_acc (expense_account_id),
                                             CONSTRAINT fk_exp_vdl_draft
                                                 FOREIGN KEY (draft_id) REFERENCES expense_voucher_drafts(id) ON DELETE CASCADE,
                                             CONSTRAINT fk_exp_vdl_acc
                                                 FOREIGN KEY (expense_account_id) REFERENCES expense_accounts(id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE TABLE expense_vouchers (
                                  id                   BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                  org_unit_id           BIGINT UNSIGNED NOT NULL,
                                  voucher_no            VARCHAR(50) NOT NULL,
                                  voucher_date          DATE NOT NULL,
                                  draft_id              BIGINT UNSIGNED NULL,
                                  supplier_id           BIGINT UNSIGNED NULL,
                                  monetary_account_id   BIGINT UNSIGNED NOT NULL,
                                  payment_method_id     BIGINT UNSIGNED NOT NULL,
                                  total_amount          DECIMAL(14,2) NOT NULL,
                                  created_by            BIGINT UNSIGNED NULL,
                                  created_at            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  updated_at            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                  UNIQUE KEY uq_exp_voucher_no (voucher_no),
                                  INDEX idx_exp_v_org (org_unit_id),
                                  INDEX idx_exp_v_supplier (supplier_id),
                                  INDEX idx_exp_v_account (monetary_account_id),
                                  CONSTRAINT fk_exp_v_org
                                      FOREIGN KEY (org_unit_id) REFERENCES org_units(id),
                                  CONSTRAINT fk_exp_v_draft
                                      FOREIGN KEY (draft_id) REFERENCES expense_voucher_drafts(id),
                                  CONSTRAINT fk_exp_v_supplier
                                      FOREIGN KEY (supplier_id) REFERENCES suppliers(id),
                                  CONSTRAINT fk_exp_v_account
                                      FOREIGN KEY (monetary_account_id) REFERENCES monetary_accounts(id),
                                  CONSTRAINT fk_exp_v_method
                                      FOREIGN KEY (payment_method_id) REFERENCES payment_methods(id),
                                  CONSTRAINT fk_exp_v_user
                                      FOREIGN KEY (created_by) REFERENCES users(id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE TABLE purchase_voucher_drafts (
                                         id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                         org_unit_id      BIGINT UNSIGNED NOT NULL,
                                         supplier_id      BIGINT UNSIGNED NOT NULL,
                                         grn_id           BIGINT UNSIGNED NOT NULL,
                                         reference_no     VARCHAR(50) NOT NULL,
                                         draft_date       DATE NOT NULL,
                                         paid_amount      DECIMAL(14,2) NOT NULL DEFAULT 0.00,
                                         status           ENUM('PENDING','ACCEPTED','DECLINED','CONVERTED','REVERSED') NOT NULL DEFAULT 'PENDING',
                                         created_by       BIGINT UNSIGNED NULL,
                                         created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                         updated_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                         UNIQUE KEY uq_pur_vd_ref (reference_no),
                                         INDEX idx_pur_vd_org (org_unit_id),
                                         INDEX idx_pur_vd_supplier (supplier_id),
                                         INDEX idx_pur_vd_grn (grn_id),
                                         INDEX idx_pur_vd_status (status),
                                         CONSTRAINT fk_pur_vd_org
                                             FOREIGN KEY (org_unit_id) REFERENCES org_units(id),
                                         CONSTRAINT fk_pur_vd_supplier
                                             FOREIGN KEY (supplier_id) REFERENCES suppliers(id),
                                         CONSTRAINT fk_pur_vd_grn
                                             FOREIGN KEY (grn_id) REFERENCES grn_invoices(id),
                                         CONSTRAINT fk_pur_vd_user
                                             FOREIGN KEY (created_by) REFERENCES users(id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE TABLE purchase_vouchers (
                                   id                   BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                   org_unit_id           BIGINT UNSIGNED NOT NULL,
                                   voucher_no            VARCHAR(50) NOT NULL,
                                   voucher_date          DATE NOT NULL,
                                   draft_id              BIGINT UNSIGNED NULL,
                                   supplier_id           BIGINT UNSIGNED NOT NULL,
                                   grn_id                BIGINT UNSIGNED NOT NULL,
                                   monetary_account_id   BIGINT UNSIGNED NOT NULL,
                                   payment_method_id     BIGINT UNSIGNED NOT NULL,
                                   paid_amount           DECIMAL(14,2) NOT NULL,
                                   created_by            BIGINT UNSIGNED NULL,
                                   created_at            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   updated_at            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                   UNIQUE KEY uq_pur_voucher_no (voucher_no),
                                   INDEX idx_pur_v_org (org_unit_id),
                                   INDEX idx_pur_v_supplier (supplier_id),
                                   INDEX idx_pur_v_grn (grn_id),
                                   INDEX idx_pur_v_account (monetary_account_id),
                                   CONSTRAINT fk_pur_v_org
                                       FOREIGN KEY (org_unit_id) REFERENCES org_units(id),
                                   CONSTRAINT fk_pur_v_draft
                                       FOREIGN KEY (draft_id) REFERENCES purchase_voucher_drafts(id),
                                   CONSTRAINT fk_pur_v_supplier
                                       FOREIGN KEY (supplier_id) REFERENCES suppliers(id),
                                   CONSTRAINT fk_pur_v_grn
                                       FOREIGN KEY (grn_id) REFERENCES grn_invoices(id),
                                   CONSTRAINT fk_pur_v_account
                                       FOREIGN KEY (monetary_account_id) REFERENCES monetary_accounts(id),
                                   CONSTRAINT fk_pur_v_method
                                       FOREIGN KEY (payment_method_id) REFERENCES payment_methods(id),
                                   CONSTRAINT fk_pur_v_user
                                       FOREIGN KEY (created_by) REFERENCES users(id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS sales_invoices (
                                              id                 BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                              org_unit_id         BIGINT UNSIGNED NOT NULL,
                                              billing_zone_id     BIGINT UNSIGNED NULL,
                                              invoice_no          VARCHAR(50) NOT NULL,
                                              invoice_type        ENUM('CUSTOMER','NON_CUSTOMER') NOT NULL,
                                              customer_name       VARCHAR(255) NULL,
                                              customer_nic        VARCHAR(30) NULL,
                                              customer_address    VARCHAR(500) NULL,
                                              customer_mobile     VARCHAR(20) NULL,
                                              status              ENUM('DRAFT','POSTED','SETTLED','REVERSED','CANCELLED') NOT NULL DEFAULT 'POSTED',
                                              issued_at           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                              description         VARCHAR(500) NULL,
                                              total_revenue       DECIMAL(14,2) NOT NULL DEFAULT 0.00,
                                              total_sales_expense DECIMAL(14,2) NOT NULL DEFAULT 0.00,
                                              total_due           DECIMAL(14,2) NOT NULL DEFAULT 0.00,
                                              created_by          BIGINT UNSIGNED NULL,
                                              created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                              updated_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                              UNIQUE KEY uq_sales_invoice_no (invoice_no),
                                              INDEX idx_sales_inv_org (org_unit_id),
                                              INDEX idx_sales_inv_zone (billing_zone_id),
                                              INDEX idx_sales_inv_type (invoice_type),
                                              INDEX idx_sales_inv_status (status),
                                              CONSTRAINT fk_sales_inv_org FOREIGN KEY (org_unit_id) REFERENCES org_units(id),
                                              CONSTRAINT fk_sales_inv_zone FOREIGN KEY (billing_zone_id) REFERENCES billing_zones(id),
                                              CONSTRAINT fk_sales_inv_user FOREIGN KEY (created_by) REFERENCES users(id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS sales_invoice_lines (
                                                   id               BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                                   invoice_id       BIGINT UNSIGNED NOT NULL,
                                                   revenue_account_id BIGINT UNSIGNED NOT NULL,
                                                   line_type        ENUM('REVENUE','PENALTY','OPERATION','WELFARE','NON_OPERATION') NOT NULL,
                                                   description      VARCHAR(500) NULL,
                                                   amount           DECIMAL(14,2) NOT NULL,
                                                   created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                   INDEX idx_sales_inv_line_inv (invoice_id),
                                                   INDEX idx_sales_inv_line_acc (revenue_account_id),
                                                   CONSTRAINT fk_sales_inv_line_inv FOREIGN KEY (invoice_id) REFERENCES sales_invoices(id) ON DELETE CASCADE,
                                                   CONSTRAINT fk_sales_inv_line_acc FOREIGN KEY (revenue_account_id) REFERENCES revenue_accounts(id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS sales_invoice_inventory_issues (
                                                              id                    BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                                              invoice_id             BIGINT UNSIGNED NOT NULL,
                                                              record_as              ENUM('SALES_EXPENSE','INVENTORY_CONSUMPTION') NOT NULL,
                                                              charged_from_customer  TINYINT(1) NOT NULL DEFAULT 0,
                                                              total_inventory_cost   DECIMAL(14,2) NOT NULL DEFAULT 0.00,
                                                              created_at             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                              UNIQUE KEY uq_sales_inv_issue (invoice_id),
                                                              CONSTRAINT fk_sales_inv_issue_inv
                                                                  FOREIGN KEY (invoice_id) REFERENCES sales_invoices(id) ON DELETE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS sales_invoice_inventory_issue_items (
                                                                   id                 BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                                                   issue_id            BIGINT UNSIGNED NOT NULL,
                                                                   inventory_item_id   BIGINT UNSIGNED NOT NULL,
                                                                   batch_no            VARCHAR(50) NULL,
                                                                   quantity            DECIMAL(14,3) NOT NULL,
                                                                   unit_cost           DECIMAL(14,2) NOT NULL,
                                                                   amount              DECIMAL(14,2) NOT NULL,
                                                                   created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                                   INDEX idx_sales_inv_issue_item_issue (issue_id),
                                                                   CONSTRAINT fk_sales_inv_issue_item_issue
                                                                       FOREIGN KEY (issue_id) REFERENCES sales_invoice_inventory_issues(id) ON DELETE CASCADE,
                                                                   CONSTRAINT fk_sales_inv_issue_item_item
                                                                       FOREIGN KEY (inventory_item_id) REFERENCES branch_inventory_items(id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS sales_invoice_installment_plans (
                                                               id               BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                                               invoice_id        BIGINT UNSIGNED NOT NULL,
                                                               plan_type         ENUM('ONE_TIME','INSTALLMENTS') NOT NULL,
                                                               down_payment      DECIMAL(14,2) NOT NULL DEFAULT 0.00,
                                                               installment_count INT NULL,
                                                               installment_value DECIMAL(14,2) NULL,
                                                               total_amount      DECIMAL(14,2) NOT NULL,
                                                               created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                               UNIQUE KEY uq_sales_inv_plan (invoice_id),
                                                               CONSTRAINT fk_sales_inv_plan_inv
                                                                   FOREIGN KEY (invoice_id) REFERENCES sales_invoices(id) ON DELETE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS sales_invoice_installments (
                                                          id             BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                                          plan_id         BIGINT UNSIGNED NOT NULL,
                                                          installment_no  INT NOT NULL,
                                                          due_date        DATE NULL,
                                                          amount          DECIMAL(14,2) NOT NULL,
                                                          status          ENUM('NEXT','POSTED','PAID') NOT NULL DEFAULT 'NEXT',
                                                          created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                          UNIQUE KEY uq_sales_inst_no (plan_id, installment_no),
                                                          CONSTRAINT fk_sales_inst_plan
                                                              FOREIGN KEY (plan_id) REFERENCES sales_invoice_installment_plans(id) ON DELETE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS recurring_invoices (
                                                  id               BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                                  org_unit_id       BIGINT UNSIGNED NOT NULL,
                                                  billing_zone_id   BIGINT UNSIGNED NOT NULL,
                                                  name              VARCHAR(255) NOT NULL,
                                                  is_enabled        TINYINT(1) NOT NULL DEFAULT 1,
                                                  frequency         ENUM('MONTHLY') NOT NULL DEFAULT 'MONTHLY',
                                                  next_run_month    CHAR(7) NOT NULL,
                                                  created_by        BIGINT UNSIGNED NULL,
                                                  created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                  updated_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                                  UNIQUE KEY uq_rec_inv_org_zone_name (org_unit_id, billing_zone_id, name),
                                                  INDEX idx_rec_inv_zone (billing_zone_id),
                                                  CONSTRAINT fk_rec_inv_org FOREIGN KEY (org_unit_id) REFERENCES org_units(id),
                                                  CONSTRAINT fk_rec_inv_zone FOREIGN KEY (billing_zone_id) REFERENCES billing_zones(id),
                                                  CONSTRAINT fk_rec_inv_user FOREIGN KEY (created_by) REFERENCES users(id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS recurring_invoice_lines (
                                                       id                BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                                       recurring_invoice_id BIGINT UNSIGNED NOT NULL,
                                                       revenue_account_id BIGINT UNSIGNED NOT NULL,
                                                       description       VARCHAR(500) NULL,
                                                       amount            DECIMAL(14,2) NOT NULL,
                                                       created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                       INDEX idx_rec_inv_line_rec (recurring_invoice_id),
                                                       CONSTRAINT fk_rec_inv_line_rec
                                                           FOREIGN KEY (recurring_invoice_id) REFERENCES recurring_invoices(id) ON DELETE CASCADE,
                                                       CONSTRAINT fk_rec_inv_line_acc
                                                           FOREIGN KEY (revenue_account_id) REFERENCES revenue_accounts(id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS invoice_connection_rules (
                                                        id             BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                                        invoice_id      BIGINT UNSIGNED NOT NULL,
                                                        rule_mode       ENUM('INCLUDE','EXCLUDE') NOT NULL,
                                                        rule_type       ENUM('SPECIFIC_CONNECTIONS','STATE','GROUPS') NOT NULL,
                                                        state_filter    ENUM('ALL_PENDING','ALL_CONNECTED','ALL_DISCONNECTED') NULL,
                                                        group_category  ENUM('TARIFF','GN_DIVISION','VALVE','BILLING_ZONE','SOCIETY','CLUSTER') NULL,
                                                        created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                        INDEX idx_inv_rule_inv (invoice_id),
                                                        CONSTRAINT fk_inv_rule_inv
                                                            FOREIGN KEY (invoice_id) REFERENCES sales_invoices(id) ON DELETE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS invoice_rule_connections (
                                                        id        BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                                        rule_id   BIGINT UNSIGNED NOT NULL,
                                                        connection_id BIGINT UNSIGNED NOT NULL,
                                                        UNIQUE KEY uq_inv_rule_conn (rule_id, connection_id),
                                                        CONSTRAINT fk_inv_rule_conn_rule
                                                            FOREIGN KEY (rule_id) REFERENCES invoice_connection_rules(id) ON DELETE CASCADE,
                                                        CONSTRAINT fk_inv_rule_conn_conn
                                                            FOREIGN KEY (connection_id) REFERENCES connections(id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS invoice_rule_group_values (
                                                         id        BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                                         rule_id   BIGINT UNSIGNED NOT NULL,
                                                         value_id  BIGINT UNSIGNED NOT NULL,
                                                         UNIQUE KEY uq_inv_rule_group (rule_id, value_id),
                                                         CONSTRAINT fk_inv_rule_group_rule
                                                             FOREIGN KEY (rule_id) REFERENCES invoice_connection_rules(id) ON DELETE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS receipts (
                                        id                 BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                        org_unit_id         BIGINT UNSIGNED NOT NULL,
                                        billing_zone_id     BIGINT UNSIGNED NULL,
                                        connection_id       BIGINT UNSIGNED NULL,
                                        receipt_no          VARCHAR(50) NOT NULL,
                                        receipt_type        ENUM('CUSTOMER','NON_CUSTOMER','UNRECOGNIZED','AGENCY') NOT NULL,
                                        monetary_account_id   BIGINT UNSIGNED NOT NULL,
                                        payment_method_id   BIGINT UNSIGNED NOT NULL,
                                        paid_amount         DECIMAL(14,2) NOT NULL,
                                        paid_date           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                        cheque_no           VARCHAR(50) NULL,
                                        reference_text      VARCHAR(255) NULL,
                                        customer_mobile_updated VARCHAR(20) NULL,
                                        status              ENUM('POSTED','REVERSED') NOT NULL DEFAULT 'POSTED',
                                        created_by          BIGINT UNSIGNED NULL,
                                        created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                        updated_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                        UNIQUE KEY uq_receipt_no (receipt_no),
                                        INDEX idx_receipt_org (org_unit_id),
                                        INDEX idx_receipt_conn (connection_id),
                                        INDEX idx_receipt_paid_date (paid_date),
                                        INDEX idx_receipt_cheque (cheque_no),
                                        CONSTRAINT fk_receipt_org
                                            FOREIGN KEY (org_unit_id) REFERENCES org_units(id),
                                        CONSTRAINT fk_receipt_zone
                                            FOREIGN KEY (billing_zone_id) REFERENCES billing_zones(id),
                                        CONSTRAINT fk_receipt_conn
                                            FOREIGN KEY (connection_id) REFERENCES connections(id),
                                        CONSTRAINT fk_receipt_cash
                                            FOREIGN KEY (monetary_account_id) REFERENCES monetary_accounts(id),
                                        CONSTRAINT fk_receipt_method
                                            FOREIGN KEY (payment_method_id) REFERENCES payment_methods(id),
                                        CONSTRAINT fk_receipt_user
                                            FOREIGN KEY (created_by) REFERENCES users(id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS receipt_settlements (
                                                   id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                                   receipt_id     BIGINT UNSIGNED NOT NULL,
                                                   invoice_id     BIGINT UNSIGNED NULL,
                                                   installment_id BIGINT UNSIGNED NULL,
                                                   settled_amount DECIMAL(14,2) NOT NULL,
                                                   created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                   INDEX idx_rc_set_rc (receipt_id),
                                                   CONSTRAINT fk_rc_set_rc
                                                       FOREIGN KEY (receipt_id) REFERENCES receipts(id) ON DELETE CASCADE,
                                                   CONSTRAINT fk_rc_set_inv
                                                       FOREIGN KEY (invoice_id) REFERENCES sales_invoices(id),
                                                   CONSTRAINT fk_rc_set_inst
                                                       FOREIGN KEY (installment_id) REFERENCES sales_invoice_installments(id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS bulk_receipt_upload_batches (
                                                           id          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                                           org_unit_id  BIGINT UNSIGNED NOT NULL,
                                                           file_name   VARCHAR(255) NOT NULL,
                                                           status      ENUM('UPLOADED','PROCESSED','FAILED') NOT NULL DEFAULT 'UPLOADED',
                                                           created_by  BIGINT UNSIGNED NULL,
                                                           created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                           INDEX idx_bulk_rc_org (org_unit_id),
                                                           CONSTRAINT fk_bulk_rc_org FOREIGN KEY (org_unit_id) REFERENCES org_units(id),
                                                           CONSTRAINT fk_bulk_rc_user FOREIGN KEY (created_by) REFERENCES users(id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS bulk_receipt_upload_rows (
                                                        id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                                        batch_id      BIGINT UNSIGNED NOT NULL,
                                                        account_number VARCHAR(50) NULL,
                                                        paid_amount   DECIMAL(14,2) NOT NULL,
                                                        paid_date     DATETIME NULL,
                                                        status        ENUM('PENDING','POSTED','FAILED') NOT NULL DEFAULT 'PENDING',
                                                        error_message VARCHAR(500) NULL,
                                                        created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                        INDEX idx_bulk_rc_row_batch (batch_id),
                                                        CONSTRAINT fk_bulk_rc_row_batch
                                                            FOREIGN KEY (batch_id) REFERENCES bulk_receipt_upload_batches(id) ON DELETE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS unrecognized_receipts (
                                                     id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                                     org_unit_id    BIGINT UNSIGNED NOT NULL,
                                                     receipt_id     BIGINT UNSIGNED NOT NULL,
                                                     liability_account_id BIGINT UNSIGNED NOT NULL,
                                                     status        ENUM('OPEN','REFUNDED','SETTLED_AS_CUSTOMER','SETTLED_AS_INCOME','REVERSED') NOT NULL DEFAULT 'OPEN',
                                                     created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                     UNIQUE KEY uq_unrec_receipt (receipt_id),
                                                     CONSTRAINT fk_unrec_org FOREIGN KEY (org_unit_id) REFERENCES org_units(id),
                                                     CONSTRAINT fk_unrec_receipt FOREIGN KEY (receipt_id) REFERENCES receipts(id),
                                                     CONSTRAINT fk_unrec_liab FOREIGN KEY (liability_account_id) REFERENCES liability_accounts(id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS cheque_tracking (
                                               id           BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                               cheque_no     VARCHAR(50) NOT NULL,
                                               receipt_id    BIGINT UNSIGNED NULL,
                                               status        ENUM('RECEIVED','DEPOSITED','CLEARED','BOUNCED','CANCELLED') NOT NULL DEFAULT 'RECEIVED',
                                               status_date   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                               note          VARCHAR(500) NULL,
                                               UNIQUE KEY uq_cheque_no (cheque_no),
                                               INDEX idx_cheque_receipt (receipt_id),
                                               CONSTRAINT fk_cheque_receipt FOREIGN KEY (receipt_id) REFERENCES receipts(id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS bill_runs (
                                         id                 BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                         org_unit_id         BIGINT UNSIGNED NOT NULL,
                                         operation_month     CHAR(7) NOT NULL,
                                         bill_print_date     DATE NOT NULL,
                                         status              ENUM('GENERATED','OPEN','CLOSED') NOT NULL DEFAULT 'GENERATED',
                                         late_fee_method     ENUM('CREDIT_LIMIT','OVERDUE_BILLS') NOT NULL,
                                         password_verified_by BIGINT UNSIGNED NULL,
                                         generated_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                         created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                         UNIQUE KEY uq_bill_run_org_month_date (org_unit_id, operation_month, bill_print_date),
                                         CONSTRAINT fk_bill_run_org FOREIGN KEY (org_unit_id) REFERENCES org_units(id),
                                         CONSTRAINT fk_bill_run_user FOREIGN KEY (password_verified_by) REFERENCES users(id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS bill_run_zones (
                                              id           BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                              bill_run_id  BIGINT UNSIGNED NOT NULL,
                                              billing_zone_id BIGINT UNSIGNED NOT NULL,
                                              last_generated_at DATETIME NULL,
                                              created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                              UNIQUE KEY uq_bill_run_zone (bill_run_id, billing_zone_id),
                                              CONSTRAINT fk_bill_run_zone_run FOREIGN KEY (bill_run_id) REFERENCES bill_runs(id) ON DELETE CASCADE,
                                              CONSTRAINT fk_bill_run_zone_zone FOREIGN KEY (billing_zone_id) REFERENCES billing_zones(id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS monthly_bills (
                                             id                 BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                             bill_run_id         BIGINT UNSIGNED NOT NULL,
                                             billing_zone_id     BIGINT UNSIGNED NOT NULL,
                                             connection_id       BIGINT UNSIGNED NOT NULL,
                                             statement_no        VARCHAR(80) NOT NULL,
                                             bill_status         ENUM('OPEN','CLOSED') NOT NULL DEFAULT 'OPEN',
                                             opened_at           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                             closed_at           DATETIME NULL,

                                             previous_bill_amount DECIMAL(14,2) NOT NULL DEFAULT 0.00,
                                             payments_amount      DECIMAL(14,2) NOT NULL DEFAULT 0.00,
                                             adjustments_amount   DECIMAL(14,2) NOT NULL DEFAULT 0.00,
                                             balance_amount       DECIMAL(14,2) NOT NULL DEFAULT 0.00,
                                             penalty_amount       DECIMAL(14,2) NOT NULL DEFAULT 0.00,
                                             other_amount         DECIMAL(14,2) NOT NULL DEFAULT 0.00,
                                             installment_amount   DECIMAL(14,2) NOT NULL DEFAULT 0.00,

                                             previous_reading     INT NULL,
                                             current_reading      INT NULL,
                                             usage_units          INT NULL,

                                             consumption_charge   DECIMAL(14,2) NOT NULL DEFAULT 0.00,
                                             fixed_rental         DECIMAL(14,2) NOT NULL DEFAULT 0.00,
                                             total_due_amount     DECIMAL(14,2) NOT NULL DEFAULT 0.00,

                                             is_red_bill          TINYINT(1) NOT NULL DEFAULT 0,

                                             created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                             updated_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                                             UNIQUE KEY uq_monthly_bill_statement (statement_no),
                                             INDEX idx_monthly_bill_conn (connection_id),
                                             INDEX idx_monthly_bill_zone (billing_zone_id),
                                             INDEX idx_monthly_bill_status (bill_status),

                                             CONSTRAINT fk_monthly_bill_run FOREIGN KEY (bill_run_id) REFERENCES bill_runs(id),
                                             CONSTRAINT fk_monthly_bill_zone FOREIGN KEY (billing_zone_id) REFERENCES billing_zones(id),
                                             CONSTRAINT fk_monthly_bill_conn FOREIGN KEY (connection_id) REFERENCES connections(id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS meter_readings (
                                              id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                              bill_id        BIGINT UNSIGNED NOT NULL,
                                              connection_id  BIGINT UNSIGNED NOT NULL,
                                              previous_reading INT NOT NULL,
                                              current_reading  INT NOT NULL,
                                              usage_units      INT NOT NULL,
                                              entered_from     ENUM('WEB','MOBILE_APP') NOT NULL,
                                              status           ENUM('ACTIVE','REVERSED') NOT NULL DEFAULT 'ACTIVE',
                                              reversed_reason  VARCHAR(500) NULL,
                                              created_by       BIGINT UNSIGNED NULL,
                                              created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                              updated_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                              INDEX idx_meter_bill (bill_id),
                                              INDEX idx_meter_conn (connection_id),
                                              CONSTRAINT fk_meter_bill FOREIGN KEY (bill_id) REFERENCES monthly_bills(id) ON DELETE CASCADE,
                                              CONSTRAINT fk_meter_conn FOREIGN KEY (connection_id) REFERENCES connections(id),
                                              CONSTRAINT fk_meter_user FOREIGN KEY (created_by) REFERENCES users(id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS bill_custom_notes (
                                                 id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                                 org_unit_id      BIGINT UNSIGNED NOT NULL,
                                                 note_scope       ENUM('TARIFF','ZONE','SOCIETY','GN_DIVISION','VALVE','CLUSTER','ALL') NOT NULL,
                                                 scope_id         BIGINT UNSIGNED NULL,
                                                 note_text        VARCHAR(1000) NOT NULL,
                                                 is_active        TINYINT(1) NOT NULL DEFAULT 1,
                                                 created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                 updated_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                                 INDEX idx_bill_note_org (org_unit_id),
                                                 INDEX idx_bill_note_scope (note_scope, scope_id),
                                                 CONSTRAINT fk_bill_note_org FOREIGN KEY (org_unit_id) REFERENCES org_units(id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS customer_ledger_entries (
                                                       id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                                       org_unit_id      BIGINT UNSIGNED NOT NULL,
                                                       connection_id    BIGINT UNSIGNED NOT NULL,
                                                       entry_type       ENUM('INVOICE','RECEIPT','REVERSAL','GL','ADJUSTMENT_NOTE') NOT NULL,
                                                       entry_date       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                       effective_date   DATETIME NULL,
                                                       amount           DECIMAL(14,2) NOT NULL,
                                                       dr_cr            ENUM('DR','CR') NOT NULL,
                                                       affects_bill     TINYINT(1) NOT NULL DEFAULT 1,
                                                       reference_table  VARCHAR(80) NULL,
                                                       reference_id     BIGINT UNSIGNED NULL,
                                                       description      VARCHAR(500) NULL,
                                                       created_by       BIGINT UNSIGNED NULL,
                                                       created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                       INDEX idx_led_conn_date (connection_id, entry_date),
                                                       INDEX idx_led_org (org_unit_id),
                                                       CONSTRAINT fk_led_org FOREIGN KEY (org_unit_id) REFERENCES org_units(id),
                                                       CONSTRAINT fk_led_conn FOREIGN KEY (connection_id) REFERENCES connections(id),
                                                       CONSTRAINT fk_led_user FOREIGN KEY (created_by) REFERENCES users(id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS bill_adjustments (
                                                id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                                org_unit_id      BIGINT UNSIGNED NOT NULL,
                                                connection_id    BIGINT UNSIGNED NOT NULL,
                                                amount           DECIMAL(14,2) NOT NULL,
                                                reason           VARCHAR(500) NOT NULL,
                                                applied_status   ENUM('PENDING','APPLIED') NOT NULL DEFAULT 'PENDING',
                                                created_by       BIGINT UNSIGNED NULL,
                                                created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                updated_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                                INDEX idx_bill_adj_conn (connection_id),
                                                CONSTRAINT fk_bill_adj_org FOREIGN KEY (org_unit_id) REFERENCES org_units(id),
                                                CONSTRAINT fk_bill_adj_conn FOREIGN KEY (connection_id) REFERENCES connections(id),
                                                CONSTRAINT fk_bill_adj_user FOREIGN KEY (created_by) REFERENCES users(id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS tariff_versions (
                                               id           BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                               tariff_id     BIGINT UNSIGNED NOT NULL,
                                               version_no    INT NOT NULL,
                                               valid_from    DATE NOT NULL,
                                               valid_to      DATE NULL,
                                               created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                               UNIQUE KEY uq_tariff_version (tariff_id, version_no),
                                               INDEX idx_tariff_version_valid (tariff_id, valid_from),
                                               CONSTRAINT fk_tariff_version_tariff
                                                   FOREIGN KEY (tariff_id) REFERENCES tariffs(id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS tariff_slabs (
                                            id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                            tariff_version_id BIGINT UNSIGNED NOT NULL,
                                            from_unit     INT NOT NULL,
                                            to_unit       INT NOT NULL,
                                            unit_price    DECIMAL(14,2) NOT NULL,
                                            created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                            INDEX idx_tariff_slab_ver (tariff_version_id),
                                            CONSTRAINT fk_tariff_slab_ver
                                                FOREIGN KEY (tariff_version_id) REFERENCES tariff_versions(id) ON DELETE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS agencies (
                                        id                   BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                        business_name         VARCHAR(255) NOT NULL,
                                        mobile_number         VARCHAR(20) NOT NULL,
                                        nic_number            VARCHAR(30) NOT NULL,
                                        business_address      VARCHAR(500) NOT NULL,
                                        brc_number            VARCHAR(100) NULL,
                                        owner_name            VARCHAR(255) NOT NULL,
                                        owner_nic_number      VARCHAR(30) NOT NULL,
                                        secondary_contact_no  VARCHAR(20) NULL,
                                        service_charge_percent DECIMAL(6,2) NOT NULL DEFAULT 15.00,
                                        subscription_fee      DECIMAL(14,2) NOT NULL DEFAULT 5.00,
                                        total_charges         DECIMAL(14,2) NOT NULL DEFAULT 20.00,
                                        billing_mode            ENUM('PREPAID','POSTPAID') NOT NULL DEFAULT 'PREPAID',
                                        is_active             TINYINT(1) NOT NULL DEFAULT 1,
                                        created_at            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                        updated_at            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                        UNIQUE KEY uq_agency_mobile (mobile_number),
                                        UNIQUE KEY uq_agency_nic (nic_number)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS agency_accounts (
                                               id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                               agency_id        BIGINT UNSIGNED NOT NULL,
                                               cr_balance       DECIMAL(14,2) NOT NULL DEFAULT 0.00,
                                               subscription_due DECIMAL(14,2) NOT NULL DEFAULT 0.00,
                                               credit_limit     DECIMAL(14,2) NOT NULL DEFAULT 0.00,
                                               updated_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                               UNIQUE KEY uq_agency_account (agency_id),
                                               CONSTRAINT fk_agency_account_agency FOREIGN KEY (agency_id) REFERENCES agencies(id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS agency_deposit_requests (
                                                       id                 BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                                       agency_id           BIGINT UNSIGNED NOT NULL,
                                                       monetary_account_id   BIGINT UNSIGNED NOT NULL,
                                                       payment_method_id   BIGINT UNSIGNED NOT NULL,
                                                       reference_text      VARCHAR(255) NOT NULL,
                                                       paid_date           DATE NOT NULL,
                                                       amount              DECIMAL(14,2) NOT NULL,
                                                       status              ENUM('REQUESTED','APPROVED','REJECTED','POSTED','REVERSED') NOT NULL DEFAULT 'REQUESTED',
                                                       created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                       updated_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                                       INDEX idx_ag_dep_req_agency (agency_id),
                                                       CONSTRAINT fk_ag_dep_req_agency FOREIGN KEY (agency_id) REFERENCES agencies(id),
                                                       CONSTRAINT fk_ag_dep_req_cash FOREIGN KEY (monetary_account_id) REFERENCES monetary_accounts(id),
                                                       CONSTRAINT fk_ag_dep_req_method FOREIGN KEY (payment_method_id) REFERENCES payment_methods(id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS agency_receipts (
                                               id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                               agency_id      BIGINT UNSIGNED NOT NULL,
                                               receipt_id     BIGINT UNSIGNED NOT NULL,
                                               subscription_fee_amount DECIMAL(14,2) NOT NULL DEFAULT 0.00,
                                               status         ENUM('POSTED','REVERSAL_REQUESTED','REVERSED') NOT NULL DEFAULT 'POSTED',
                                               created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                               UNIQUE KEY uq_agency_receipt (receipt_id),
                                               CONSTRAINT fk_ag_receipt_agency FOREIGN KEY (agency_id) REFERENCES agencies(id),
                                               CONSTRAINT fk_ag_receipt_receipt FOREIGN KEY (receipt_id) REFERENCES receipts(id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS agency_reversal_requests (
                                                        id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                                        agency_id        BIGINT UNSIGNED NOT NULL,
                                                        receipt_id       BIGINT UNSIGNED NOT NULL,
                                                        reason           VARCHAR(500) NOT NULL,
                                                        status           ENUM('REQUESTED','APPROVED','REJECTED','REVERSED') NOT NULL DEFAULT 'REQUESTED',
                                                        created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                        updated_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                                        INDEX idx_ag_rev_req_agency (agency_id),
                                                        CONSTRAINT fk_ag_rev_req_agency FOREIGN KEY (agency_id) REFERENCES agencies(id),
                                                        CONSTRAINT fk_ag_rev_req_receipt FOREIGN KEY (receipt_id) REFERENCES receipts(id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS service_requests (
                                                id                 BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                                org_unit_id         BIGINT UNSIGNED NOT NULL,
                                                connection_id       BIGINT UNSIGNED NULL,
                                                request_group       ENUM('CUSTOMER_FAULTS','CONNECTION_METER_MANAGEMENT','MAIN_LINE_MAINTENANCE') NOT NULL,
                                                category            ENUM('MAINTENANCE','BILLING','STAFF','MANAGEMENT','OTHER') NOT NULL,
                                                description         VARCHAR(1000) NOT NULL,
                                                status              ENUM('DRAFT','OPEN','IN_PROGRESS','PAUSED','CLOSED') NOT NULL DEFAULT 'OPEN',
                                                saved_at            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                closed_at           DATETIME NULL,
                                                expiry_days         INT NOT NULL DEFAULT 7,
                                                created_by          BIGINT UNSIGNED NULL,
                                                updated_by          BIGINT UNSIGNED NULL,
                                                created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                updated_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                                INDEX idx_sr_org_status (org_unit_id, status),
                                                INDEX idx_sr_conn (connection_id),
                                                CONSTRAINT fk_sr_org FOREIGN KEY (org_unit_id) REFERENCES org_units(id),
                                                CONSTRAINT fk_sr_conn FOREIGN KEY (connection_id) REFERENCES connections(id),
                                                CONSTRAINT fk_sr_created_by FOREIGN KEY (created_by) REFERENCES users(id),
                                                CONSTRAINT fk_sr_updated_by FOREIGN KEY (updated_by) REFERENCES users(id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS service_request_work_orders (
                                                           id                 BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                                           service_request_id  BIGINT UNSIGNED NOT NULL,
                                                           action_type         ENUM(
                                                               'MAINTENANCE_DEPT',
                                                               'EXEC_COMMITTEE_APPROVAL',
                                                               'COMPUTER_OPERATOR',
                                                               'ACCOUNT_CLERK',
                                                               'INFO_VERIFICATION',
                                                               'OTHER'
                                                               ) NOT NULL,
                                                           committee_meeting_date DATE NULL,
                                                           notes              VARCHAR(1000) NULL,
                                                           status             ENUM('OPEN','UPDATED','COMPLETED') NOT NULL DEFAULT 'OPEN',
                                                           created_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                           updated_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                                           INDEX idx_sr_wo_sr (service_request_id),
                                                           CONSTRAINT fk_sr_wo_sr FOREIGN KEY (service_request_id) REFERENCES service_requests(id) ON DELETE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS service_request_work_order_employees (
                                                                    id           BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                                                    work_order_id BIGINT UNSIGNED NOT NULL,
                                                                    employee_id  BIGINT UNSIGNED NOT NULL,
                                                                    UNIQUE KEY uq_sr_wo_emp (work_order_id, employee_id),
                                                                    CONSTRAINT fk_sr_wo_emp_wo FOREIGN KEY (work_order_id) REFERENCES service_request_work_orders(id) ON DELETE CASCADE,
                                                                    CONSTRAINT fk_sr_wo_emp_emp FOREIGN KEY (employee_id) REFERENCES employees(id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS service_request_solutions (
                                                         id                 BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                                         service_request_id  BIGINT UNSIGNED NOT NULL,
                                                         resolution_type     ENUM(
                                                             'NEW_SERVICE_CONNECTION',
                                                             'SERVICE_DISCONNECTED_NON_PAYMENT',
                                                             'SERVICE_DISCONNECTED_CUSTOMER_REQUEST',
                                                             'SERVICE_RECONNECTED',
                                                             'NEW_METER_INSTALLED',
                                                             'METER_REPAIRED',
                                                             'METER_READING_ADJUSTED',
                                                             'SERVICE_LINE_REPAIRED',
                                                             'MAIN_LINE_REPAIRED',
                                                             'OTHER'
                                                             ) NOT NULL,
                                                         serial_number       VARCHAR(100) NULL,
                                                         meter_reading       INT NULL,
                                                         adjustment_description VARCHAR(500) NULL,
                                                         other_description   VARCHAR(1000) NULL,
                                                         status              ENUM('PENDING','APPLIED') NOT NULL DEFAULT 'PENDING',
                                                         created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                         updated_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                                         INDEX idx_sr_sol_sr (service_request_id),
                                                         CONSTRAINT fk_sr_sol_sr FOREIGN KEY (service_request_id) REFERENCES service_requests(id) ON DELETE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS service_request_material_consumptions (
                                                                     id                 BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                                                     service_request_id  BIGINT UNSIGNED NOT NULL,
                                                                     import_from_mcn     TINYINT(1) NOT NULL DEFAULT 0,
                                                                     maintain_charge_amount DECIMAL(14,2) NULL,
                                                                     description         VARCHAR(500) NULL,
                                                                     created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                                     UNIQUE KEY uq_sr_mcn (service_request_id),
                                                                     CONSTRAINT fk_sr_mcn_sr FOREIGN KEY (service_request_id) REFERENCES service_requests(id) ON DELETE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS service_request_feedback (
                                                        id                 BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                                        service_request_id  BIGINT UNSIGNED NOT NULL,
                                                        final_response      ENUM('CONTACTED_AND_INFORMED','CONTACTED_NOT_INFORM','BUSY','NO_ANSWER','NOT_RESPONDING') NOT NULL,
                                                        updated_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                                        UNIQUE KEY uq_sr_feedback (service_request_id),
                                                        CONSTRAINT fk_sr_feedback_sr FOREIGN KEY (service_request_id) REFERENCES service_requests(id) ON DELETE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE sms_mask_configs (
                                  id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                  org_unit_id    BIGINT UNSIGNED NULL,  -- NULL = global default
                                  mask_scope     ENUM('PRODUCT','CLIENT') NOT NULL,
                                  provider_name  VARCHAR(50) NOT NULL,   -- e.g., "Dialog", "Mobitel", etc.
                                  sender_mask    VARCHAR(50) NOT NULL,   -- mask text / sender id
                                  is_active      TINYINT(1) NOT NULL DEFAULT 1,
                                  created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  updated_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                  UNIQUE KEY uq_sms_mask_scope_org (mask_scope, org_unit_id),
                                  CONSTRAINT fk_sms_mask_org FOREIGN KEY (org_unit_id) REFERENCES org_units(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE sms_templates (
                               id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                               org_unit_id    BIGINT UNSIGNED NULL, -- NULL = global template
                               sms_type       VARCHAR(80) NOT NULL, -- or ENUM; keep flexible in DB
                               mask_scope     ENUM('PRODUCT','CLIENT') NOT NULL,
                               lang           ENUM('EN','SI','TA') NOT NULL DEFAULT 'EN',
                               template_text  VARCHAR(1000) NOT NULL,
                               is_active      TINYINT(1) NOT NULL DEFAULT 1,
                               created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               updated_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                               INDEX idx_sms_tpl_type (sms_type),
                               INDEX idx_sms_tpl_org (org_unit_id),
                               CONSTRAINT fk_sms_tpl_org FOREIGN KEY (org_unit_id) REFERENCES org_units(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE sms_outbox (
                            id               BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                            org_unit_id       BIGINT UNSIGNED NULL,
                            sms_type          VARCHAR(80) NOT NULL,
                            mask_scope        ENUM('PRODUCT','CLIENT') NOT NULL,
                            to_mobile         VARCHAR(20) NOT NULL,
                            message_text      VARCHAR(1000) NOT NULL,
                            reference_table   VARCHAR(80) NULL,    -- e.g., "receipts"
                            reference_id      BIGINT UNSIGNED NULL,
                            status            ENUM('PENDING','SENT','FAILED') NOT NULL DEFAULT 'PENDING',
                            provider_message_id VARCHAR(100) NULL,
                            provider_response VARCHAR(2000) NULL,
                            retry_count       INT NOT NULL DEFAULT 0,
                            last_attempt_at   DATETIME NULL,
                            created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            INDEX idx_sms_outbox_status (status, created_at),
                            INDEX idx_sms_outbox_ref (reference_table, reference_id),
                            CONSTRAINT fk_sms_outbox_org FOREIGN KEY (org_unit_id) REFERENCES org_units(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;




















































