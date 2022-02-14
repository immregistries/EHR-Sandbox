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
      
        doHeader(out, session);
        String show = req.getParameter(PARAM_SHOW);
        Patient patient = new Patient();
        List<Patient> patientList = null;
        List<VaccinationEvent> entryList = null;
        Silo silo = (Silo) session.getAttribute("silo");
        if(req.getParameter("paramPatientId")!=null && silo!=null) {
          Query query = dataSession.createQuery("from Patient where patient_id=? and silo_id=?");
          query.setParameter(0, Integer.parseInt(req.getParameter("paramPatientId")));
          query.setParameter(1, silo.getSiloId());
          patientList = query.list();
          patient = patientList.get(0);
          session.setAttribute("patient", patient);
        }
        if(session.getAttribute("patient")!=null) {
          patient = (Patient) session.getAttribute("patient");
        }
        if(session.getAttribute("facility")==null) {
          session.setAttribute("facility", patient.getFacility());         
        }
        Query query = dataSession.createQuery("from VaccinationEvent where patient=?");
        query.setParameter(0, patient);
        entryList = query.list();
        
        
       
        resp.setContentType("text/html");
        
        
        Tester tester = (Tester) session.getAttribute("tester");
        
        List<Silo> siloList = null;
        String siloId = req.getParameter("paramSiloId");
        if (siloId != null) {
          query = dataSession.createQuery("from Silo where siloId=? and tester_id=?");
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
        query = dataSession.createQuery("from Facility where silo=?");
        query.setParameter(0, silo);
        facilityList = query.list();
      
      
        
        out.println("<div class=\"w3-margin-bottom\"style=\"width:100% height:auto \" >"
            + "<label class=\"w3-text-green w3-margin-right w3-margin-bottom\"><b>Current tenant : "
            + silo.getNameDisplay() + "</b></label>");
        Facility facility = new Facility();
        
          
          List<Facility> currentFacility = null;
          query = dataSession.createQuery("from Facility where facilityId=?");
          query.setParameter(0, facilityList.get(0).getFacilityId());
          currentFacility = query.list();
          facility = currentFacility.get(0);
          session.setAttribute("facility", facility);
          query = dataSession.createQuery("from Patient where facility=?");
          query.setParameter(0, facility);
          
          out.println( "<label class=\"w3-text-green w3-margin-left w3-margin-bottom\"><b>Current Facility : "
                  + facility.getNameDisplay() + "</b></label>");
        
        
        out.println(
             "<label class=\"w3-text-green w3-margin-left \"><b>     Current Patient : "
            + patient.getNameFirst() + "  " + patient.getNameLast() + "</b></label>"+"</div>"
            );
        out.println( "<div class=\"w3-left\" style=\"width:45%\">"
            + "<table class=\"w3-table-all\"style=\"width:100% ;overflow:auto\">"
                + "<thead>"
                + "<tr class=\"w3-green\">"
                + "<th> Entries</th>"
                + "</thead>"
                + "<tbody>");


        for (VaccinationEvent entryDisplay : entryList) {
          Vaccine vaccineAdmin = entryDisplay.getVaccine();
          String link = "paramEntryId=" + entryDisplay.getVaccinationEventId();
          out.println("<tr>"
              +"<td class = \"w3-hover-teal\">"
              + "<a href='entry_record?" + link+ "' style=\"text-decoration:none\">"
              + "<div style=\"text-decoration:none;height:100%\">"  
              + vaccineAdmin.getVaccineCvxCode() + "   "+vaccineAdmin.getVaccineMvxCode()+ "   "+vaccineAdmin.getVaccineNdcCode()
              + "</div>"
              + "</a>"              
              + "</td>"
              + "</tr>");
          }
        String link = "paramPatientId=" + patient.getPatientId();
        out.println(
                "</table>"
                + "</div>"
                + "<div class=\"w3-display-right w3-margin\"style=\"width:15%\">\r\n "
                + "<button onclick=\"location.href='entry_creation?" +link+"'\" style=\"width:100%;height:20%\" class=\"w3-button w3-margin w3-round-large w3-green w3-hover-teal\">Create new entry </button>"

                + "<button onclick=\"location.href='patient_modification?" +link+"'\" style=\"width:100%;height:20%\" class=\"w3-button w3-margin w3-round-large w3-green w3-hover-teal\">Modify patient</button>"
                + "<button onclick=\"location.href='FHIR_messaging?patientOnly=1'\" style=\"width:100%;height:20%\" class=\"w3-button w3-margin w3-round-large w3-green w3-hover-teal\">FHIR Messaging</button>"

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
        + "  <a href = 'silos ' class=\"w3-bar-item w3-button\">List of tenants </a>\r\n"
        + "  <a href = 'facility_patient_display' class=\"w3-bar-item w3-button\">Facilities/patients list</a>\r\n"
        + "  <a href = 'Settings' class=\"w3-bar-item w3-right w3-button\">Settings </a>\r\n"
        + "</div>" + "    	</header>");
    out.println("<div class=\"w3-display-container w3-margin\" style=\"height:600px;\">");
  }


  public static void doFooter(PrintWriter out, HttpSession session) {
    out.println("</div>\r\n" + "    </body>\r\n" + "</html>");
  }

  public static String breadCrumbs(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    
    String html = "";
    
    HttpSession session = req.getSession(true);
    resp.setContentType("text/html");
    PrintWriter out = new PrintWriter(resp.getOutputStream());
    Session dataSession = PopServlet.getDataSession();
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
  
  String showFacility = null;
  if (req.getParameter("paramFacilityId") != null) {
    showFacility = req.getParameter("paramFacilityId");
    
    html+="<div class=\"w3-margin-bottom\"style=\"width:100% height:auto \" >"
        + "<label class=\"w3-text-green w3-margin-right w3-margin-bottom\"><b>Current Silo : "
        + silo.getNameDisplay() + "</b></label>";
    Facility facility = new Facility();
    if (showFacility != null) {
      
      List<Facility> currentFacility = null;
      query = dataSession.createQuery("from Facility where facilityId=?");
      query.setParameter(0, Integer.parseInt(showFacility));
      currentFacility = query.list();
      facility = currentFacility.get(0);
      session.setAttribute("facility", facility);
      query = dataSession.createQuery("from Patient where facility=?");
      query.setParameter(0, facility);
      
    }
    if (facility != null) {
      html+=
          "<label class=\"w3-text-green w3-margin-left w3-margin-bottom\"><b>Current Facility : "
              + facility.getNameDisplay() + "</b></label>";
    }
  }
  return html;
  }
  
}
