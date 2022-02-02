package org.immregistries.ehr.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import ca.uhn.fhir.parser.IParser;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hl7.fhir.r4.model.Immunization;
import org.immregistries.ehr.fhir.CustomClientBuilder;
import org.immregistries.ehr.fhir.FhirImmunizationCreation;
import org.immregistries.ehr.fhir.FhirPatientCreation;
import org.immregistries.ehr.fhir.ResourceClient;
import org.immregistries.ehr.model.Facility;
import org.immregistries.ehr.model.ImmunizationRegistry;
import org.immregistries.ehr.model.Patient;
import org.immregistries.ehr.model.Tester;
import org.immregistries.ehr.model.VaccinationEvent;

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
    String operationType = req.getParameter("operationType");

    String fhirResponse = "";
    String fhirResourceString = req.getParameter("fhir"+ resourceType +"String");

    switch(resourceType){
      case "Patient":{
        try {
          org.hl7.fhir.r4.model.Patient fhirPatient = (org.hl7.fhir.r4.model.Patient) parser
              .parseResource(fhirResourceString);
          switch (operationType) {
            case "POST":
              fhirResponse = ResourceClient.write(fhirPatient);
              break;
            case "PUT" :
              fhirResponse = ResourceClient.update(fhirPatient, fhirPatient.getId());
              break;
          }
        } catch (Exception e) {
          e.printStackTrace(); // TODO Deal with more errors
          fhirResponse = "LOCAL PARSING ERROR : Invalid Resource";
        }
        break;
      }
      case "Immunization":{
        try {
          org.hl7.fhir.r4.model.Immunization fhirImmunization = (org.hl7.fhir.r4.model.Immunization) parser
              .parseResource(fhirResourceString);
            switch (operationType) {
              case "POST":
                fhirResponse = ResourceClient.write(fhirImmunization);
                break;
              case "PUT" :
                fhirResponse = ResourceClient.update(fhirImmunization, fhirImmunization.getId());
                break;
            }
        } catch (Exception e) {
          e.printStackTrace();
          fhirResponse = "LOCAL PARSING ERROR : Invalid Resource";
        }
        break;
      }
    }
    List<String> fhirResponseList = (List<String>) session.getAttribute("fhir"+ resourceType +"ResponseList");
    fhirResponseList.add(fhirResponse);
    session.setAttribute("fhir"+ resourceType +"ResponseList", fhirResponseList);
    // resp.sendRedirect(req.getHeader("referer"));
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

        out.println("<div class=\"w3-margin w3-left\" style=\"width:45%\">");
        doPatientForm(out, session, req);
        out.println("</div>");

        if (req.getParameter("paramEntryId") != null) { // Immunization
          out.println("<div class=\"w3-margin w3-right\" style=\"width:45%\">");
          doImmunizationForm(out, session, req);
          out.println("</div>");
        }

        out.println("</div>");
        doFooter(out, session);
      }
    } catch (Exception e) {
      e.printStackTrace(System.err);
    }
    out.flush();
    out.close();
  }

  public static void doHeader(PrintWriter out, HttpSession session, HttpServletRequest req) {
    out.println("<html>");
    out.println("  <head>");
    out.println("    <title>EHR Sandbox</title>");
    out.println("<link rel=\"stylesheet\" href=\"https://www.w3schools.com/w3css/4/w3.css\">");
    out.println("  </head>");
    out.println("  <body>");
    // out.println("<div class=\"w3-container \">");
    out.println("<header >\r\n" + "<div class=\"w3-bar w3-green w3-margin-bottom\">\r\n"
        + "  <a href = \'silos \' class=\"w3-bar-item w3-button\">List of tenants </a>\r\n"
        + "  <a href = \'facility_patient_display\' class=\"w3-bar-item w3-button\">Facilities/patients list</a>\r\n"
        
        + "  <a href = \'Settings\' class=\"w3-bar-item w3-right w3-button\">Settings </a>\r\n"
        + "</div>" + "      </header>");
    out.println("<div class=\"w3-display-container w3-margin\" style=\"height:600px;\">");
  }

  public static void doFooter(PrintWriter out, HttpSession session) {
    out.println("</div>\r\n" + "    </body>\r\n" + "</html>");
  }


  private static void doPatientForm(PrintWriter out, HttpSession session, HttpServletRequest req) throws ParseException {
    out.println("<form method=\"POST\">");
    out.println("<input type=\"hidden\" name=\"resourceType\" value=\"Patient\">");

    Facility facility = new Facility();
    Patient patient = new Patient();

    List<String> fhirPatientResponseList = (List<String>) session.getAttribute("fhirPatientResponseList");
    if (fhirPatientResponseList == null){
      fhirPatientResponseList = new ArrayList<String>();
      session.setAttribute("fhirPatientResponseList", fhirPatientResponseList);
    }
    IParser parser = CustomClientBuilder.getCTX().newXmlParser().setPrettyPrint(true);

    patient = (Patient) session.getAttribute("patient");
    String fhirPatientString = "";
    if (req.getAttribute("fhirPatientString") != null) {
      fhirPatientString = req.getParameter("fhirPatientString");
    } else {
      try {
        fhirPatientString = parser.encodeResourceToString(FhirPatientCreation.dbPatientToFhirPatient(patient));
      } catch (Exception e) {
        fhirPatientResponseList.add("Parsing Error : Invalid Resource");
      }
    }

    { // Patient
      out.println("<textarea class =\"w3-border w3-border-green\" id=\"story\" style=\"width:75%\" name=\"fhirPatientString\"\r\n"
        + "rows=\"20\" cols=\"200\">\r\n"
        + fhirPatientString
        + "</textarea><br/>");
        out.println("<button class=\"w3-button w3-round-large w3-green w3-hover-teal w3-margin\"" 
        + " type=\"submit\"  name=\"operationType\" value=\"POST\">Send Patient to IIS</button>\r\n");
      out.println("<button class=\"w3-button w3-round-large w3-green w3-hover-teal w3-margin\"" 
        + " type=\"submit\"  name=\"operationType\" value=\"PUT\">Update Patient in IIS</button>\r\n");
      if (!fhirPatientResponseList.isEmpty()) {
        out.println("<textarea class =\"w3-border w3-border-red\" id=\"story\" style=\"width:75%\"\r\n"
          + "rows=\"3\" cols=\"200\" readonly>");
        for (int i = fhirPatientResponseList.size() - 1; i >= 0; i--) {
          out.println(fhirPatientResponseList.get(i));
        }
        out.println("</textarea><br/>");
      }
    }
    out.println("</form>");
  }

  private static void doImmunizationForm(PrintWriter out, HttpSession session, HttpServletRequest req) throws ParseException {
    out.println("<form method=\"POST\">");
    out.println("<input type=\"hidden\" name=\"resourceType\" value=\"Immunization\">");

    VaccinationEvent vacc_ev = new VaccinationEvent();
    Patient patient = (Patient) session.getAttribute("patient");
    Session dataSession = PopServlet.getDataSession();
    
    Query queryVaccination = dataSession.createQuery("from VaccinationEvent where vaccination_event_Id=? and patient_id=?");
    queryVaccination.setParameter(0, Integer.parseInt(req.getParameter("paramEntryId")));
    queryVaccination.setParameter(1, patient.getPatientId());
    List<VaccinationEvent> vaccinationList = queryVaccination.list();
    vacc_ev=vaccinationList.get(0);

    List<String> fhirImmunizationResponseList = (List<String>) session.getAttribute("fhirImmunizationResponseList");
    if (fhirImmunizationResponseList == null){
      fhirImmunizationResponseList = new ArrayList<String>();
      session.setAttribute("fhirImmunizationResponseList", fhirImmunizationResponseList);
    }

    IParser parser = CustomClientBuilder.getCTX().newXmlParser().setPrettyPrint(true);
    String fhirImmunizationString = "";
    if (req.getAttribute("fhirImmunizationString") != null) {
      fhirImmunizationString = req.getParameter("fhirImmunizationString");
    } else {
      try { 
        Immunization immunization = FhirImmunizationCreation.dbVaccinationToFhirVaccination(vacc_ev);
        fhirImmunizationString = parser.encodeResourceToString(immunization);
      } catch (Exception e) {
        e.printStackTrace();
        fhirImmunizationResponseList.add("Invalid Resource");
      }
    }

    { // Immunization
      out.println("<textarea class =\"w3-border w3-border-green\" id=\"story\" style=\"width:75%\" name=\"fhirImmunizationString\"\r\n"
        + "rows=\"20\" cols=\"200\">\r\n"
        + fhirImmunizationString
        + "</textarea><br/>");
      out.println("<button class=\"w3-button w3-round-large w3-green w3-hover-teal w3-margin\"" 
        + " type=\"submit\"  name=\"operationType\" value=\"POST\">Send Immunization to IIS</button>\r\n");
      out.println("<button class=\"w3-button w3-round-large w3-green w3-hover-teal w3-margin\"" 
        + " type=\"submit\"  name=\"operationType\" value=\"PUT\">Update Immunization in IIS</button>\r\n");
      if (!fhirImmunizationResponseList.isEmpty()) {
        out.println("<textarea class =\"w3-border w3-border-red\" id=\"story\" style=\"width:75%\"\r\n"
          + "rows=\"3\" cols=\"200\" readonly>");
        for (int i = fhirImmunizationResponseList.size() - 1; i >= 0; i--) {
          out.println(fhirImmunizationResponseList.get(i));
        }
        out.println("</textarea><br/>");
      }
    }
    out.println("</form>");
  }



}
