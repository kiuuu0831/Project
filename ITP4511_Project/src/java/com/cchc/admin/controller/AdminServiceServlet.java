package com.cchc.admin.controller;

import com.cchc.admin.dao.AdminClinicDAO;
import com.cchc.admin.dao.AdminQuotaDAO;
import com.cchc.admin.dao.AdminServiceDAO;
import com.cchc.admin.util.AuditLogger;
import com.cchc.model.Clinic;
import com.cchc.model.Service;
import com.cchc.model.User;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class AdminServiceServlet extends HttpServlet {

    private final AdminServiceDAO serviceDAO = new AdminServiceDAO();
    private final AdminQuotaDAO quotaDAO = new AdminQuotaDAO();
    private final AdminClinicDAO clinicDAO = new AdminClinicDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            String action = request.getParameter("action");
            serviceDAO.ensureQuotaRowsForAllServices();

            List<Clinic> clinics = clinicDAO.findAll();
            request.setAttribute("clinics", clinics);
            request.setAttribute("services", serviceDAO.findAllWithClinic());

            String clinicIdParam = request.getParameter("clinicId");
            Integer selectedClinicId = null;
            if (clinicIdParam != null && !clinicIdParam.isBlank()) {
                try {
                    int cid = Integer.parseInt(clinicIdParam.trim());
                    selectedClinicId = Integer.valueOf(cid);
                    request.setAttribute("quotas", quotaDAO.findRowsByClinicId(cid));
                } catch (NumberFormatException nfe) {
                    request.setAttribute("quotas", quotaDAO.findAllRows());
                }
            } else {
                request.setAttribute("quotas", quotaDAO.findAllRows());
            }
            request.setAttribute("selectedClinicId", selectedClinicId);

            List<Clinic> quotaFilterClinics = clinicDAO.findActiveClinicsWithQuotasOrderByName();
            if (selectedClinicId != null) {
                int sid = selectedClinicId.intValue();
                boolean found = false;
                for (Clinic x : quotaFilterClinics) {
                    if (x.getId() == sid) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    Clinic extra = clinicDAO.findById(sid);
                    if (extra != null && extra.isActive()) {
                        quotaFilterClinics.add(extra);
                        quotaFilterClinics.sort(Comparator.comparing(Clinic::getName, String.CASE_INSENSITIVE_ORDER));
                    }
                }
            }
            request.setAttribute("quotaFilterClinics", quotaFilterClinics);

            if ("edit".equals(action) || "new".equals(action)) {
                if ("edit".equals(action)) {
                    String id = request.getParameter("id");
                    if (id != null) {
                        request.setAttribute("service", serviceDAO.findById(Integer.parseInt(id)));
                    }
                } else {
                    request.setAttribute("service", new Service());
                }
                request.getRequestDispatcher("/admin/serviceForm.jsp").forward(request, response);
                return;
            }

            request.getRequestDispatcher("/admin/services.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Failed to load services.");
            try {
                request.getRequestDispatcher("/admin/services.jsp").forward(request, response);
            } catch (Exception ex) {
                throw new ServletException(ex);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User admin = session != null ? (User) session.getAttribute("adminUser") : null;
        if (admin == null) {
            response.sendRedirect(request.getContextPath() + "/adminLogin.jsp");
            return;
        }

        String action = request.getParameter("action");
        String redirectBase = request.getContextPath() + "/admin/AdminServiceServlet";
        String filterClinicId = request.getParameter("filterClinicId");

        try {
            if ("saveService".equals(action)) {
                Service s = new Service();
                String idStr = request.getParameter("id");
                if (idStr != null && !idStr.isEmpty()) {
                    s.setId(Integer.parseInt(idStr));
                }
                s.setClinicId(Integer.parseInt(request.getParameter("clinicId")));
                s.setName(request.getParameter("name"));
                s.setDescription(request.getParameter("description"));
                s.setDurationMinutes(Integer.parseInt(request.getParameter("durationMinutes")));
                s.setActive(request.getParameter("active") != null);

                if (s.getId() <= 0) {
                    int sid = serviceDAO.insert(s);
                    serviceDAO.ensureQuotaRow(s.getClinicId(), sid);
                    AuditLogger.record(request, admin.getUserId(), "SERVICE_CREATE", "SERVICE", sid, s.getName());
                } else {
                    serviceDAO.update(s);
                    AuditLogger.record(request, admin.getUserId(), "SERVICE_UPDATE", "SERVICE", s.getId(), s.getName());
                }
                session.setAttribute("adminFlashSuccess", "Service saved.");
            } else if ("saveQuota".equals(action)) {
                AdminQuotaDAO.ServiceQuotaRow r = new AdminQuotaDAO.ServiceQuotaRow();
                r.setId(Integer.parseInt(request.getParameter("quotaId")));
                r.setMaxPerDay(Integer.parseInt(request.getParameter("maxPerDay")));
                r.setMaxPerTimeslot(Integer.parseInt(request.getParameter("maxPerTimeslot")));
                r.setCancellationCutoffHours(Integer.parseInt(request.getParameter("cancellationCutoffHours")));
                r.setMaxActiveBookingsPerPatient(Integer.parseInt(request.getParameter("maxActiveBookingsPerPatient")));
                quotaDAO.update(r);
                AuditLogger.record(request, admin.getUserId(), "QUOTA_UPDATE", "SERVICE_QUOTA", r.getId(), "Quota row updated");
                session.setAttribute("adminFlashSuccess", "Quota saved.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            request.getSession().setAttribute("adminFlashError", "Save failed: " + msg);
        }
        String redirectUrl = redirectBase;
        if (filterClinicId != null && !filterClinicId.isBlank()) {
            try {
                int cid = Integer.parseInt(filterClinicId.trim());
                redirectUrl = redirectBase + "?clinicId=" + cid;
            } catch (NumberFormatException ignored) {
                // stay on redirectBase
            }
        }
        response.sendRedirect(redirectUrl);
    }
}
