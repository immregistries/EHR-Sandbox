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

import ca.uhn.fhir.context.ConfigurationException;
import ca.uhn.fhir.parser.IParser;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hl7.fhir.r5.model.Immunization;
import org.immregistries.ehr.fhir.CustomClientBuilder;
import org.immregistries.ehr.fhir.ImmunizationHandler;
import org.immregistries.ehr.fhir.PatientHandler;
import org.immregistries.ehr.fhir.ResourceClient;
import org.immregistries.ehr.model.Facility;
import org.immregistries.ehr.model.Patient;
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
    req.setAttribute("fhir"+ resourceType +"String", fhirResourceString);

    String id = req.getParameter("fhir"+ resourceType +"Id");

    switch(resourceType){
      case "Patient":{
        try {
          org.hl7.fhir.r5.model.Patient fhirPatient;
          switch (operationType) {
            case "POST":
              fhirPatient = (org.hl7.fhir.r5.model.Patient) parser
                .parseResource(fhirResourceString);
              fhirResponse = ResourceClient.write(fhirPatient, session);
              break;
            case "PUT" :
              fhirPatient = (org.hl7.fhir.r5.model.Patient) parser
                .parseResource(fhirResourceString);
              fhirResponse = ResourceClient.update(fhirPatient, fhirPatient.getId(), session);
              break;
            case "GET" :
              fhirResponse = ResourceClient.read(resourceType, id, session);
              req.setAttribute("fhir"+ resourceType + "GetResponse", fhirResponse);
              fhirResponse = "";
              break;
          }
        } catch (ConfigurationException ce) {
          ce.printStackTrace(); // TODO Deal with more errors
          fhirResponse = "LOCAL PARSING ERROR : Invalid Resource";
        } catch (Exception e) {
          e.printStackTrace();
          fhirResponse = "Error";
        }
        break;
      }
      case "Immunization":{
        try {
          org.hl7.fhir.r5.model.Immunization fhirImmunization;
            switch (operationType) {
              case "POST":
                fhirImmunization = (org.hl7.fhir.r5.model.Immunization) parser
                  .parseResource(fhirResourceString);
                fhirResponse = ResourceClient.write(fhirImmunization, session);
                break;
              case "PUT" :
                fhirImmunization = (org.hl7.fhir.r5.model.Immunization) parser
                  .parseResource(fhirResourceString);
                fhirResponse = ResourceClient.update(fhirImmunization, fhirImmunization.getId(), session);
                break;
              case "GET" :
                fhirResponse = ResourceClient.read(resourceType, id, session);
                req.setAttribute("fhir"+ resourceType + "GetResponse", fhirResponse);
                fhirResponse = "";
                break;
            }
        } catch (ConfigurationException ce) {
          ce.printStackTrace(); // TODO Deal with more errors
          fhirResponse = "LOCAL PARSING ERROR : Invalid Resource";
        } catch (Exception e) {
          e.printStackTrace();            
          fhirResponse = "Error";
        }
        break;
      }
    }
    if (fhirResponse.length() > 0){
      List<String> fhirResponseList = (List<String>) session.getAttribute("fhir"+ resourceType +"ResponseList");
      fhirResponseList.add(fhirResponse);
      session.setAttribute("fhir"+ resourceType +"ResponseList", fhirResponseList);
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
        ServletHelper.doStandardHeader(out, req, "FHIR messaging");

        out.println("<div id=\"formulaire\">");

        out.println("<div class=\"w3-margin w3-left\" style=\"width:45%\">");
        doPatientForm(out, session, req);
        FhirGet.doPatientForm(out, session, req);
        out.println("</div>");

        out.println("<div class=\"w3-margin w3-right\" style=\"width:45%\">");
        if (req.getParameter("paramEntryId") != null) { // Immunization
          doImmunizationForm(out, session, req);
        }
        FhirGet.doImmunizationForm(out, session, req);
        out.println("</div>");
        out.println("</div>");
        ServletHelper.doStandardFooter(out, session);
      }
    } catch (Exception e) {
      e.printStackTrace(System.err);
    }
    out.flush();
    out.close();
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
        fhirPatientString = parser.encodeResourceToString(PatientHandler.dbPatientToFhirPatient(patient));
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

    VaccinationEvent vaccinationEvent = new VaccinationEvent();
    Patient patient = (Patient) session.getAttribute("patient");
    Session dataSession = PopServlet.getDataSession();
    
    Query queryVaccination = dataSession.createQuery("from VaccinationEvent where vaccination_event_Id=? and patient_id=?");
    queryVaccination.setParameter(0, Integer.parseInt(req.getParameter("paramEntryId")));
    queryVaccination.setParameter(1, patient.getPatientId());
    List<VaccinationEvent> vaccinationList = queryVaccination.list();
    vaccinationEvent=vaccinationList.get(0);

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
        Immunization immunization = ImmunizationHandler.dbVaccinationToFhirVaccination(vaccinationEvent);
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
