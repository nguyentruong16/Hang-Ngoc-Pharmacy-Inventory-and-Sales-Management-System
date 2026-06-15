INSERT INTO status (statusID, name)
SELECT next_status.next_id, 'Ngừng hoạt động'
FROM (
    SELECT COALESCE(MAX(statusID), 0) + 1 AS next_id
    FROM status
) AS next_status
WHERE NOT EXISTS (
    SELECT 1
    FROM status
    WHERE name = 'Ngừng hoạt động'
);

INSERT INTO status (statusID, name)
SELECT next_status.next_id, 'Chờ xử lý'
FROM (
    SELECT COALESCE(MAX(statusID), 0) + 1 AS next_id
    FROM status
) AS next_status
WHERE NOT EXISTS (
    SELECT 1
    FROM status
    WHERE name = 'Chờ xử lý'
);

INSERT INTO status (statusID, name)
SELECT next_status.next_id, 'Bản nháp'
FROM (
    SELECT COALESCE(MAX(statusID), 0) + 1 AS next_id
    FROM status
) AS next_status
WHERE NOT EXISTS (
    SELECT 1
    FROM status
    WHERE name = 'Bản nháp'
);

INSERT INTO status (statusID, name)
SELECT next_status.next_id, 'Đã phê duyệt'
FROM (
    SELECT COALESCE(MAX(statusID), 0) + 1 AS next_id
    FROM status
) AS next_status
WHERE NOT EXISTS (
    SELECT 1
    FROM status
    WHERE name = 'Đã phê duyệt'
);

INSERT INTO status (statusID, name)
SELECT next_status.next_id, 'Đã xác nhận'
FROM (
    SELECT COALESCE(MAX(statusID), 0) + 1 AS next_id
    FROM status
) AS next_status
WHERE NOT EXISTS (
    SELECT 1
    FROM status
    WHERE name = 'Đã xác nhận'
);

INSERT INTO status (statusID, name)
SELECT next_status.next_id, 'Từ chối'
FROM (
    SELECT COALESCE(MAX(statusID), 0) + 1 AS next_id
    FROM status
) AS next_status
WHERE NOT EXISTS (
    SELECT 1
    FROM status
    WHERE name = 'Từ chối'
);
