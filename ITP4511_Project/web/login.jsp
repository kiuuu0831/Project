<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%
    String cp = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>CCHC Staff Login</title>
    <link rel="stylesheet" href="<%= cp %>/css/style.css">
</head>
<body>
<div class="auth-layout">
    <div class="auth-card">
        <div class="auth-brand">
            <span class="brand-mark" aria-hidden="true" title="CCHC">C</span>
            <span class="auth-title">Clinic Staff</span>
            <span class="auth-sub">Sign in to the staff console</span>
        </div>

        <form action="<%= cp %>/LoginServlet" method="post">
            <label for="staff-user">Username</label>
            <input id="staff-user" type="text" name="username" required autocomplete="username">

            <label for="staff-pass">Password</label>
            <input id="staff-pass" type="password" name="password" required autocomplete="current-password">

            <button type="submit">Sign in</button>
        </form>

        <% if (request.getAttribute("error") != null && !request.getAttribute("error").toString().isBlank()) { %>
        <p class="error">${error}</p>
        <% } %>

        <p class="login-switch">
            <a href="<%= cp %>/patient/login.jsp">Patient portal</a>
            &nbsp;·&nbsp;
            <a href="<%= cp %>/adminLogin.jsp">Administrator login</a>
        </p>
    </div>
</div>
</body>
</html>
