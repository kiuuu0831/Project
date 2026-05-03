package com.cchc.tag;

import java.io.IOException;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;

/** Prints remaining places: maxCapacity - currentBooked (not below 0). */
public class SlotRemainingTag extends SimpleTagSupport {

    private int maxCapacity;
    private int currentBooked;

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public void setCurrentBooked(int currentBooked) {
        this.currentBooked = currentBooked;
    }

    @Override
    public void doTag() throws JspException, IOException {
        JspWriter out = getJspContext().getOut();
        int r = Math.max(0, maxCapacity - currentBooked);
        out.print(r);
    }
}
