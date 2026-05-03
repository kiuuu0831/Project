package com.cchc.controller;

import com.cchc.dao.AppointmentDAO;
import com.cchc.dao.NotificationDAO;
import com.cchc.dao.QueueDAO;
import com.cchc.model.User;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/StaffDashboardServlet")
public class StaffDashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("staffUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        User user = (User) session.getAttribute("staffUser");

        AppointmentDAO appointmentDAO = new AppointmentDAO();
        QueueDAO queueDAO = new QueueDAO();
        NotificationDAO notificationDAO = new NotificationDAO();

        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        request.setAttribute("appointmentCount", appointmentDAO.countByAppointmentDate(today));
        request.setAttribute("queueCount", queueDAO.countJoinedToday());
        request.setAttribute("notifications", notificationDAO.findByUserId(user.getUserId()));

        request.getRequestDispatcher("/staffDashboard.jsp").forward(request, response);
    }
}