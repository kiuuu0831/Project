-- Optional test user for Staff login on cchc_admin (ENUM role STAFF).
-- Re-run safe: refreshes password/role if staff1 already exists.
INSERT INTO users (username, password, full_name, email, phone, role, clinic_id, is_active)
VALUES ('staff1', 'staff123', 'Demo Staff', 'staff@cchc.hk', NULL, 'STAFF', 1, 1)
ON DUPLICATE KEY UPDATE
  password = VALUES(password),
  full_name = VALUES(full_name),
  role = VALUES(role),
  clinic_id = VALUES(clinic_id),
  is_active = VALUES(is_active);
