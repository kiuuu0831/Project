package com.cchc.admin.controller;

import com.cchc.admin.dao.AdminClinicDAO;
import com.cchc.admin.dao.AdminUserDAO;
import com.cchc.admin.util.AuditLogger;
import com.cchc.model.Clinic;
import com.cchc.model.User;
import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AdminUserServlet extends HttpServlet {

    private final AdminUserDAO userDAO = new AdminUserDAO();
    private final AdminClinicDAO clinicDAO = new AdminClinicDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        try {
            List<Clinic> clinics = clinicDAO.findAll();
            request.setAttribute("clinics", clinics);

            if ("edit".equals(action) || "new".equals(action)) {
                if ("edit".equals(action)) {
                    String idStr = request.getParameter("id");
                    if (idStr != null) {
                        User u = userDAO.findById(Integer.parseInt(idStr));
                        request.setAttribute("editUser", u);
                    }
                }
                request.getRequestDispatcher("/admin/userForm.jsp").forward(request, response);
                return;
            }

            request.setAttribute("users", userDAO.findAll());
            request.getRequestDispatcher("/admin/users.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Failed to load users.");
            request.getRequestDispatcher("/admin/users.jsp").forward(request, response);
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
            if ("delete".equals(action)) {
                int id = Integer.parseInt(request.getParameter("id"));
                userDAO.softDelete(id);
                AuditLogger.record(request, admin.getUserId(), "USER_DEACTIVATE", "USER", id, "Soft-delete user id=" + id);
                response.sendRedirect(request.getContextPath() + "/admin/AdminUserServlet");
                return;
            }

            if ("save".equals(action)) {
                User u = new User();
                String idStr = request.getParameter("id");
                boolean isNew = idStr == null || idStr.isEmpty();

                if (!isNew) {
                    u.setUserId(Integer.parseInt(idStr));
                }

                u.setUsername(request.getParameter("username"));
                u.setPassword(request.getParameter("password"));
                u.setFullName(request.getParameter("fullName"));
                u.setEmail(request.getParameter("email"));
                u.setPhone(request.getParameter("phone"));
                u.setRole(AdminUserDAO.normalizeRole(request.getParameter("role")));

                String clinicParam = request.getParameter("clinicId");
                if (clinicParam != null && !clinicParam.isEmpty()) {
                    u.setClinicId(Integer.parseInt(clinicParam));
                } else {
                    u.setClinicId(null);
                }

                u.setActive(request.getParameter("active") != null);

                String roleUpper = u.getRole().toUpperCase();
                if ("STAFF".equals(roleUpper) && u.getClinicId() == null) {
                    request.setAttribute("error", "Staff accounts must have a clinic assigned.");
                    request.setAttribute("clinics", clinicDAO.findAll());
                    request.setAttribute("editUser", u);
                    request.getRequestDispatcher("/admin/userForm.jsp").forward(request, response);
                    return;
                }

                if ("STAFF".equals(roleUpper) || "ADMIN".equals(roleUpper)) {
                    // clinic optional for admin
                }

                if (isNew) {
                    if (u.getPassword() == null || u.getPassword().isBlank()) {
                        request.setAttribute("error", "Password is required for new users.");
                        request.setAttribute("clinics", clinicDAO.findAll());
                        request.setAttribute("editUser", u);
                        request.getRequestDispatcher("/admin/userForm.jsp").forward(request, response);
                        return;
                    }
                    int newId = userDAO.insert(u);
                    AuditLogger.record(request, admin.getUserId(), "USER_CREATE", "USER", newId, "Created " + u.getUsername());
                } else {
                    boolean changePw = u.getPassword() != null && !u.getPassword().isBlank();
                    userDAO.update(u, changePw);
                    AuditLogger.record(request, admin.getUserId(), "USER_UPDATE", "USER", u.getUserId(), "Updated " + u.getUsername());
                }

                response.sendRedirect(request.getContextPath() + "/admin/AdminUserServlet");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/admin/AdminUserServlet");
    }
}
