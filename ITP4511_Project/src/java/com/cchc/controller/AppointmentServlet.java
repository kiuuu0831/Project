package com.cchc.controller;

import com.cchc.admin.util.AuditLogger;
import com.cchc.dao.AppointmentDAO;
import com.cchc.model.User;
import com.cchc.util.AppointmentStatusUtil;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/AppointmentServlet")
public class AppointmentServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("staffUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        AppointmentDAO appointmentDAO = new AppointmentDAO();
        request.setAttribute("appointments", appointmentDAO.getAllAppointments());

        request.getRequestDispatcher("/appointmentList.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("staffUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        String idParam = request.getParameter("appointmentId");
        int appointmentId;
        try {
            appointmentId = Integer.parseInt(idParam);
        } catch (NumberFormatException ex) {
            response.sendRedirect(request.getContextPath() + "/AppointmentServlet");
            return;
        }

        String status = AppointmentStatusUtil.toCanonical(request.getParameter("status"));
        if (status == null) {
            response.sendRedirect(request.getContextPath() + "/AppointmentServlet");
            return;
        }

        AppointmentDAO appointmentDAO = new AppointmentDAO();
        appointmentDAO.updateStatus(appointmentId, status);

        User staff = (User) session.getAttribute("staffUser");
        if (staff != null) {
            AuditLogger.record(
                    request,
                    staff.getUserId(),
                    "APPOINTMENT_STATUS_CHANGE",
                    "APPOINTMENT",
                    appointmentId,
                    "Status set to: " + status);
        }

        response.sendRedirect(request.getContextPath() + "/AppointmentServlet");
    }
}