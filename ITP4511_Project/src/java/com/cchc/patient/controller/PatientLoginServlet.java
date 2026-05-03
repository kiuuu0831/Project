package com.cchc.patient.controller;

import com.cchc.dao.UserDAO;
import com.cchc.model.User;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/patient/PatientLoginServlet")
public class PatientLoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/patient/login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        UserDAO dao = new UserDAO();
        User user = dao.loginAsPatient(username, password);

        if (user != null && user.isActive()) {
            HttpSession session = request.getSession();
            session.setAttribute("patientUser", user);
            response.sendRedirect(request.getContextPath() + "/patient/PatientDashboardServlet");
        } else {
            request.setAttribute("error", "Invalid patient credentials.");
            request.getRequestDispatcher("/patient/login.jsp").forward(request, response);
        }
    }
}
