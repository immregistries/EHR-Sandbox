package org.immregistries.ehr.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.immregistries.ehr.fhir.ResourceClient;

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

    String fhirGetResponse = "";
    String resourceType = req.getParameter("resourceType");
    String id = req.getParameter("fhir"+ resourceType +"Id");

    switch(resourceType){
      case "Patient":{
        try {
          fhirGetResponse = ResourceClient.read(resourceType, id, session);
        } catch (Exception e) {
          e.printStackTrace();
          fhirGetResponse = "ERROR";
        }
        break;
      }
      case "Immunization":{
        try {    
          fhirGetResponse = ResourceClient.read(resourceType, id, session);
        } catch (Exception e) {
          e.printStackTrace();
          fhirGetResponse = "ERROR";
        }
        break;
      }
    }
    
    req.setAttribute("fhir"+ resourceType + "GetResponse", fhirGetResponse);
    resp.sendRedirect(req.getHeader("referer"));
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

        out.println("<div class=\"w3-margin w3-right\" style=\"width:45%\">");
        doImmunizationForm(out, session, req);
        out.println("</div>");

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

  protected static void doPatientForm(PrintWriter out, HttpSession session, HttpServletRequest req) throws ParseException {
    out.println("<form method=\"POST\" action='FHIR_get'>");
    String fhirPatientGetResponse = (String) req.getAttribute("fhirPatientGetResponse");
    String fhirPatientId = req.getParameter("");

    { // Patient
      out.println("<textarea class =\"w3-border w3-border-green\" id=\"story\" style=\"width:20%\" name=\"fhirPatientId\"\r\n"
        + "rows=\"1\" cols=\"12\" placeholder=\"id\">\r\n"
        + fhirPatientId
        + "</textarea>");
      out.println("<button class=\"w3-button w3-round-large w3-green w3-hover-teal w3-margin\"" 
        + " type=\"submit\"  name=\"resourceType\" value=\"Patient\">Visualise Patient</button>\r\n");
      if (fhirPatientGetResponse != null) {
        out.println("<label class=\"w3-text-red w3-margin w3-margin-bottom\">"
          + fhirPatientGetResponse + "</label>");
      }
    }
    out.println("</form>");
  }

  protected static void doImmunizationForm(PrintWriter out, HttpSession session, HttpServletRequest req) throws ParseException {
    out.println("<form method=\"POST\" target='FHIR_get'>");
    String fhirImmunizationGetResponse = (String) req.getAttribute("fhirImmunizationGetResponse");
    String fhirImmunizationId = "";

    { // Immunization
      out.println("<textarea class =\"w3-border w3-border-green\" id=\"story\" style=\"width:20%\" name=\"fhirImmunizationId\"\r\n"
        + "rows=\"1\" cols=\"12\" placeholder=\"id\">\r\n"
        + fhirImmunizationId
        + "</textarea>");
      out.println("<button class=\"w3-button w3-round-large w3-green w3-hover-teal w3-margin\"" 
        + " type=\"submit\"  name=\"resourceType\" value=\"Immunization\">Visualise Immunization</button>\r\n");
      if (fhirImmunizationGetResponse != null) {
        out.println("<label class=\"w3-text-red w3-margin w3-margin-bottom\">"
          + fhirImmunizationGetResponse + "</label>");
      }
    }
    out.println("</form>");
  }
}
