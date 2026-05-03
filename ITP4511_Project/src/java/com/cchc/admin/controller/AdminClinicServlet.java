package com.cchc.admin.controller;

import com.cchc.admin.dao.AdminClinicDAO;
import com.cchc.admin.util.AuditLogger;
import com.cchc.model.Clinic;
import com.cchc.model.User;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AdminClinicServlet extends HttpServlet {

    private final AdminClinicDAO clinicDAO = new AdminClinicDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            String action = request.getParameter("action");
            if ("edit".equals(action) || "new".equals(action)) {
                if ("edit".equals(action)) {
                    String id = request.getParameter("id");
                    if (id != null) {
                        request.setAttribute("clinic", clinicDAO.findById(Integer.parseInt(id)));
                    }
                } else {
                    request.setAttribute("clinic", new Clinic());
                }
                request.getRequestDispatcher("/admin/clinicForm.jsp").forward(request, response);
                return;
            }
            request.setAttribute("clinics", clinicDAO.findAll());
            request.getRequestDispatcher("/admin/clinics.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Failed to load clinics.");
            request.getRequestDispatcher("/admin/clinics.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User admin = (User) request.getSession().getAttribute("adminUser");
        if (admin == null) {
            response.sendRedirect(request.getContextPath() + "/adminLogin.jsp");
            return;
        }
        String action = request.getParameter("action");

        try {
            if ("save".equals(action)) {
                Clinic c = new Clinic();
                String idStr = request.getParameter("id");
                if (idStr != null && !idStr.isEmpty()) {
                    c.setId(Integer.parseInt(idStr));
                }
                c.setName(request.getParameter("name"));
                c.setAddress(request.getParameter("address"));
                c.setPhone(request.getParameter("phone"));
                c.setActive(request.getParameter("active") != null);

                if (c.getId() <= 0) {
                    clinicDAO.insert(c);
                    AuditLogger.record(request, admin.getUserId(), "CLINIC_CREATE", "CLINIC", null, c.getName());
                } else {
                    clinicDAO.update(c);
                    AuditLogger.record(request, admin.getUserId(), "CLINIC_UPDATE", "CLINIC", c.getId(), c.getName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        response.sendRedirect(request.getContextPath() + "/admin/AdminClinicServlet");
    }
}
