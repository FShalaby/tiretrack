DELIMITER $$

CREATE PROCEDURE tiretrack_v10_tire_requests_and_availability_status()
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = DATABASE()
          AND table_name = 'appointments'
    ) AND EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'appointments'
          AND column_name = 'status'
    ) THEN
        ALTER TABLE appointments
            MODIFY COLUMN status enum ('BOOKED','CANCELLED','COMPLETED','PENDING_TIRE_AVAILABILITY');
    END IF;

    CREATE TABLE IF NOT EXISTS tire_requests (
        id BIGINT NOT NULL AUTO_INCREMENT,
        customer_id BIGINT NULL,
        vehicle_id BIGINT NULL,
        shop_id BIGINT NULL,
        location_id BIGINT NULL,
        appointment_id BIGINT NULL,
        requested_by BIGINT NULL,
        created_at DATETIME(6) NULL,
        updated_at DATETIME(6) NULL,
        requested_size VARCHAR(255) NULL,
        status ENUM ('AVAILABLE','CANCELLED','DECLINED','FULFILLED','PENDING','SOURCING','UNAVAILABLE') NOT NULL,
        source ENUM ('ADMIN','CUSTOMER_PORTAL','EMPLOYEE') NOT NULL,
        notes VARCHAR(1000) NULL,
        admin_response VARCHAR(1000) NULL,
        PRIMARY KEY (id)
    ) ENGINE=InnoDB;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = 'tire_requests'
          AND index_name = 'idx_tire_requests_shop_status'
    ) THEN
        CREATE INDEX idx_tire_requests_shop_status ON tire_requests (shop_id, status);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = 'tire_requests'
          AND index_name = 'idx_tire_requests_customer'
    ) THEN
        CREATE INDEX idx_tire_requests_customer ON tire_requests (customer_id);
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.key_column_usage kcu
        JOIN information_schema.table_constraints tc
          ON tc.constraint_schema = kcu.constraint_schema
         AND tc.table_name = kcu.table_name
         AND tc.constraint_name = kcu.constraint_name
        WHERE kcu.table_schema = DATABASE()
          AND kcu.table_name = 'tire_requests'
          AND kcu.column_name = 'customer_id'
          AND tc.constraint_type = 'FOREIGN KEY'
    ) THEN
        ALTER TABLE tire_requests
            ADD CONSTRAINT fk_tire_requests_customer
            FOREIGN KEY (customer_id) REFERENCES users(id);
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.key_column_usage kcu
        JOIN information_schema.table_constraints tc
          ON tc.constraint_schema = kcu.constraint_schema
         AND tc.table_name = kcu.table_name
         AND tc.constraint_name = kcu.constraint_name
        WHERE kcu.table_schema = DATABASE()
          AND kcu.table_name = 'tire_requests'
          AND kcu.column_name = 'vehicle_id'
          AND tc.constraint_type = 'FOREIGN KEY'
    ) THEN
        ALTER TABLE tire_requests
            ADD CONSTRAINT fk_tire_requests_vehicle
            FOREIGN KEY (vehicle_id) REFERENCES customer_vehicles(id);
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.key_column_usage kcu
        JOIN information_schema.table_constraints tc
          ON tc.constraint_schema = kcu.constraint_schema
         AND tc.table_name = kcu.table_name
         AND tc.constraint_name = kcu.constraint_name
        WHERE kcu.table_schema = DATABASE()
          AND kcu.table_name = 'tire_requests'
          AND kcu.column_name = 'shop_id'
          AND tc.constraint_type = 'FOREIGN KEY'
    ) THEN
        ALTER TABLE tire_requests
            ADD CONSTRAINT fk_tire_requests_shop
            FOREIGN KEY (shop_id) REFERENCES shops(id);
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.key_column_usage kcu
        JOIN information_schema.table_constraints tc
          ON tc.constraint_schema = kcu.constraint_schema
         AND tc.table_name = kcu.table_name
         AND tc.constraint_name = kcu.constraint_name
        WHERE kcu.table_schema = DATABASE()
          AND kcu.table_name = 'tire_requests'
          AND kcu.column_name = 'location_id'
          AND tc.constraint_type = 'FOREIGN KEY'
    ) THEN
        ALTER TABLE tire_requests
            ADD CONSTRAINT fk_tire_requests_location
            FOREIGN KEY (location_id) REFERENCES shop_locations(id);
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.key_column_usage kcu
        JOIN information_schema.table_constraints tc
          ON tc.constraint_schema = kcu.constraint_schema
         AND tc.table_name = kcu.table_name
         AND tc.constraint_name = kcu.constraint_name
        WHERE kcu.table_schema = DATABASE()
          AND kcu.table_name = 'tire_requests'
          AND kcu.column_name = 'appointment_id'
          AND tc.constraint_type = 'FOREIGN KEY'
    ) THEN
        ALTER TABLE tire_requests
            ADD CONSTRAINT fk_tire_requests_appointment
            FOREIGN KEY (appointment_id) REFERENCES appointments(id);
    END IF;
END$$

CALL tiretrack_v10_tire_requests_and_availability_status()$$

DROP PROCEDURE tiretrack_v10_tire_requests_and_availability_status$$

DELIMITER ;
