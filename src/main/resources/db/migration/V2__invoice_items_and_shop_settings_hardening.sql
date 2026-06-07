DELIMITER $$

CREATE PROCEDURE tiretrack_v2_invoice_items_and_shop_settings()
BEGIN
    DECLARE fk_name VARCHAR(128) DEFAULT NULL;
    DECLARE fk_count INT DEFAULT 0;
    DECLARE index_count INT DEFAULT 0;

    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = DATABASE()
          AND table_name = 'invoice_items'
    ) THEN
        SELECT kcu.constraint_name
        INTO fk_name
        FROM information_schema.key_column_usage kcu
        JOIN information_schema.table_constraints tc
          ON tc.constraint_schema = kcu.constraint_schema
         AND tc.table_name = kcu.table_name
         AND tc.constraint_name = kcu.constraint_name
        WHERE kcu.table_schema = DATABASE()
          AND kcu.table_name = 'invoice_items'
          AND kcu.column_name = 'invoice_id'
          AND tc.constraint_type = 'FOREIGN KEY'
        LIMIT 1;

        IF fk_name IS NOT NULL THEN
            SET @sql = CONCAT('ALTER TABLE invoice_items DROP FOREIGN KEY `', REPLACE(fk_name, '`', '``'), '`');
            PREPARE stmt FROM @sql;
            EXECUTE stmt;
            DEALLOCATE PREPARE stmt;
        END IF;

        SET fk_name = NULL;

        SELECT kcu.constraint_name
        INTO fk_name
        FROM information_schema.key_column_usage kcu
        JOIN information_schema.table_constraints tc
          ON tc.constraint_schema = kcu.constraint_schema
         AND tc.table_name = kcu.table_name
         AND tc.constraint_name = kcu.constraint_name
        WHERE kcu.table_schema = DATABASE()
          AND kcu.table_name = 'invoice_items'
          AND kcu.column_name = 'tire_id'
          AND tc.constraint_type = 'FOREIGN KEY'
        LIMIT 1;

        IF fk_name IS NOT NULL THEN
            SET @sql = CONCAT('ALTER TABLE invoice_items DROP FOREIGN KEY `', REPLACE(fk_name, '`', '``'), '`');
            PREPARE stmt FROM @sql;
            EXECUTE stmt;
            DEALLOCATE PREPARE stmt;
        END IF;

        IF EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'invoices'
              AND column_name = 'id'
        ) THEN
            ALTER TABLE invoices MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT;
        END IF;

        IF EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'tires'
              AND column_name = 'id'
        ) THEN
            ALTER TABLE tires MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT;
        END IF;

        IF EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'invoice_items'
              AND column_name = 'id'
        ) THEN
            ALTER TABLE invoice_items MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT;
        END IF;

        IF EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'invoice_items'
              AND column_name = 'invoice_id'
        ) THEN
            ALTER TABLE invoice_items MODIFY COLUMN invoice_id BIGINT NULL;
        END IF;

        IF EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'invoice_items'
              AND column_name = 'tire_id'
        ) THEN
            ALTER TABLE invoice_items MODIFY COLUMN tire_id BIGINT NULL;
        END IF;

        SELECT COUNT(*)
        INTO fk_count
        FROM information_schema.key_column_usage kcu
        JOIN information_schema.table_constraints tc
          ON tc.constraint_schema = kcu.constraint_schema
         AND tc.table_name = kcu.table_name
         AND tc.constraint_name = kcu.constraint_name
        WHERE kcu.table_schema = DATABASE()
          AND kcu.table_name = 'invoice_items'
          AND kcu.column_name = 'invoice_id'
          AND tc.constraint_type = 'FOREIGN KEY';

        IF fk_count = 0
           AND EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'invoices') THEN
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
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = DATABASE()
          AND table_name = 'company_settings'
    ) THEN
        IF EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'company_settings'
              AND column_name = 'id'
        ) THEN
            ALTER TABLE company_settings MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT;
        END IF;

        IF NOT EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'company_settings'
              AND column_name = 'shop_id'
        ) THEN
            ALTER TABLE company_settings ADD COLUMN shop_id BIGINT NULL;
        END IF;

        SELECT COUNT(*)
        INTO index_count
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = 'company_settings'
          AND index_name = 'idx_company_settings_shop_id';

        IF index_count = 0 THEN
            CREATE INDEX idx_company_settings_shop_id ON company_settings (shop_id);
        END IF;

        SELECT COUNT(*)
        INTO fk_count
        FROM information_schema.key_column_usage kcu
        JOIN information_schema.table_constraints tc
          ON tc.constraint_schema = kcu.constraint_schema
         AND tc.table_name = kcu.table_name
         AND tc.constraint_name = kcu.constraint_name
        WHERE kcu.table_schema = DATABASE()
          AND kcu.table_name = 'company_settings'
          AND kcu.column_name = 'shop_id'
          AND tc.constraint_type = 'FOREIGN KEY';

        IF fk_count = 0
           AND EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'shops') THEN
            UPDATE company_settings cs
            LEFT JOIN shops s ON s.id = cs.shop_id
            SET cs.shop_id = NULL
            WHERE cs.shop_id IS NOT NULL
              AND s.id IS NULL;

            ALTER TABLE company_settings
                ADD CONSTRAINT fk_company_settings_shop
                FOREIGN KEY (shop_id) REFERENCES shops(id);
        END IF;
    END IF;
END$$

CALL tiretrack_v2_invoice_items_and_shop_settings()$$

DROP PROCEDURE tiretrack_v2_invoice_items_and_shop_settings$$

DELIMITER ;
