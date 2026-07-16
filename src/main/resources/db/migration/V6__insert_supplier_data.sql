INSERT INTO supplier (supplierID, name, address, phone, email, taxCode)
SELECT 1, 'Cung 1', 'Hà Nội', '0958685432', 'abc@gmail.com', '1234'
WHERE NOT EXISTS (
    SELECT 1
    FROM supplier
    WHERE supplierID = 1
);

INSERT INTO supplier (supplierID, name, address, phone, email, taxCode)
SELECT 2, 'Cung 2', 'Hải Phòng', '0123456789', 'def@gmail.com', '2345'
WHERE NOT EXISTS (
    SELECT 1
    FROM supplier
    WHERE supplierID = 2
);

INSERT INTO supplier (supplierID, name, address, phone, email, taxCode)
SELECT 3, 'Cung 3', 'Quảng Ninh', '0987654321', 'ghi@gmail.com', '3456'
WHERE NOT EXISTS (
    SELECT 1
    FROM supplier
    WHERE supplierID = 3
);

INSERT INTO supplier (supplierID, name, address, phone, email, taxCode)
SELECT 4, 'Cung 4', 'Hưng Yên', '0147258369', 'xyz@gmail.com', '4567'
WHERE NOT EXISTS (
    SELECT 1
    FROM supplier
    WHERE supplierID = 4
);

ALTER TABLE supplier AUTO_INCREMENT = 5;
