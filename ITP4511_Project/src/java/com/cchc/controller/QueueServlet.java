package com.cchc.controller;

import com.cchc.dao.QueueDAO;
import com.cchc.util.QueueStatusUtil;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/QueueServlet")
public class QueueServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("staffUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        String rawFilter = request.getParameter("statusFilter");
        String statusFilter = normalizeStatusFilter(rawFilter);
        request.setAttribute("statusFilter", statusFilter);

        QueueDAO queueDAO = new QueueDAO();
        request.setAttribute(
                "queueTickets",
                "ALL".equals(statusFilter)
                        ? queueDAO.getQueueTicketsByStatus(null)
                        : queueDAO.getQueueTicketsByStatus(statusFilter));

        request.getRequestDispatcher("/queueManagement.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("staffUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        String idParam = request.getParameter("queueId");
        int queueId;
        try {
            queueId = Integer.parseInt(idParam);
        } catch (NumberFormatException ex) {
            response.sendRedirect(request.getContextPath() + "/QueueServlet");
            return;
        }

        String status = QueueStatusUtil.toCanonical(request.getParameter("status"));
        if (status == null) {
            response.sendRedirect(request.getContextPath() + "/QueueServlet");
            return;
        }

        QueueDAO queueDAO = new QueueDAO();
        queueDAO.updateQueueStatus(queueId, status);

        String preserve = normalizeStatusFilter(request.getParameter("statusFilter"));
        StringBuilder loc = new StringBuilder(request.getContextPath()).append("/QueueServlet");
        if (!"ALL".equals(preserve)) {
            loc.append("?statusFilter=").append(URLEncoder.encode(preserve, StandardCharsets.UTF_8));
        }
        response.sendRedirect(loc.toString());
    }

    /** Returns ALL or an uppercase ENUM status; invalid values become ALL. */
    private static String normalizeStatusFilter(String raw) {
        if (raw == null || raw.isBlank() || "ALL".equalsIgnoreCase(raw.trim())) {
            return "ALL";
        }
        String c = QueueStatusUtil.toCanonical(raw.trim());
        if (c == null) {
            return "ALL";
        }
        c = c.trim().toUpperCase();
        return switch (c) {
            case "WAITING", "CALLED", "SKIPPED", "COMPLETED", "EXPIRED" -> c;
            default -> "ALL";
        };
    }
}