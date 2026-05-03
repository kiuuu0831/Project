<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="com.cchc.model.User" %>
<%
    User admin = (User) session.getAttribute("adminUser");
    if (admin == null) {
        response.sendRedirect(request.getContextPath() + "/adminLogin.jsp");
        return;
    }
    String flashOk = (String) session.getAttribute("adminFlashSuccess");
    if (flashOk != null) {
        session.removeAttribute("adminFlashSuccess");
    }
    String flashErr = (String) session.getAttribute("adminFlashError");
    if (flashErr != null) {
        session.removeAttribute("adminFlashError");
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>CSV batch import</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<jsp:include page="/header.jsp" />
<div class="layout">
    <jsp:include page="includes/sidebar.jsp" />
    <div class="main-content">
        <div class="container">
            <h2>CSV batch import</h2>
            <% if (flashOk != null) { %><p class="flash-success"><%= flashOk %></p><% } %>
            <% if (flashErr != null) { %><p class="error"><%= flashErr %></p><% } %>

            <p class="muted">
                Upload UTF-8 CSV files to create multiple records in one step. The first row must be a header (skipped).
                Fields must not contain commas. Maximum file size 5 MB.
            </p>

            <h3>Users</h3>
            <p class="muted">
                Columns:
                <code>username,password,fullName,email,phone,role,clinicId,isActive</code>
                &mdash; <code>role</code> is PATIENT, STAFF, or ADMIN; STAFF requires <code>clinicId</code>;
                <code>isActive</code> is optional (true/false/1/0), default true.
            </p>

            <h3>Services</h3>
            <p class="muted">
                Columns:
                <code>clinicId,name,description,durationMinutes,isActive</code>
            </p>

            <h3>Timeslots</h3>
            <p class="muted">
                Columns:
                <code>clinicId,serviceId,slotDate,startTime,endTime,maxCapacity,currentBooked,isActive</code>
                &mdash; dates <code>YYYY-MM-DD</code>, times as stored in DB (e.g. <code>09:00:00</code>).
            </p>

            <form method="post" action="<%= request.getContextPath() %>/admin/AdminCsvImportServlet"
                  enctype="multipart/form-data" style="margin-top:20px;">
                <label for="importType">Import type</label>
                <select name="importType" id="importType" required>
                    <option value="users">Users</option>
                    <option value="services">Services</option>
                    <option value="timeslots">Timeslots</option>
                </select>

                <label for="csvFile">CSV file</label>
                <input type="file" name="csvFile" id="csvFile" accept=".csv,text/csv,text/plain" required>

                <button type="submit" class="btn-admin btn-admin-primary">Upload and import</button>
            </form>
        </div>
    </div>
</div>
</body>
</html>
