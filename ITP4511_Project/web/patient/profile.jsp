<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="com.cchc.model.User" %>
<jsp:useBean id="profileUser" type="com.cchc.model.User" scope="request"/>
<%
    String cp = request.getContextPath();
    User u = profileUser;
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>My profile</title>
    <link rel="stylesheet" href="<%= cp %>/css/style.css">
</head>
<body>
<div class="layout" style="display:block; min-height:100vh;">
    <div class="main-content" style="padding-top:1rem;">
        <div class="container">
            <%@ include file="nav.jsp" %>

            <h2>Profile &amp; security</h2>
            <p class="muted">Update your contact details or change your password.</p>

            <form action="<%= cp %>/patient/PatientProfileServlet" method="post" class="auth-card" style="max-width:420px; margin:1rem 0;">
                <label for="fullName">Full name</label>
                <input id="fullName" name="fullName" type="text" value="<%= u.getFullName() != null ? u.getFullName() : "" %>" required autocomplete="name">

                <label for="email">Email</label>
                <input id="email" name="email" type="email" value="<%= u.getEmail() != null ? u.getEmail() : "" %>" autocomplete="email">

                <label for="phone">Phone</label>
                <input id="phone" name="phone" type="text" value="<%= u.getPhone() != null ? u.getPhone() : "" %>" autocomplete="tel">

                <hr style="margin:1.25rem 0; border:none; border-top:1px solid #ddd;">

                <label for="currentPassword">Current password</label>
                <input id="currentPassword" name="currentPassword" type="password" autocomplete="current-password" placeholder="Required only if changing password">

                <label for="newPassword">New password</label>
                <input id="newPassword" name="newPassword" type="password" autocomplete="new-password" placeholder="Leave blank to keep current">

                <button type="submit">Save changes</button>
            </form>
            <p class="muted">Username: <strong><%= u.getUsername() %></strong></p>
        </div>
    </div>
</div>
</body>
</html>
