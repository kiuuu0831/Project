<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%
    String cp = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Sign in — Patient portal</title>
    <link rel="stylesheet" href="<%= cp %>/css/style.css">
</head>
<body class="patient-auth-body">
<div class="patient-auth-shell">
    <aside class="patient-auth-hero" aria-label="Patient portal overview">
        <div class="patient-auth-hero-inner">
            <span class="patient-auth-badge">CCHC · Patient access</span>
            <h1>Your care, online and on time</h1>
            <p class="patient-auth-lead">Book visits, join same-day walk-in queues, and keep track of reminders in one place.</p>
            <ul class="patient-auth-features">
                <li>Live appointment slots with remaining capacity</li>
                <li>Reschedule or cancel within policy windows</li>
                <li>Notifications for bookings, queue calls, and updates</li>
            </ul>
        </div>
    </aside>
    <main class="patient-auth-main">
        <div class="patient-auth-card">
            <header class="patient-auth-card-header">
                <h2>Sign in</h2>
                <p>Enter your patient username and password to continue.</p>
            </header>

            <% if (request.getAttribute("success") != null) { %>
            <p class="flash-success"><%= request.getAttribute("success") %></p>
            <% } %>

            <form action="<%= cp %>/patient/PatientLoginServlet" method="post" autocomplete="on">
                <label for="p-user">Username</label>
                <input id="p-user" name="username" type="text" required autocomplete="username">

                <label for="p-pass">Password</label>
                <input id="p-pass" name="password" type="password" required autocomplete="current-password">

                <button type="submit">Sign in</button>
            </form>

            <% if (request.getAttribute("error") != null) { %>
            <p class="error"><%= request.getAttribute("error") %></p>
            <% } %>

            <p class="login-switch">
                <a href="<%= cp %>/patient/register.jsp">Create an account</a>
            </p>
            <p class="patient-auth-staff-link">
                <a href="<%= cp %>/login.jsp">Staff sign-in</a>
            </p>
        </div>
    </main>
</div>
</body>
</html>
