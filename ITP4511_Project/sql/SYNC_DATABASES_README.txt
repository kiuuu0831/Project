Single authoritative DB (recommended): use only cchc_admin so primary keys (users.id, clinics.id, ...) stay consistent everywhere.

If you must keep a legacy cchc_clinic copy:
- IDs will diverge unless you use one-way mysqldump restore from cchc_admin to cchc_clinic, or MySQL replication (advanced).
- Application-level "dual write" to two schemas is error-prone for coursework.

Practical approach for the team: export one golden schema (your cchc_admin.sql), everyone imports it, no second DB for runtime.
