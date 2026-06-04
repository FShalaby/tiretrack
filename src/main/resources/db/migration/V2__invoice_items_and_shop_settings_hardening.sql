ALTER TABLE invoice_items DROP FOREIGN KEY invoice_items_ibfk_1;
ALTER TABLE invoice_items DROP FOREIGN KEY invoice_items_ibfk_2;

ALTER TABLE invoice_items MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT;
ALTER TABLE invoice_items MODIFY COLUMN invoice_id BIGINT NULL;
ALTER TABLE invoice_items MODIFY COLUMN tire_id BIGINT NULL;

ALTER TABLE invoice_items
    ADD CONSTRAINT fk_invoice_items_invoice
    FOREIGN KEY (invoice_id) REFERENCES invoices(id);

ALTER TABLE invoice_items
    ADD CONSTRAINT fk_invoice_items_tire
    FOREIGN KEY (tire_id) REFERENCES tires(id);

ALTER TABLE company_settings MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT;
ALTER TABLE company_settings ADD COLUMN shop_id BIGINT NULL;
CREATE INDEX idx_company_settings_shop_id ON company_settings (shop_id);

ALTER TABLE company_settings
    ADD CONSTRAINT fk_company_settings_shop
    FOREIGN KEY (shop_id) REFERENCES shops(id);
