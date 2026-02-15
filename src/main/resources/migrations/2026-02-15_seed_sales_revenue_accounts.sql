INSERT INTO revenue_accounts (
    main_category_id,
    account_number,
    name,
    description,
    reference_prefix,
    is_default,
    function_key,
    is_system,
    is_active,
    created_at,
    updated_at
)
SELECT rmc.id, 'REV-SALES-NEW-CONN', 'New Connection Charge', 'System seed for sales invoicing', 'NCF', 1, 'NEW_CONNECTION_FEE', 1, 1, NOW(), NOW()
FROM revenue_main_categories rmc
WHERE rmc.name = 'Customer Charges'
  AND NOT EXISTS (SELECT 1 FROM revenue_accounts ra WHERE ra.account_number = 'REV-SALES-NEW-CONN');

INSERT INTO revenue_accounts (
    main_category_id,
    account_number,
    name,
    description,
    reference_prefix,
    is_default,
    function_key,
    is_system,
    is_active,
    created_at,
    updated_at
)
SELECT rmc.id, 'REV-SALES-CONS', 'Consumption Charge', 'System seed for sales invoicing', 'WBC', 1, 'CONSUMPTION_CHARGE', 1, 1, NOW(), NOW()
FROM revenue_main_categories rmc
WHERE rmc.name = 'Customer Charges'
  AND NOT EXISTS (SELECT 1 FROM revenue_accounts ra WHERE ra.account_number = 'REV-SALES-CONS');

INSERT INTO revenue_accounts (
    main_category_id,
    account_number,
    name,
    description,
    reference_prefix,
    is_default,
    function_key,
    is_system,
    is_active,
    created_at,
    updated_at
)
SELECT rmc.id, 'REV-SALES-RECON', 'Re-Connection Charge', 'System seed for sales invoicing', 'RCC', 1, 'RECONNECTION_CHARGE', 1, 1, NOW(), NOW()
FROM revenue_main_categories rmc
WHERE rmc.name = 'Customer Charges'
  AND NOT EXISTS (SELECT 1 FROM revenue_accounts ra WHERE ra.account_number = 'REV-SALES-RECON');

INSERT INTO revenue_accounts (
    main_category_id,
    account_number,
    name,
    description,
    reference_prefix,
    is_default,
    function_key,
    is_system,
    is_active,
    created_at,
    updated_at
)
SELECT rmc.id, 'REV-SALES-MAINT', 'Maintain Charge', 'System seed for sales invoicing', 'MNT', 1, 'MAINTAIN_CHARGE', 1, 1, NOW(), NOW()
FROM revenue_main_categories rmc
WHERE rmc.name = 'Customer Charges'
  AND NOT EXISTS (SELECT 1 FROM revenue_accounts ra WHERE ra.account_number = 'REV-SALES-MAINT');

INSERT INTO revenue_accounts (
    main_category_id,
    account_number,
    name,
    description,
    reference_prefix,
    is_default,
    function_key,
    is_system,
    is_active,
    created_at,
    updated_at
)
SELECT rmc.id, 'REV-SALES-WELFARE', 'Welfare/Insurance Fund', 'System seed for sales invoicing', 'WLF', 1, 'WELFARE_INSURANCE_FUND', 1, 1, NOW(), NOW()
FROM revenue_main_categories rmc
WHERE rmc.name = 'Customer Charges'
  AND NOT EXISTS (SELECT 1 FROM revenue_accounts ra WHERE ra.account_number = 'REV-SALES-WELFARE');

INSERT INTO revenue_accounts (
    main_category_id,
    account_number,
    name,
    description,
    reference_prefix,
    is_default,
    function_key,
    is_system,
    is_active,
    created_at,
    updated_at
)
SELECT rmc.id, 'REV-SALES-NON-OP', 'Non-operation Income', 'System seed for sales invoicing', 'NOI', 1, 'NON_OPERATION_INCOME', 1, 1, NOW(), NOW()
FROM revenue_main_categories rmc
WHERE rmc.name = 'Non-Customer Charges'
  AND NOT EXISTS (SELECT 1 FROM revenue_accounts ra WHERE ra.account_number = 'REV-SALES-NON-OP');
