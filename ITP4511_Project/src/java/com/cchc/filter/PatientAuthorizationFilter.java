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

public class PatientAuthorizationFilter implements Filter {

    private static final String SESSION = "patientUser";

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

        if (isPublicPatientPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        if (session == null) {
            resp.sendRedirect(contextPath + "/patient/login.jsp");
            return;
        }

        Object attr = session.getAttribute(SESSION);
        if (!(attr instanceof User)) {
            resp.sendRedirect(contextPath + "/patient/login.jsp");
            return;
        }

        User u = (User) attr;
        if (!"PATIENT".equalsIgnoreCase(u.getRole() != null ? u.getRole().trim() : "")) {
            session.removeAttribute(SESSION);
            resp.sendRedirect(contextPath + "/patient/login.jsp");
            return;
        }

        chain.doFilter(request, response);
    }

    private static boolean isPublicPatientPath(String path) {
        return "/patient/login.jsp".equals(path)
                || "/patient/register.jsp".equals(path)
                || "/patient/PatientLoginServlet".equals(path)
                || "/patient/PatientRegisterServlet".equals(path)
                || "/patient/PatientLogoutServlet".equals(path);
    }
}
