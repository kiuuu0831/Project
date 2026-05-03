package com.cchc.admin.service;

import com.cchc.admin.dao.AdminServiceDAO;
import com.cchc.admin.dao.AdminTimeslotDAO;
import com.cchc.admin.dao.AdminUserDAO;
import com.cchc.model.Service;
import com.cchc.model.Timeslot;
import com.cchc.model.User;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Parses CSV uploads for batch admin setup (simple comma-separated rows, UTF-8). */
public final class CsvBatchImportService {

    private final AdminUserDAO userDAO = new AdminUserDAO();
    private final AdminServiceDAO serviceDAO = new AdminServiceDAO();
    private final AdminTimeslotDAO timeslotDAO = new AdminTimeslotDAO();

    public static final class ImportResult {
        private int success;
        private int failed;
        private final List<String> errors = new ArrayList<>();

        public void ok() {
            success++;
        }

        public void fail(String lineMsg) {
            failed++;
            if (lineMsg != null && !lineMsg.isBlank()) {
                errors.add(lineMsg);
            }
        }

        public int getSuccess() {
            return success;
        }

        public int getFailed() {
            return failed;
        }

        public List<String> getErrors() {
            return errors;
        }

        public String summary() {
            return "Imported " + success + " row(s), failed " + failed + ".";
        }
    }

    public ImportResult importUsers(InputStream in) throws IOException {
        ImportResult r = new ImportResult();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String header = br.readLine();
            if (header == null) {
                r.fail("Empty file");
                return r;
            }
            String line;
            int lineNo = 1;
            while ((line = br.readLine()) != null) {
                lineNo++;
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                String[] p = splitCsvLine(line);
                if (p.length < 7) {
                    r.fail("Line " + lineNo + ": expected at least 7 columns");
                    continue;
                }
                try {
                    User u = new User();
                    u.setUsername(p[0].trim());
                    u.setPassword(p[1].trim());
                    u.setFullName(p[2].trim());
                    u.setEmail(empty(p[3]) ? null : p[3].trim());
                    u.setPhone(empty(p[4]) ? null : p[4].trim());
                    u.setRole(AdminUserDAO.normalizeRole(p[5].trim()));
                    if (!empty(p[6])) {
                        u.setClinicId(Integer.parseInt(p[6].trim()));
                    } else {
                        u.setClinicId(null);
                    }
                    boolean active = p.length < 8 || parseBool(p[7]);
                    u.setActive(active);

                    String roleUp = u.getRole().toUpperCase();
                    if ("STAFF".equals(roleUp) && u.getClinicId() == null) {
                        r.fail("Line " + lineNo + ": STAFF requires clinicId");
                        continue;
                    }

                    userDAO.insert(u);
                    r.ok();
                } catch (Exception ex) {
                    r.fail("Line " + lineNo + ": " + ex.getMessage());
                }
            }
        }
        return r;
    }

    public ImportResult importServices(InputStream in) throws IOException, SQLException {
        ImportResult r = new ImportResult();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String header = br.readLine();
            if (header == null) {
                r.fail("Empty file");
                return r;
            }
            String line;
            int lineNo = 1;
            while ((line = br.readLine()) != null) {
                lineNo++;
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                String[] p = splitCsvLine(line);
                if (p.length < 5) {
                    r.fail("Line " + lineNo + ": expected clinicId,name,description,durationMinutes,isActive");
                    continue;
                }
                try {
                    Service s = new Service();
                    s.setClinicId(Integer.parseInt(p[0].trim()));
                    s.setName(p[1].trim());
                    s.setDescription(empty(p[2]) ? null : p[2].trim());
                    s.setDurationMinutes(Integer.parseInt(p[3].trim()));
                    s.setActive(parseBool(p[4]));
                    int sid = serviceDAO.insert(s);
                    if (sid > 0) {
                        serviceDAO.ensureQuotaRow(s.getClinicId(), sid);
                    }
                    r.ok();
                } catch (Exception ex) {
                    r.fail("Line " + lineNo + ": " + ex.getMessage());
                }
            }
        }
        return r;
    }

    public ImportResult importTimeslots(InputStream in) throws IOException, SQLException {
        ImportResult r = new ImportResult();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String header = br.readLine();
            if (header == null) {
                r.fail("Empty file");
                return r;
            }
            String line;
            int lineNo = 1;
            while ((line = br.readLine()) != null) {
                lineNo++;
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                String[] p = splitCsvLine(line);
                if (p.length < 8) {
                    r.fail(
                            "Line "
                                    + lineNo
                                    + ": expected clinicId,serviceId,slotDate,startTime,endTime,"
                                    + "maxCapacity,currentBooked,isActive");
                    continue;
                }
                try {
                    Timeslot t = new Timeslot();
                    t.setClinicId(Integer.parseInt(p[0].trim()));
                    t.setServiceId(Integer.parseInt(p[1].trim()));
                    t.setSlotDate(p[2].trim());
                    t.setStartTime(p[3].trim());
                    t.setEndTime(p[4].trim());
                    t.setMaxCapacity(Integer.parseInt(p[5].trim()));
                    t.setCurrentBooked(Integer.parseInt(p[6].trim()));
                    t.setActive(parseBool(p[7]));
                    timeslotDAO.insert(t);
                    r.ok();
                } catch (Exception ex) {
                    r.fail("Line " + lineNo + ": " + ex.getMessage());
                }
            }
        }
        return r;
    }

    /** Split on commas; fields must not contain commas (assignment constraint). */
    private static String[] splitCsvLine(String line) {
        return line.split(",", -1);
    }

    private static boolean empty(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static boolean parseBool(String s) {
        if (s == null) {
            return false;
        }
        String t = s.trim().toLowerCase();
        return "1".equals(t) || "true".equals(t) || "yes".equals(t);
    }
}
