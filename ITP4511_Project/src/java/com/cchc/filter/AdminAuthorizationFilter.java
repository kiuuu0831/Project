package com.cchc.filter;

import com.cchc.model.User;
import java.io.IOException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class AdminAuthorizationFilter implements Filter {

    private static final String ADMIN_SESSION = "adminUser";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String contextPath = req.getContextPath();
        String uri = req.getRequestURI();
        if (uri.length() < contextPath.length()) {
            chain.doFilter(request, response);
            return;
        }

        String path = uri.substring(contextPath.length());
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        if (isPublicAdminPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        if (session == null) {
            resp.sendRedirect(contextPath + "/adminLogin.jsp");
            return;
        }

        Object attr = session.getAttribute(ADMIN_SESSION);
        if (!(attr instanceof User)) {
            resp.sendRedirect(contextPath + "/adminLogin.jsp");
            return;
        }

        User admin = (User) attr;
        if (!isAdministratorRole(admin.getRole())) {
            session.removeAttribute(ADMIN_SESSION);
            resp.sendRedirect(contextPath + "/adminLogin.jsp");
            return;
        }

        chain.doFilter(request, response);
    }

    private static boolean isPublicAdminPath(String path) {
        if (path == null) {
            return false;
        }
        // Login POST/GET must bypass session check (also tolerate no leading slash after normalize).
        return "/admin/AdminLoginServlet".equals(path);
    }

    private static boolean isAdministratorRole(String role) {
        if (role == null) {
            return false;
        }
        // DB ENUM is ADMIN; mapUser may return "ADMIN".
        String r = role.trim().toUpperCase();
        return "ADMIN".equals(r) || "ADMINISTRATOR".equals(r);
    }
}
