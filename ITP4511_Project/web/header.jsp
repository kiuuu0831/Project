<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="com.cchc.model.User" %>
<%
    String cp = request.getContextPath();
    User staff = (User) session.getAttribute("staffUser");
    User adminU = (User) session.getAttribute("adminUser");
    String welcomeName = null;
    String roleLabel = null;
    if (staff != null) {
        welcomeName = staff.getFullName() != null && !staff.getFullName().isBlank()
                ? staff.getFullName() : staff.getUsername();
        roleLabel = "Staff";
    } else if (adminU != null) {
        welcomeName = adminU.getFullName() != null && !adminU.getFullName().isBlank()
                ? adminU.getFullName() : adminU.getUsername();
        roleLabel = "Administrator";
    }
%>
<header class="topbar">
    <div class="topbar-brand">
        <span class="brand-mark" aria-hidden="true" title="CCHC">C</span>
        <div class="brand-text">
            <span class="brand-name">CCHC</span>
            <span class="brand-tagline">Community Clinic Hub</span>
        </div>
    </div>
    <div class="topbar-actions">
        <% if (welcomeName != null) { %>
        <div class="welcome-block">
            <span class="welcome-name"><%= welcomeName %></span>
            <% if (roleLabel != null) { %>
            <span class="welcome-role"><%= roleLabel %></span>
            <% } %>
        </div>
        <% } %>
        <a class="btn-logout" href="<%= cp %>/LogoutServlet">Logout</a>
    </div>
</header>
