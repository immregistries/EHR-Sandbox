package org.immregistries.ehr.servlet;

import java.io.PrintWriter;
import java.text.ParseException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class FHIR_Get 
 * 
 * 
 */
public class FhirGet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  public static final String PARAM_SHOW = "show";

  // @Override
  // protected void doPost(HttpServletRequest req, HttpServletResponse resp)
  //     throws ServletException, IOException {
  //   HttpSession session = req.getSession(true);

  //   String fhirGetResponse = "";
  //   String resourceType = req.getParameter("resourceType");
  //   String id = req.getParameter("fhir"+ resourceType +"Id");

  //   switch(resourceType){
  //     case "Patient":{
  //       try {
  //         fhirGetResponse = ResourceClient.read(resourceType, id, session);
  //       } catch (Exception e) {
  //         e.printStackTrace();
  //         fhirGetResponse = "ERROR";
  //       }
  //       break;
  //     }
  //     case "Immunization":{
  //       try {    
  //         fhirGetResponse = ResourceClient.read(resourceType, id, session);
  //       } catch (Exception e) {
  //         e.printStackTrace();
  //         fhirGetResponse = "ERROR";
  //       }
  //       break;
  //     }
  //   }
  //   System.out.println(fhirGetResponse);
  //   req.setAttribute("fhir"+ resourceType + "GetResponse", fhirGetResponse);

  //   // RequestDispatcher dispatcher = getServletContext()
  //   //   .getRequestDispatcher(req.getHeader("referer").split("/ehr-sandbox")[1]);
  //   // System.out.println(req.getHeader("referer").split("/ehr-sandbox")[1]);
  //   // dispatcher.forward(req, resp);
  //   doGet(req, resp);
  // }

  // @Override
  // protected void doGet(HttpServletRequest req, HttpServletResponse resp)
  //     throws ServletException, IOException {

  //   HttpSession session = req.getSession(true);
  //   resp.setContentType("text/html");


  //   PrintWriter out = new PrintWriter(resp.getOutputStream());
  //   try {
  //     {
  //       doHeader(out, session, req);

  //       out.println("<div id=\"formulaire\">");

  //       out.println("<div class=\"w3-margin w3-left\" style=\"width:45%\">");
  //       doPatientForm(out, session, req);
  //       out.println("</div>");

  //       out.println("<div class=\"w3-margin w3-right\" style=\"width:45%\">");
  //       doImmunizationForm(out, session, req);
  //       out.println("</div>");

  //       out.println("</div>");
  //       doFooter(out, session);
  //     }
  //   } catch (Exception e) {
  //     e.printStackTrace(System.err);
  //   }
  //   out.flush();
  //   out.close();
  // }

  // public static void doHeader(PrintWriter out, HttpSession session, HttpServletRequest req) {
  //   out.println("<html>");
  //   out.println("  <head>");
  //   out.println("    <title>EHR Sandbox</title>");
  //   out.println("<link rel=\"stylesheet\" href=\"https://www.w3schools.com/w3css/4/w3.css\">");
       
  //   out.println("  </head>");
  //   out.println("  <body>");
  //   // out.println("<div class=\"w3-container \">");
  //   out.println("<header >\r\n" + "<div class=\"w3-bar w3-green w3-margin-bottom\">\r\n"
  //       + "  <a href = \'silos \' class=\"w3-bar-item w3-button\">List of tenants </a>\r\n"
  //       + "  <a href = \'facility_patient_display\' class=\"w3-bar-item w3-button\">Facilities/patients list</a>\r\n"
        
  //       + "  <a href = \'Settings\' class=\"w3-bar-item w3-right w3-button\">Settings </a>\r\n"
  //       + "</div>" + "      </header>");
  //   out.println("<div class=\"w3-display-container w3-margin\" style=\"height:600px;\">");
  // }

  // public static void doFooter(PrintWriter out, HttpSession session) {
  //   out.println("</div>\r\n" + "    </body>\r\n" + "</html>");
  // }

  protected static void doPatientForm(PrintWriter out, HttpSession session, HttpServletRequest req) throws ParseException {
    out.println("<form method=\"POST\">");
    out.println("<input type=\"hidden\" name=\"operationType\" value=\"GET\">");

    String fhirPatientGetResponse = (String) req.getAttribute("fhirPatientGetResponse");
    String fhirPatientId = req.getParameter("fhirPatientId");
    if (fhirPatientId == null) {
      fhirPatientId = "";
    }


    { // Patient
      out.println("<textarea class =\"w3-border w3-border-green\" id=\"story\" style=\"width:20%\" name=\"fhirPatientId\"\r\n"
        + "rows=\"1\" cols=\"12\" placeholder=\"id\">\r\n"
        + fhirPatientId
        + "</textarea>");
      out.println("<button class=\"w3-button w3-round-large w3-green w3-hover-teal w3-margin\"" 
        + " type=\"submit\"  name=\"resourceType\" value=\"Patient\">Visualise Patient</button>\r\n");
      if (fhirPatientGetResponse != null) {
        out.println("<textarea class =\"w3-border w3-border-red\" id=\"story\" style=\"width:75%\"\r\n"
          + "rows=\"25\" cols=\"200\" readonly>");
        out.println(fhirPatientGetResponse);
        out.println("</textarea><br/>");
      }
    }
    out.println("</form>");
  }

  protected static void doImmunizationForm(PrintWriter out, HttpSession session, HttpServletRequest req) throws ParseException {
    out.println("<form method=\"POST\">");
    out.println("<input type=\"hidden\" name=\"operationType\" value=\"GET\">");

    String fhirImmunizationGetResponse = (String) req.getAttribute("fhirImmunizationGetResponse");
    String fhirImmunizationId = req.getParameter("fhirImmunizationId");
    if (fhirImmunizationId == null) {
      fhirImmunizationId = "";
    }

    { // Immunization
      out.println("<textarea class =\"w3-border w3-border-green\" id=\"story\" style=\"width:20%\" name=\"fhirImmunizationId\"\r\n"
        + "rows=\"1\" cols=\"12\" placeholder=\"id\">\r\n"
        + fhirImmunizationId
        + "</textarea>");
      out.println("<button class=\"w3-button w3-round-large w3-green w3-hover-teal w3-margin\"" 
        + " type=\"submit\"  name=\"resourceType\" value=\"Immunization\">Visualise Immunization</button>\r\n");
      if (fhirImmunizationGetResponse != null) {
        out.println("<textarea class =\"w3-border w3-border-red\" id=\"story\" style=\"width:75%\"\r\n"
          + "rows=\"25\" cols=\"200\" readonly>");
        out.println(fhirImmunizationGetResponse);
        out.println("</textarea><br/>");
      }
    }
    out.println("</form>");
  }
}
