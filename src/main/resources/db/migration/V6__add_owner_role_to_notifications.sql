DELIMITER $$

CREATE PROCEDURE tiretrack_v6_add_owner_role_to_notifications()
BEGIN
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

CALL tiretrack_v6_add_owner_role_to_notifications()$$

DROP PROCEDURE tiretrack_v6_add_owner_role_to_notifications$$

DELIMITER ;
