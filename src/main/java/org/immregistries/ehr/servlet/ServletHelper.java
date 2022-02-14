package org.immregistries.ehr.servlet;

import javax.servlet.http.HttpSession;
import java.io.PrintWriter;

public class ServletHelper {

    public static void doStandardHeader(PrintWriter out, HttpSession session) {
        out.println("<html>");
        out.println("  <head>");
        out.println("    <title>EHR Sandbox</title>");
        out.println("<link rel=\"stylesheet\" href=\"https://www.w3schools.com/w3css/4/w3.css\">");
        out.println("  </head>");
        out.println("  <body>");
        // out.println("<div class=\"w3-container \">");
        out.println("<header >\r\n" + "<div class=\"w3-bar w3-green w3-margin-bottom\">\r\n"
                + "  <a href = 'silos ' class=\"w3-bar-item w3-button\">List of tenants </a>\r\n"
                + "  <a href = 'facility_patient_display' class=\"w3-bar-item w3-button\">Facilities/patients list</a>\r\n"
                + "  <a href = 'Settings' class=\"w3-bar-item w3-right w3-button\">Settings </a>\r\n"
                + "</div>" + "    	</header>");
        out.println("<div class=\"w3-display-container w3-margin\" style=\"height:600px;\">");
    }

    public static void doStandardFooter(PrintWriter out, HttpSession session) {
        out.println("</div>\r\n" + "    </body>\r\n" + "</html>");
    }
}
