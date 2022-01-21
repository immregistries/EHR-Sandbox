package org.immregistries.ehr.servlet;

import java.io.Console;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.immregistries.codebase.client.CodeMap;
import org.immregistries.codebase.client.generated.Code;
import org.immregistries.codebase.client.reference.CodesetType;
import org.immregistries.ehr.model.Facility;
import org.immregistries.ehr.model.Patient;
import org.immregistries.ehr.model.Silo;
import org.immregistries.iis.kernal.model.CodeMapManager;
import com.github.javafaker.Faker;
import org.immregistries.ehr.FhirPatientCreation;

/**
 * Servlet implementation class patient_creation
 */
public class PatientCreation extends HttpServlet {
  private static final long serialVersionUID = 1L;
  public static final String PARAM_SHOW = "show";

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    HttpSession session = req.getSession(true);
    Session dataSession = PopServlet.getDataSession();

    Silo silo = new Silo();
    Facility facility = new Facility();
    Patient patient = new Patient();

    silo = (Silo) session.getAttribute("silo");
    facility = (Facility) session.getAttribute("facility");

    patient.setSilo(silo);
    patient.setFacility(facility);

    patient.setNameFirst(req.getParameter("first_name"));
    patient.setNameLast(req.getParameter("last_name"));
    patient.setNameMiddle(req.getParameter("middle_name"));
    patient.setAddressCity(req.getParameter("city"));
    patient.setAddressCountry(req.getParameter("country"));
    patient.setAddressCountyParish(req.getParameter("county"));
    patient.setAddressState(req.getParameter("state"));
    //patient.setBirthDate(req.getParameter("DoB"));
    patient.setBirthFlag(req.getParameter("birth_flag"));
    patient.setBirthOrder(req.getParameter("birth_order"));
    //patient.setDeathDate(null);
    patient.setDeathFlag(req.getParameter("death_flag"));
    patient.setEmail(req.getParameter("email"));
    patient.setEthnicity(req.getParameter("ethnicity"));
    patient.setGuardianFirst(req.getParameter("guardian_first_name"));
    patient.setGuardianLast(req.getParameter("guardian_last_name"));
    patient.setGuardianMiddle(req.getParameter("guardian_middle_name"));
    patient.setGuardianRelationship(req.getParameter("guardian_relation"));
    patient.setMotherMaiden(req.getParameter("mother_maiden"));
    patient.setPhone(req.getParameter("phone"));
    patient.setProtectionIndicator(req.getParameter("protection_indicator"));
    patient.setProtectionIndicatorDate(null);
    patient.setPublicityIndicator(req.getParameter("publicity_indicator"));
    patient.setPublicityIndicatorDate(null);
    patient.setRace(req.getParameter("race"));
    patient.setRegistryStatusIndicator(req.getParameter("registry_status_indicator"));
    patient.setRegistryStatusIndicatorDate(null);
    patient.setSex(req.getParameter("sex"));
    Date updatedDate = new Date();
    patient.setUpdatedDate(updatedDate);
    patient.setCreatedDate(updatedDate);
    patient.setBirthDate(updatedDate);
    Transaction transaction = dataSession.beginTransaction();
    dataSession.save(patient);
    transaction.commit();

    ServletContext context = getServletContext( );
    // context.log(FhirPatientCreation.dbPatientToFhirPatient(patient,"default"));

    System.out.println(FhirPatientCreation.dbPatientToFhirPatient(patient,"default"));
    resp.sendRedirect("facility_patient_display");
    
    doGet(req, resp);
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    HttpSession session = req.getSession(true);
    resp.setContentType("text/html");
    PrintWriter out = new PrintWriter(resp.getOutputStream());
    CodeMap codeMap = CodeMapManager.getCodeMap();
    Collection<Code> codeListRelation =codeMap.getCodesForTable(CodesetType.PERSON_RELATIONSHIP);
    try {
      { 
        if(req.getParameter("noFacility")!=null) {
          resp.sendRedirect("facility_patient_display?chooseFacility=1");
        }
        doHeader(out, session);
        String show = req.getParameter(PARAM_SHOW);
        out.println("<button onclick=\"location.href=\'patient_creation?testPatient=1\'\" class=\"w3-button w3-round-large w3-green w3-hover-teal w3-margin \"  >Fill with test informations</button><br/>");
        String testDoB="";
        String testNameFirst="";
        String testNameLast="";
        String testMiddleName="";
        String testMotherMaidenName="";
        String testSex="";
        String testRace="";
        String testAdress="";
        String testCity="";
        String testCountryCode="";
        String testState="";
        String testCountyParish="";
        String testPhone="";
        String testEmail="";
        String testEthnicity="";
        String testBirthFlag="";
        String testBirthOrder="";
        String testDeathFlag="";
        String testDeathDate="";
        String testPubIndic="";
        String testPubIndicDate="";
        String testProtecIndic="";
        String testProtecIndicDate="";
        String testRegIndicDate="";
        String testRegStatus="";
        String testRegStatusDate="";
        String testGuardNameFirst="";
        String testGuardNameLast="";
        String testGuardMiddleName="";
        String testGuardRelationship="";
        Faker faker = new Faker();
        String pattern = "yyyyMMdd";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        Date birthDate = new Date();
        int randDay = (int) (Math.random()*30+1);
        int randMonth = (int) (Math.random()*11);
        int randYear = (int) (Math.random()*121+1900);
        birthDate.setDate(randDay);
        birthDate.setMonth(randMonth);
        birthDate.setYear(randYear);
        if(req.getParameter("testPatient")!=null) {
          testDoB=simpleDateFormat.format(birthDate);;
          testNameFirst=faker.name().firstName();
          testNameLast=faker.name().lastName();
          testMiddleName=faker.name().firstName();
          testMotherMaidenName=faker.name().lastName();
          if(randMonth%2==0) {
            testSex="F";
          }else {
            testSex="M";
          }
          testRace="Asian";
          testAdress=faker.address().buildingNumber()+faker.address().streetName();
          testCity=faker.address().city();
          testCountryCode=faker.address().countryCode();
          testState=faker.address().state();
          testCountyParish="county";
          testPhone=faker.phoneNumber().phoneNumber();
          testEmail=testNameFirst+ randDay +"@gmail.com";
          testEthnicity="Indian";
          testBirthFlag="";
          testBirthOrder="";
          testDeathFlag="";
          testDeathDate="O";
          testPubIndic="O";
          testPubIndicDate="O";
          testProtecIndic="O";
          testProtecIndicDate="O";
          testRegIndicDate="O";
          testRegStatus="O";
          testRegStatusDate="O";
          testGuardNameFirst=faker.name().firstName();
          testGuardNameLast=testNameLast;
          testGuardMiddleName=faker.name().firstName();
          testGuardRelationship="BRO";
          
        }
        out.println("<form method=\"post\" class=\"w3-container\" action=\"patient_creation\">\r\n"
            
            + "<div class = \"w3-margin w3-border w3-border-green\" style=\"width:100% ; display:flex \">"
            + "<div style =\"width: 50% ;align-items:center\" "
            
            + "    <label class=\"w3-text-green\"><b>First Name</b></label>"
            + "                         <input type=\"text\"  class = \"w3-input w3-margin w3-border\"  value=\""+testNameFirst+"\" style=\"width:75% \" name=\"first_name\" />\r\n"

           
            +"</div>"            
            + "<div style =\"width: 50% ;align-items:center\" "
           
            + " <label class=\"w3-text-green\"><b>Last name</b></label>"
            + "                         <input type=\"text\"  class = \"w3-input w3-margin w3-border\"  value=\""+testNameLast+"\" style=\"width:75% \" name=\"last_name\" />\r\n"

            +"</div>"            
            + "<div style =\"width: 50% ;align-items:center\" "
           
            + "    <label class=\"w3-text-green\"><b>Middle name</b></label>"
            + "                         <input type=\"text\"  class = \"w3-input w3-margin w3-border\"  value=\""+testMiddleName+"\" style=\"width:75% \" name=\"middle_name\" />\r\n"

            +"</div>"
            
            + "<div style =\"width: 50% ;align-items:center\" "
            
            + "    <label class=\"w3-text-green\"><b>Mother maiden name</b></label>"
            + "                         <input type=\"text\"  class = \"w3-input w3-margin w3-border\"  value=\""+testMotherMaidenName+"\" style=\"width:75% \" name=\"mother_maiden_name\" />\r\n"

            +"</div>"
            +"</div>"
            
            + "<div class = \"w3-margin w3-border w3-border-green\" style=\"width:100% ; display:flex \">"
            + "<div style =\"width: 50% ;align-items:center\" "
           
            + "<label class=\"w3-text-green\"><b>Date of birth</b></label>"
            + "                         <input type=\"text\" class = \"w3-input w3-margin w3-border \"  value=\""+testDoB+"\" style=\"width:75% \" name=\"DoB\" />\r\n"

            +"</div>"
            
            + "<div style =\"width: 50% ;align-items:center\" "
           
            + "    <label class=\"w3-text-green\"><b>Sex (F or M) </b></label>"
            + "                         <input type=\"text\"  class = \"w3-input w3-margin w3-border\"  value=\""+testSex+"\" style=\"width:75% \" name=\"sex\"/>\r\n"

            +"</div>"           
            +"</div>"
            
            + "<div class = \"w3-margin w3-border w3-border-green\" style=\"width:100% ; display:flex \">"
            + "<div style =\"align-items:center\" "
            
            + "    <label class=\"w3-text-green\"><b>Address 1</b></label>"
            + "                         <input type=\"text\"  class = \"w3-input w3-margin w3-border\"  value=\""+testAdress+"\" style=\"width:75% \" name=\"address\"/>\r\n"

            +"</div>"            
            + "<div style =\"width: 30% ;align-items:center\" "
           
            + " <label class=\"w3-text-green\"><b>City</b></label>"
            + "                         <input type=\"text\"  class = \"w3-input w3-margin w3-border\"  value=\""+testCity+"\" style=\"width:75% \" name=\"city\"/>\r\n"

            +"</div>"            
            + "<div style =\"width: 30% ;align-items:center\" "
            
            + " <label class=\"w3-text-green\"><b>State</b></label>"
            + "                         <input type=\"text\"  class = \"w3-input w3-margin w3-border\"  value=\""+testState+"\" style=\"width:75% \" name=\"state\" />\r\n"

            
            +"</div>"            
            + "<div style =\"width: 50% ;align-items:center\" "
            
            + " <label class=\"w3-text-green\"><b>County/parish</b></label>"
            + "                         <input type=\"text\"  class = \"w3-input w3-margin w3-border\"  value=\""+testCountyParish+"\" style=\"width:75% \" name=\"county\"/>\r\n"

            +"</div>"            
            + "<div style =\"width: 50% ;align-items:center\" "
            
            + " <label class=\"w3-text-green\"><b>Country Code</b></label>"
            + "                         <input type=\"text\" class = \"w3-input w3-margin w3-border\"  value=\""+testCountryCode+"\" style=\"width:75% \"   name=\"country\"/>\r\n"

            +"</div>"            
            +"</div>"
            
            + "<div class = \"w3-margin w3-border w3-border-green\" style=\"width:100% ; display:flex \">"
            + "<div style =\"width: 50% ;align-items:center\" "
            
            + "   <label class=\"w3-text-green\"><b>phone</b></label>"
            + "                         <input type=\"text\"  class = \"w3-input w3-margin w3-border\"  value=\""+testPhone+"\" style=\"width:75% \"name=\"phone\" />\r\n"

            
            +"</div>"            
            + "<div style =\"width: 50% ;align-items:center\" "
           
            + " <label class=\"w3-text-green\"><b>E-mail</b></label>"
            + "                         <input type=\"text\"  class = \"w3-input w3-margin w3-border\"  value=\""+testEmail+"\" style=\"width:75% \" name=\"email\"/>\r\n"

            +"</div>" 
            +"</div>"            
            + "<div class = \"w3-margin w3-border w3-border-green\" style=\"width:100% ; display:flex \">"
            + "<div style =\"width: 50% ;align-items:center\" "
            
            + "    <label class=\"w3-text-green\"><b>Ethnicity</b></label>"
            + "                         <input type=\"text\"  class = \"w3-input w3-margin w3-border\"  value=\""+testEthnicity+"\" style=\"width:75% \" name=\"ethnicity\" />\r\n"
            
            
            +"</div>"
            
            + "<div style =\"width: 50% ;align-items:center\" "
             
            + " <label class=\"w3-text-green\"><b>Race</b></label>"
            + "                         <input type=\"text\"  class = \"w3-input w3-margin w3-border\"  value=\""+testRace+"\" style=\"width:75% \" name=\"race\"/>\r\n"
             
            +"</div>"
            
            +"</div>"
            
            + "<div class = \"w3-margin w3-border w3-border-green\" style=\"width:100% ; display:flex \">"
            + "<div style =\"width: 50% ;align-items:center\" "
            
            + " <label class=\"w3-text-green\"><b>Birth flag</b></label>"
            //+ "                           <input type=\"text\"  class = \"w3-input w3-margin w3-border\"  value=\"\" style=\"width:75% \" name=\"birth_flag\"/>\r\n"
            +"  <p>"
            +"                          <SELECT style=\"width:75% \" name=\"birth_flag\" size=\"1\">\r\n"
            + "                             <OPTION value=\"\">Unknown</Option>\r\n"
            + "                             <OPTION value=\"Y\">Y</Option>\r\n"
            + "                             <OPTION value=\"N\">N</Option>\r\n"
            + "                        </SELECT>\r\n"
            +"  </p>"
            
            +"</div>"
            
            + "<div style =\"width: 50% ;align-items:center\" "
            
            + "    <label class=\"w3-text-green\"><b>Birth order</b></label>"
            + "                         <input type=\"text\"  class = \"w3-input w3-margin w3-border\"  value=\""+testBirthOrder+"\" style=\"width:75% \" name=\"birth_order\"/>\r\n"

            +"</div>"
            
            + "<div style =\"width: 50% ;align-items:center\" "
            
            + "    <label class=\"w3-text-green\"><b>Death flag</b></label>"
            //+ "                           <input type=\"text\"  class = \"w3-input w3-margin w3-border\"  value=\"\" style=\"width:75% \" name=\"death_flag\" />\r\n"
            +"  <p>"
            +"                          <SELECT style=\"width:75% \" name=\"death_flag\" size=\"1\">\r\n"
            + "                             <OPTION value=\"\">Unknown</Option>\r\n"
            + "                             <OPTION value=\"Y\">Y</Option>\r\n"
            + "                             <OPTION value=\"N\">N</Option>\r\n"
            + "                        </SELECT>\r\n"
            +"  </p>"
            
            +"</div>"
            
            + "<div style =\"width: 50% ;align-items:center\" "
            
            + "   <label class=\"w3-text-green\"><b>Death date</b></label>"
            + "                         <input type=\"text\"  class = \"w3-input w3-margin w3-border\"  value=\""+testDeathDate+"\" style=\"width:75% \" name=\"DoD\"/>\r\n"

            +"</div>"
            +"</div>"

            + "<div class = \"w3-margin w3-border w3-border-green\" style=\"width:100% ; display:flex \">"
            +"<div style=\"width:100% \">"
            + "<div style =\" align-items:center\" "
            
            + " <label class=\"w3-text-green\"><b>publicity indicator</b></label>"
            + "                         <input type=\"text\"  class = \"w3-input w3-margin w3-border\"  value=\""+testPubIndic+"\" style=\"width:75% \"name=\"publicity_indicator\" />\r\n"

            +"</div>"
            
            + "<div style =\"align-items:center\" "
            
            + "    <label class=\"w3-text-green\"><b>publicity indicator date</b></label>"
            + "                         <input type=\"text\"  class = \"w3-input w3-margin w3-border\"  value=\""+testPubIndicDate+"\" style=\"width:75% \" name=\"publicity_date\"/>\r\n"

            +"</div>"
            
            + "<div style =\"align-items:center\" "
            
            + "   <label class=\"w3-text-green\"><b>protection indicator</b></label>"
            + "                         <input type=\"text\"  class = \"w3-input w3-margin w3-border\"  value=\""+testProtecIndic+"\" style=\"width:75% \"name=\"protection\" />\r\n"
            +"</div>"
            +"</div>"
            +"<div style=\"width:100% \">"
            + "<div style =\"align-items:center\" "
            
            + "    <label class=\"w3-text-green\"><b>protection indicator date</b></label>"
            + "                         <input type=\"text\"  class = \"w3-input w3-margin w3-border\"  value=\""+testProtecIndicDate+"\" style=\"width:75% \"name=\"protection_date\" />\r\n"

            +"</div>"
            
            + "<div style =\"align-items:center\" "
            
            + "    <label class=\"w3-text-green\"><b>Registry indicator date  </b></label>"
            + "                         <input type=\"text\"  class = \"w3-input w3-margin w3-border\"  value=\""+testRegIndicDate+"\" style=\"width:75% \" name=\"registry_indicator_date\"/>\r\n"
 
            +"</div>"
            
            + "<div style =\"align-items:center\" "
           
            + "    <label class=\"w3-text-green\"><b>registry status indicator</b></label>"
            + "                         <input type=\"text\"  class = \"w3-input w3-margin w3-border\"  value=\""+testRegStatus+"\" style=\"width:75% \" name=\"registry_status_indicator\"/>\r\n"

            +"</div>"
            +"</div>"
            +"<div style=\"width:100% \">"
            + "<div style =\"align-items:center\" "
           
            + "   <label class=\"w3-text-green\"><b>registry status indicator date</b></label>"
            + "                         <input type=\"text\"  class = \"w3-input w3-margin w3-border\"  value=\""+testRegStatusDate+"\" style=\"width:75% \" name=\"registry_status_indicator_date\"/>\r\n"

            +"</div>"
            +"</div>"
            +"</div>"

            + "<div class = \"w3-margin w3-border w3-border-green\" style=\"width:100% ; display:flex \">"
            + "<div style =\"width: 50% ;align-items:center\" "
            
            + "    <label class=\"w3-text-green\"><b>Guardian last name</b></label>"
            + "                         <input type=\"text\"  class = \"w3-input w3-margin w3-border\"  value=\""+testGuardNameFirst+"\" style=\"width:75% \"name=\"guardian_last_name\" />\r\n"

            +"</div>"
            
            + "<div style =\"width: 50% ;align-items:center\" "
            
            + "    <label class=\"w3-text-green\"><b>Guardian first name</b></label>"
            + "                         <input type=\"text\"  class = \"w3-input w3-margin w3-border\"  value=\""+testGuardNameLast+"\" style=\"width:75% \"name=\"guardian_first_name\" />\r\n"

            
            +"</div>"
            
            + "<div style =\"width: 50% ;align-items:center\" "
            
            + " <label class=\"w3-text-green\"><b>Guardian middle name</b></label>"
            + "                         <input type=\"text\"  class = \"w3-input w3-margin w3-border\"  value=\""+testGuardMiddleName+"\" style=\"width:75% \"name=\"guardian_middle_name\" />\r\n"

            +"</div>"
            
            + "<div style =\"width: 50% ;align-items:center\" "
            
            + " <label class=\"w3-text-green\"><b>Guardian relationship to patient</b></label>"
            //+ "                         <input type=\"text\"  class = \"w3-input w3-margin w3-border\"  value=\""+testGuardRelationship+"\" style=\"width:75% \"name=\"guardian_relation\" />\r\n"
            +"  <p class=\"w3-margin\" style=\"width:30% height:5%\">"
            +"                          <SELECT style=\"width : 100%\" name=\"guardian_relation\" size=\"1\">\r\n");
            for(Code code : codeListRelation) {
              out.println("                             <OPTION value=\""+code.getValue()+"\">"+code.getLabel()+"</Option>\r\n");
            }
            out.println( "                        </SELECT>\r\n"
            +"  </p>"
            +"</div>"
            +"</div>"

           

            + "                <button class=\"w3-button w3-round-large w3-green w3-hover-teal w3-margin \"  >Validate</button>\r\n"
            + "                </form> " + "</div\r\n");


        doFooter(out, session);
      }
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
        + "  <a href = \'silos \' class=\"w3-bar-item w3-button\">List of silos </a>\r\n"
        + "  <a href = \'facility_patient_display\' class=\"w3-bar-item w3-button\">Facilities/patients list</a>\r\n"
        + "  <a href = \'silo_creation\' class=\"w3-bar-item w3-button\">Silo creation </a>\r\n"
        + "</div>" + "    	</header>");
    out.println("<div class=\"w3-display-container w3-margin\" style=\"height:600px;\">");
  }

  public static void doFooter(PrintWriter out, HttpSession session) {
    out.println("</div>\r\n" + "    </body>\r\n" + "</html>");
  }

}
