package com.cchc.patient.controller;

import com.cchc.model.User;
import com.cchc.patient.dao.BookingException;
import com.cchc.patient.dao.PatientBookingDAO;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/patient/PatientBookServlet")
public class PatientBookServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User patient = session == null ? null : (User) session.getAttribute("patientUser");
        if (patient == null) {
            response.sendRedirect(request.getContextPath() + "/patient/login.jsp");
            return;
        }

        String timeslotIdStr = request.getParameter("timeslotId");
        if (timeslotIdStr == null) {
            response.sendRedirect(request.getContextPath() + "/patient/PatientBrowseServlet");
            return;
        }

        try {
            int timeslotId = Integer.parseInt(timeslotIdStr);
            new PatientBookingDAO().bookAppointment(patient.getUserId(), timeslotId);
            session.setAttribute("patientFlash", "Booking confirmed.");
        } catch (BookingException e) {
            session.setAttribute("patientFlashError", e.getMessage());
        } catch (Exception e) {
            session.setAttribute("patientFlashError", "Unable to complete booking.");
        }

        String clinicId = request.getParameter("clinicId");
        String serviceId = request.getParameter("serviceId");
        String date = request.getParameter("date");
        StringBuilder redir = new StringBuilder(request.getContextPath()).append("/patient/PatientBrowseServlet?");
        boolean amp = false;
        if (clinicId != null && !clinicId.isBlank()) {
            redir.append("clinicId=").append(URLEncoder.encode(clinicId.trim(), StandardCharsets.UTF_8));
            amp = true;
        }
        if (serviceId != null && !serviceId.isBlank()) {
            if (amp) {
                redir.append("&");
            }
            redir.append("serviceId=").append(URLEncoder.encode(serviceId.trim(), StandardCharsets.UTF_8));
            amp = true;
        }
        if (date != null && !date.isBlank()) {
            if (amp) {
                redir.append("&");
            }
            redir.append("date=").append(URLEncoder.encode(date.trim(), StandardCharsets.UTF_8));
        }
        response.sendRedirect(redir.toString());
    }
}
