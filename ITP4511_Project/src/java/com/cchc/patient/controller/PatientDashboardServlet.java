package com.cchc.patient.controller;

import com.cchc.model.User;
import com.cchc.patient.dao.PatientBookingDAO;
import com.cchc.patient.dao.PatientQueueDAO;
import com.cchc.patient.model.PatientAppointmentView;
import com.cchc.patient.model.PatientDashboardStats;
import com.cchc.patient.model.PatientQueueTicketView;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/patient/PatientDashboardServlet")
public class PatientDashboardServlet extends HttpServlet {

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
            PatientQueueDAO queueDAO = new PatientQueueDAO();

            List<PatientAppointmentView> all = bookingDAO.listAppointments(patient.getUserId());
            List<PatientAppointmentView> upcoming = new ArrayList<>();
            for (PatientAppointmentView v : all) {
                if ("CANCELLED".equalsIgnoreCase(v.getStatus())) {
                    continue;
                }
                if (isUpcoming(v)) {
                    upcoming.add(v);
                }
                if (upcoming.size() >= 5) {
                    break;
                }
            }

            Map<String, Integer> byMonth = bookingDAO.appointmentCountsByMonth(patient.getUserId(), 6);
            PatientDashboardStats stats = new PatientDashboardStats();
            LocalDate m = LocalDate.now().minusMonths(5).withDayOfMonth(1);
            String[] labels = new String[6];
            int[] counts = new int[6];
            DateTimeFormatter keyFmt = DateTimeFormatter.ofPattern("yyyy-MM");
            DateTimeFormatter labelFmt = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);
            for (int i = 0; i < 6; i++) {
                String key = m.format(keyFmt);
                labels[i] = m.format(labelFmt);
                counts[i] = byMonth.getOrDefault(key, 0);
                m = m.plusMonths(1);
            }
            stats.setMonthLabels(labels);
            stats.setMonthCounts(counts);
            stats.setUpcomingCount(bookingDAO.countUpcomingAppointments(patient.getUserId()));
            stats.setActiveQueueCount(queueDAO.countActiveQueueToday(patient.getUserId()));

            List<PatientQueueTicketView> queues = queueDAO.listMyTicketsRecent(patient.getUserId(), 8);

            request.setAttribute("upcomingAppointments", upcoming);
            request.setAttribute("queueTickets", queues);
            request.setAttribute("dashStats", stats);
            request.getRequestDispatcher("/patient/dashboard.jsp").forward(request, response);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private static boolean isUpcoming(PatientAppointmentView v) {
        try {
            LocalDateTime appt =
                    LocalDateTime.parse(v.getAppointmentDate() + "T" + v.getAppointmentTime().trim());
            return !appt.isBefore(LocalDateTime.now());
        } catch (Exception e) {
            return false;
        }
    }
}
