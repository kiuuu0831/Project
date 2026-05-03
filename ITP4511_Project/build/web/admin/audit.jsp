<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="com.cchc.model.User" %>
<%@ page import="com.cchc.admin.dao.AuditLogDAO" %>
<%@ page import="java.util.List" %>
<%
    User admin = (User) session.getAttribute("adminUser");
    if (admin == null) {
        response.sendRedirect(request.getContextPath() + "/adminLogin.jsp");
        return;
    }
    List<AuditLogDAO.AuditEntry> logs = (List<AuditLogDAO.AuditEntry>) request.getAttribute("auditLogs");
    String fu = (String) request.getAttribute("filterUsername");
    String fa = (String) request.getAttribute("filterAuditAction");
    String ff = (String) request.getAttribute("filterDateFrom");
    String ft = (String) request.getAttribute("filterDateTo");
    String error = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Audit trail</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<jsp:include page="/header.jsp" />
<div class="layout">
    <jsp:include page="includes/sidebar.jsp" />
    <div class="main-content">
        <div class="container">
            <h2>Audit trail</h2>
            <% if (error != null) { %><p class="error"><%= error %></p><% } %>

            <form method="get" action="<%= request.getContextPath() %>/admin/AdminAuditServlet" style="margin-bottom:16px;">
                <label>Username contains</label>
                <input type="text" name="username" value="<%= fu != null ? fu : "" %>">
                <label>Action contains</label>
                <input type="text" name="auditAction" value="<%= fa != null ? fa : "" %>">
                <label>Date from</label>
                <input type="date" name="dateFrom" lang="en" title="YYYY-MM-DD" value="<%= ff != null ? ff : "" %>">
                <label>Date to</label>
                <input type="date" name="dateTo" lang="en" title="YYYY-MM-DD" value="<%= ft != null ? ft : "" %>">
                <button type="submit" class="btn-admin btn-admin-primary">Filter</button>
            </form>

            <table>
                <tr>
                    <th>Time</th><th>User</th><th>Action</th><th>Target</th><th>Details</th><th>IP</th>
                </tr>
                <% if (logs != null) { for (AuditLogDAO.AuditEntry e : logs) { %>
                <tr>
                    <td><%= e.getCreatedAt() %></td>
                    <td><%= e.getUsername() %></td>
                    <td><%= e.getAction() %></td>
                    <td><%= e.getTargetType() != null ? e.getTargetType() : "" %> #<%= e.getTargetId() != null ? e.getTargetId() : "" %></td>
                    <td><%= e.getDetails() != null ? e.getDetails() : "" %></td>
                    <td><%= e.getIpAddress() != null ? e.getIpAddress() : "" %></td>
                </tr>
                <% } } %>
            </table>
        </div>
    </div>
</div>
</body>
</html>
