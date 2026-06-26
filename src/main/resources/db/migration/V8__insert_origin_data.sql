INSERT INTO origin (name)
SELECT 'Việt Nam'
WHERE NOT EXISTS (
    SELECT 1
    FROM origin
    WHERE name = 'Việt Nam'
);

INSERT INTO origin (name)
SELECT 'Úc'
WHERE NOT EXISTS (
    SELECT 1
    FROM origin
    WHERE name = 'Úc'
);

INSERT INTO origin (name)
SELECT 'Nhật Bản'
WHERE NOT EXISTS (
    SELECT 1
    FROM origin
    WHERE name = 'Nhật Bản'
);

INSERT INTO origin (name)
SELECT 'Trung Quốc'
WHERE NOT EXISTS (
    SELECT 1
    FROM origin
    WHERE name = 'Trung Quốc'
);

INSERT INTO origin (name)
SELECT 'Ấn Độ'
WHERE NOT EXISTS (
    SELECT 1
    FROM origin
    WHERE name = 'Ấn Độ'
);

INSERT INTO origin (name)
SELECT 'Đức'
WHERE NOT EXISTS (
    SELECT 1
    FROM origin
    WHERE name = 'Đức'
);

INSERT INTO origin (name)
SELECT 'Đan Mạch'
WHERE NOT EXISTS (
    SELECT 1
    FROM origin
    WHERE name = 'Đan Mạch'
);

INSERT INTO origin (name)
SELECT 'Anh'
WHERE NOT EXISTS (
    SELECT 1
    FROM origin
    WHERE name = 'Anh'
);
