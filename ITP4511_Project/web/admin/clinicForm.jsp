<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="com.cchc.model.User" %>
<%@ page import="com.cchc.model.Clinic" %>
<%
    User admin = (User) session.getAttribute("adminUser");
    if (admin == null) {
        response.sendRedirect(request.getContextPath() + "/adminLogin.jsp");
        return;
    }
    Clinic c = (Clinic) request.getAttribute("clinic");
    boolean isNew = (c == null || c.getId() == 0);
    if (isNew) {
        c = new Clinic();
        c.setActive(true);
    }
    String cp = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Clinic</title>
    <link rel="stylesheet" href="<%= cp %>/css/style.css">
</head>
<body>
<jsp:include page="/header.jsp" />
<div class="layout">
    <jsp:include page="includes/sidebar.jsp" />
    <div class="main-content">
        <div class="container">
            <div class="page-header">
                <h2><%= isNew ? "New clinic" : "Edit clinic" %></h2>
                <a class="link-back" href="<%= cp %>/admin/AdminClinicServlet">Back to list</a>
            </div>
            <form method="post" action="<%= cp %>/admin/AdminClinicServlet">
                <input type="hidden" name="action" value="save">
                <% if (!isNew) { %><input type="hidden" name="id" value="<%= c.getId() %>"><% } %>
                <label>Name</label>
                <input type="text" name="name" value="<%= c.getName() != null ? c.getName() : "" %>" required>
                <label>Address</label>
                <input type="text" name="address" value="<%= c.getAddress() != null ? c.getAddress() : "" %>">
                <label>Phone</label>
                <input type="text" name="phone" value="<%= c.getPhone() != null ? c.getPhone() : "" %>">
                <div class="check-one-line">
                    <input type="checkbox" id="clinicActive" name="active" value="1" <%= c.isActive() ? "checked" : "" %>>
                    <label for="clinicActive">Active</label>
                </div>
                <div class="admin-form-actions">
                    <button type="submit" class="btn-admin btn-admin-primary">Save</button>
                    <a class="btn-admin btn-admin-secondary" href="<%= cp %>/admin/AdminClinicServlet">Back to list</a>
                </div>
            </form>
        </div>
    </div>
</div>
</body>
</html>
