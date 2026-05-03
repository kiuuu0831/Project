package com.cchc.admin.controller;

import com.cchc.admin.dao.AdminDashboardDAO;
import com.cchc.admin.model.DashboardStats;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AdminDashboardServlet extends HttpServlet {

    private final AdminDashboardDAO dashboardDAO = new AdminDashboardDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            DashboardStats stats = dashboardDAO.fetchDashboardStats();
            request.setAttribute("stats", stats);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("dashError", "Could not load dashboard statistics.");
        }

        request.getRequestDispatcher("/admin/dashboard.jsp").forward(request, response);
    }
}
