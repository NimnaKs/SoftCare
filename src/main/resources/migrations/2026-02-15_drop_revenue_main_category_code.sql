-- Remove obsolete `code` column from revenue main categories.
-- Safe for existing databases.
ALTER TABLE revenue_main_categories
    DROP INDEX uq_rev_main_code;

ALTER TABLE revenue_main_categories
    DROP COLUMN code;
