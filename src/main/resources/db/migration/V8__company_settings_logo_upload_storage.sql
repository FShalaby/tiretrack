DELIMITER $$

CREATE PROCEDURE tiretrack_v8_company_settings_logo_upload_storage()
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = DATABASE()
          AND table_name = 'company_settings'
    ) AND EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'company_settings'
          AND column_name = 'logo_url'
    ) THEN
        ALTER TABLE company_settings
            MODIFY COLUMN logo_url TEXT;
    END IF;
END$$

CALL tiretrack_v8_company_settings_logo_upload_storage()$$

DROP PROCEDURE tiretrack_v8_company_settings_logo_upload_storage$$

DELIMITER ;
