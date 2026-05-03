<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="com.cchc.model.User" %>
<%@ page import="com.cchc.model.Service" %>
<%@ page import="com.cchc.admin.dao.AdminQuotaDAO" %>
<%@ page import="com.cchc.model.Clinic" %>
<%@ page import="java.util.List" %>
<%
    User admin = (User) session.getAttribute("adminUser");
    if (admin == null) {
        response.sendRedirect(request.getContextPath() + "/adminLogin.jsp");
        return;
    }
    List<Service> services = (List<Service>) request.getAttribute("services");
    List<AdminQuotaDAO.ServiceQuotaRow> quotas = (List<AdminQuotaDAO.ServiceQuotaRow>) request.getAttribute("quotas");
    String error = (String) request.getAttribute("error");
    String flashOk = (String) session.getAttribute("adminFlashSuccess");
    if (flashOk != null) {
        session.removeAttribute("adminFlashSuccess");
    }
    String flashErr = (String) session.getAttribute("adminFlashError");
    if (flashErr != null) {
        session.removeAttribute("adminFlashError");
    }
    List<Clinic> quotaFilterClinics = (List<Clinic>) request.getAttribute("quotaFilterClinics");
    if (quotaFilterClinics == null) {
        quotaFilterClinics = java.util.Collections.emptyList();
    }
    Integer selectedClinicId = (Integer) request.getAttribute("selectedClinicId");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Services &amp; quotas</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<jsp:include page="/header.jsp" />
<div class="layout">
    <jsp:include page="includes/sidebar.jsp" />
    <div class="main-content">
        <div class="container">
            <h2>Services</h2>
            <% if (error != null) { %><p class="error"><%= error %></p><% } %>
            <% if (flashOk != null) { %><p class="flash-success"><%= flashOk %></p><% } %>
            <% if (flashErr != null) { %><p class="error"><%= flashErr %></p><% } %>
            <div class="admin-actions">
                <a class="btn-admin btn-admin-primary" href="<%= request.getContextPath() %>/admin/AdminServiceServlet?action=new">Add service</a>
            </div>
            <table>
                <tr><th>ID</th><th>Clinic</th><th>Service</th><th>Duration</th><th>Active</th><th></th></tr>
                <% if (services != null) { for (Service s : services) { %>
                <tr>
                    <td><%= s.getId() %></td>
                    <td><%= s.getClinicNameDisplay() %></td>
                    <td><%= s.getName() %></td>
                    <td><%= s.getDurationMinutes() %> min</td>
                    <td><%= s.isActive() ? "Yes" : "No" %></td>
                    <td><a class="btn-admin btn-admin-sm btn-admin-ghost" href="<%= request.getContextPath() %>/admin/AdminServiceServlet?action=edit&id=<%= s.getId() %>">Edit</a></td>
                </tr>
                <% } } %>
            </table>

            <h3 style="margin-top:28px;">Service quotas</h3>
            <p class="muted" style="margin:0 0 12px;font-size:14px;">
                Each service has its own quota block. &quot;All clinics&quot; lists every service quota (scroll to see all).
                Values are not bulk-applied to every clinic in one form; edit and save each block as needed.
                Missing quota rows are created automatically when this page loads.
            </p>
            <form method="get" action="<%= request.getContextPath() %>/admin/AdminServiceServlet" class="quota-clinic-filter">
                <label for="quotaClinicSel">Clinic</label>
                <select name="clinicId" id="quotaClinicSel" onchange="this.form.submit()">
                    <option value="" <%= selectedClinicId == null ? "selected" : "" %>>All clinics</option>
                    <% for (Clinic c : quotaFilterClinics) {
                            boolean sel = selectedClinicId != null && selectedClinicId.intValue() == c.getId();
                    %>
                    <option value="<%= c.getId() %>" <%= sel ? "selected" : "" %>><%= c.getName() %></option>
                    <% } %>
                </select>
                <span class="muted quota-filter-hint">Only clinics with quota rows in the database are listed. Use &quot;All clinics&quot; for every row.</span>
            </form>
            <% if (quotas != null && quotas.isEmpty() && selectedClinicId != null) { %>
                <p class="muted">No service quotas for this clinic yet. Add a service under this clinic first.</p>
            <% } %>
            <% if (quotas != null) { for (AdminQuotaDAO.ServiceQuotaRow q : quotas) { %>
            <div class="container quota-card" style="margin-bottom:16px;padding:16px;">
                <% if (selectedClinicId == null) { %>
                <div class="quota-block-head">
                    <strong class="quota-service-title"><%= q.getServiceName() %></strong>
                    <span class="muted quota-clinic-label"><%= q.getClinicName() %></span>
                </div>
                <% } else { %>
                <div class="quota-block-head">
                    <strong class="quota-service-title"><%= q.getServiceName() %></strong>
                </div>
                <% } %>
                <form class="quota-edit-form" method="post" action="<%= request.getContextPath() %>/admin/AdminServiceServlet">
                    <input type="hidden" name="action" value="saveQuota">
                    <input type="hidden" name="quotaId" value="<%= q.getId() %>">
                    <% if (selectedClinicId != null) { %>
                    <input type="hidden" name="filterClinicId" value="<%= selectedClinicId %>">
                    <% } %>
                    <div class="quota-form-grid">
                        <div class="qf-field">
                            <label>Max / day</label>
                            <input type="number" name="maxPerDay" value="<%= q.getMaxPerDay() %>" min="0" required>
                        </div>
                        <div class="qf-field">
                            <label>Max / timeslot</label>
                            <input type="number" name="maxPerTimeslot" value="<%= q.getMaxPerTimeslot() %>" min="0" required>
                        </div>
                        <div class="qf-field">
                            <label>Cancellation cutoff (hours)</label>
                            <input type="number" name="cancellationCutoffHours" value="<%= q.getCancellationCutoffHours() %>" min="0" required>
                        </div>
                        <div class="qf-field">
                            <label>Max active bookings / patient</label>
                            <input type="number" name="maxActiveBookingsPerPatient" value="<%= q.getMaxActiveBookingsPerPatient() %>" min="0" required>
                        </div>
                        <div class="qf-actions">
                            <button type="submit" class="btn-admin btn-admin-primary">Save quota</button>
                        </div>
                    </div>
                </form>
            </div>
            <% } } %>
        </div>
    </div>
</div>
</body>
</html>
