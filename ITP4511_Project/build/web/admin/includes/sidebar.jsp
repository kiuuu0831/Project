<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%
    String uri = request.getRequestURI();
    String cp = request.getContextPath();
%>
<div class="sidebar">
    <p class="sidebar-heading">Administration</p>
    <a href="<%= cp %>/admin/AdminDashboardServlet" class="sidebar-link<%= uri.contains("AdminDashboardServlet") ? " active" : "" %>">Dashboard</a>
    <a href="<%= cp %>/admin/AdminUserServlet" class="sidebar-link<%= uri.contains("AdminUserServlet") ? " active" : "" %>">Users</a>
    <a href="<%= cp %>/admin/AdminClinicServlet" class="sidebar-link<%= uri.contains("AdminClinicServlet") ? " active" : "" %>">Clinics</a>
    <a href="<%= cp %>/admin/AdminServiceServlet" class="sidebar-link<%= uri.contains("AdminServiceServlet") ? " active" : "" %>">Services &amp; quotas</a>
    <a href="<%= cp %>/admin/AdminTimeslotServlet" class="sidebar-link<%= uri.contains("AdminTimeslotServlet") ? " active" : "" %>">Timeslots</a>
    <a href="<%= cp %>/admin/AdminPolicyServlet" class="sidebar-link<%= uri.contains("AdminPolicyServlet") ? " active" : "" %>">Policies</a>
    <a href="<%= cp %>/admin/AdminReportServlet" class="sidebar-link<%= uri.contains("AdminReportServlet") ? " active" : "" %>">Reports</a>
    <a href="<%= cp %>/admin/AdminCsvImportServlet" class="sidebar-link<%= uri.contains("AdminCsvImportServlet") ? " active" : "" %>">CSV import</a>
    <a href="<%= cp %>/admin/AdminAuditServlet" class="sidebar-link<%= uri.contains("AdminAuditServlet") ? " active" : "" %>">Audit trail</a>
</div>
