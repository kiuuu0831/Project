<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="com.cchc.model.Notification" %>
<%@ page import="java.util.List" %>
<%
    String cp = request.getContextPath();
    List<Notification> notifications = (List<Notification>) request.getAttribute("notifications");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Notifications</title>
    <link rel="stylesheet" href="<%= cp %>/css/style.css">
</head>
<body>
<div class="layout" style="display:block; min-height:100vh;">
    <div class="main-content" style="padding-top:1rem;">
        <div class="container">
            <%@ include file="nav.jsp" %>

            <h2>Notifications</h2>
            <% if (notifications == null || notifications.isEmpty()) { %>
            <p class="muted">No notifications yet.</p>
            <% } else { %>
            <ul class="notification-list" style="list-style:none; padding:0;">
                <% for (Notification n : notifications) { %>
                <li style="border:1px solid var(--border, #ddd); border-radius:8px; padding:1rem; margin-bottom:0.75rem;">
                    <div style="display:flex; justify-content:space-between; gap:1rem;">
                        <strong><%= n.getTitle() != null ? n.getTitle() : "Notice" %></strong>
                        <span class="muted" style="font-size:0.85rem;"><%= n.getCreatedAt() %></span>
                    </div>
                    <% if (n.getType() != null && !n.getType().isBlank()) { %>
                    <span class="muted" style="font-size:0.8rem;"><%= n.getType() %></span>
                    <% } %>
                    <p style="margin:0.5rem 0 0 0;"><%= n.getMessage() != null ? n.getMessage() : "" %></p>
                    <% if (!n.isIsRead()) { %>
                    <span class="flash-success" style="display:inline-block; margin-top:0.5rem; font-size:0.8rem;">Unread</span>
                    <% } %>
                </li>
                <% } %>
            </ul>
            <% } %>
        </div>
    </div>
</div>
</body>
</html>
