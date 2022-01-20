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
    Patient  patient = (Patient) session.getAttribute("patient") ;
    

    String fhirPatientString = (String) session.getAttribute("fhirPatient");
    IParser parser = CustomClientBuilder.getCTX().newXmlParser().setPrettyPrint(true);
    String fhirPatientResponse = "";
    try {
      // org.hl7.fhir.r4.model.Patient fhirPatient =  (org.hl7.fhir.r4.model.Patient) parser.parseResource(fhirPatientString);
      org.hl7.fhir.r4.model.Patient fhirPatient =  FhirPatientCreation.dbPatientToFhirPatient(patient);
      fhirPatientResponse = (String) session.getAttribute("fhirPatientResponse") ;

      fhirPatientResponse = ResourceClient.write(fhirPatient);
    } catch (Exception e) {
      //TODO: handle exception
      fhirPatientResponse = "LOCAL PARSING ERROR : Invalid Resource";
    }
    session.setAttribute("fhirPatientResponse", fhirPatientResponse);
    resp.sendRedirect("FHIR_messaging");
    doGet(req, resp);

  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    HttpSession session = req.getSession(true);
    resp.setContentType("text/html");

    Tester tester = new Tester();
    Facility facility = new Facility();
    Patient patient = new Patient();

    String fhirPatientResponse = " ";

    IParser parser = CustomClientBuilder.getCTX().newXmlParser().setPrettyPrint(true);


    tester = (Tester) session.getAttribute("tester");
    facility = (Facility) session.getAttribute("facility");
    patient = (Patient) session.getAttribute("patient") ;
    fhirPatientResponse = (String) session.getAttribute("fhirPatientResponse") ;

    org.hl7.fhir.r4.model.Patient fhirPatient = FhirPatientCreation.dbPatientToFhirPatient(patient);
    String fhirPatientString = "";
    try {
      fhirPatientString = parser.encodeResourceToString(fhirPatient);
    } catch (Exception e) {
      fhirPatientResponse = "Invalid Resource";
    }

    PrintWriter out = new PrintWriter(resp.getOutputStream());
    try {
      {
        doHeader(out, session, req);

        out.println("<div id=\"formulaire\">");
        out.println("<form method=\"POST\"  target=\"FHIR_Messaging\">");
        // IIS authentication  form
        out.println(" <label class=\"w3-text-green\"><b>IIS UserID</b></label>"
            + "<input type=\"text\"  class = \"w3-input w3-margin w3-border\" hidden value=\""+ tester.getLoginUsername()
            +"\" style =\"width:75%\" name=\"USERID\"/>\r\n");
        out.println(" <label class=\"w3-text-green\"><b>IIS Password</b></label>"
            + "<input type=\"password\"  class = \"w3-input w3-margin w3-border\" hidden value=\""+tester.getLoginPassword()
            +"\" style =\"width:75%\" name=\"PASSWORD\"/>\r\n");
        out.println(" <label class=\"w3-text-green\"><b>Facility ID</b></label>"
            + "<input type=\"text\"  class = \"w3-input w3-margin w3-border\" hidden value=\""+facility.getNameDisplay()
            +"\" style =\"width:75%\" name=\"FACILITYID\"/>\r\n");

        out.println("<div class=\"w3-margin\">"
            + "<textarea class =\"w3-border w3-border-green\" id=\"story\" style=\"width:75%\"name=\"FHIRPatient\"\r\n"
            + "     rows=\"20\" cols=\"200\">\r\n"
            + fhirPatientString + " \r\n"
            + " \r\n" + "</textarea><br/>");
        out.println("<button class=\"w3-button w3-round-large w3-green w3-hover-teal w3-margin \"  >send FHIR Patient to IIS</button>\r\n");
        if(fhirPatientResponse != null){
          out.println( "<label class=\"w3-text-red w3-margin w3-margin-bottom\"><b class=\"w3-margin\">" 
            + fhirPatientResponse + "</b></label><br/>");
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
        + "</div>\r\n" + "      </header>");
    
  }

  public static void doFooter(PrintWriter out, HttpSession session) {
    out.println("</div>\r\n" + "    </body>\r\n" + "</html>");
  }

}
