<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="com.cchc.model.Clinic" %>
<%@ page import="com.cchc.model.Service" %>
<%@ page import="com.cchc.model.Timeslot" %>
<%@ page import="java.util.List" %>
<%@ taglib prefix="cchc" uri="/WEB-INF/cchc.tld" %>
<%!
    private static String esc(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
%>
<%
    String cp = request.getContextPath();
    List<Clinic> clinics = (List<Clinic>) request.getAttribute("clinics");
    List<Service> services = (List<Service>) request.getAttribute("services");
    List<Timeslot> slots = (List<Timeslot>) request.getAttribute("slots");
    Integer selClinic = (Integer) request.getAttribute("selectedClinicId");
    Integer selService = (Integer) request.getAttribute("selectedServiceId");
    String selDate = (String) request.getAttribute("selectedDate");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Book appointment</title>
    <link rel="stylesheet" href="<%= cp %>/css/style.css">
</head>
<body>
<div class="layout" style="display:block; min-height:100vh;">
    <div class="main-content" style="padding-top:1rem;">
        <div class="container">
            <%@ include file="nav.jsp" %>

            <h2>Book an appointment</h2>
            <p class="patient-booking-lead">Choose a clinic, service, and date. Only locations and services that exist in our system and have booking quotas configured are shown.</p>

            <div class="patient-booking-step">
                <span class="patient-step-badge" aria-hidden="true">1</span>
                <h3>Clinic</h3>
            </div>
            <% if (selClinic != null) {
                Clinic selected = null;
                if (clinics != null) {
                    for (Clinic c : clinics) {
                        if (c.getId() == selClinic) {
                            selected = c;
                            break;
                        }
                    }
                }
                if (selected != null) { %>
            <p class="patient-booking-context">Selected: <strong><%= esc(selected.getName()) %></strong> · <a href="<%= cp %>/patient/PatientBrowseServlet">Change clinic</a></p>
            <% } else { %>
            <p class="patient-booking-context"><a href="<%= cp %>/patient/PatientBrowseServlet">← Back to clinic list</a></p>
            <% } } %>

            <% if (clinics == null || clinics.isEmpty()) { %>
            <p class="patient-booking-empty">No clinics are available for online booking right now. Please check again later or contact the clinic.</p>
            <% } else if (selClinic == null) { %>
            <ul class="patient-card-grid">
                <% for (Clinic c : clinics) {
                    String addr = c.getAddress();
                    String phone = c.getPhone();
                    boolean hasMeta = (addr != null && !addr.isBlank()) || (phone != null && !phone.isBlank());
                %>
                <li>
                    <a class="patient-choice-card" href="<%= cp %>/patient/PatientBrowseServlet?clinicId=<%= c.getId() %>">
                        <h4 class="patient-choice-title"><%= esc(c.getName()) %></h4>
                        <% if (hasMeta) { %>
                        <p class="patient-choice-meta"><%= esc(addr != null ? addr : "") %><%= (addr != null && !addr.isBlank() && phone != null && !phone.isBlank()) ? " · " : "" %><%= esc(phone != null ? phone : "") %></p>
                        <% } %>
                        <span class="patient-choice-hint">Select clinic</span>
                    </a>
                </li>
                <% } %>
            </ul>
            <% } %>

            <% if (selClinic != null && services != null) { %>
            <div class="patient-booking-step">
                <span class="patient-step-badge" aria-hidden="true">2</span>
                <h3>Service</h3>
            </div>
            <% if (services.isEmpty()) { %>
            <p class="patient-booking-empty">No bookable services for this clinic. <a href="<%= cp %>/patient/PatientBrowseServlet">Choose another clinic</a>.</p>
            <% } else if (selService == null) { %>
            <ul class="patient-card-grid">
                <% for (Service s : services) { %>
                <li>
                    <a class="patient-choice-card" href="<%= cp %>/patient/PatientBrowseServlet?clinicId=<%= selClinic %>&serviceId=<%= s.getId() %>&date=<%= selDate != null ? esc(selDate) : "" %>">
                        <h4 class="patient-choice-title"><%= esc(s.getName()) %></h4>
                        <% if (s.getDescription() != null && !s.getDescription().isBlank()) { %>
                        <p class="patient-choice-meta"><%= esc(s.getDescription()) %></p>
                        <% } else { %>
                        <p class="patient-choice-meta"><%= s.getDurationMinutes() %> min appointment</p>
                        <% } %>
                        <span class="patient-choice-hint">Choose service</span>
                    </a>
                </li>
                <% } %>
            </ul>
            <% } %>

            <% if (selService != null) {
                Service curSvc = null;
                for (Service s : services) {
                    if (s.getId() == selService) {
                        curSvc = s;
                        break;
                    }
                }
            %>
            <div class="patient-booking-step">
                <span class="patient-step-badge" aria-hidden="true">3</span>
                <h3>Date &amp; time slots</h3>
            </div>
            <% if (curSvc != null) { %>
            <p class="patient-booking-context">Service: <strong><%= esc(curSvc.getName()) %></strong> · <a href="<%= cp %>/patient/PatientBrowseServlet?clinicId=<%= selClinic %>">Change service</a></p>
            <% } %>
            <form method="get" action="<%= cp %>/patient/PatientBrowseServlet" class="patient-date-form quota-clinic-filter">
                <input type="hidden" name="clinicId" value="<%= selClinic %>">
                <input type="hidden" name="serviceId" value="<%= selService %>">
                <div>
                    <label for="book-date">Date</label>
                    <p class="input-date-locale-hint muted">The calendar popup follows your browser language. The value saved is always <strong>YYYY-MM-DD</strong> (ISO).</p>
                    <input id="book-date" type="date" name="date" lang="en" title="YYYY-MM-DD"
                           value="<%= selDate != null ? esc(selDate) : "" %>" required>
                </div>
                <button type="submit">Show available slots</button>
            </form>

            <% if (slots != null && selDate != null && !selDate.isBlank()) { %>
            <div class="patient-slots-section">
            <div class="table-wrap">
                <table>
                    <tr>
                        <th>Time</th>
                        <th>Remaining</th>
                        <th>Action</th>
                    </tr>
                    <% if (slots.isEmpty()) { %>
                    <tr><td colspan="3" class="muted">No open slots for this date.</td></tr>
                    <% } else {
                        for (Timeslot t : slots) { %>
                    <tr>
                        <td><%= esc(t.getStartTime()) %> – <%= esc(t.getEndTime()) %></td>
                        <td><cchc:slotRemaining maxCapacity="<%= t.getMaxCapacity() %>" currentBooked="<%= t.getCurrentBooked() %>"/> / <%= t.getMaxCapacity() %></td>
                        <td>
                            <form action="<%= cp %>/patient/PatientBookServlet" method="post" style="margin:0;">
                                <input type="hidden" name="timeslotId" value="<%= t.getId() %>">
                                <input type="hidden" name="clinicId" value="<%= selClinic %>">
                                <input type="hidden" name="serviceId" value="<%= selService %>">
                                <input type="hidden" name="date" value="<%= esc(selDate) %>">
                                <button type="submit">Book</button>
                            </form>
                        </td>
                    </tr>
                    <% } } %>
                </table>
            </div>
            </div>
            <% } %>
            <% } %>
            <% } %>
        </div>
    </div>
</div>
</body>
</html>
