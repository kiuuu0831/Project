package com.cchc.patient.controller;

import com.cchc.dao.UserDAO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/patient/PatientRegisterServlet")
public class PatientRegisterServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/patient/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");

        if (username == null
                || username.isBlank()
                || password == null
                || password.isBlank()
                || fullName == null
                || fullName.isBlank()) {
            request.setAttribute("error", "Username, password and full name are required.");
            request.getRequestDispatcher("/patient/register.jsp").forward(request, response);
            return;
        }

        UserDAO dao = new UserDAO();
        if (dao.isUsernameTaken(username)) {
            request.setAttribute("error", "Username already taken.");
            request.getRequestDispatcher("/patient/register.jsp").forward(request, response);
            return;
        }

        int id = dao.registerPatient(username, password, fullName, email, phone);
        if (id > 0) {
            request.setAttribute("success", "Registration successful. Please sign in.");
            request.getRequestDispatcher("/patient/login.jsp").forward(request, response);
        } else {
            request.setAttribute("error", "Registration failed. Please try again.");
            request.getRequestDispatcher("/patient/register.jsp").forward(request, response);
        }
    }
}
