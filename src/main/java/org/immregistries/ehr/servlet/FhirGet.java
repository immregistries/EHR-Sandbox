package org.immregistries.ehr.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import ca.uhn.fhir.parser.IParser;

import org.hl7.fhir.r4.model.Immunization;
import org.immregistries.ehr.fhir.CustomClientBuilder;
import org.immregistries.ehr.fhir.FhirImmunizationCreation;
import org.immregistries.ehr.fhir.FhirPatientCreation;
import org.immregistries.ehr.fhir.ResourceClient;
import org.immregistries.ehr.model.Facility;
import org.immregistries.ehr.model.Patient;
import org.immregistries.ehr.model.Tester;
import org.immregistries.ehr.model.VaccinationEvent;

/**
 * Servlet implementation class FHIR_Get
 */
public class FhirGet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  public static final String PARAM_SHOW = "show";

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    HttpSession session = req.getSession(true);
    String id;

    String resourceType = req.getParameter("resourceType");
    switch(resourceType){
      case "Patient":{
        id = req.getParameter("fhirPatientId");
        String fhirPatientResponse = "";
        try {
          fhirPatientResponse = ResourceClient.read(resourceType, id);
        } catch (Exception e) {
          e.printStackTrace();
          fhirPatientResponse = "ERROR";
        }
        session.setAttribute("fhirPatientResponse", fhirPatientResponse);
        break;
      }
      case "Immunization":{
        id = req.getParameter("fhirImmunizationId");
        String fhirImmunizationResponse = "";
        try {    
          fhirImmunizationResponse = ResourceClient.read(resourceType, id);
        } catch (Exception e) {
          e.printStackTrace();
          fhirImmunizationResponse = "ERROR";
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
        out.println("<form method=\"POST\"  target=\"FHIR_get\">");
        // IIS authentication form
        // doLoginForm(out, session, req);

        out.println("<div class=\"w3-margin w3-left\" style=\"width:45%\">");
        doPatientForm(out, session, req);
        out.println("</div>");

        out.println("<div class=\"w3-margin w3-right\" style=\"width:45%\">");
        doImmunizationForm(out, session, req);
        out.println("</div>");

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
    FhirMessaging.doLoginForm(out, session, req);
  }

  private static void doPatientForm(PrintWriter out, HttpSession session, HttpServletRequest req) throws ParseException {
    Facility facility = new Facility();
    Patient patient = new Patient();

    String fhirPatientResponse = " ";

    patient = (Patient) session.getAttribute("patient");
    fhirPatientResponse = (String) session.getAttribute("fhirPatientResponse");
    String fhirPatientId = "";

    { // Patient
      out.println("<textarea class =\"w3-border w3-border-green\" id=\"story\" style=\"width:75%\" name=\"fhirPatientId\"\r\n"
        + "rows=\"1\" cols=\"12\">\r\n"
        + fhirPatientId
        + "</textarea>");
      out.println("<button class=\"w3-button w3-round-large w3-green w3-hover-teal w3-margin\"" 
        + " type=\"submit\"  name=\"resourceType\" value=\"Patient\">GET</button>\r\n");
      if (fhirPatientResponse != null) {
        out.println("<label class=\"w3-text-red w3-margin w3-margin-bottom\">"
          + fhirPatientResponse + "</label>");
      }
    }
  }

  private static void doImmunizationForm(PrintWriter out, HttpSession session, HttpServletRequest req) throws ParseException {
    VaccinationEvent vacc_ev = new VaccinationEvent();


    String fhirImmunizationResponse = " ";
    IParser parser = CustomClientBuilder.getCTX().newXmlParser().setPrettyPrint(true);


    vacc_ev = (VaccinationEvent) session.getAttribute("vacc_ev");
    fhirImmunizationResponse = (String) session.getAttribute("fhirImmunizationResponse");
    String fhirImmunizationId = "";

    { // Patient
      out.println("<textarea class =\"w3-border w3-border-green\" id=\"story\" style=\"width:75%\" name=\"fhirImmunizationId\"\r\n"
        + "rows=\"1\" cols=\"12\">\r\n"
        + fhirImmunizationId
        + "</textarea>");
      out.println("<button class=\"w3-button w3-round-large w3-green w3-hover-teal w3-margin\"" 
        + " type=\"submit\"  name=\"resourceType\" value=\"Immunization\">GET</button>\r\n");
      if (fhirImmunizationResponse != null) {
        out.println("<label class=\"w3-text-red w3-margin w3-margin-bottom\">"
          + fhirImmunizationResponse + "</label>");
      }
    }

  }
}
