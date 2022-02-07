package org.immregistries.ehr.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.immregistries.ehr.model.Facility;
import org.immregistries.ehr.model.Patient;
import org.immregistries.ehr.model.Silo;
import org.immregistries.ehr.model.Tester;

/**
 * Servlet implementation class FacilityPatientDisplay
 */
public class FacilityPatientDisplay extends HttpServlet {
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

      doHeader(out, session);
      session.setAttribute("patient", null);
      Tester tester = (Tester) session.getAttribute("tester");
      Silo silo = new Silo();
      List<Silo> siloList = null;
      String siloId = req.getParameter("paramSiloId");
      if (siloId != null) {
        Query query = dataSession.createQuery("from Silo where siloId=? and tester_id=?");
        query.setParameter(0, Integer.parseInt(siloId));
        query.setParameter(1, tester.getTesterId());
        siloList = query.list();
        silo = siloList.get(0);
        session.setAttribute("silo", silo);
      } else {
        if (session.getAttribute("silo")!=null) {
          silo = (Silo) session.getAttribute("silo");
        }
        else {
          resp.sendRedirect("silos?chooseSilo=1");
        }
        
      }
      List<Facility> facilityList = null;
      Query query = dataSession.createQuery("from Facility where silo=?");
      query.setParameter(0, silo);
      facilityList = query.list();

      List<Patient> patientList = null;
      query = dataSession.createQuery("from Patient where silo=?");
      query.setParameter(0, silo);
      patientList = query.list();
      String facilityId = null;
      Facility facility = null;

      if (req.getParameter("paramFacilityId") != null) {
        facilityId = req.getParameter("paramFacilityId");
        patientList = null;
        List<Facility> facilityListQuery = null;
        query = dataSession.createQuery("from Facility where facilityId=?");
        query.setParameter(0, Integer.parseInt(facilityId));

        facilityListQuery = query.list();

        facility = facilityListQuery.get(0);
        session.setAttribute("facility", facility);
        query = dataSession.createQuery("from Patient where facility=?");
        query.setParameter(0, facility);
        patientList = query.list();
      }

      String show = req.getParameter(PARAM_SHOW);
      String noFacility = "";
      if (req.getParameter("chooseFacility") != null) {
        out.println(
                "<div class=\"w3-margin-bottom\"style=\"width:100% height:auto \" >"+
                "<label class=\"w3-text-red w3-margin-bottom \"><b>Choose a facility</b></label><br/>"
               +"</div>" );
      }
      
      out.print("<div class=\"w3-margin-bottom\"style=\"width:100% height:auto \" >"
          + "<label class=\"w3-text-green w3-margin-right w3-margin-bottom\"><b>Current Tenant : "
          + silo.getNameDisplay() + "</b></label>");
      // if (facility != null) {
      //   out.println(
      //       "<label class=\"w3-text-green w3-margin-left w3-margin-bottom\"><b>Current Facility : "
      //           + facility.getNameDisplay() + "</b></label>");
      // }

// ---------------- Facility list ------------------
      out.println(
              "</div>"
              + "<div class=\"w3-left \"style=\"width:45%\">"
              + "<table class=\"  w3-table-all \" >"
              + "<thead>"
              + "<tr class=\"w3-green\">"
              + "<th> Facilities</th>"
              + "</thead>"
              + "<tbody>");
      for (Facility facilityDisplay : facilityList) {
        String link = "paramFacilityId=" + facilityDisplay.getFacilityId();
        out.println("<tr>");
        // Defining style of the tile depending on whether facility is selected
        if (facility == null) {
          out.println("<td class = \"w3-hover-teal\">");
        } else {
          out.println("<td class = \""
              + ((facilityDisplay.getFacilityId() == facility.getFacilityId()) ? "w3-hover-cyan  w3-light-green" : "w3-hover-teal")
              + "\">");
        }

        out.println("<a href=\'facility_patient_display?" + link + "\' style=\"text-decoration:none\">"
            + "<div style=\"text-decoration:none;height:100%\">"
            + facilityDisplay.getNameDisplay()
            + "</div>"
            + "</a>"
            + "</td>"
            + "</tr>");
      }
// ---------------- Patient list ------------------
      out.println("</tbody>"
          + "</table>"
          + "</div>"
          + "<div class=\"w3-right \"style=\"width:45%\">"
          + "<table class=\" w3-table-all  \">\r\n"
          + "<thead>"
          + "<tr class=\"w3-green\">"
          + "<th>Patients </th>"
          + "</thead>"
          + "<tbody>");
      for (Patient patientDisplay : patientList) {
        String link = "paramPatientId=" + patientDisplay.getPatientId();
        out.println("<tr>"
            + "<td class = \"w3-hover-teal\">"
            + "<a href=\'patient_record?" + link + "\' style=\"text-decoration:none\";>"
            + "<div style=\"text-decoration:none;height:100%\">"
            + patientDisplay.getNameFirst() + " " + patientDisplay.getNameLast()
            + "</div>"
            + "</a>"
            + "</td>"
            + "</tr>");
      }
      if (session.getAttribute("facility") == null) {
        noFacility = "?noFacility=1";
      }
      out.println("</tbody>"
          + "</table>"
          + "</div>"
          + "  <div class=\"w3-display-bottommiddle w3-margin\"style=\"height:5%\">\r\n "
          + "<button onclick=\"location.href=\'facility_creation'\"  class=\"w3-button w3-margin w3-round-large w3-green w3-hover-teal\">Create new facility</button>"
          + "<button onclick=\"location.href=\'patient_creation"
          + "'\"  class=\"w3-button w3-margin w3-round-large w3-green w3-hover-teal\">Create new patient </button>"

          + "</div\r\n");

      doFooter(out, session);

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
        + "  <a href = \'silos \' class=\"w3-bar-item w3-button\">List of tenants </a>\r\n"
        + "  <a href = \'facility_patient_display\' class=\"w3-bar-item w3-button\">List of patients</a>\r\n"
        
        + "  <a href = \'Settings\' class=\"w3-bar-item w3-right w3-button\">Settings </a>\r\n"
        + "</div>" + "      </header>");
    out.println("<div class=\"w3-display-container w3-margin\" style=\"height:600px;\">");
  }
  public static void doFooter(PrintWriter out, HttpSession session) {
    out.println("</div>\r\n" + "    </body>\r\n" + "</html>");
  }

}