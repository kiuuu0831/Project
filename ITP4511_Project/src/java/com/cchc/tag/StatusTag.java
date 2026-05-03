package com.cchc.tag;

import java.io.IOException;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;

public class StatusTag extends SimpleTagSupport {

    private String value;

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public void doTag() throws JspException, IOException {
        JspWriter out = getJspContext().getOut();

        String color = "#6c757d";

        if (value == null) {
            out.print("<span style='background:#6c757d;color:white;padding:4px 10px;border-radius:12px;font-size:12px;'>-</span>");
            return;
        }

        String v = value.trim();

        if ("PENDING".equalsIgnoreCase(v) || "Booked".equalsIgnoreCase(v)) {
            color = "#6c757d";
        } else if ("CONFIRMED".equalsIgnoreCase(v) || "Arrived".equalsIgnoreCase(v)) {
            color = "#0d6efd";
        } else if ("COMPLETED".equalsIgnoreCase(v) || "Served".equalsIgnoreCase(v)) {
            color = "#198754";
        } else if ("NO_SHOW".equalsIgnoreCase(v) || "No-show".equalsIgnoreCase(v) || "Skipped".equalsIgnoreCase(v)) {
            color = "#dc3545";
        } else if ("CALLED".equalsIgnoreCase(v)) {
            color = "#fd7e14";
        } else if ("WAITING".equalsIgnoreCase(v) || "Waiting".equalsIgnoreCase(v)) {
            color = "#0dcaf0";
        } else if ("CANCELLED".equalsIgnoreCase(v) || "Cancelled by clinic".equalsIgnoreCase(v)) {
            color = "#6f42c1";
        } else if ("EXPIRED".equalsIgnoreCase(v)) {
            color = "#adb5bd";
        }

        out.print("<span style='background:" + color + ";color:white;padding:4px 10px;border-radius:12px;font-size:12px;'>"
                + escapeHtml(v) + "</span>");
    }

    private static String escapeHtml(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
