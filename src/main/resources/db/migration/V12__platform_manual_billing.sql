DELIMITER $$

CREATE PROCEDURE tiretrack_v12_platform_manual_billing()
BEGIN
    CREATE TABLE IF NOT EXISTS shop_subscriptions (
        id BIGINT NOT NULL AUTO_INCREMENT,
        shop_id BIGINT NOT NULL,
        plan_name VARCHAR(255) NOT NULL,
        billing_cycle ENUM ('MONTHLY','ANNUAL') NOT NULL,
        price_cents BIGINT NOT NULL,
        currency VARCHAR(10) NOT NULL DEFAULT 'CAD',
        tax_rate DECIMAL(8,4) NULL,
        tax_cents BIGINT NOT NULL DEFAULT 0,
        total_cents BIGINT NOT NULL DEFAULT 0,
        status ENUM ('TRIAL','ACTIVE','PAST_DUE','READ_ONLY','CANCELLED','EXPIRED') NOT NULL,
        trial_start_date DATE NULL,
        trial_end_date DATE NULL,
        current_period_start DATE NULL,
        current_period_end DATE NULL,
        cancel_at_period_end BOOLEAN NOT NULL DEFAULT FALSE,
        cancelled_at DATETIME(6) NULL,
        grace_period_ends_at DATE NULL,
        notes TEXT NULL,
        external_customer_id VARCHAR(255) NULL,
        external_subscription_id VARCHAR(255) NULL,
        created_at DATETIME(6) NULL,
        updated_at DATETIME(6) NULL,
        PRIMARY KEY (id),
        UNIQUE KEY uk_shop_subscriptions_shop (shop_id)
    ) ENGINE=InnoDB;

    CREATE TABLE IF NOT EXISTS shop_payments (
        id BIGINT NOT NULL AUTO_INCREMENT,
        shop_id BIGINT NOT NULL,
        subscription_id BIGINT NULL,
        amount_cents BIGINT NOT NULL DEFAULT 0,
        currency VARCHAR(10) NOT NULL DEFAULT 'CAD',
        tax_cents BIGINT NOT NULL DEFAULT 0,
        total_cents BIGINT NOT NULL DEFAULT 0,
        payment_method ENUM ('E_TRANSFER','CASH','CHEQUE','BANK_TRANSFER','OTHER') NOT NULL,
        payment_status ENUM ('PENDING','PAID','FAILED','REFUNDED','PARTIAL') NOT NULL,
        paid_at DATETIME(6) NULL,
        period_start DATE NULL,
        period_end DATE NULL,
        reference_number VARCHAR(255) NULL,
        invoice_number VARCHAR(255) NULL,
        notes TEXT NULL,
        recorded_by_admin_id BIGINT NULL,
        external_invoice_id VARCHAR(255) NULL,
        external_payment_id VARCHAR(255) NULL,
        created_at DATETIME(6) NULL,
        updated_at DATETIME(6) NULL,
        PRIMARY KEY (id)
    ) ENGINE=InnoDB;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = 'shop_payments'
          AND index_name = 'idx_shop_payments_shop_created'
    ) THEN
        CREATE INDEX idx_shop_payments_shop_created ON shop_payments (shop_id, created_at);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = 'shop_payments'
          AND index_name = 'idx_shop_payments_status_paid_at'
    ) THEN
        CREATE INDEX idx_shop_payments_status_paid_at ON shop_payments (payment_status, paid_at);
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.key_column_usage kcu
        JOIN information_schema.table_constraints tc
          ON tc.constraint_schema = kcu.constraint_schema
         AND tc.table_name = kcu.table_name
         AND tc.constraint_name = kcu.constraint_name
        WHERE kcu.table_schema = DATABASE()
          AND kcu.table_name = 'shop_subscriptions'
          AND kcu.column_name = 'shop_id'
          AND tc.constraint_type = 'FOREIGN KEY'
    ) THEN
        ALTER TABLE shop_subscriptions
            ADD CONSTRAINT fk_shop_subscriptions_shop
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
          AND kcu.table_name = 'shop_payments'
          AND kcu.column_name = 'shop_id'
          AND tc.constraint_type = 'FOREIGN KEY'
    ) THEN
        ALTER TABLE shop_payments
            ADD CONSTRAINT fk_shop_payments_shop
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
          AND kcu.table_name = 'shop_payments'
          AND kcu.column_name = 'subscription_id'
          AND tc.constraint_type = 'FOREIGN KEY'
    ) THEN
        ALTER TABLE shop_payments
            ADD CONSTRAINT fk_shop_payments_subscription
            FOREIGN KEY (subscription_id) REFERENCES shop_subscriptions(id);
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.key_column_usage kcu
        JOIN information_schema.table_constraints tc
          ON tc.constraint_schema = kcu.constraint_schema
         AND tc.table_name = kcu.table_name
         AND tc.constraint_name = kcu.constraint_name
        WHERE kcu.table_schema = DATABASE()
          AND kcu.table_name = 'shop_payments'
          AND kcu.column_name = 'recorded_by_admin_id'
          AND tc.constraint_type = 'FOREIGN KEY'
    ) THEN
        ALTER TABLE shop_payments
            ADD CONSTRAINT fk_shop_payments_recorded_by
            FOREIGN KEY (recorded_by_admin_id) REFERENCES users(id);
    END IF;
END$$

CALL tiretrack_v12_platform_manual_billing()$$

DROP PROCEDURE tiretrack_v12_platform_manual_billing$$

DELIMITER ;
