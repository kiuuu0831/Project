package com.cchc.admin.controller;

import com.cchc.dao.UserDAO;
import com.cchc.model.User;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class AdminLoginServlet extends HttpServlet {

    private static final String ERR_SESSION = "adminLoginError";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/adminLogin.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        UserDAO userDAO = new UserDAO();
        User user = userDAO.loginForAdministrator(username, password);

        HttpSession session = request.getSession();

        if (user != null) {
            session.removeAttribute(ERR_SESSION);
            session.setAttribute("adminUser", user);
            response.sendRedirect(request.getContextPath() + "/admin/AdminDashboardServlet");
        } else {
            session.setAttribute(ERR_SESSION, "Invalid administrator credentials.");
            response.sendRedirect(request.getContextPath() + "/adminLogin.jsp");
        }
    }
}
