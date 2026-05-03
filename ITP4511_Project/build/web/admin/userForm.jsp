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
    User u = (User) request.getAttribute("editUser");
    boolean isNew = (u == null || u.getUserId() == 0);
    if (isNew) {
        u = new User();
        u.setActive(true);
        u.setRole("PATIENT");
    }
    List<Clinic> clinics = (List<Clinic>) request.getAttribute("clinics");
    String error = (String) request.getAttribute("error");
    String cp = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <title><%= isNew ? "New" : "Edit" %> user</title>
    <link rel="stylesheet" href="<%= cp %>/css/style.css">
</head>
<body>
<jsp:include page="/header.jsp" />
<div class="layout">
    <jsp:include page="includes/sidebar.jsp" />
    <div class="main-content">
        <div class="container">
            <div class="page-header">
                <h2><%= isNew ? "New user" : "Edit user" %></h2>
                <a class="link-back" href="<%= cp %>/admin/AdminUserServlet">Back to list</a>
            </div>
            <% if (error != null) { %><p class="error"><%= error %></p><% } %>

            <form method="post" action="<%= cp %>/admin/AdminUserServlet">
                <input type="hidden" name="action" value="save">
                <% if (!isNew) { %>
                <input type="hidden" name="id" value="<%= u.getUserId() %>">
                <% } %>

                <label>Username</label>
                <input type="text" name="username" value="<%= u.getUsername() != null ? u.getUsername() : "" %>" required>

                <label>Password <%= isNew ? "" : "(leave blank to keep)" %></label>
                <input type="password" name="password" <%= isNew ? "required" : "" %>>

                <label>Full name</label>
                <input type="text" name="fullName" value="<%= u.getFullName() != null ? u.getFullName() : "" %>" required>

                <label>Email</label>
                <input type="text" name="email" value="<%= u.getEmail() != null ? u.getEmail() : "" %>">

                <label>Phone</label>
                <input type="text" name="phone" value="<%= u.getPhone() != null ? u.getPhone() : "" %>">

                <label>Role</label>
                <select name="role" id="roleSel">
                    <option value="PATIENT" <%= "PATIENT".equalsIgnoreCase(u.getRole()) ? "selected" : "" %>>Patient</option>
                    <option value="STAFF" <%= "STAFF".equalsIgnoreCase(u.getRole()) ? "selected" : "" %>>Staff</option>
                    <option value="ADMIN" <%= "ADMIN".equalsIgnoreCase(u.getRole()) ? "selected" : "" %>>Admin</option>
                </select>

                <label>Clinic (required for Staff)</label>
                <select name="clinicId">
                    <option value="">(None)</option>
                    <% if (clinics != null) { for (Clinic c : clinics) { %>
                    <option value="<%= c.getId() %>"
                            <%= u.getClinicId() != null && u.getClinicId() == c.getId() ? "selected" : "" %>>
                        <%= c.getName() %>
                    </option>
                    <% } } %>
                </select>

                <div class="check-one-line">
                    <input type="checkbox" id="userActive" name="active" value="1" <%= u.isActive() ? "checked" : "" %>>
                    <label for="userActive">Active</label>
                </div>

                <div class="admin-form-actions">
                    <button type="submit" class="btn-admin btn-admin-primary">Save</button>
                    <a class="btn-admin btn-admin-secondary" href="<%= cp %>/admin/AdminUserServlet">Back to list</a>
                </div>
            </form>
        </div>
    </div>
</div>
</body>
</html>
