package com.cchc.controller;

import com.cchc.dao.NotificationDAO;
import com.cchc.model.User;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/NotificationServlet")
public class NotificationServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("staffUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        User user = (User) session.getAttribute("staffUser");

        NotificationDAO notificationDAO = new NotificationDAO();
        request.setAttribute("notifications", notificationDAO.findByUserId(user.getUserId()));

        request.getRequestDispatcher("/notifications.jsp").forward(request, response);
    }
}