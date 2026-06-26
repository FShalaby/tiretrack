DELIMITER $$

CREATE PROCEDURE tiretrack_v13_subscription_demo_and_business_hours()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'shop_subscriptions'
          AND column_name = 'demo_mode'
    ) THEN
        ALTER TABLE shop_subscriptions
            ADD COLUMN demo_mode BOOLEAN NOT NULL DEFAULT FALSE;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'shop_subscriptions'
          AND column_name = 'demo_multi_location'
    ) THEN
        ALTER TABLE shop_subscriptions
            ADD COLUMN demo_multi_location BOOLEAN NOT NULL DEFAULT FALSE;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'company_settings'
          AND column_name = 'opening_time'
    ) THEN
        ALTER TABLE company_settings
            ADD COLUMN opening_time VARCHAR(5) NULL DEFAULT '09:00';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'company_settings'
          AND column_name = 'closing_time'
    ) THEN
        ALTER TABLE company_settings
            ADD COLUMN closing_time VARCHAR(5) NULL DEFAULT '17:00';
    END IF;
END$$

CALL tiretrack_v13_subscription_demo_and_business_hours()$$
DROP PROCEDURE tiretrack_v13_subscription_demo_and_business_hours$$

DELIMITER ;
