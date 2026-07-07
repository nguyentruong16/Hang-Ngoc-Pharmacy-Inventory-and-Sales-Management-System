INSERT INTO customer (customerID, customerType, name, taxCode, address, bankAccountNumber, bankName, phoneNumber, note)
SELECT 1, 'INDIVIDUAL', 'ABC', NULL, 'Uông Bí - Quảng Ninh', NULL, NULL, '0998877665', NULL
WHERE NOT EXISTS (
    SELECT 1
    FROM customer
    WHERE customerID = 1
);

INSERT INTO customer (customerID, customerType, name, taxCode, address, bankAccountNumber, bankName, phoneNumber, note)
SELECT 2, 'COMPANY', 'Công ty FPT', NULL, 'Hà Nội', '1030008857', 'Vietcombank', '18001919', NULL
WHERE NOT EXISTS (
    SELECT 1
    FROM customer
    WHERE customerID = 2
);

ALTER TABLE customer AUTO_INCREMENT = 3;
