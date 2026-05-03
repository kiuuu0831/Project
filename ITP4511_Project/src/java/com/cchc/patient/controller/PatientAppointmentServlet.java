package com.cchc.patient.controller;

import com.cchc.model.User;
import com.cchc.patient.dao.BookingException;
import com.cchc.patient.dao.PatientBookingDAO;
import com.cchc.patient.dao.PatientCatalogDAO;
import com.cchc.patient.model.PatientAppointmentView;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/patient/PatientAppointmentServlet")
public class PatientAppointmentServlet extends HttpServlet {

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
            PatientBookingDAO bookingDAO = new PatientBookingDAO();
            PatientCatalogDAO cat = new PatientCatalogDAO();
            List<PatientAppointmentView> list = bookingDAO.listAppointments(patient.getUserId());
            LocalDate from = LocalDate.now();
            LocalDate to = LocalDate.now().plusDays(21);
            for (PatientAppointmentView v : list) {
                if (v.isCanModify()) {
                    v.setRescheduleOptions(cat.listBookableSlotsInRange(v.getClinicId(), v.getServiceId(), from, to));
                }
            }
            request.setAttribute("appointments", list);
            request.getRequestDispatcher("/patient/appointments.jsp").forward(request, response);
        } catch (Exception e) {
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

        String action = request.getParameter("action");
        try {
            if ("cancel".equals(action)) {
                String aid = request.getParameter("appointmentId");
                if (aid == null || aid.isBlank()) {
                    session.setAttribute("patientFlashError", "Missing appointment.");
                } else {
                    int id = Integer.parseInt(aid.trim());
                    new PatientBookingDAO().cancelAppointment(patient.getUserId(), id);
                    session.setAttribute("patientFlash", "Appointment cancelled.");
                }
            } else if ("reschedule".equals(action)) {
                String aid = request.getParameter("appointmentId");
                String nid = request.getParameter("newTimeslotId");
                if (aid == null || aid.isBlank() || nid == null || nid.isBlank()) {
                    session.setAttribute("patientFlashError", "Missing appointment or timeslot.");
                } else {
                    int id = Integer.parseInt(aid.trim());
                    int newSlot = Integer.parseInt(nid.trim());
                    new PatientBookingDAO().rescheduleAppointment(patient.getUserId(), id, newSlot);
                    session.setAttribute("patientFlash", "Appointment rescheduled.");
                }
            } else {
                session.setAttribute("patientFlashError", "Invalid request.");
            }
        } catch (BookingException e) {
            session.setAttribute("patientFlashError", e.getMessage());
        } catch (NumberFormatException e) {
            session.setAttribute("patientFlashError", "Invalid request.");
        } catch (Exception e) {
            session.setAttribute("patientFlashError", "Request could not be completed.");
        }
        response.sendRedirect(request.getContextPath() + "/patient/PatientAppointmentServlet");
    }
}
