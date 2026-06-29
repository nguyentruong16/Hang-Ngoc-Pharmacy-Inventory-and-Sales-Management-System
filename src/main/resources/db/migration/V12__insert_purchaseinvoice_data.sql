INSERT INTO purchaseinvoice (purchaseID, date, supplierID, branchID, employeeID, additionCost, discount, totalAmount, requisitionID, paid, statusID, note)
SELECT 1, '2026-06-26 00:00:00', 1, 1, 3, 5000.00, 0.00, 3405000.00, NULL, 3405000.00, NULL, NULL
WHERE NOT EXISTS (
    SELECT 1
    FROM purchaseinvoice
    WHERE purchaseID = 1
);

INSERT INTO purchaseinvoice (purchaseID, date, supplierID, branchID, employeeID, additionCost, discount, totalAmount, requisitionID, paid, statusID, note)
SELECT 2, '2026-06-25 00:00:00', 2, 1, 3, 10000.00, 7000.00, 5743000.00, NULL, 5743000.00, NULL, NULL
WHERE NOT EXISTS (
    SELECT 1
    FROM purchaseinvoice
    WHERE purchaseID = 2
);

INSERT INTO purchaseinvoice (purchaseID, date, supplierID, branchID, employeeID, additionCost, discount, totalAmount, requisitionID, paid, statusID, note)
SELECT 3, '2026-06-26 00:00:00', 3, 1, 3, 0.00, 50000.00, 7787500.00, NULL, 7787500.00, NULL, NULL
WHERE NOT EXISTS (
    SELECT 1
    FROM purchaseinvoice
    WHERE purchaseID = 3
);

INSERT INTO purchaseinvoice (purchaseID, date, supplierID, branchID, employeeID, additionCost, discount, totalAmount, requisitionID, paid, statusID, note)
SELECT 4, '2026-06-25 00:00:00', 4, 1, 3, 50000.00, 0.00, 5250000.00, NULL, 5250000.00, NULL, NULL
WHERE NOT EXISTS (
    SELECT 1
    FROM purchaseinvoice
    WHERE purchaseID = 4
);

ALTER TABLE purchaseinvoice AUTO_INCREMENT = 5;
