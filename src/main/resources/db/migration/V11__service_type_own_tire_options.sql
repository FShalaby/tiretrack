DELIMITER $$

CREATE PROCEDURE tiretrack_v11_service_type_own_tire_options()
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'appointments'
          AND column_name = 'service_type'
    ) THEN
        ALTER TABLE appointments
            MODIFY COLUMN service_type enum ('BALANCING','BOLT_ON','INSTALLATION','REPAIR','RE_AND_RE','ROTATION') NOT NULL;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'work_orders'
          AND column_name = 'service_type'
    ) THEN
        ALTER TABLE work_orders
            MODIFY COLUMN service_type enum ('BALANCING','BOLT_ON','INSTALLATION','REPAIR','RE_AND_RE','ROTATION') NOT NULL;
    END IF;
END $$

CALL tiretrack_v11_service_type_own_tire_options() $$

DROP PROCEDURE tiretrack_v11_service_type_own_tire_options $$

DELIMITER ;
