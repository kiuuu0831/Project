<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="com.cchc.model.User" %>
<%@ page import="com.cchc.admin.dao.PolicyDAO" %>
<%@ page import="java.util.List" %>
<%
    User admin = (User) session.getAttribute("adminUser");
    if (admin == null) {
        response.sendRedirect(request.getContextPath() + "/adminLogin.jsp");
        return;
    }
    List<PolicyDAO.PolicyRow> policies = (List<PolicyDAO.PolicyRow>) request.getAttribute("policies");
    String error = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Policies</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<jsp:include page="/header.jsp" />
<div class="layout">
    <jsp:include page="includes/sidebar.jsp" />
    <div class="main-content">
        <div class="container">
            <h2>Policy settings</h2>
            <p class="muted">
                Adjust global behaviour without redeploying code. Typical keys include limits on active bookings,
                cancellation cutoff (hours), and walk-in toggles. Values are stored in the <code>policies</code> table.
                Ensure required rows exist (run <code>sql/migration_policies_extra.sql</code> if your schema is older).
            </p>
            <% if (error != null) { %><p class="error"><%= error %></p><% } %>

            <% if (policies != null) { for (PolicyDAO.PolicyRow p : policies) { %>
            <form method="post" action="<%= request.getContextPath() %>/admin/AdminPolicyServlet" style="margin-bottom:20px;padding-bottom:16px;border-bottom:1px solid #e5e7eb;">
                <input type="hidden" name="policyId" value="<%= p.getId() %>">
                <h4><%= p.getPolicyKey() %></h4>
                <p class="muted"><%= p.getDescription() != null ? p.getDescription() : "" %></p>
                <label>Value</label>
                <input type="text" name="policyValue" value="<%= p.getPolicyValue() != null ? p.getPolicyValue() : "" %>" style="max-width:360px;">
                <button type="submit" class="btn-admin btn-admin-primary">Save</button>
            </form>
            <% } } %>
        </div>
    </div>
</div>
</body>
</html>
