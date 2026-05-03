<%
    String cpNav = request.getContextPath();
%>
<nav class="patient-nav" aria-label="Patient portal">
    <a href="<%= cpNav %>/patient/PatientDashboardServlet">Dashboard</a>
    <span class="nav-sep">|</span>
    <a href="<%= cpNav %>/patient/PatientBrowseServlet">Book appointment</a>
    <span class="nav-sep">|</span>
    <a href="<%= cpNav %>/patient/PatientAppointmentServlet">My appointments</a>
    <span class="nav-sep">|</span>
    <a href="<%= cpNav %>/patient/PatientQueueServlet">Walk-in queue</a>
    <span class="nav-sep">|</span>
    <a href="<%= cpNav %>/patient/PatientNotificationServlet">Notifications</a>
    <span class="nav-sep">|</span>
    <a href="<%= cpNav %>/patient/PatientProfileServlet">Profile</a>
    <span class="nav-sep">|</span>
    <a href="<%= cpNav %>/patient/PatientLogoutServlet">Logout</a>
</nav>
<%
    String patientFlash = (String) session.getAttribute("patientFlash");
    String patientFlashErr = (String) session.getAttribute("patientFlashError");
    if (patientFlash != null) {
        session.removeAttribute("patientFlash");
    }
    if (patientFlashErr != null) {
        session.removeAttribute("patientFlashError");
    }
%>
<% if (patientFlash != null) { %>
<p class="flash-success"><%= patientFlash %></p>
<% } %>
<% if (patientFlashErr != null) { %>
<p class="error"><%= patientFlashErr %></p>
<% } %>
