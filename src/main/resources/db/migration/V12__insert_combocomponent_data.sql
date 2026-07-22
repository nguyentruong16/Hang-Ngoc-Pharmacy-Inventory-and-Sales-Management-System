INSERT INTO combocomponent (comboComponentID, comboID, componentProductID, componentUnitID, quantity, note)
SELECT 1, 14, 11, 19, 1.0000, NULL
WHERE NOT EXISTS (
    SELECT 1
    FROM combocomponent
    WHERE comboComponentID = 1
);

INSERT INTO combocomponent (comboComponentID, comboID, componentProductID, componentUnitID, quantity, note)
SELECT 2, 14, 12, 20, 1.0000, NULL
WHERE NOT EXISTS (
    SELECT 1
    FROM combocomponent
    WHERE comboComponentID = 2
);

INSERT INTO combocomponent (comboComponentID, comboID, componentProductID, componentUnitID, quantity, note)
SELECT 3, 14, 13, 21, 1.0000, NULL
WHERE NOT EXISTS (
    SELECT 1
    FROM combocomponent
    WHERE comboComponentID = 3
);

ALTER TABLE combocomponent AUTO_INCREMENT = 4;
