package com.cchc.admin.controller;

import com.cchc.admin.dao.AuditLogDAO;
import com.cchc.model.User;
import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AdminAuditServlet extends HttpServlet {

    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User admin = (User) request.getSession().getAttribute("adminUser");
        if (admin == null) {
            response.sendRedirect(request.getContextPath() + "/adminLogin.jsp");
            return;
        }

        String user = request.getParameter("username");
        String auditAction = request.getParameter("auditAction");
        String dateFrom = request.getParameter("dateFrom");
        String dateTo = request.getParameter("dateTo");

        request.setAttribute("filterUsername", user);
        request.setAttribute("filterAuditAction", auditAction);
        request.setAttribute("filterDateFrom", dateFrom);
        request.setAttribute("filterDateTo", dateTo);

        try {
            List<AuditLogDAO.AuditEntry> logs =
                    auditLogDAO.search(user, auditAction, dateFrom, dateTo);
            request.setAttribute("auditLogs", logs);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Could not load audit logs.");
        }

        request.getRequestDispatcher("/admin/audit.jsp").forward(request, response);
    }
}
