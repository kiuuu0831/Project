package com.cchc.admin.controller;

import com.cchc.admin.dao.PolicyDAO;
import com.cchc.admin.util.AuditLogger;
import com.cchc.model.User;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AdminPolicyServlet extends HttpServlet {

    private final PolicyDAO policyDAO = new PolicyDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User admin = (User) request.getSession().getAttribute("adminUser");
        if (admin == null) {
            response.sendRedirect(request.getContextPath() + "/adminLogin.jsp");
            return;
        }

        try {
            request.setAttribute("policies", policyDAO.findAll());
            request.getRequestDispatcher("/admin/policies.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Could not load policies.");
            request.getRequestDispatcher("/admin/policies.jsp").forward(request, response);
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
        try {
            int id = Integer.parseInt(request.getParameter("policyId"));
            String value = request.getParameter("policyValue");
            policyDAO.updateValue(id, value, admin.getUserId());
            AuditLogger.record(request, admin.getUserId(), "POLICY_UPDATE", "POLICY", id, "Updated policy id=" + id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        response.sendRedirect(request.getContextPath() + "/admin/AdminPolicyServlet");
    }
}
