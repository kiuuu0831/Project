<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="com.cchc.model.User" %>
<%@ page import="com.cchc.model.Service" %>
<%@ page import="com.cchc.model.Clinic" %>
<%@ page import="java.util.List" %>
<%
    User admin = (User) session.getAttribute("adminUser");
    if (admin == null) {
        response.sendRedirect(request.getContextPath() + "/adminLogin.jsp");
        return;
    }
    Service s = (Service) request.getAttribute("service");
    boolean isNew = (s == null || s.getId() == 0);
    if (isNew) {
        s = new Service();
        s.setActive(true);
        s.setDurationMinutes(30);
    }
    List<Clinic> clinics = (List<Clinic>) request.getAttribute("clinics");
    String cp = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Service</title>
    <link rel="stylesheet" href="<%= cp %>/css/style.css">
</head>
<body>
<jsp:include page="/header.jsp" />
<div class="layout">
    <jsp:include page="includes/sidebar.jsp" />
    <div class="main-content">
        <div class="container">
            <div class="page-header">
                <h2><%= isNew ? "New service" : "Edit service" %></h2>
                <a class="link-back" href="<%= cp %>/admin/AdminServiceServlet">Back to list</a>
            </div>
            <form method="post" action="<%= cp %>/admin/AdminServiceServlet">
                <input type="hidden" name="action" value="saveService">
                <% if (!isNew) { %><input type="hidden" name="id" value="<%= s.getId() %>"><% } %>
                <label>Clinic</label>
                <select name="clinicId" required>
                    <% if (clinics != null) { for (Clinic c : clinics) { %>
                    <option value="<%= c.getId() %>" <%= s.getClinicId() == c.getId() ? "selected" : "" %>><%= c.getName() %></option>
                    <% } } %>
                </select>
                <label>Name</label>
                <input type="text" name="name" value="<%= s.getName() != null ? s.getName() : "" %>" required>
                <label>Description</label>
                <textarea name="description" rows="3"><%= s.getDescription() != null ? s.getDescription() : "" %></textarea>
                <label>Duration (minutes)</label>
                <input type="number" name="durationMinutes" value="<%= s.getDurationMinutes() %>" min="5" step="5">
                <div class="check-one-line">
                    <input type="checkbox" id="serviceActive" name="active" value="1" <%= s.isActive() ? "checked" : "" %>>
                    <label for="serviceActive">Active</label>
                </div>
                <div class="admin-form-actions">
                    <button type="submit" class="btn-admin btn-admin-primary">Save</button>
                    <a class="btn-admin btn-admin-secondary" href="<%= cp %>/admin/AdminServiceServlet">Back to list</a>
                </div>
            </form>
        </div>
    </div>
</div>
</body>
</html>
