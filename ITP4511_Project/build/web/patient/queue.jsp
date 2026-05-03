<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="com.cchc.model.Clinic" %>
<%@ page import="com.cchc.model.Service" %>
<%@ page import="com.cchc.patient.model.PatientQueueTicketView" %>
<%@ page import="java.util.List" %>
<%@ taglib prefix="cchc" uri="/WEB-INF/cchc.tld" %>
<%
    String cp = request.getContextPath();
    List<Clinic> clinics = (List<Clinic>) request.getAttribute("clinics");
    List<Service> allServices = (List<Service>) request.getAttribute("allServices");
    List<PatientQueueTicketView> myTickets = (List<PatientQueueTicketView>) request.getAttribute("myQueueTickets");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Walk-in queue</title>
    <link rel="stylesheet" href="<%= cp %>/css/style.css">
</head>
<body>
<div class="layout" style="display:block; min-height:100vh;">
    <div class="main-content" style="padding-top:1rem;">
        <div class="container">
            <%@ include file="nav.jsp" %>

            <h2>Same-day walk-in queue</h2>
            <p class="muted">Join the queue when the clinic accepts walk-ins for your service.</p>

            <h3>Take a number</h3>
            <form action="<%= cp %>/patient/PatientQueueServlet" method="post" class="quota-clinic-filter">
                <label for="queueClinic">Clinic</label>
                <select name="clinicId" id="queueClinic" required>
                    <option value="">— Select —</option>
                    <% if (clinics != null) {
                        for (Clinic c : clinics) { %>
                    <option value="<%= c.getId() %>"><%= c.getName() %></option>
                    <% } } %>
                </select>
                <label for="queueService">Service</label>
                <select name="serviceId" id="queueService" required aria-describedby="queue-service-hint">
                    <option value="">— Select a clinic first —</option>
                    <% if (allServices != null) {
                        for (Service s : allServices) { %>
                    <option value="<%= s.getId() %>" data-clinic="<%= s.getClinicId() %>"><%= s.getName() %></option>
                    <% } } %>
                </select>
                <p id="queue-service-hint" class="muted" style="margin:0.35rem 0 0;font-size:0.8rem;max-width:36rem;">Choose a clinic to see only that clinic&rsquo;s services (from the database).</p>
                <button type="submit">Join queue</button>
            </form>
            <script>
                (function () {
                    var c = document.getElementById('queueClinic');
                    var s = document.getElementById('queueService');
                    if (!c || !s) return;
                    function filter() {
                        var cid = c.value;
                        if (cid === '') {
                            s.value = '';
                        }
                        for (var i = 0; i < s.options.length; i++) {
                            var o = s.options[i];
                            if (!o.value) {
                                o.hidden = false;
                                o.disabled = false;
                                continue;
                            }
                            var show = cid !== '' && String(o.getAttribute('data-clinic')) === String(cid);
                            o.hidden = !show;
                            o.disabled = !show;
                        }
                        if (cid !== '' && s.selectedOptions.length
                                && (s.selectedOptions[0].hidden || s.selectedOptions[0].disabled)) {
                            s.value = '';
                        }
                    }
                    c.addEventListener('change', filter);
                    filter();
                })();
            </script>

            <h3>My queue tickets</h3>
            <% if (myTickets == null || myTickets.isEmpty()) { %>
            <p class="muted">No queue history yet.</p>
            <% } else { %>
            <div class="table-wrap">
                <table>
                    <tr><th>#</th><th>Clinic</th><th>Service</th><th>Status</th><th>Joined</th></tr>
                    <% for (PatientQueueTicketView q : myTickets) { %>
                    <tr>
                        <td><%= q.getQueueNumber() %></td>
                        <td><%= q.getClinicName() %></td>
                        <td><%= q.getServiceName() %></td>
                        <td><cchc:statusBadge value="<%= q.getStatus() %>"/></td>
                        <td><%= q.getJoinedAt() %></td>
                    </tr>
                    <% } %>
                </table>
            </div>
            <% } %>
        </div>
    </div>
</div>
</body>
</html>
