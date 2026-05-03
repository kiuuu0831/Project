package com.cchc.controller;

import com.cchc.dao.IncidentDAO;
import com.cchc.model.IncidentReport;
import com.cchc.model.User;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/IncidentServlet")
public class IncidentServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("staffUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        request.getRequestDispatcher("/incidentReport.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("staffUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        User user = (User) session.getAttribute("staffUser");

        IncidentReport incident = new IncidentReport();
        incident.setClinicName(request.getParameter("clinicName"));
        incident.setServiceName(request.getParameter("serviceName"));
        incident.setIssueType(request.getParameter("issueType"));
        incident.setDescription(request.getParameter("description"));
        incident.setReportedBy(user.getUsername());

        IncidentDAO incidentDAO = new IncidentDAO();
        incidentDAO.addIncident(incident);

        response.sendRedirect(request.getContextPath() + "/StaffDashboardServlet");
    }
}