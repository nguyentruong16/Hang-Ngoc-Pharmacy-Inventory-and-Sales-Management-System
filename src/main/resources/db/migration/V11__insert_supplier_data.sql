INSERT INTO supplier (supplierID, name, address, phone, email)
SELECT 1, 'Cung 1', 'Hà Nội', '0958685432', 'abc@gmail.com'
WHERE NOT EXISTS (
    SELECT 1
    FROM supplier
    WHERE supplierID = 1
);

INSERT INTO supplier (supplierID, name, address, phone, email)
SELECT 2, 'Cung 2', 'Hải Phòng', '0123456789', 'def@gmail.com'
WHERE NOT EXISTS (
    SELECT 1
    FROM supplier
    WHERE supplierID = 2
);

INSERT INTO supplier (supplierID, name, address, phone, email)
SELECT 3, 'Cung 3', 'Quảng Ninh', '0987654321', 'ghi@gmail.com'
WHERE NOT EXISTS (
    SELECT 1
    FROM supplier
    WHERE supplierID = 3
);

INSERT INTO supplier (supplierID, name, address, phone, email)
SELECT 4, 'Cung 4', 'Hưng Yên', '0147258369', 'xyz@gmail.com'
WHERE NOT EXISTS (
    SELECT 1
    FROM supplier
    WHERE supplierID = 4
);

ALTER TABLE supplier AUTO_INCREMENT = 5;
