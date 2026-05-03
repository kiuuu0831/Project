package com.cchc.admin.controller;

import com.cchc.admin.dao.AdminReportingDAO;
import com.cchc.model.Appointment;
import com.cchc.model.User;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AdminReportServlet extends HttpServlet {

    private final AdminReportingDAO reportingDAO = new AdminReportingDAO();
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User admin = (User) request.getSession().getAttribute("adminUser");
        if (admin == null) {
            response.sendRedirect(request.getContextPath() + "/adminLogin.jsp");
            return;
        }

        String df = request.getParameter("dateFrom");
        String dt = request.getParameter("dateTo");
        String status = request.getParameter("status");

        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(30);
        try {
            if (df != null && !df.isBlank()) {
                from = LocalDate.parse(df, ISO);
            }
            if (dt != null && !dt.isBlank()) {
                to = LocalDate.parse(dt, ISO);
            }
        } catch (Exception ignored) {
            // keep defaults
        }

        String fromStr = from.format(ISO);
        String toStr = to.format(ISO);

        request.setAttribute("dateFrom", fromStr);
        request.setAttribute("dateTo", toStr);
        request.setAttribute("statusFilter", status != null ? status : "ALL");

        try {
            List<Appointment> rows = reportingDAO.searchAppointments(fromStr, toStr, status);
            request.setAttribute("appointments", rows);

            Map<String, Integer> byStatus = reportingDAO.countAppointmentsByStatus(fromStr, toStr);
            request.setAttribute("statusCounts", byStatus);

            int total = reportingDAO.countTotalAppointments(fromStr, toStr);
            int noShow = reportingDAO.countNoShow(fromStr, toStr);
            request.setAttribute("totalAppts", total);
            request.setAttribute("noShowCount", noShow);
            double nsRate = total > 0 ? (100.0 * noShow / total) : 0;
            request.setAttribute("noShowRate", nsRate);

            int[] util = reportingDAO.sumTimeslotBookedAndCapacity(fromStr, toStr);
            int booked = util[0];
            int cap = util[1];
            request.setAttribute("slotBookedSum", booked);
            request.setAttribute("slotCapacitySum", cap);
            int free = Math.max(0, cap - booked);
            request.setAttribute("slotFreeSum", free);
            double utilPct = cap > 0 ? (100.0 * booked / cap) : 0;
            request.setAttribute("utilizationPercent", utilPct);

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("reportError", "Report query failed.");
        }

        request.getRequestDispatcher("/admin/reports.jsp").forward(request, response);
    }
}
