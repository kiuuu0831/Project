package com.cchc.patient.controller;

import com.cchc.dao.NotificationDAO;
import com.cchc.model.User;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/patient/PatientNotificationServlet")
public class PatientNotificationServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User patient = session == null ? null : (User) session.getAttribute("patientUser");
        if (patient == null) {
            response.sendRedirect(request.getContextPath() + "/patient/login.jsp");
            return;
        }

        NotificationDAO dao = new NotificationDAO();
        request.setAttribute("notifications", dao.findByUserId(patient.getUserId()));
        request.getRequestDispatcher("/patient/notifications.jsp").forward(request, response);
    }
}
