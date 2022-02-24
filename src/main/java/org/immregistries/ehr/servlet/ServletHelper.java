package org.immregistries.ehr.servlet;

import org.immregistries.codebase.client.generated.Code;
import org.immregistries.ehr.model.Facility;
import org.immregistries.ehr.model.Patient;
import org.immregistries.ehr.model.Silo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

public class ServletHelper {

    public static void doStandardHeader(PrintWriter out, HttpSession session, HttpServletRequest req) {
        Silo silo = (Silo) session.getAttribute("silo");
        Facility facility = (Facility) session.getAttribute("facility");
        Patient patient = (Patient) session.getAttribute("patient");
        out.println("<html>");
        out.println("  <head>");
        out.println("    <title>EHR Sandbox</title>");
        out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + req.getContextPath() + "/inc/style.css\">");
        out.println("<link href=\"https://fonts.googleapis.com/icon?family=Material+Icons\" rel=\"stylesheet\">");
        out.println("  </head>");
        out.println("  <body>");
        // out.println("<div class=\"w3-container \">");
        out.println("<header >" + "<div class=\"w3-bar w3-light-grey w3-text-shadow w3-margin-bottom\">"
                + "<a href='silos' class=\"w3-bar-item w3-button\">"
                + (silo == null ? "<i class=\"material-icons\" style=\"font-size:22px ; vertical-align: bottom\" >folder</i> Tenants"
                    : "<i class=\"material-icons\" style=\"font-size:22px ; vertical-align: bottom\" >folder_open</i> "
                        + silo.getNameDisplay())
                + "</a>"

                +  (facility == null ? ""
                : "<a href='facility_patient_display?paramFacilityId="+ facility.getFacilityId() +"' class=\"w3-bar-item w3-button\">"
                    + "<i class=\"material-icons\" style=\"font-size:22px ; vertical-align: bottom\" >business</i> "
                    + facility.getNameDisplay())
                    + "</a>"

                + (patient == null ? ""
                    : "<a href='patient_record?paramPatientId=" + patient.getPatientId() + "' class=\"w3-bar-item w3-button\">"
                    + "<i class=\"material-icons\" style=\"font-size:22px ; vertical-align: bottom\" >person</i> "
                    + "" + patient.getNameFirst() + " " + patient.getNameMiddle() + " " + patient.getNameLast()
                    + "</a>")

                + "<a href='authentication' class=\"w3-bar-item w3-right w3-button\">"
                + "<i class=\"material-icons\" style=\"font-size:23px ; vertical-align: bottom\">exit_to_app</i>"
                + "</a>"
                + "<a href='Settings' class=\"w3-bar-item w3-right w3-button w3-green\">Settings</a>"
                + "</div>" + "</header>");
        out.println("<div class=\"w3-display-container w3-margin\" style=\"height:600px;\">");
    }

    public static void doStandardFooter(PrintWriter out, HttpSession session) {
        out.println("</div>" + "    </body>" + "</html>");
    }


    public static void printOpenContainer(PrintWriter out, int width, String direction) {
      out.println("<div class=\"w3-container w3-sand\" " +
              "style=\"width:" + width + "% ;" +
              "display:flex ;" +
  //            "flex-flow: wrap ;" +
              "justify-content: space-around ;" +
              "flex-direction: " + direction + "\">");
    }

    public static void printCloseContainer(PrintWriter out) {
      out.println("</div>");
    }

    public static void printSimpleInput(PrintWriter out, String input, String fieldName, String label, boolean required, int size){
      out.println("<div class=\"w3-margin\">"
              + "<label class=\"w3-text-green\"><b>"
              + label + (required? "<b class=\"w3-text-red\">*</b>" : "")
              + "</b></label>"
              + "<input type=\"text\""
              + " class=\"w3-margin w3-border\" "
              + " value=\"" + input + "\""
              + " name=\"" + fieldName + "\""
              + " size=\"" + size + "\""
              + "/>"
              + "</div>");
    }

    public static void printDateInput(PrintWriter out, Date dateInput, String fieldName, String label, boolean required){
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
      String input = "";
      if (dateInput != null){
        input= sdf.format(dateInput);
      }
      out.println("<div class=\"w3-margin\">"
              + "<label class=\"w3-text-green\"><b>"
              + label + (required? "<b class=\"w3-text-red\">*</b>" : "")
              + "</b></label>"
              + "<input type=\"date\""
              + " class=\"w3-margin w3-border\" "
              + " value=\"" + input + "\""
              + " name=\"" + fieldName + "\""
              + " size=\"12\""
              + "/>"
              + "</div>");
    }

    public static void printSelectForm(PrintWriter out, String value, Collection<Code> codeList, String fieldName, String label, int width) {
        Code code = null;
        for(Code codeItem : codeList) {
            if (codeItem.getValue().equals(value)) {
                code=codeItem;
                break;
            }
        }
        out.println("<div class=\"w3-margin\">"
                + "<label class=\"w3-text-green\"><b>" + label + "</b></label>"
                + "<SELECT style=\"width:" + width + "px\" "
                + "class=\"w3-margin w3-border\" size=\"1\" "
                + "name=\"" + fieldName + "\">");
        out.println( "<OPTION value=\"\" "
                + (code == null ? "selected" : "")
                + ">-Select " + label + "-</Option>");
        for(Code codeItem : codeList) {
            out.println("<OPTION value=\""
                    + codeItem.getValue() + "\" "
                    + (codeItem.equals(code) ? "selected" : "")
                    + " >" /* + codeItem.getValue() */
                    + codeItem.getLabel() + "</Option>");
        }
        out.println( "</SELECT></div>");
    }

    public static void printSelectYesNo(PrintWriter out, String value, String fieldName, String label) {
        out.println("<div class=\"w3-margin\">"
                + "<label class=\"w3-text-green\"><b>" + label + "</b></label>"
                + "<SELECT style=\"width:100px\" "
                +   "class=\"w3-margin w3-border\" size=\"1\" "
                +   "name=\"" + fieldName + "\">");
        out.println("<OPTION value=\"\""
                + (value.equals("") ? "selected" : "")
                + ">Unknown</Option>"
                + "<OPTION value=\"Y\" "
                + (value.equals("Y") ? "selected" : "")
                + ">Y</Option>"
                + "<OPTION value=\"N\">N</Option>");
        out.println( "</SELECT></div>");
    }
}
