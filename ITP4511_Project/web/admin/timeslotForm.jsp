<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="com.cchc.model.User" %>
<%@ page import="com.cchc.model.Timeslot" %>
<%@ page import="com.cchc.model.Clinic" %>
<%@ page import="com.cchc.model.Service" %>
<%@ page import="java.util.List" %>
<%
    User admin = (User) session.getAttribute("adminUser");
    if (admin == null) {
        response.sendRedirect(request.getContextPath() + "/adminLogin.jsp");
        return;
    }
    Timeslot t = (Timeslot) request.getAttribute("slot");
    boolean isNew = (t == null || t.getId() == 0);
    if (isNew) {
        t = new Timeslot();
        t.setActive(true);
        t.setMaxCapacity(5);
        t.setCurrentBooked(0);
    }
    List<Clinic> clinics = (List<Clinic>) request.getAttribute("clinics");
    List<Service> allServices = (List<Service>) request.getAttribute("allServices");
    String cp = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Timeslot</title>
    <link rel="stylesheet" href="<%= cp %>/css/style.css">
</head>
<body>
<jsp:include page="/header.jsp" />
<div class="layout">
    <jsp:include page="includes/sidebar.jsp" />
    <div class="main-content">
        <div class="container">
            <div class="page-header">
                <h2><%= isNew ? "New timeslot" : "Edit timeslot" %></h2>
                <a class="link-back" href="<%= cp %>/admin/AdminTimeslotServlet">Back to list</a>
            </div>
            <% if (request.getAttribute("timeslotError") != null) { %>
            <p class="error"><%= request.getAttribute("timeslotError") %></p>
            <% } %>
            <form method="post" action="<%= cp %>/admin/AdminTimeslotServlet">
                <% if (!isNew) { %><input type="hidden" name="id" value="<%= t.getId() %>"><% } %>
                <label for="timeslotClinic">Clinic</label>
                <select name="clinicId" id="timeslotClinic" required>
                    <% if (clinics != null) { for (Clinic c : clinics) { %>
                    <option value="<%= c.getId() %>" <%= t.getClinicId() == c.getId() ? "selected" : "" %>><%= c.getName() %></option>
                    <% } } %>
                </select>
                <label for="timeslotService">Service</label>
                <select name="serviceId" id="timeslotService" required>
                    <% if (allServices != null) { for (Service s : allServices) { %>
                    <option value="<%= s.getId() %>" data-clinic="<%= s.getClinicId() %>"
                            <%= t.getServiceId() == s.getId() ? "selected" : "" %>>
                        <%= s.getClinicNameDisplay() %> | <%= s.getName() %>
                    </option>
                    <% } } %>
                </select>
                <p class="muted" style="font-size:0.85rem;margin:0.25rem 0 0;max-width:40rem;">Only services for the <strong>selected clinic</strong> can be chosen. If clinic and service do not match, patients will not see this slot when booking.</p>
                <label>Date</label>
                <input type="date" name="slotDate" lang="en" title="YYYY-MM-DD"
                       value="<%= t.getSlotDate() != null ? t.getSlotDate() : "" %>" required>
                <label>Start time</label>
                <input type="time" name="startTime" value="<%
                    String st = t.getStartTime();
                    if (st != null && st.length() >= 5) { %><%= st.substring(0, 5) %><% } %>" required>
                <label>End time</label>
                <input type="time" name="endTime" value="<%
                    String et = t.getEndTime();
                    if (et != null && et.length() >= 5) { %><%= et.substring(0, 5) %><% } %>" required>
                <label>Max capacity</label>
                <input type="number" name="maxCapacity" value="<%= t.getMaxCapacity() %>" min="1">
                <label>Current booked</label>
                <input type="number" name="currentBooked" value="<%= t.getCurrentBooked() %>" min="0">
                <div class="check-one-line">
                    <input type="checkbox" id="timeslotActive" name="active" value="1" <%= t.isActive() ? "checked" : "" %>>
                    <label for="timeslotActive">Active</label>
                </div>
                <div class="admin-form-actions">
                    <button type="submit" class="btn-admin btn-admin-primary">Save</button>
                    <a class="btn-admin btn-admin-secondary" href="<%= cp %>/admin/AdminTimeslotServlet">Back to list</a>
                </div>
            </form>
            <script>
                (function () {
                    var clinic = document.getElementById('timeslotClinic');
                    var svc = document.getElementById('timeslotService');
                    if (!clinic || !svc) return;
                    function filterServices() {
                        var cid = clinic.value;
                        for (var i = 0; i < svc.options.length; i++) {
                            var o = svc.options[i];
                            var match = String(o.getAttribute('data-clinic')) === String(cid);
                            o.hidden = !match;
                            o.disabled = !match;
                        }
                        if (svc.selectedOptions.length && (svc.selectedOptions[0].hidden || svc.selectedOptions[0].disabled)) {
                            svc.value = '';
                            for (var j = 0; j < svc.options.length; j++) {
                                var p = svc.options[j];
                                if (!p.hidden && p.value) {
                                    svc.value = p.value;
                                    break;
                                }
                            }
                        }
                    }
                    clinic.addEventListener('change', filterServices);
                    filterServices();
                })();
            </script>
        </div>
    </div>
</div>
</body>
</html>
