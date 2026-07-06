INSERT INTO type (typeID, sortType, name)
SELECT 1, 'thuốc', 'Thuốc kê đơn'
WHERE NOT EXISTS (
    SELECT 1
    FROM type
    WHERE sortType = 'thuốc'
      AND name = 'Thuốc kê đơn'
);

INSERT INTO type (typeID, sortType, name)
SELECT 2, 'thuốc', 'Thuốc không kê đơn'
WHERE NOT EXISTS (
    SELECT 1
    FROM type
    WHERE sortType = 'thuốc'
      AND name = 'Thuốc không kê đơn'
);

INSERT INTO type (typeID, sortType, name)
SELECT 3, 'thuốc', 'Thuốc chưa phân loại'
WHERE NOT EXISTS (
    SELECT 1
    FROM type
    WHERE sortType = 'thuốc'
      AND name = 'Thuốc chưa phân loại'
);

INSERT INTO type (typeID, sortType, name)
SELECT 4, 'hàng hóa', 'Thực phẩm chức năng'
WHERE NOT EXISTS (
    SELECT 1
    FROM type
    WHERE sortType = 'hàng hóa'
      AND name = 'Thực phẩm chức năng'
);

INSERT INTO type (typeID, sortType, name)
SELECT 5, 'hàng hóa', 'Thực phẩm dinh dưỡng'
WHERE NOT EXISTS (
    SELECT 1
    FROM type
    WHERE sortType = 'hàng hóa'
      AND name = 'Thực phẩm dinh dưỡng'
);

INSERT INTO type (typeID, sortType, name)
SELECT 6, 'hàng hóa', 'Mỹ phẩm'
WHERE NOT EXISTS (
    SELECT 1
    FROM type
    WHERE sortType = 'hàng hóa'
      AND name = 'Mỹ phẩm'
);

INSERT INTO type (typeID, sortType, name)
SELECT 7, 'thiết bị y tế', 'Thiết bị y tế (máy)'
WHERE NOT EXISTS (
    SELECT 1
    FROM type
    WHERE sortType = 'thiết bị y tế'
      AND name = 'Thiết bị y tế (máy)'
);

INSERT INTO type (typeID, sortType, name)
SELECT 8, 'thiết bị y tế', 'Thiết bị y tế (có hạn)'
WHERE NOT EXISTS (
    SELECT 1
    FROM type
    WHERE sortType = 'thiết bị y tế'
      AND name = 'Thiết bị y tế (có hạn)'
);

INSERT INTO type (typeID, sortType, name)
SELECT 9, 'thiết bị y tế', 'Thiết bị y tế (không hạn)'
WHERE NOT EXISTS (
    SELECT 1
    FROM type
    WHERE sortType = 'thiết bị y tế'
      AND name = 'Thiết bị y tế (không hạn)'
);

INSERT INTO type (typeID, sortType, name)
SELECT 10, 'combo', 'Combo'
WHERE NOT EXISTS (
    SELECT 1
    FROM type
    WHERE sortType = 'combo'
      AND name = 'Combo'
);

ALTER TABLE type AUTO_INCREMENT = 11;
