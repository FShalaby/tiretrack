DELIMITER $$

CREATE PROCEDURE tiretrack_v4_multi_location_operational_fields()
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = DATABASE()
          AND table_name = 'shop_locations'
    ) THEN
        IF EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'shop_locations'
              AND column_name = 'type'
        ) THEN
            ALTER TABLE shop_locations
                MODIFY COLUMN type enum ('MOBILE','MOBILE_SERVICE','OTHER','STORAGE','STORE','WAREHOUSE') NOT NULL;
        END IF;

        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'shop_locations'
              AND column_name = 'city'
        ) THEN
            ALTER TABLE shop_locations ADD COLUMN city VARCHAR(255);
        END IF;

        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'shop_locations'
              AND column_name = 'province'
        ) THEN
            ALTER TABLE shop_locations ADD COLUMN province VARCHAR(255);
        END IF;

        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'shop_locations'
              AND column_name = 'postal_code'
        ) THEN
            ALTER TABLE shop_locations ADD COLUMN postal_code VARCHAR(255);
        END IF;

        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'shop_locations'
              AND column_name = 'phone'
        ) THEN
            ALTER TABLE shop_locations ADD COLUMN phone VARCHAR(255);
        END IF;

        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'shop_locations'
              AND column_name = 'email'
        ) THEN
            ALTER TABLE shop_locations ADD COLUMN email VARCHAR(255);
        END IF;

        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'shop_locations'
              AND column_name = 'customer_facing'
        ) THEN
            ALTER TABLE shop_locations ADD COLUMN customer_facing BIT NOT NULL DEFAULT b'1';
        END IF;

        IF EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'shop_locations'
              AND column_name = 'customer_facing'
        ) THEN
            UPDATE shop_locations
            SET customer_facing = b'1'
            WHERE customer_facing IS NULL;

            ALTER TABLE shop_locations
                MODIFY COLUMN customer_facing BIT NOT NULL DEFAULT b'1';
        END IF;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = DATABASE()
          AND table_name = 'customer_vehicles'
    ) THEN
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'customer_vehicles'
              AND column_name = 'shop_id'
        ) THEN
            ALTER TABLE customer_vehicles ADD COLUMN shop_id BIGINT NULL;
        END IF;

        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'customer_vehicles'
              AND column_name = 'location_id'
        ) THEN
            ALTER TABLE customer_vehicles ADD COLUMN location_id BIGINT NULL;
        END IF;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = DATABASE()
          AND table_name = 'expenses'
    ) THEN
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'expenses'
              AND column_name = 'location_id'
        ) THEN
            ALTER TABLE expenses ADD COLUMN location_id BIGINT NULL;
        END IF;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = DATABASE()
          AND table_name = 'journal_entries'
    ) THEN
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'journal_entries'
              AND column_name = 'location_id'
        ) THEN
            ALTER TABLE journal_entries ADD COLUMN location_id BIGINT NULL;
        END IF;
    END IF;
END$$

CALL tiretrack_v4_multi_location_operational_fields()$$

DROP PROCEDURE tiretrack_v4_multi_location_operational_fields$$

DELIMITER ;
