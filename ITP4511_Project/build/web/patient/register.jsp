<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%
    String cp = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Create account — Patient portal</title>
    <link rel="stylesheet" href="<%= cp %>/css/style.css">
</head>
<body class="patient-auth-body">
<div class="patient-auth-shell">
    <aside class="patient-auth-hero" aria-label="Why register">
        <div class="patient-auth-hero-inner">
            <span class="patient-auth-badge">CCHC · New patient</span>
            <h1>Create your patient profile</h1>
            <p class="patient-auth-lead">Registration takes a minute. After that you can manage bookings and queue tickets whenever you need.</p>
            <ul class="patient-auth-features">
                <li>Secure access with your own username and password</li>
                <li>Update contact details anytime after you sign in</li>
                <li>Same portal for appointments, queue, and messages</li>
            </ul>
        </div>
    </aside>
    <main class="patient-auth-main">
        <div class="patient-auth-card">
            <header class="patient-auth-card-header">
                <h2>Register</h2>
                <p>Username, password, and full name are required. Email and phone are optional but recommended.</p>
            </header>

            <form action="<%= cp %>/patient/PatientRegisterServlet" method="post" autocomplete="on">
                <label for="r-user">Username</label>
                <input id="r-user" name="username" type="text" required autocomplete="username">

                <label for="r-pass">Password</label>
                <input id="r-pass" name="password" type="password" required autocomplete="new-password">

                <label for="r-name">Full name</label>
                <input id="r-name" name="fullName" type="text" required autocomplete="name">

                <label for="r-email">Email <span class="muted">(optional)</span></label>
                <input id="r-email" name="email" type="email" autocomplete="email">

                <label for="r-phone">Phone <span class="muted">(optional)</span></label>
                <input id="r-phone" name="phone" type="tel" autocomplete="tel">

                <button type="submit">Create account</button>
            </form>

            <% if (request.getAttribute("error") != null) { %>
            <p class="error"><%= request.getAttribute("error") %></p>
            <% } %>

            <p class="login-switch">
                <a href="<%= cp %>/patient/login.jsp">Already registered? Sign in</a>
            </p>
            <p class="patient-auth-staff-link">
                <a href="<%= cp %>/login.jsp">Staff sign-in</a>
            </p>
        </div>
    </main>
</div>
</body>
</html>
