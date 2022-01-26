package org.immregistries.ehr.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import ca.uhn.fhir.parser.IParser;

import org.hl7.fhir.r4.model.Immunization;
import org.immregistries.ehr.FhirImmunizationCreation;
import org.immregistries.ehr.FhirPatientCreation;
import org.immregistries.ehr.HL7printer;
import org.immregistries.ehr.fhir.CustomClientBuilder;
import org.immregistries.ehr.fhir.ResourceClient;
import org.immregistries.ehr.model.Clinician;
import org.immregistries.ehr.model.Facility;
import org.immregistries.ehr.model.LogsOfModifications;
import org.immregistries.ehr.model.Patient;
import org.immregistries.ehr.model.Silo;
import org.immregistries.ehr.model.Tester;
import org.immregistries.ehr.model.VaccinationEvent;
import org.immregistries.ehr.model.Vaccine;
import org.immregistries.iis.kernal.model.CodeMapManager;
import io.github.linuxforhealth.hl7.HL7ToFHIRConverter;

/**
 * Servlet implementation class FHIR_messaging
 */
public class FhirMessaging extends HttpServlet {
  private static final long serialVersionUID = 1L;
  public static final String PARAM_SHOW = "show";

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    HttpSession session = req.getSession(true);

    IParser parser = CustomClientBuilder.getCTX().newXmlParser().setPrettyPrint(true);
    String resourceType = req.getParameter("resourceType");
    switch(resourceType){
      case "patient":{
        String fhirPatientString = req.getParameter("fhirPatientString");
        String fhirPatientResponse = "";
        try {
          org.hl7.fhir.r4.model.Patient fhirPatient = (org.hl7.fhir.r4.model.Patient) parser
              .parseResource(fhirPatientString);
          fhirPatientResponse = (String) session.getAttribute("fhirPatientResponse");
    
          fhirPatientResponse = ResourceClient.write(fhirPatient);
        } catch (Exception e) {
          // TODO: handle exception
          fhirPatientResponse = "LOCAL PARSING ERROR : Invalid Resource";
        }
        session.setAttribute("fhirPatientResponse", fhirPatientResponse);
        break;
      }
      case "immunization":{
        String fhirImmunizationString = req.getParameter("fhirImmunizationString");
        String fhirImmunizationResponse = "";
        try {
          org.hl7.fhir.r4.model.Immunization fhirImmunization = (org.hl7.fhir.r4.model.Immunization) parser
              .parseResource(fhirImmunizationString);
          fhirImmunizationResponse = (String) session.getAttribute("fhirImmunizationResponse");
    
          fhirImmunizationResponse = ResourceClient.write(fhirImmunization);
        } catch (Exception e) {
          // TODO: handle exception
          fhirImmunizationResponse = "LOCAL PARSING ERROR : Invalid Resource";
        }
        session.setAttribute("fhirImmunizationResponse", fhirImmunizationResponse);
        break;
      }
    }
    doGet(req, resp);
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    HttpSession session = req.getSession(true);
    resp.setContentType("text/html");


    PrintWriter out = new PrintWriter(resp.getOutputStream());
    try {
      {
        doHeader(out, session, req);

        out.println("<div id=\"formulaire\">");
        out.println("<form method=\"POST\"  target=\"FHIR_Messaging\">");
        // IIS authentication form
        doLoginForm(out, session, req);

        out.println("<div class=\"w3-margin w3-left\" style=\"width:45%\">");
        doPatientForm(out, session, req);
        out.println("</div>");

        if (req.getHeader("paramEntryId") == null) { // Immunization
          out.println("<div class=\"w3-margin w3-right\" style=\"width:45%\">");
          doImmunizationForm(out, session, req);
          out.println("</div>");
        }

        out.println("</form></div>");
        doFooter(out, session);
      }
    } catch (Exception e) {
      e.printStackTrace(System.err);
    }
    out.flush();
    out.close();
  }

  public static void doHeader(PrintWriter out, HttpSession session, HttpServletRequest req) throws ParseException {
    out.println("<html>");
    out.println("  <head>");
    out.println("    <title>EHR Sandbox</title>");
    out.println("<link rel=\"stylesheet\" href=\"https://www.w3schools.com/w3css/4/w3.css\">");
    out.println("  </head>");
    out.println("  <body>");
    out.println("<header >\r\n" + "<div class=\"w3-bar w3-green w3-margin-bottom\">\r\n"
        + "  <a href = 'silos ' class=\"w3-bar-item w3-button\">List of silos </a>"
        + "  <a href = 'facility_patient_display' class=\"w3-bar-item w3-button\">Facilities/patients list</a>\r\n"
        + "  <a href = 'silo_creation' class=\"w3-bar-item w3-button\">Silo creation </a> \r\n"
        + "</div>\r\n" 
        + "</header>");

  }

  public static void doFooter(PrintWriter out, HttpSession session) {
    out.println("</div>\r\n" + "    </body>\r\n" + "</html>");
  }

  private static void doLoginForm(PrintWriter out, HttpSession session, HttpServletRequest req) throws ParseException {
    Tester tester = new Tester();
    Facility facility = new Facility();

    tester = (Tester) session.getAttribute("tester");
    facility = (Facility) session.getAttribute("facility");
    
    out.println("<div>");
    out.println("<div class=\"w3-margin w3-left\" style=\"width:30%\">"
        + " <label class=\"w3-text-green\"><b>IIS UserID</b></label>"
        + "<input type=\"text\"  class = \"w3-input w3-margin w3-border\" hidden value=\""
        + tester.getLoginUsername()
        + "\" style=\"width:75%\" name=\"USERID\"/>\r\n</div>");
    out.println("<div class=\"w3-margin w3-left\" style=\"width:30%\">"
        + " <label class=\"w3-text-green\"><b>IIS Password</b></label>"
        + "<input type=\"password\"  class=\"w3-input w3-margin w3-border\" hidden value=\""
        + tester.getLoginPassword()
        + "\" style =\"width:75%\" name=\"PASSWORD\"/>\r\n</div>");
    out.println("<div class=\"w3-margin w3-left\" style=\"width:30%\">"
        + " <label class=\"w3-text-green\"><b>Facility ID</b></label>"
        + "<input type=\"text\"  class=\"w3-input w3-margin w3-border\" hidden value=\""
        + facility.getNameDisplay()
        + "\" style =\"width:75%\" name=\"FACILITYID\"/>\r\n</div>");
    out.println("</div>");
  }

  private static void doPatientForm(PrintWriter out, HttpSession session, HttpServletRequest req) throws ParseException {
    Facility facility = new Facility();
    Patient patient = new Patient();

    String fhirPatientResponse = " ";
    IParser parser = CustomClientBuilder.getCTX().newXmlParser().setPrettyPrint(true);


    patient = (Patient) session.getAttribute("patient");
    fhirPatientResponse = (String) session.getAttribute("fhirPatientResponse");
    String fhirPatientString = "";
    if (req.getAttribute("fhirPatientString") != null) {
      fhirPatientString = req.getParameter("fhirPatientString");
    } else {
      try {
        fhirPatientString = parser.encodeResourceToString(FhirPatientCreation.dbPatientToFhirPatient(patient));
      } catch (Exception e) {
        fhirPatientResponse = "Invalid Resource";
      }
    }

    { // Patient
      out.println("<textarea class =\"w3-border w3-border-green\" id=\"story\" style=\"width:75%\" name=\"fhirPatientString\"\r\n"
        + "rows=\"20\" cols=\"200\">\r\n"
        + fhirPatientString
        + "</textarea><br/>");
      out.println("<button class=\"w3-button w3-round-large w3-green w3-hover-teal w3-margin\"" 
        + " type=\"submit\"  name=\"resourceType\" value=\"patient\">send FHIR Patient to IIS</button>\r\n");
      if (fhirPatientResponse != null) {
        out.println("<label class=\"w3-text-red w3-margin w3-margin-bottom\">"
          + fhirPatientResponse + "</label><br/>");
      }
    }
  }

  private static void doImmunizationForm(PrintWriter out, HttpSession session, HttpServletRequest req) throws ParseException {
    VaccinationEvent vacc_ev = new VaccinationEvent();


    String fhirImmunizationResponse = " ";
    IParser parser = CustomClientBuilder.getCTX().newXmlParser().setPrettyPrint(true);


    vacc_ev = (VaccinationEvent) session.getAttribute("vacc_ev");
    fhirImmunizationResponse = (String) session.getAttribute("fhirImmunizationResponse");
    String fhirImmunizationString = "";
    if (req.getAttribute("fhirImmunizationString") != null) {
      fhirImmunizationString = req.getParameter("fhirImmunizationString");
    } else {
      try { 
        Immunization immunization = FhirImmunizationCreation.dbVaccinationToFhirVaccination(vacc_ev);
        fhirImmunizationString = parser.encodeResourceToString(immunization);
      } catch (Exception e) {
        e.printStackTrace();
        fhirImmunizationResponse = "Invalid Resource";
      }
    }

    { // Patient
      out.println("<textarea class =\"w3-border w3-border-green\" id=\"story\" style=\"width:75%\" name=\"fhirImmunizationString\"\r\n"
        + "rows=\"20\" cols=\"200\">\r\n"
        + fhirImmunizationString
        + "</textarea><br/>");
      out.println("<button class=\"w3-button w3-round-large w3-green w3-hover-teal w3-margin\"" 
        + " type=\"submit\"  name=\"resourceType\" value=\"patient\">send FHIR Immunization to IIS</button>\r\n");
      if (fhirImmunizationResponse != null) {
        out.println("<label class=\"w3-text-red w3-margin w3-margin-bottom\">"
          + fhirImmunizationResponse + "</label><br/>");
      }
    }

  }



}
