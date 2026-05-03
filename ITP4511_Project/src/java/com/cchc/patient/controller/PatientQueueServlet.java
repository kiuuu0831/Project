package com.cchc.patient.controller;

import com.cchc.model.User;
import com.cchc.patient.dao.BookingException;
import com.cchc.patient.dao.PatientCatalogDAO;
import com.cchc.patient.dao.PatientQueueDAO;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/patient/PatientQueueServlet")
public class PatientQueueServlet extends HttpServlet {

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
            PatientCatalogDAO cat = new PatientCatalogDAO();
            request.setAttribute("clinics", cat.listActiveClinicsWithActiveServices());
            request.setAttribute("allServices", cat.listAllActiveServicesAtActiveClinics());
            request.setAttribute(
                    "myQueueTickets",
                    new PatientQueueDAO().listMyTicketsRecent(patient.getUserId(), 20));
            request.getRequestDispatcher("/patient/queue.jsp").forward(request, response);
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

        try {
            int clinicId = Integer.parseInt(request.getParameter("clinicId"));
            int serviceId = Integer.parseInt(request.getParameter("serviceId"));
            new PatientQueueDAO().joinQueueToday(patient.getUserId(), clinicId, serviceId);
            session.setAttribute("patientFlash", "Joined walk-in queue.");
        } catch (BookingException e) {
            session.setAttribute("patientFlashError", e.getMessage());
        } catch (Exception e) {
            session.setAttribute("patientFlashError", "Could not join queue.");
        }
        response.sendRedirect(request.getContextPath() + "/patient/PatientQueueServlet");
    }
}
