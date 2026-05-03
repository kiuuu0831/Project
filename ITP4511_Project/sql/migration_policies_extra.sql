-- Optional policies for admin UI (run on cchc_admin after main schema).
INSERT INTO policies (policy_key, policy_value, description, updated_by)
VALUES ('walkin_enabled', '1', 'Allow walk-in queue registration (1=yes, 0=no)', 1)
ON DUPLICATE KEY UPDATE policy_value = VALUES(policy_value);
