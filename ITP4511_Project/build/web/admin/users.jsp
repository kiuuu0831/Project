<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="com.cchc.model.User" %>
<%@ page import="java.util.List" %>
<%
    User admin = (User) session.getAttribute("adminUser");
    if (admin == null) {
        response.sendRedirect(request.getContextPath() + "/adminLogin.jsp");
        return;
    }
    List<User> users = (List<User>) request.getAttribute("users");
    String error = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>User management</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<jsp:include page="/header.jsp" />
<div class="layout">
    <jsp:include page="includes/sidebar.jsp" />
    <div class="main-content">
        <div class="container">
            <h2>User management</h2>
            <% if (error != null) { %><p class="error"><%= error %></p><% } %>
            <div class="admin-actions">
                <a class="btn-admin btn-admin-primary" href="<%= request.getContextPath() %>/admin/AdminUserServlet?action=new">Add user</a>
            </div>

            <table>
                <tr>
                    <th>ID</th>
                    <th>Username</th>
                    <th>Full name</th>
                    <th>Role</th>
                    <th>Clinic</th>
                    <th>Active</th>
                    <th></th>
                </tr>
                <% if (users != null) { for (User u : users) { %>
                <tr>
                    <td><%= u.getUserId() %></td>
                    <td><%= u.getUsername() %></td>
                    <td><%= u.getFullName() %></td>
                    <td><%= u.getRole() %></td>
                    <td><%= u.getClinicDisplayName() != null ? u.getClinicDisplayName() : "-" %></td>
                    <td><%= u.isActive() ? "Yes" : "No" %></td>
                    <td>
                        <a class="btn-admin btn-admin-sm btn-admin-ghost" href="<%= request.getContextPath() %>/admin/AdminUserServlet?action=edit&id=<%= u.getUserId() %>">Edit</a>
                        <% if (u.getUserId() != admin.getUserId()) { %>
                        <form style="display:inline" method="post" action="<%= request.getContextPath() %>/admin/AdminUserServlet"
                              onsubmit="return confirm('Deactivate this user?');">
                            <input type="hidden" name="action" value="delete">
                            <input type="hidden" name="id" value="<%= u.getUserId() %>">
                            <button type="submit" class="btn-danger">Deactivate</button>
                        </form>
                        <% } %>
                    </td>
                </tr>
                <% } } %>
            </table>
        </div>
    </div>
</div>
</body>
</html>
