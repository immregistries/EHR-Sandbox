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

      ServletHelper.doStandardHeader(out, req, "Facility and patient selection");
      session.setAttribute("patient", null);
      Tester tester = (Tester) session.getAttribute("tester");

      Silo silo = (Silo) session.getAttribute("silo");

      String siloId = req.getParameter("paramSiloId");
      if (siloId != null) {
        if (silo != null) {
          if (silo.getSiloId() != Integer.parseInt(siloId)){
            Query query = dataSession.createQuery("from Silo where siloId=? and tester_id=?");
            query.setParameter(0, Integer.parseInt(siloId));
            query.setParameter(1, tester.getTesterId());
            silo = (Silo) query.uniqueResult();
            session.setAttribute("silo", silo);
            // Reload page to update header
            resp.sendRedirect("facility_patient_display?paramSiloId=" + siloId);
          }
        } else {
          Query query = dataSession.createQuery("from Silo where siloId=? and tester_id=?");
          query.setParameter(0, Integer.parseInt(siloId));
          query.setParameter(1, tester.getTesterId());
          silo = (Silo) query.uniqueResult();
          session.setAttribute("silo", silo);
          // Reload page to update header
          resp.sendRedirect("facility_patient_display?paramSiloId=" + siloId);
        }
      }else {
        resp.sendRedirect("facility_patient_display?paramSiloId=" + silo.getSiloId());
      }

      if (silo == null) {
        resp.sendRedirect("silos?chooseSilo=1");
      }

      Query query = dataSession.createQuery("from Facility where silo=?");
      query.setParameter(0, silo);
      List<Facility> facilityList = query.list();

      query = dataSession.createQuery("from Patient where silo=?");
      query.setParameter(0, silo);
      List<Patient> patientList = query.list();
      String facilityId =  req.getParameter("paramFacilityId");

      Facility facility = (Facility) session.getAttribute("facility");
      if (facilityId != null) { // if facilityId parameter specified i.e. facility selected
        if (facility != null) {
          if (facility.getFacilityId() != Integer.parseInt(facilityId)){
            query = dataSession.createQuery("from Facility where facilityId=? and silo=?");
            query.setParameter(0, Integer.parseInt(facilityId));
            query.setParameter(1, silo);
            facility = (Facility) query.uniqueResult();
            session.setAttribute("facility", facility);
            resp.sendRedirect("facility_patient_display?paramFacilityId=" + facilityId);
          }
        } else {
          query = dataSession.createQuery("from Facility where facilityId=? and silo=?");
          query.setParameter(0, Integer.parseInt(facilityId));
          query.setParameter(1, silo);
          facility = (Facility) query.uniqueResult();
          session.setAttribute("facility", facility);
          resp.sendRedirect("facility_patient_display?paramFacilityId=" + facilityId);
        }
      }

      if (facility != null){
        query = dataSession.createQuery("from Patient where facility=?");
        query.setParameter(0, facility);
        patientList = query.list();
      }

      String show = req.getParameter(PARAM_SHOW);
      if (req.getParameter("chooseFacility") != null) {
        out.println("<div class=\"w3-margin-bottom\"style=\"width:100% height:auto \" >"+
                "<label class=\"w3-text-red w3-margin-bottom \"><b>Choose a facility</b></label><br/>"
               +"</div>" );
      }


// ---------------- Facility list ------------------
      out.println("<div class=\"w3-left \"style=\"width:45%\">"
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

        out.println("<a href='facility_patient_display?" + link + "' style=\"text-decoration:none\">"
            + "<div style=\"text-decoration:none;height:100%\">"
            + facilityDisplay.getNameDisplay()
            + "</div>"
            + "</a>"
            + "</td>"
            + "</tr>");
      }
      out.println("</tbody></table>");
      out.println("<button onclick=\"location.href='facility_creation'\"  class=\"w3-button w3-margin w3-round-large w3-green w3-hover-teal w3-left\">Create new facility</button>");
      out.println("</div>");
// ---------------- Patient list ------------------
      out.println("<div class=\"w3-right \"style=\"width:45%\">"
          + "<table class=\"w3-table-all\">"
          + "<thead>"
          + "<tr class=\"w3-green\">"
          + "<th>" + (facility == null ? "All Patients" : facility.getNameDisplay() + "'s patients" )  + "</th>"
          + "</thead>"
          + "<tbody>");
      for (Patient patientDisplay : patientList) {
        String link = "paramPatientId=" + patientDisplay.getPatientId();
        out.println("<tr>"
            + "<td class = \"w3-hover-teal\">"
            + "<a href='patient_record?" + link + "' style=\"text-decoration:none\";>"
            + "<div style=\"text-decoration:none;height:100%\">"
            + patientDisplay.getNameFirst() + " " + patientDisplay.getNameLast()
            + "</div>"
            + "</a>"
            + "</td>"
            + "</tr>");
      }

      out.println("</tbody></table>");
      if(facility!=null){
        out.println("<button onclick=\"location.href='patient_form'\" class=\"w3-button w3-margin w3-round-large w3-green w3-hover-teal\">Create new patient </button>");
      }
      out.println("</div>");

      out.println("<div class=\"w3-display-bottommiddle w3-margin\"style=\"height:5%\">");
      out.println("</div>");

      ServletHelper.doStandardFooter(out, session);

    } catch (Exception e) {
      e.printStackTrace(System.err);
    }
    out.flush();
    out.close();
  }

}