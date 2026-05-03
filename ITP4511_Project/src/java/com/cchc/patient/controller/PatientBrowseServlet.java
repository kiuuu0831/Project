package com.cchc.patient.controller;

import com.cchc.model.User;
import com.cchc.patient.dao.PatientCatalogDAO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/patient/PatientBrowseServlet")
public class PatientBrowseServlet extends HttpServlet {

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
            request.setAttribute("clinics", cat.listClinicsEligibleForBooking());

            String clinicIdParam = request.getParameter("clinicId");
            String serviceIdParam = request.getParameter("serviceId");
            String date = request.getParameter("date");

            if (clinicIdParam != null && !clinicIdParam.isBlank()) {
                try {
                    int cid = Integer.parseInt(clinicIdParam.trim());
                    request.setAttribute("selectedClinicId", cid);
                    request.setAttribute("services", cat.listActiveServicesForClinic(cid));

                    if (serviceIdParam != null && !serviceIdParam.isBlank()) {
                        int sid = Integer.parseInt(serviceIdParam.trim());
                        request.setAttribute("selectedServiceId", sid);
                        if (date != null && !date.isBlank()) {
                            request.setAttribute("selectedDate", date);
                            request.setAttribute("slots", cat.listBookableSlots(cid, sid, date));
                        }
                    }
                } catch (NumberFormatException ignore) {
                    // Ignore bad query params (avoid 500); show clinic list only.
                }
            }

            request.getRequestDispatcher("/patient/browse.jsp").forward(request, response);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
