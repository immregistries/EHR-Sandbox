package org.immregistries.ehr.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.immregistries.codebase.client.CodeMap;
import org.immregistries.codebase.client.generated.Code;
import org.immregistries.codebase.client.reference.CodesetType;
import org.immregistries.ehr.HL7printer;
import org.immregistries.ehr.model.Patient;
import org.immregistries.ehr.model.Silo;
import org.immregistries.ehr.model.VaccinationEvent;
import org.immregistries.ehr.model.Vaccine;
import org.immregistries.iis.kernal.model.CodeMapManager;
import com.github.javafaker.Faker;
import org.immregistries.ehr.model.Clinician;
import org.immregistries.ehr.model.Facility;
import org.immregistries.ehr.model.LogsOfModifications;

/**
 * Servlet implementation class Entry
 */
public class Entry extends HttpServlet {
  private static final long serialVersionUID = 1L;
  public static final String PARAM_SHOW = "show";

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    
    HttpSession session = req.getSession(true);
    Session dataSession = PopServlet.getDataSession();
    
    
    //Silo silo = new Silo();
    Facility facility = new Facility();
    Patient patient = new Patient();
    VaccinationEvent vacc_ev = new VaccinationEvent();
    Vaccine vaccine = new Vaccine();  
   /* 
    String hql = "SELECT P.silo FROM Patient P WHERE P.id = "+req.getParameter("paramPatientId");
    Query query = dataSession.createQuery(hql);
    List<Silo> siloList = query.list();
    silo = siloList.get(0);
    
    hql = "SELECT P.facility FROM Patient P WHERE P.id = "+req.getParameter("paramPatientId");
    query = dataSession.createQuery(hql);
    List<Facility> facilityList = query.list();
    facility = facilityList.get(0);    
    */

    //silo = (Silo) session.getAttribute("silo");
    facility = (Facility) session.getAttribute("facility");
    patient = (Patient) session.getAttribute("patient") ;
    String nameAdmi = req.getParameter("administering_cli");  
    String nameOrder = req.getParameter("ordering_cli");  
    String nameEnter = req.getParameter("entering_cli");  
    Clinician admicli = new Clinician();
    
    admicli.setNameLast(nameAdmi);    
    admicli.setNameFirst("alan");
    admicli.setNameMiddle("quentin");
    
    Clinician ordercli = new Clinician();
    ordercli.setNameLast(nameOrder);    
    ordercli.setNameFirst("alan");
    ordercli.setNameMiddle("quentin");
    
    Clinician entercli = new Clinician();
    entercli.setNameLast(nameEnter); 
    entercli.setNameFirst("alan");
    entercli.setNameMiddle("quentin");

    Date updatedDate = new Date();
    LogsOfModifications log = new LogsOfModifications();
    log.setModifDate(updatedDate);
    log.setModifType("modif");
    String vaccCode = req.getParameter("action_code");
    vaccine.setActionCode(vaccCode);
    vaccine.setAdministeredAmount(req.getParameter("administered_amount"));
    vaccine.setAdministeredDate(updatedDate);
    vaccine.setBodyRoute(req.getParameter("body_route"));
    vaccine.setBodySite(req.getParameter("body_site"));
    vaccine.setCompletionStatus(req.getParameter("completion_status"));
    vaccine.setCreatedDate(updatedDate);
    vaccine.setExpirationDate(updatedDate);
    vaccine.setFundingEligibility(req.getParameter("funding_eligibility"));
    vaccine.setFundingSource(req.getParameter("funding_source"));
    vaccine.setInformationSource(req.getParameter("information_source"));
    vaccine.setLotnumber(req.getParameter("lot_number"));
    vaccine.setManufacturer(req.getParameter("manufacturer"));
    vaccine.setRefusalReasonCode(req.getParameter("refusal_reason_code"));    
    vaccine.setUpdatedDate(updatedDate);
    vaccine.setVaccineCvxCode(req.getParameter("vacc_cvx"));
    vaccine.setVaccineMvxCode(req.getParameter("vacc_mvx"));
    vaccine.setVaccineNdcCode(req.getParameter("vacc_ndc"));
    
    Transaction transaction = dataSession.beginTransaction();
    dataSession.save(log);
    dataSession.save(admicli);
    dataSession.save(ordercli);
    dataSession.save(entercli);
    dataSession.save(vaccine);
    transaction.commit();

    System.out.print(entercli.getClinicianId());
    vacc_ev.setLog(log);
    vacc_ev.setAdministeringFacility(facility);
    vacc_ev.setPatient(patient);
    vacc_ev.setEnteringClinician(entercli);
    vacc_ev.setOrderingClinician(ordercli);
    vacc_ev.setAdministeringClinician(admicli);
    vacc_ev.setVaccine(vaccine);  
    
    Transaction transaction2 = dataSession.beginTransaction();
    dataSession.save(vacc_ev);
    transaction2.commit();
    resp.sendRedirect("patient_record");
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
        doHeader(out, session);
        
        /*Silo silo = new Silo();
        silo= (Silo) req.getAttribute("silo");
       */
        Facility facility = new Facility();
        Patient patient = new Patient();
        CodeMap codeMap = CodeMapManager.getCodeMap();
        Collection<Code>codeListCVX=codeMap.getCodesForTable(CodesetType.VACCINATION_CVX_CODE);
        Collection<Code>codeListMVX=codeMap.getCodesForTable(CodesetType.VACCINATION_MANUFACTURER_CODE);
        Collection<Code>codeListNDC=codeMap.getCodesForTable(CodesetType.VACCINATION_NDC_CODE_UNIT_OF_USE);
        Collection<Code>codeListInfSource=codeMap.getCodesForTable(CodesetType.VACCINATION_INFORMATION_SOURCE);
        
        facility = (Facility) session.getAttribute("facility");
        patient = (Patient) session.getAttribute("patient") ;
        
        //System.out.println(silo.getNameDisplay()+"  current silo");
        
        System.out.println(facility.getNameDisplay()+"  current facility");
        System.out.println(patient.getNameFirst()+"  current patient");
        
        String show = req.getParameter(PARAM_SHOW);
        String testAdministering = "";
        String testEntering = "";
        String testOrdering = "";
        String testAdministeredDate = "";
        String testVaccId = "";
        Code testCodeCvx= null;
        Code testCodeNDC= null;
        
        String testNdc="";
        Code testCodeMvx= null;
        String testAmount="";
        String testManufacturer="";
        String testInfSource="";
        String testLot="";
        String testExpDate="";
        String testCompletion="";
        String testActionCode="";
        String testRefusal="";
        String testBodySite="";
        String testBodyRoute="";
        String fundingSource="";
        String fundingRoute="";
        Faker faker = new Faker();
        String pattern = "yyyyMMdd";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        Date currentDate = new Date();
        String streetAddress = faker.address().streetAddress();
            if(req.getParameter("testEntry")!=null) {
              int randomN = (int) (Math.random()*9);
              int randDay = (int) (Math.random()*31);
              int randMonth = (int) (Math.random()*11);
              int randYear = (int) (Math.random()*20);
              int compteur =0;
              testAdministering = faker.name().firstName();
              testEntering = faker.name().firstName();
              testOrdering = faker.name().firstName();
              System.out.println(simpleDateFormat.format(currentDate)); 
              testAdministeredDate = simpleDateFormat.format(currentDate);
              testVaccId = Integer.toString(randomN);
              for(Code code : codeListCVX) {
                testCodeCvx=code;
                
                if(randDay==compteur) {
                  break;
                }
                compteur+=1;
              }
              compteur = 0;
              for(Code code : codeListNDC) {
                testCodeNDC=code;
                compteur+=1;
                if(randomN==compteur) {
                  break;
                }
              }
              compteur=0;
              testNdc=Integer.toString(randomN*23);
              for(Code code : codeListMVX) {
                testCodeMvx=code;
                
                if(randDay==compteur) {
                  break;
                }
                compteur+=1;
              }
              testAmount=Integer.toString(randomN)+".5";
              testManufacturer="Pfizer";
              testInfSource="infSource";
              testLot=Integer.toString(randomN);
              currentDate.setYear(currentDate.getYear()+randYear+1);
              currentDate.setMonth(randMonth);
              currentDate.setDate(randDay);
              testExpDate=simpleDateFormat.format(currentDate);
              testCompletion="Complete";
              testActionCode="Add";
              testRefusal="none";
              testBodySite="Left Thigh";
              testBodyRoute="Intradermal";
              fundingSource="fundS";
              fundingRoute="fundR";
              
              
            }
            
            out.println( "<label class=\"w3-text-green\"><b>Administering clinician</b></label>"
            + "  						<input type=\"text\" class = \"w3-input w3-margin w3-border \" required value=\""+testAdministering +"\" size=\"40\" maxlength=\"60\" name=\"administering_cli\" />\r\n"

            + "	<label class=\"w3-text-green\"><b>Entering clinician</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\""+testEntering +"\" size=\"40\" maxlength=\"60\" name=\"entering_cli\" />\r\n"

            + "	<label class=\"w3-text-green\"><b>Ordering clinician</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\""+testOrdering +"\" size=\"40\" maxlength=\"60\" name=\"ordering_cli\" />\r\n"

            + "	<label class=\"w3-text-green\"><b>Administered date</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\""+testAdministeredDate +"\" placeholder=\"YYYYMMDD\" size=\"40\" maxlength=\"60\" name=\"administered_date\" />\r\n"


            + "	<label class=\"w3-text-green\"><b>Vaccine ID</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\""+testVaccId+"\" size=\"40\" maxlength=\"60\" name=\"vacc_id\" />\r\n"


            + "	<label class=\"w3-text-green\"><b>Vaccine CVX code </b></label>"
            //+ "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" name=\"vacc_cvx\"/>\r\n"
            +"  <p>"
            +"                          <SELECT name=\"vacc_cvx\" size=\"1\">\r\n");
            if(testCodeCvx!=null) {
            out.println( "                            <OPTION value=\""+testCodeCvx.getValue()+"\">"+testCodeCvx.getValue()+" : "+testCodeCvx.getLabel()+"</Option>\r\n");
            }
            out.println( "                             <OPTION value=\"\">Select a vaccine</Option>\r\n");
            
            for(Code code : codeListCVX) {
              out.println("                             <OPTION value=\""+code.getValue()+"\">"+code.getValue()+" : "+code.getLabel()+"</Option>\r\n");
            }
            out.println( "                        </SELECT>\r\n"
            +"  </p>"
            + "	<label class=\"w3-text-green\"><b>Vaccine NDC code</b></label>"
            //+ "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\""+testNdc+"\" size=\"40\" maxlength=\"60\" name=\"vacc_ndc\"/>\r\n"
            +"  <p>"
            +"                          <SELECT name=\"vacc_ndc\" size=\"1\">\r\n");
            if(testCodeNDC!=null) {
            out.println( "                            <OPTION value=\""+testCodeNDC.getValue()+"\">"+testCodeNDC.getValue()+" : "+testCodeNDC.getLabel()+"</Option>\r\n");
            }
            out.println( "                             <OPTION value=\"\">Select a vaccine</Option>\r\n");
            
            for(Code code : codeListNDC) {
              out.println("                             <OPTION value=\""+code.getValue()+"\">"+code.getValue()+" : "+code.getLabel()+"</Option>\r\n");
            }
            out.println( "                        </SELECT>\r\n"
            +"  </p>"
            + "	<label class=\"w3-text-green\"><b>Vaccine MVX code</b></label>"
            //+ "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" name=\"vacc_mvx\"/>\r\n"
            +"  <p>"
            +"<input list=\"listmvx\" name=\"vacc_mvx\">"
            +"                          <datalist id=\"listmvx\"  size=\"40\">\r\n");
            if(testCodeMvx!=null) {
              out.println( "                            <OPTION value=\""+testCodeMvx.getValue()+"\">"+testCodeMvx.getValue()+" : "+testCodeMvx.getLabel()+"</Option>\r\n");
              }

            out.println( "                             <OPTION value=\"\">Select a vaccine</Option>\r\n");
              
            for(Code code : codeListMVX) {
              out.println("                             <OPTION value=\""+code.getValue()+"\">"+code.getValue()+" : "+code.getLabel()+"</Option>\r\n");
            }
            out.println( "                        </datalist>\r\n"
            +"  </p>"
            
            + "	<label class=\"w3-text-green\"><b>Administered amount</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\""+ testAmount +"\" size=\"40\" maxlength=\"60\" name=\"administered_amount\"/>\r\n"

            + "	<label class=\"w3-text-green\"><b>Manufacturer</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\""+testManufacturer+"\" size=\"40\" maxlength=\"60\" name=\"manufacturer\" />\r\n"

            + "	<label class=\"w3-text-green\"><b>Information source</b></label>"
            //+ "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\""+testInfSource+"\" size=\"40\" maxlength=\"60\"  name=\"info_source\"/>\r\n"
            +"  <p>"
            +"                          <SELECT name=\"info_source\" size=\"1\">\r\n");
            if(testInfSource.equals("")) {
            out.println( "                            <OPTION value=\"00\">"+"00 Administered"+"</Option>\r\n");
            }
            for(Code code : codeListInfSource) {
              out.println("                             <OPTION value=\""+code.getValue()+"\">"+code.getValue()+" : "+code.getLabel()+"</Option>\r\n");
            }
            out.println( "                        </SELECT>\r\n"
            +"  </p>"
            + "	<label class=\"w3-text-green\"><b>Lot number</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\""+testLot+"\" size=\"40\" maxlength=\"60\" name=\"lot_number\"/>\r\n"

            + "	<label class=\"w3-text-green\"><b>Expiration_date</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\""+testExpDate+"\" placeholder=\"YYYYMMDD\" size=\"40\" maxlength=\"60\"name=\"expiration_date\" />\r\n"


            + "	<label class=\"w3-text-green\"><b>Completion status</b></label>"
            //+ "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" name=\"completion_status\" />\r\n"
            +"  <p>"
            +"                          <SELECT name=\"completion_status\" size=\"1\">\r\n"
            + "                             <OPTION value=\"CP\">Complete</Option>\r\n"
            + "                             <OPTION value=\"RE\">Refused</Option>\r\n"
            + "                             <OPTION value=\"NA\">Not administered</Option>\r\n"
            + "                             <OPTION value=\"PA\">Partially administered </Option>\r\n"
            + "                        </SELECT>\r\n"
            +"  </p>"
            + "	<label class=\"w3-text-green\"><b>Action code</b></label>"
            //+ "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" name=\"action_code\"/>\r\n"
            +"  <p>"
            +"                          <SELECT name=\"action_code\" size=\"1\">\r\n"
            + "                             <OPTION value=\"A\">Add</Option>\r\n"
            + "                             <OPTION value=\"D\">Delete</Option>\r\n"
            + "                             <OPTION value=\"U\">Update</Option>\r\n"
            + "                        </SELECT>\r\n"
            +"  </p>"
            + "	<label class=\"w3-text-green\"><b>Refusal reason code</b></label>"
            //+ "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" name=\"refusal_reason_code\"/>\r\n"
            +"  <p>"
            +"                          <SELECT name=\"refusal_reason_code\" size=\"1\">\r\n"
            + "                             <OPTION value=\"\">none</Option>\r\n"
            + "                             <OPTION value=\"00\">Parental decision</Option>\r\n"
            + "                             <OPTION value=\"01\">Religious exemption</Option>\r\n"
            + "                             <OPTION value=\"02\">Other</Option>\r\n"
            + "                             <OPTION value=\"03\">Patient decision </Option>\r\n"
            + "                        </SELECT>\r\n"
            +"  </p>"
            + "	<label class=\"w3-text-green\"><b>Body site</b></label>"
            //+ "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" name=\"body_site\" />\r\n"
            +"  <p>"
            +"                          <SELECT name=\"body_site\" size=\"1\">\r\n"
            + "                             <OPTION value=\"LT\">Left Thigh</Option>\r\n"
            + "                             <OPTION value=\"LA\">Left Arm</Option>\r\n"
            + "                             <OPTION value=\"LD\">Left Deltoid</Option>\r\n"
            + "                             <OPTION value=\"LG\">Left Gluteous Medius</Option>\r\n"
            + "                             <OPTION value=\"LVL\">Left Vastus Lateralis</Option>\r\n"
            + "                             <OPTION value=\"LLFA\">Left Lower Forearm</Option>\r\n"
            + "                             <OPTION value=\"RT\">Right Thigh</Option>\r\n"
            + "                             <OPTION value=\"RA\">Right Arm</Option>\r\n"
            + "                             <OPTION value=\"RD\">Right Deltoid</Option>\r\n"
            + "                             <OPTION value=\"RG\">Right Gluteous Medius</Option>\r\n"
            + "                             <OPTION value=\"RVL\">Right Vastus Lateralis</Option>\r\n"
            + "                             <OPTION value=\"RLFA\">Right Lower Forearm</Option>\r\n"
            + "                        </SELECT>\r\n"
            +"  </p>"
            + "	<label class=\"w3-text-green\"><b>Body route</b></label>"
            //+ "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" name=\"body_route\"/>\r\n"
            +"  <p>"
            +"                          <SELECT name=\"body_route\" size=\"1\">\r\n"
            + "                             <OPTION value=\"C38238\">Intradermal</Option>\r\n"
            + "                             <OPTION value=\"C28161\">Intramuscular</Option>\r\n"
            + "                             <OPTION value=\"C38284\">Nasal</Option>\r\n"
            + "                             <OPTION value=\"C38276\">Intravenous</Option>\r\n"
            + "                             <OPTION value=\"C38288\">Oral</Option>\r\n"
            + "                             <OPTION value=\"C38288\">Other/Miscellaneous</Option>\r\n"
            + "                             <OPTION value=\"C38276\">Percutaneous</Option>\r\n"
            + "                             <OPTION value=\"C38299\">Subcutaneous</Option>\r\n"
            + "                             <OPTION value=\"C38305\">Transdermal</Option>\r\n"
            + "                        </SELECT>\r\n"
            +"  </p>"
            + "	<label class=\"w3-text-green\"><b>Funding source</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\""+fundingSource+"\" size=\"40\" maxlength=\"60\"name=\"funding_source\" />\r\n"

            + "	<label class=\"w3-text-green\"><b>Funding eligbility</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\""+fundingRoute+"\" size=\"40\" maxlength=\"60\" name=\"funding_eligibility\"/>\r\n");
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
    out.println("<link rel=\"stylesheet\" href=\"https://www.w3schools.com/w3css/4/w3.css\" />");
    out.println("  </head>");
    out.println("  <body>");
    out.println("    <header class=\"w3-container w3-light-grey\">"
        + "    		<h1>Entry creation</h1>\r\n" + "    	</header>");
    out.println("<div class=\"w3-display-container w3-margin\" style=\"height:600px;\">");

  }

  public static void doFooter(PrintWriter out, HttpSession session) {
    out.println("</div>\r\n" + "    </body>\r\n" + "</html>");
  }


}