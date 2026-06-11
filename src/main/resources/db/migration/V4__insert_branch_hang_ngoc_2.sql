INSERT INTO status (statusID, name)
SELECT next_status.next_id, 'Đang hoạt động'
FROM (
    SELECT COALESCE(MAX(statusID), 0) + 1 AS next_id
    FROM status
) AS next_status
WHERE NOT EXISTS (
    SELECT 1
    FROM status
    WHERE name = 'Đang hoạt động'
);

INSERT INTO branch (branchID, name, address, statusID)
SELECT next_branch.next_id,
       'Hằng Ngọc 2',
       '146 Tuệ Tĩnh, Uông Bí, Quảng Ninh',
       (
           SELECT statusID
           FROM status
           WHERE name = 'Đang hoạt động'
           ORDER BY statusID
           LIMIT 1
       )
FROM (
    SELECT COALESCE(MAX(branchID), 0) + 1 AS next_id
    FROM branch
) AS next_branch
WHERE NOT EXISTS (
    SELECT 1
    FROM branch
    WHERE name = 'Hằng Ngọc 2'
);
