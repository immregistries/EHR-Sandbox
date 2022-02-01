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
 * Servlet implementation class EntryRecord
 */
public class EntryRecord extends HttpServlet {
  private static final long serialVersionUID = 1L;
  public static final String PARAM_SHOW = "show";

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    
    HttpSession session = req.getSession(true);
    Session dataSession = PopServlet.getDataSession();
    
    Transaction transaction = dataSession.beginTransaction();
    //Silo silo = new Silo();
    Facility facility = (Facility) session.getAttribute("facility");
    Patient patient = (Patient) session.getAttribute("patient");
    
    VaccinationEvent vacc_ev = new VaccinationEvent(); 
    Vaccine vaccine = new Vaccine();
    
    int paramEntry =  Integer.parseInt(req.getParameter("paramEntry"))+1;
    
    
    vaccine = (Vaccine) dataSession.load(vaccine.getClass(),paramEntry);
   
    Query query = dataSession.createQuery("From VaccinationEvent");
    List<VaccinationEvent> list = query.list();
    for(VaccinationEvent vaccEv : list) {
      
      
      if (vaccEv.getVaccine().getVaccineId() == paramEntry) {
        
        vacc_ev = vaccEv;
        session.setAttribute("vacc_ev", vacc_ev);
      }
    }
    
    System.out.println(vacc_ev.getVaccine().getVaccineId() + " Vacc Id from Vacc_ev");
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
    String nameAdmi = req.getParameter("administering_cli");  
    String nameOrder = req.getParameter("ordering_cli");  
    String nameEnter = req.getParameter("entering_cli"); 
    Clinician admicli = new Clinician();
    Clinician ordercli = new Clinician();
    Clinician entercli = new Clinician();
    
    admicli =  (Clinician) dataSession.load(admicli.getClass(), vacc_ev.getAdministeringClinician().getClinicianId()); 
    ordercli =  (Clinician) dataSession.load(ordercli.getClass(), vacc_ev.getOrderingClinician().getClinicianId()); 
    entercli =  (Clinician) dataSession.load(entercli.getClass(), vacc_ev.getEnteringClinician().getClinicianId()); 
    
    
    admicli.setNameLast(nameAdmi.split("").length>0 ? nameAdmi.split(" ")[0]:" ");    
    admicli.setNameFirst(nameAdmi.split(" ").length>1 ? nameAdmi.split(" ")[1]: " ");
    admicli.setNameMiddle(nameAdmi.split(" ").length>2 ? nameAdmi.split(" ")[2]:" ");
    
    
    ordercli.setNameLast(nameOrder.split(" ").length>0 ? nameOrder.split(" ")[0]:" ");    
    ordercli.setNameFirst(nameOrder.split(" ").length>1 ? nameOrder.split(" ")[1]:" ");
    ordercli.setNameMiddle(nameOrder.split(" ").length>2 ? nameOrder.split(" ")[2]:" ");
    
    
    
    entercli.setNameLast(nameEnter.split(" ").length>0 ? nameEnter.split(" ")[0]:" "); 
    entercli.setNameFirst(nameEnter.split(" ").length>1 ? nameEnter.split(" ")[1]:" ");
    entercli.setNameMiddle(nameEnter.split(" ").length>2 ? nameEnter.split(" ")[2]:" ");
    System.out.println(nameEnter.split(" ")[0]);
    System.out.println(entercli.getNameFirst()+" entercli First name after set");

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
   
    
    vacc_ev.setLog(log);
    vacc_ev.setAdministeringFacility(facility);
    vacc_ev.setPatient(patient);
    vacc_ev.setEnteringClinician(entercli);
    vacc_ev.setOrderingClinician(ordercli);
    vacc_ev.setAdministeringClinician(admicli);
    vacc_ev.setVaccine(vaccine);  
    
    
    dataSession.save(log);
    dataSession.update(admicli);
    dataSession.update(ordercli);
    dataSession.update(entercli);
    dataSession.update(vaccine);
    dataSession.update(vacc_ev);
    transaction.commit();
    resp.sendRedirect("patient_record");
   // doGet(req, resp);
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    HttpSession session = req.getSession(true);
    resp.setContentType("text/html");
    PrintWriter out = new PrintWriter(resp.getOutputStream());
    Session dataSession = PopServlet.getDataSession();
    try {
      {
        doHeader(out, session);
        System.out.println(req.getParameter("paramEntryId"));
        Vaccine vaccine=new Vaccine();
        VaccinationEvent vaccination=new VaccinationEvent();
        List<VaccinationEvent>vaccinationList=null;
        List<Vaccine> entryList = null;
        Patient patient = (Patient) session.getAttribute("patient");
        Facility facility = (Facility) session.getAttribute("facility");

        if(req.getParameter("paramEntryId")!=null && patient != null) {
          Query queryVaccination = dataSession.createQuery("from VaccinationEvent where vaccination_event_Id=? and patient_id=?");
          queryVaccination.setParameter(0, Integer.parseInt(req.getParameter("paramEntryId")));
          queryVaccination.setParameter(1, patient.getPatientId());
          vaccinationList= queryVaccination.list();
          vaccination=vaccinationList.get(0);
          vaccine = vaccination.getVaccine();
          session.setAttribute("vaccine", vaccine);
          session.setAttribute("vacc_ev", vaccination);
          Clinician ordering=vaccination.getOrderingClinician();
          Clinician entering=vaccination.getEnteringClinician();
          Clinician administrating=vaccination.getAdministeringClinician();
        }
        /*Silo silo = new Silo();
        silo= (Silo) req.getAttribute("silo");
       */
        CodeMap codeMap = CodeMapManager.getCodeMap();
        Collection<Code>codeListCVX=codeMap.getCodesForTable(CodesetType.VACCINATION_CVX_CODE);
        Collection<Code>codeListMVX=codeMap.getCodesForTable(CodesetType.VACCINATION_MANUFACTURER_CODE);
        Collection<Code>codeListNDC=codeMap.getCodesForTable(CodesetType.VACCINATION_NDC_CODE_UNIT_OF_USE);
        Collection<Code>codeListInfSource=codeMap.getCodesForTable(CodesetType.VACCINATION_INFORMATION_SOURCE);
        
        
        //System.out.println(silo.getNameDisplay()+"  current silo");
        
        System.out.println(facility.getNameDisplay()+"  current facility");
        System.out.println(patient.getNameFirst()+"  current patient");
        
        String show = req.getParameter(PARAM_SHOW);
        String testAdministering = ""+vaccination.getAdministeringClinician().getNameFirst()+" "
            +vaccination.getAdministeringClinician().getNameMiddle()+" "
            +vaccination.getAdministeringClinician().getNameLast();
        String testEntering = ""+vaccination.getEnteringClinician().getNameFirst()+" "
            +vaccination.getEnteringClinician().getNameMiddle()+" "
            +vaccination.getEnteringClinician().getNameLast();
        String testOrdering = ""+vaccination.getOrderingClinician().getNameFirst()+" "
            +vaccination.getOrderingClinician().getNameMiddle()+" "
            +vaccination.getOrderingClinician().getNameLast();
        String testAdministeredDate = ""+vaccine.getAdministeredDate();
        String testVaccId = ""+vaccine.getVaccineId();
        Code testCodeCvx= null;
        Code testCodeNDC= null;
        String testCvx=""+vaccine.getVaccineCvxCode();
        String testMvx=""+vaccine.getVaccineMvxCode();
        String testNdc=""+vaccine.getVaccineNdcCode();
        Code testCodeMvx= null;
        String testAmount=""+vaccine.getAdministeredAmount();
        String testManufacturer="";
        String testInfSource=""+vaccine.getInformationSource();
        String testLot=""+vaccine.getLotnumber();
        String testExpDate=""+vaccine.getExpirationDate();
        String testCompletion=""+vaccine.getCompletionStatus();
        String testActionCode=""+vaccine.getActionCode();
        String testRefusal=""+vaccine.getRefusalReasonCode();
        String testBodySite=""+vaccine.getBodySite();
        String testBodyRoute=""+vaccine.getBodyRoute();
        String fundingSource=""+vaccine.getFundingSource();
        String fundingRoute=""+vaccine.getFundingEligibility();
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
              testAdministering = faker.name().firstName()+" "+faker.name().firstName()+" "+faker.name().lastName();
              testEntering = faker.name().firstName()+" "+faker.name().firstName()+" "+faker.name().lastName();
              testOrdering = faker.name().firstName()+" "+faker.name().firstName()+" "+faker.name().lastName();
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
            
            out.println("<form method=\"post\" class=\"w3-container\" action=\"entry_record\">\r\n"
                + "<div class = \"w3-margin w3-border w3-border-green\" style=\"width:100% ; display:flex \">"
                + "<div style =\"width: 50% ;align-items:center\" "
               
                + " <label class=\"w3-text-green\"><b>Administered date</b></label>"
                + "                         <input type=\"text\"     class = \" w3-margin w3-border\"  value=\""+testAdministeredDate +"\" style=\"width:75% \"  name=\"administered_date\" />\r\n"
                +"</div>"
                + "<div style =\"width: 50%; align-items:center \" "
                + " <label class=\"w3-text-green\"><b>Administered amount</b></label>"
                + "                         <input type=\"text\"    class = \" w3-margin w3-border\"  value=\""+ testAmount +"\" style=\"width:75% \"  name=\"administered_amount\"/>\r\n"
                +"</div>"
                
                + "</div>"
                + "<div class = \"w3-margin w3-border w3-border-green\" style=\"width:100% ;display:flex\">"
                + "<div style =\"width: 30% ;align-items:center\" "
                + "    <label class=\"w3-text-green\"><b>Vaccine CVX code </b></label>"


            + "                           <input type=\"text\"    class = \" w3-margin w3-border\"  value=\""+testCvx+"\" size=\"40\" maxlength=\"60\" name=\"vacc_cvx\"/>\r\n"

                +"</div>"
                + "<div style =\"width: 30% ;align-items:center\" "
                + " <label class=\"w3-text-green\"><b>Vaccine NDC code</b></label>"
                + "                         <input type=\"text\"   style=\"width:90%; height:23\" class = \" w3-margin w3-border\"  value=\""+testNdc+"\" size=\"40\" maxlength=\"60\" name=\"vacc_ndc\"/>\r\n"
                +"</div>"
                + "<div style =\"width: 30% ;align-items:center\" "
                + " <label class=\"w3-text-green\"><b>Vaccine MVX code</b></label>"
                
               
                + "                           <input type=\"text\"    class = \" w3-margin w3-border\"  value=\""+testMvx+"\" size=\"40\" maxlength=\"60\" name=\"vacc_mvx\"/>\r\n"
                

                +"</div>"
                + "</div>"
                + "<div class = \"w3-margin w3-border w3-border-green\" style=\"width:100% ;height:auto; display:flex\">"
                + "<div style =\"width: 50%; align-items:center \" "
                + " <label class=\"w3-text-green\"><b>Lot number</b></label>"
                + "                         <input type=\"text\"     style=\"width:75% \" class = \" w3-margin w3-border\"  value=\""+testLot+"\" size=\"40\" maxlength=\"60\" name=\"lot_number\"/>\r\n"
                +"</div>"
                + "<div style =\"width: 50%; align-items:center \" "
                + " <label class=\"w3-text-green\"><b>Body route</b></label>"
                + "                           <input type=\"text\"    class = \" w3-margin w3-border\"  value=\""+testBodyRoute+"\" size=\"40\" maxlength=\"60\" name=\"body_route\"/>\r\n"
                
                +"</div>"
                + "</div>"
                
                + "<div class = \"w3-margin w3-border w3-border-green\" style=\"width:100%; height:auto; display:flex\">"
                + "<div style =\"width: 50%; align-items:center \" "
                + "    <label class=\"w3-text-green\"><b>Funding source</b></label>"
                + "                         <input type=\"text\"   style=\"width:75% \" class = \" w3-margin w3-border\"  value=\""+fundingSource+"\" size=\"40\" maxlength=\"60\"name=\"funding_source\" />\r\n"
                +"</div>"
                + "<div style =\"width: 50%; align-items:center \" "
                + " <label class=\"w3-text-green\"><b>Funding eligbility</b></label>"
                + "                         <input type=\"text\"   style=\"width:75% \" class = \" w3-margin w3-border\"  value=\""+fundingRoute+"\" size=\"40\" maxlength=\"60\" name=\"funding_eligibility\"/>\r\n"
                +"</div>"
                
                + "</div>"
                + "<div class = \"w3-margin w3-border w3-border-green\" style=\"width:100%; display:flex\">"
                + "<div style =\"width: 30%; align-items:center \" "
                + " <label class=\"w3-text-green\"><b>Entering clinician</b></label>"               
                + "                         <input type=\"text\"   style=\"width:75%\" class = \" w3-margin w3-border\"  value=\""+testEntering +"\" size=\"40\" maxlength=\"60\" name=\"entering_cli\" />\r\n"
                +"</div>"
                + "<div style =\"width: 30%; align-items:center \" "
                + " <label class=\"w3-text-green\"><b>Ordering clinician</b></label>"
                + "                         <input type=\"text\"   style=\"width:75%\" class = \" w3-margin w3-border\"  value=\""+testOrdering +"\" size=\"40\" maxlength=\"60\" name=\"ordering_cli\" />\r\n"
                +"</div>"
                + "<div style =\"width: 30%; align-items:center \" "
                +"<label class=\"w3-text-green\"><b>Administering clinician</b></label>"
                + "                         <input type=\"text\"   style=\"width: 75%\"class = \" w3-margin w3-border \"  value=\""+testAdministering +"\" size=\"40\" maxlength=\"60\" name=\"administering_cli\" />\r\n"
                +"</div>"
                
                + "</div>"
                + "<div class = \"w3-margin w3-border w3-border-green\" style=\"width:100% ;display:flex\">"
                
                
                + "<div style =\"width: 50%; align-items:center \" "

                + " <label class=\"w3-text-green\"><b>Vaccine ID</b></label>"
                
                +"<input type=\"text\"   style=\"width:75%\" class = \" w3-margin w3-border\"  value=\""+testVaccId+"\" size=\"40\" maxlength=\"20\" name=\"vacc_id\" />\r\n"

                +"</div>"
                
                + "<div style =\"width: 50%; align-items:center \" "
                + " <label class=\"w3-text-green\"><b>Information source</b></label>"
                + "                         <input type=\"text\"   style=\"width:75%\" class = \" w3-margin w3-border\"  value=\""+testInfSource+"\" size=\"40\" maxlength=\"60\"  name=\"information_source\"/>\r\n"
                +"</div>"
                
                + "</div>"
                + "<div class = \"w3-margin w3-border w3-border-green\" style=\"width:100% ;display:flex\">"
                + "<div style =\"width: 50%; align-items:center \" "
                + " <label class=\"w3-text-green\"><b>Expiration_date</b></label>"
                + "                         <input type=\"text\"   style=\"width:75%\" class = \" w3-margin w3-border\"  value=\""+testExpDate+"\" size=\"40\" maxlength=\"60\"name=\"expiration_date\" />\r\n"
                +"</div>"
                + "<div style =\"width: 50%; align-items:center \" "
                + " <label class=\"w3-text-green\"><b>Completion status</b></label>"
                + "                           <input type=\"text\"    class = \" w3-margin w3-border\"  value=\""+testCompletion+"\" size=\"40\" maxlength=\"60\" name=\"completion_status\" />\r\n"
                
                +"</div>"
                + "</div>"
                + "<div class = \"w3-margin w3-border w3-border-green\" style=\"width:100% ;display:flex\">"
                + "<div style =\"width: 50%; align-items:center \" "
                + " <label class=\"w3-text-green\"><b>Action code</b></label>"
                + "                           <input type=\"text\"    class = \" w3-margin w3-border\"  value=\""+testActionCode+"\" size=\"40\" maxlength=\"60\" name=\"action_code\"/>\r\n"
                
                +"</div>"
                + "<div style =\"width: 50%; align-items:center \" "
                + " <label class=\"w3-text-green\"><b>Refusal reason code</b></label>"
                + "                           <input type=\"text\"    class = \" w3-margin w3-border\"  value=\""+(testRefusal==""? "none":testRefusal)+"\" size=\"40\" maxlength=\"60\" name=\"refusal_reason_code\"/>\r\n"
                
                +"</div>"
                
                + "</div>"
                + "<div class = \"w3-margin w3-border w3-border-green\" style=\"width:100% ;display:flex\">"
                
                + "<label class=\"w3-text-green\"><b>Body site</b></label>"
                + "                           <input type=\"text\"    class = \" w3-margin w3-border\"  value=\""+testBodySite+"\" size=\"40\" maxlength=\"60\" name=\"body_site\" />\r\n"
                + "<input type=\"hidden\" id=\"paramEntry\" name=\"paramEntry\" value="+req.getParameter("paramEntryId")+">"
                   

                + "</div>"
                + "<button class=\"w3-button w3-round-large w3-green w3-hover-teal w3-margin\">Validate</button>\r\n"
                +"                  <button formaction=\"IIS_message\" class=\"w3-button w3-round-large w3-green w3-hover-teal w3-margin \"  >HL7v2 messaging</button>\r\n"
                + "</form>" 
                
                + "<button onclick=\"location.href='FHIR_messaging?paramEntryId=" + req.getParameter("paramEntryId") + "'\" " 
                + "class=\"w3-button w3-round-large w3-green w3-hover-teal w3-margin \">FHIR Messaging</button>\r\n"
                + "</div\r\n");

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
        
        + "  <a href = \'Settings\' class=\"w3-bar-item w3-right w3-button\">Settings </a>\r\n"
        + "</div>" + "      </header>");
    out.println("<div class=\"w3-display-container w3-margin\" style=\"height:600px;\">");
  }


  public static void doFooter(PrintWriter out, HttpSession session) {
    out.println("</div>\r\n" + "    </body>\r\n" + "</html>");
  }




}