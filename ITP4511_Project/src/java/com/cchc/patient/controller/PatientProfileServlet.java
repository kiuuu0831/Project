package com.cchc.patient.controller;

import com.cchc.admin.dao.AdminUserDAO;
import com.cchc.dao.UserDAO;
import com.cchc.model.User;
import java.io.IOException;
import java.sql.SQLException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/patient/PatientProfileServlet")
public class PatientProfileServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User patient = session == null ? null : (User) session.getAttribute("patientUser");
        if (patient == null) {
            response.sendRedirect(request.getContextPath() + "/patient/login.jsp");
            return;
        }

        try {
            User fresh = new AdminUserDAO().findById(patient.getUserId());
            if (fresh != null) {
                session.setAttribute("patientUser", fresh);
            }
            request.setAttribute("profileUser", fresh != null ? fresh : patient);
            request.getRequestDispatcher("/patient/profile.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User patient = session == null ? null : (User) session.getAttribute("patientUser");
        if (patient == null) {
            response.sendRedirect(request.getContextPath() + "/patient/login.jsp");
            return;
        }

        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String currentPassword = request.getParameter("currentPassword");
        String newPassword = request.getParameter("newPassword");

        UserDAO dao = new UserDAO();
        try {
            boolean touched = false;
            if (fullName != null && !fullName.isBlank()) {
                dao.updatePatientProfile(patient.getUserId(), fullName, email, phone);
                touched = true;
            }

            if (newPassword != null && !newPassword.isBlank()) {
                if (currentPassword == null
                        || dao.loginAsPatient(patient.getUsername(), currentPassword) == null) {
                    session.setAttribute("patientFlashError", "Current password is incorrect.");
                    response.sendRedirect(request.getContextPath() + "/patient/PatientProfileServlet");
                    return;
                }
                dao.updatePasswordForPatient(patient.getUserId(), newPassword);
                touched = true;
            }

            if (touched) {
                User fresh = new AdminUserDAO().findById(patient.getUserId());
                if (fresh != null) {
                    session.setAttribute("patientUser", fresh);
                }
                session.setAttribute("patientFlash", "Profile updated.");
            } else {
                session.setAttribute("patientFlashError", "Nothing to update. Full name is required.");
            }
        } catch (SQLException e) {
            session.setAttribute("patientFlashError", "Update failed.");
        }
        response.sendRedirect(request.getContextPath() + "/patient/PatientProfileServlet");
    }
}
