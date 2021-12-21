package org.immregistries.ehr.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.hibernate.Query;
import org.hibernate.Session;
import org.immregistries.ehr.model.Facility;
import org.immregistries.ehr.model.Patient;
import org.immregistries.ehr.model.Silo;
import org.immregistries.ehr.model.VaccinationEvent;
import org.immregistries.ehr.model.Vaccine;

/**
 * Servlet implementation class patient_record
 */
public class PatientRecord extends HttpServlet {
  private static final long serialVersionUID = 1L;
  public static final String PARAM_SHOW = "show";

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    doGet(req, resp);
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    HttpSession session = req.getSession(true);
    resp.setContentType("text/html");
    PrintWriter out = new PrintWriter(resp.getOutputStream());
    Session dataSession = PopServlet.getDataSession();
    try {
      {
        doHeader(out, session);
        String show = req.getParameter(PARAM_SHOW);
        Patient patient = new Patient();
        List<Patient> patientList = null;
        List<VaccinationEvent> entryList = null;
        Query query = dataSession.createQuery("from Patient where patientId=?");
        query.setParameter(0, Integer.parseInt(req.getParameter("paramPatientId")));
        patientList = query.list();
        patient = patientList.get(0);
        session.setAttribute("patient", patient);
        query = dataSession.createQuery("from VaccinationEvent where patient=?");
        query.setParameter(0, patient);
        entryList = query.list();
        out.println("<label class=\"w3-text-green\"><b>     Current Patient : "
            + patient.getNameFirst() + "  " + patient.getNameLast() + "</b></label>");
        out.println(
            "  <div class=\"w3-display-left w3-border-green w3-border w3-bar-block w3-margin\"style=\"width:40% ;height:100%;overflow:auto\">\r\n"
                + "<h3>Entries</h3>");


        for (VaccinationEvent entryDisplay : entryList) {
          Vaccine vaccineAdmin = entryDisplay.getVaccine();
          String link = "paramEntryId=" + entryDisplay.getVaccinationEventId();
          out.println("<a href=\'patient_record?" + link
              + "'\"style=\"text-decoration:none;height:20%\" class=\"w3-bar-item w3-button w3-green w3-hover-teal\" \">"
              + vaccineAdmin.getManufacturer() + "</a>");
          }
        out.println(
           "<a href=\'Entry'\"style=\"text-decoration:none;height:20%\" class=\"w3-bar-item w3-button w3-green w3-hover-teal\"  \">"

                + "Entry 1</a>"
                + "<a href=\'Entry'\"style=\"text-decoration:none;height:20%\" class=\"w3-bar-item w3-button w3-green w3-hover-teal\"  \">"

                + "Entry 2</a>" + "</div>"
                + "  <div class=\"w3-display-right w3-margin\"style=\"width:15%\">\r\n "
                + "<button onclick=\"location.href=\'entry_creation'\" style=\"width:100%;height:20%\" class=\"w3-button w3-margin w3-round-large w3-green w3-hover-teal\">Create new entry </button>"
                + "</div\r\n");
        doFooter(out, session);
      }
    } catch (Exception e) {
      e.printStackTrace(System.err);
    }
    out.flush();
    out.close();
  }

  public static void doHeader(PrintWriter out, HttpSession session) {
    out.println("<html>");
    out.println("  <head>");
    out.println("    <title>EHR Sandbox</title>");
    out.println("<link rel=\"stylesheet\" href=\"https://www.w3schools.com/w3css/4/w3.css\">");
    out.println("  </head>");
    out.println("  <body>");
    // out.println("<div class=\"w3-container \">");
    out.println("<header >\r\n" + "<div class=\"w3-bar w3-green w3-margin-bottom\">\r\n"
        + "  <a href = \'silos \' class=\"w3-bar-item w3-button\">List of silos </a>\r\n"
        + "  <a href = \'facility_patient_display\' class=\"w3-bar-item w3-button\">Facilities/patients list</a>\r\n"
        + "  <a href = \'silo_creation\' class=\"w3-bar-item w3-button\">Silo creation </a>\r\n"
        + "</div>" + "    	</header>");
    out.println("<div class=\"w3-display-container w3-margin\" style=\"height:600px;\">");
  }


  public static void doFooter(PrintWriter out, HttpSession session) {
    out.println("</div>\r\n" + "    </body>\r\n" + "</html>");
  }

}
