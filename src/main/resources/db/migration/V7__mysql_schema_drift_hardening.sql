DELIMITER $$

CREATE PROCEDURE tiretrack_v7_mysql_schema_drift_hardening()
BEGIN
    DECLARE fk_name VARCHAR(128) DEFAULT NULL;
    DECLARE done INT DEFAULT 0;
    DECLARE fk_cursor CURSOR FOR
        SELECT kcu.constraint_name
        FROM information_schema.key_column_usage kcu
        JOIN information_schema.table_constraints tc
          ON tc.constraint_schema = kcu.constraint_schema
         AND tc.table_name = kcu.table_name
         AND tc.constraint_name = kcu.constraint_name
        WHERE kcu.table_schema = DATABASE()
          AND kcu.table_name = 'invoice_items'
          AND kcu.column_name IN ('invoice_id', 'tire_id')
          AND tc.constraint_type = 'FOREIGN KEY';
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = DATABASE()
          AND table_name = 'invoice_items'
    ) THEN
        OPEN fk_cursor;

        drop_fk_loop: LOOP
            FETCH fk_cursor INTO fk_name;
            IF done = 1 THEN
                LEAVE drop_fk_loop;
            END IF;

            SET @sql = CONCAT('ALTER TABLE invoice_items DROP FOREIGN KEY `', REPLACE(fk_name, '`', '``'), '`');
            PREPARE stmt FROM @sql;
            EXECUTE stmt;
            DEALLOCATE PREPARE stmt;
        END LOOP;

        CLOSE fk_cursor;

        IF EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'invoices'
              AND column_name = 'id'
        ) THEN
            ALTER TABLE invoices MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT;
        END IF;

        IF EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'tires'
              AND column_name = 'id'
        ) THEN
            ALTER TABLE tires MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT;
        END IF;

        IF EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'invoice_items'
              AND column_name = 'id'
        ) THEN
            ALTER TABLE invoice_items MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT;
        END IF;

        IF EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'invoice_items'
              AND column_name = 'invoice_id'
        ) THEN
            ALTER TABLE invoice_items MODIFY COLUMN invoice_id BIGINT NULL;
        END IF;

        IF EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'invoice_items'
              AND column_name = 'tire_id'
        ) THEN
            ALTER TABLE invoice_items MODIFY COLUMN tire_id BIGINT NULL;
        END IF;

        IF EXISTS (
            SELECT 1 FROM information_schema.tables
            WHERE table_schema = DATABASE()
              AND table_name = 'invoices'
        ) AND NOT EXISTS (
            SELECT 1
            FROM information_schema.key_column_usage kcu
            JOIN information_schema.table_constraints tc
              ON tc.constraint_schema = kcu.constraint_schema
             AND tc.table_name = kcu.table_name
             AND tc.constraint_name = kcu.constraint_name
            WHERE kcu.table_schema = DATABASE()
              AND kcu.table_name = 'invoice_items'
              AND kcu.column_name = 'invoice_id'
              AND tc.constraint_type = 'FOREIGN KEY'
        ) THEN
            UPDATE invoice_items ii
            LEFT JOIN invoices i ON i.id = ii.invoice_id
            SET ii.invoice_id = NULL
            WHERE ii.invoice_id IS NOT NULL
              AND i.id IS NULL;

            ALTER TABLE invoice_items
                ADD CONSTRAINT fk_invoice_items_invoice
                FOREIGN KEY (invoice_id) REFERENCES invoices(id);
        END IF;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = DATABASE()
          AND table_name = 'app_notifications'
    ) AND EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'app_notifications'
          AND column_name = 'recipient_role'
    ) THEN
        ALTER TABLE app_notifications
            MODIFY COLUMN recipient_role enum ('ADMIN','CUSTOMER','EMPLOYEE','OWNER','SUPER_ADMIN');
    END IF;
END$$

CALL tiretrack_v7_mysql_schema_drift_hardening()$$

DROP PROCEDURE tiretrack_v7_mysql_schema_drift_hardening$$

DELIMITER ;
