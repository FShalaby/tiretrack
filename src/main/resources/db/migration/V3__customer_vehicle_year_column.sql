DELIMITER $$

CREATE PROCEDURE tiretrack_v3_customer_vehicle_year_column()
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = DATABASE()
          AND table_name = 'customer_vehicles'
    ) THEN
        IF EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'customer_vehicles'
              AND column_name = 'year'
        ) AND NOT EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'customer_vehicles'
              AND column_name = 'vehicle_year'
        ) THEN
            ALTER TABLE customer_vehicles CHANGE COLUMN `year` vehicle_year VARCHAR(255);
        END IF;

        IF NOT EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'customer_vehicles'
              AND column_name = 'vehicle_year'
        ) THEN
            ALTER TABLE customer_vehicles ADD COLUMN vehicle_year VARCHAR(255);
        END IF;
    END IF;
END$$

CALL tiretrack_v3_customer_vehicle_year_column()$$

DROP PROCEDURE tiretrack_v3_customer_vehicle_year_column$$

DELIMITER ;
