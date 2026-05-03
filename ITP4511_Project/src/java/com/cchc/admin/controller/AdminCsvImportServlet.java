package com.cchc.admin.controller;

import com.cchc.admin.service.CsvBatchImportService;
import com.cchc.admin.service.CsvBatchImportService.ImportResult;
import com.cchc.admin.util.AuditLogger;
import com.cchc.model.User;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

@MultipartConfig(
        fileSizeThreshold = 0,
        maxFileSize = 5 * 1024 * 1024,
        maxRequestSize = 6 * 1024 * 1024)
public class AdminCsvImportServlet extends HttpServlet {

    private final CsvBatchImportService importService = new CsvBatchImportService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User admin = (User) request.getSession().getAttribute("adminUser");
        if (admin == null) {
            response.sendRedirect(request.getContextPath() + "/adminLogin.jsp");
            return;
        }
        request.getRequestDispatcher("/admin/csvImport.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User admin = session != null ? (User) session.getAttribute("adminUser") : null;
        if (admin == null) {
            response.sendRedirect(request.getContextPath() + "/adminLogin.jsp");
            return;
        }

        String type = request.getParameter("importType");
        String cp = request.getContextPath();
        StringBuilder message = new StringBuilder();
        String error = null;

        try {
            if (!"users".equals(type) && !"services".equals(type) && !"timeslots".equals(type)) {
                error = "Invalid import type.";
            } else {
                Part filePart = request.getPart("csvFile");
                if (filePart == null || filePart.getSize() == 0) {
                    error = "Please choose a CSV file.";
                } else {
                    try (InputStream in = filePart.getInputStream()) {
                        ImportResult r;
                        if ("users".equals(type)) {
                            r = importService.importUsers(in);
                        } else if ("services".equals(type)) {
                            r = importService.importServices(in);
                        } else {
                            r = importService.importTimeslots(in);
                        }
                        message.append(r.summary());
                        int i = 0;
                        for (String lineErr : r.getErrors()) {
                            if (i++ < 20) {
                                message.append(" ").append(lineErr);
                            }
                        }
                        if (r.getErrors().size() > 20) {
                            message.append(" ... and ")
                                    .append(r.getErrors().size() - 20)
                                    .append(" more error lines.");
                        }
                        AuditLogger.record(
                                request,
                                admin.getUserId(),
                                "CSV_IMPORT_" + type.toUpperCase(),
                                "CSV",
                                null,
                                message.toString());
                    } catch (SQLException e) {
                        e.printStackTrace();
                        error = "Database error: " + e.getMessage();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            error = e.getMessage() != null ? e.getMessage() : "Import failed.";
        }

        if (error != null) {
            session.setAttribute("adminFlashError", error);
            session.removeAttribute("adminFlashSuccess");
        } else {
            session.setAttribute("adminFlashSuccess", message.toString());
            session.removeAttribute("adminFlashError");
        }
        response.sendRedirect(cp + "/admin/AdminCsvImportServlet");
    }
}
