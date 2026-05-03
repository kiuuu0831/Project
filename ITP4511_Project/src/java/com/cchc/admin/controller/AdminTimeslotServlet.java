package com.cchc.admin.controller;

import com.cchc.admin.dao.AdminClinicDAO;
import com.cchc.admin.dao.AdminServiceDAO;
import com.cchc.admin.dao.AdminTimeslotDAO;
import com.cchc.admin.util.AuditLogger;
import com.cchc.model.Clinic;
import com.cchc.model.Service;
import com.cchc.model.Timeslot;
import com.cchc.model.User;
import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class AdminTimeslotServlet extends HttpServlet {

    private final AdminTimeslotDAO timeslotDAO = new AdminTimeslotDAO();
    private final AdminClinicDAO clinicDAO = new AdminClinicDAO();
    private final AdminServiceDAO serviceDAO = new AdminServiceDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            request.setAttribute("timeslots", timeslotDAO.findRecent(80));
            List<Clinic> clinics = clinicDAO.findAll();
            List<Service> services = serviceDAO.findAllWithClinic();
            request.setAttribute("clinics", clinics);
            request.setAttribute("allServices", services);

            String action = request.getParameter("action");
            if ("edit".equals(action) || "new".equals(action)) {
                HttpSession sess = request.getSession(false);
                if (sess != null) {
                    Object err = sess.getAttribute("adminTimeslotError");
                    if (err != null) {
                        request.setAttribute("timeslotError", err);
                        sess.removeAttribute("adminTimeslotError");
                    }
                }
                if ("edit".equals(action)) {
                    String id = request.getParameter("id");
                    if (id != null) {
                        request.setAttribute("slot", timeslotDAO.findById(Integer.parseInt(id)));
                    }
                } else {
                    request.setAttribute("slot", new Timeslot());
                }
                request.getRequestDispatcher("/admin/timeslotForm.jsp").forward(request, response);
                return;
            }

            HttpSession sess = request.getSession(false);
            if (sess != null) {
                Object err = sess.getAttribute("adminTimeslotError");
                if (err != null) {
                    request.setAttribute("timeslotError", err);
                    sess.removeAttribute("adminTimeslotError");
                }
            }
            request.getRequestDispatcher("/admin/timeslots.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Failed to load timeslots.");
            request.getRequestDispatcher("/admin/timeslots.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User admin = (User) request.getSession().getAttribute("adminUser");
        if (admin == null) {
            response.sendRedirect(request.getContextPath() + "/adminLogin.jsp");
            return;
        }
        try {
            Timeslot t = new Timeslot();
            String idStr = request.getParameter("id");
            if (idStr != null && !idStr.isEmpty()) {
                t.setId(Integer.parseInt(idStr));
            }
            t.setClinicId(Integer.parseInt(request.getParameter("clinicId")));
            t.setServiceId(Integer.parseInt(request.getParameter("serviceId")));
            t.setSlotDate(request.getParameter("slotDate"));
            t.setStartTime(request.getParameter("startTime"));
            t.setEndTime(request.getParameter("endTime"));
            t.setMaxCapacity(Integer.parseInt(request.getParameter("maxCapacity")));
            String cb = request.getParameter("currentBooked");
            t.setCurrentBooked(cb != null && !cb.isEmpty() ? Integer.parseInt(cb) : 0);
            t.setActive(request.getParameter("active") != null);

            Service svc = serviceDAO.findById(t.getServiceId());
            if (svc == null || svc.getClinicId() != t.getClinicId()) {
                request.getSession().setAttribute(
                        "adminTimeslotError",
                        "Service must belong to the selected clinic. Choose a service listed under that clinic.");
                if (t.getId() > 0) {
                    response.sendRedirect(
                            request.getContextPath() + "/admin/AdminTimeslotServlet?action=edit&id=" + t.getId());
                } else {
                    response.sendRedirect(request.getContextPath() + "/admin/AdminTimeslotServlet?action=new");
                }
                return;
            }

            if (t.getId() <= 0) {
                timeslotDAO.insert(t);
                AuditLogger.record(request, admin.getUserId(), "TIMESLOT_CREATE", "TIMESLOT", null, t.getSlotDate() + " " + t.getStartTime());
            } else {
                timeslotDAO.update(t);
                AuditLogger.record(request, admin.getUserId(), "TIMESLOT_UPDATE", "TIMESLOT", t.getId(), "Updated slot");
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.getSession().setAttribute("adminTimeslotError", "Could not save timeslot. Check all fields.");
            response.sendRedirect(request.getContextPath() + "/admin/AdminTimeslotServlet");
            return;
        }
        response.sendRedirect(request.getContextPath() + "/admin/AdminTimeslotServlet");
    }
}
