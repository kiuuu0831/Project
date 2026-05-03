-- Run once on cchc_admin (not in cchc_admin.sql dump). Required for Incident Report feature.
CREATE TABLE IF NOT EXISTS incident_reports (
  id INT NOT NULL AUTO_INCREMENT,
  clinic_name VARCHAR(255) NOT NULL,
  service_name VARCHAR(255) NOT NULL,
  issue_type VARCHAR(100) NOT NULL,
  description TEXT,
  reported_by VARCHAR(50) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
