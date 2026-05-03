package com.cchc.controller;

import com.cchc.dao.IncidentDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/** Staff: read-only list of {@code incident_reports}. */
@WebServlet("/IncidentListServlet")
public class IncidentListServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("staffUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        request.setAttribute("incidentReports", new IncidentDAO().findRecent(200));
        request.getRequestDispatcher("/incidentReportsList.jsp").forward(request, response);
    }
}
