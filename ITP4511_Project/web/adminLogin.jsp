<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%
    String cp = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>CCHC Administrator Login</title>
    <link rel="stylesheet" href="<%= cp %>/css/style.css">
</head>
<body>
<div class="auth-layout">
    <div class="auth-card">
        <div class="auth-brand">
            <span class="brand-mark" aria-hidden="true" title="CCHC">C</span>
            <span class="auth-title">Administrator</span>
            <span class="auth-sub">Sign in to the admin console</span>
        </div>

        <form action="<%= cp %>/admin/AdminLoginServlet" method="post">
            <label for="adm-user">Username</label>
            <input id="adm-user" type="text" name="username" required autocomplete="username">

            <label for="adm-pass">Password</label>
            <input id="adm-pass" type="password" name="password" required autocomplete="current-password">

            <button type="submit">Sign in</button>
        </form>

        <%
            Object adminErr = session.getAttribute("adminLoginError");
            if (adminErr != null && !adminErr.toString().isBlank()) {
        %>
        <p class="error"><%= adminErr %></p>
        <%
            }
        %>

        <p class="login-switch">
            <a href="<%= cp %>/login.jsp">Clinic Staff login</a>
        </p>
    </div>
</div>
</body>
</html>
