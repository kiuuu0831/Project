<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="com.cchc.model.User" %>
<%@ page import="com.cchc.model.Clinic" %>
<%@ page import="java.util.List" %>
<%
    User admin = (User) session.getAttribute("adminUser");
    if (admin == null) {
        response.sendRedirect(request.getContextPath() + "/adminLogin.jsp");
        return;
    }
    List<Clinic> clinics = (List<Clinic>) request.getAttribute("clinics");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Clinics</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<jsp:include page="/header.jsp" />
<div class="layout">
    <jsp:include page="includes/sidebar.jsp" />
    <div class="main-content">
        <div class="container">
            <h2>Clinics</h2>
            <div class="admin-actions">
                <a class="btn-admin btn-admin-primary" href="<%= request.getContextPath() %>/admin/AdminClinicServlet?action=new">Add clinic</a>
            </div>
            <table>
                <tr><th>ID</th><th>Name</th><th>Phone</th><th>Active</th><th></th></tr>
                <% if (clinics != null) { for (Clinic c : clinics) { %>
                <tr>
                    <td><%= c.getId() %></td>
                    <td><%= c.getName() %></td>
                    <td><%= c.getPhone() != null ? c.getPhone() : "" %></td>
                    <td><%= c.isActive() ? "Yes" : "No" %></td>
                    <td><a class="btn-admin btn-admin-sm btn-admin-ghost" href="<%= request.getContextPath() %>/admin/AdminClinicServlet?action=edit&id=<%= c.getId() %>">Edit</a></td>
                </tr>
                <% } } %>
            </table>
        </div>
    </div>
</div>
</body>
</html>
