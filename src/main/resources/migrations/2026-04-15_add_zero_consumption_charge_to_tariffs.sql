ALTER TABLE tariffs
    ADD COLUMN IF NOT EXISTS zero_consumption_charge DOUBLE NULL AFTER avg_monthly_max_consumption;
