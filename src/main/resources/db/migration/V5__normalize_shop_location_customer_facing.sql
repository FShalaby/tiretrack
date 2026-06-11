DELIMITER $$

CREATE PROCEDURE tiretrack_v5_normalize_shop_location_customer_facing()
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = DATABASE()
          AND table_name = 'shop_locations'
    ) AND EXISTS (
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
END$$

CALL tiretrack_v5_normalize_shop_location_customer_facing()$$

DROP PROCEDURE tiretrack_v5_normalize_shop_location_customer_facing$$

DELIMITER ;
