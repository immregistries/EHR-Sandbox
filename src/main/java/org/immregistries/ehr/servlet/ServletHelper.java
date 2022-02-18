package org.immregistries.ehr.servlet;

import org.immregistries.ehr.model.Facility;
import org.immregistries.ehr.model.Patient;
import org.immregistries.ehr.model.Silo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;

public class ServletHelper {

    public static void doStandardHeader(PrintWriter out, HttpSession session, HttpServletRequest req) {
        Silo silo = (Silo) session.getAttribute("silo");
        Facility facility = (Facility) session.getAttribute("facility");
        Patient patient = (Patient) session.getAttribute("patient");
        out.println("<html>");
        out.println("  <head>");
        out.println("    <title>EHR Sandbox</title>");
        out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + req.getContextPath() + "/inc/style.css\">");
        out.println("  </head>");
        out.println("  <body>");
        // out.println("<div class=\"w3-container \">");
        out.println("<header >" + "<div class=\"w3-bar w3-blue-gray w3-text-shadow w3-margin-bottom\">"
                + "<a href = 'silos' class=\"w3-bar-item w3-button\">"
                + (silo == null ? "List of Tenants" : "Tenant : " + silo.getNameDisplay())
                + "</a>"

                +  (facility == null ? ""
                : "<a href = 'facility_patient_display' class=\"w3-bar-item w3-button\">"
                + "Facility : " + facility.getNameDisplay())
                + "</a>"

                + (patient == null ? ""
                    : "<a href = 'patient_record?paramPatientId=" + patient.getPatientId() + "' class=\"w3-bar-item w3-button\">"
                    +   "Patient : " + patient.getNameFirst() + " " + patient.getNameMiddle() + " " + patient.getNameLast()
                    + "</a>")

                + "<a href = 'Settings' class=\"w3-bar-item w3-right w3-button\">Settings</a>"

                + "</div>" + "</header>");
        out.println("<div class=\"w3-display-container w3-margin\" style=\"height:600px;\">");
    }

    public static void doStandardFooter(PrintWriter out, HttpSession session) {
        out.println("</div>" + "    </body>" + "</html>");
    }
}
