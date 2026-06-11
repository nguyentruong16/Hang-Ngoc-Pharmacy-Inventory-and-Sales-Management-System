INSERT INTO branch (branchID, name, address, statusID)
SELECT next_branch.next_id, 'Hang Ngoc Pharmacy - Main Branch', 'Main branch address', NULL
FROM (
    SELECT COALESCE(MAX(branchID), 0) + 1 AS next_id
    FROM branch
) AS next_branch
WHERE NOT EXISTS (
    SELECT 1
    FROM branch
    WHERE name = 'Hang Ngoc Pharmacy - Main Branch'
);
