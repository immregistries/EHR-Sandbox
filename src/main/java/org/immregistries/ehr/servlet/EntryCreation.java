package org.immregistries.ehr.servlet;

import java.io.IOException;
import java.io.PrintWriter;
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
import org.immregistries.ehr.model.Clinician;
import org.immregistries.ehr.model.Facility;
import org.immregistries.ehr.model.LogsOfModifications;

/**
 * Servlet implementation class Entry
 */
public class EntryCreation extends HttpServlet {
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
    
    List<LogsOfModifications> logList = null;
    Query query = dataSession.createQuery("from LogsOfModifications where modifType=?");
    query.setParameter(0, "modif");
    logList = query.list();
    log = logList.get(0);
    
    List<Clinician> clinicianList1 = null;
    query = dataSession.createQuery("from Clinician where nameLast=?");
    query.setParameter(0, nameAdmi);
    clinicianList1 = query.list();
    admicli = clinicianList1.get(0);
    
    List<Clinician> clinicianList2 = null;
    query = dataSession.createQuery("from Clinician where nameLast=?");
    query.setParameter(0, nameOrder);
    clinicianList2 = query.list();
    ordercli = clinicianList2.get(0);
    
    List<Clinician> clinicianList3 = null;
    query = dataSession.createQuery("from Clinician where nameLast=?");
    query.setParameter(0, nameEnter);
    clinicianList3 = query.list();
    entercli = clinicianList3.get(0);
    
    List<Vaccine> vaccineList = null;
    query = dataSession.createQuery("from Vaccine where actionCode=?");
    query.setParameter(0, vaccCode);
    vaccineList = query.list();
    vaccine = vaccineList.get(0);
    
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
        

        
        facility = (Facility) session.getAttribute("facility");
        patient = (Patient) session.getAttribute("patient") ;
        
        //System.out.println(silo.getNameDisplay()+"  current silo");
        
        System.out.println(facility.getNameDisplay()+"  current facility");
        System.out.println(patient.getNameFirst()+"  current patient");
        
        
        String show = req.getParameter(PARAM_SHOW);
        out.println("<form method=\"post\" class=\"w3-container\" action=\"entry_creation\">\r\n"

            + "<label class=\"w3-text-green\"><b>Administering clinician</b></label>"
            + "  						<input type=\"text\" class = \"w3-input w3-margin w3-border \" required value=\"\" size=\"40\" maxlength=\"60\" name=\"administering_cli\" />\r\n"

            + "	<label class=\"w3-text-green\"><b>Entering clinician</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" name=\"entering_cli\" />\r\n"

            + "	<label class=\"w3-text-green\"><b>Ordering clinician</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" name=\"ordering_cli\" />\r\n"

            + "	<label class=\"w3-text-green\"><b>Administered date</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" name=\"administered_date\" />\r\n"


            + "	<label class=\"w3-text-green\"><b>Vaccine ID</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" name=\"vacc_id\" />\r\n"


            + "	<label class=\"w3-text-green\"><b>Vaccine CVX code </b></label>"
            //+ "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" name=\"vacc_cvx\"/>\r\n"
            +"  <p>"
            +"                          <SELECT name=\"vacc_cvx\" size=\"1\">\r\n"
            + "                             <OPTION value=\"\">Select a vaccine</Option>\r\n");
            for(Code code : codeListCVX) {
              out.println("                             <OPTION value=\""+code.getValue()+"\">"+code.getLabel()+"</Option>\r\n");
            }
            out.println( "                        </SELECT>\r\n"
            +"  </p>"
            + "	<label class=\"w3-text-green\"><b>Vaccine NDC code</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" name=\"vacc_ndc\"/>\r\n"

            + "	<label class=\"w3-text-green\"><b>Vaccine MVX code</b></label>"
            //+ "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" name=\"vacc_mvx\"/>\r\n"
            +"  <p>"
            +"                          <SELECT name=\"vacc_mvx\" size=\"1\">\r\n"
            + "                             <OPTION value=\"\">Select a vaccine</Option>\r\n");
            for(Code code : codeListMVX) {
              out.println("                             <OPTION value=\""+code.getValue()+"\">"+code.getLabel()+"</Option>\r\n");
            }
            out.println( "                        </SELECT>\r\n"
            +"  </p>"
            
            + "	<label class=\"w3-text-green\"><b>Administered amount</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" name=\"administered_amount\"/>\r\n"

            + "	<label class=\"w3-text-green\"><b>Manufacturer</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" name=\"manufacturer\" />\r\n"

            + "	<label class=\"w3-text-green\"><b>Information source</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\"  name=\"info_source\"/>\r\n"

            + "	<label class=\"w3-text-green\"><b>Lot number</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" name=\"lot_number\"/>\r\n"

            + "	<label class=\"w3-text-green\"><b>Expiration_date</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\"name=\"expiration_date\" />\r\n"


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
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\"name=\"funding_source\" />\r\n"

            + "	<label class=\"w3-text-green\"><b>Funding eligbility</b></label>"
            + "	                    	<input type=\"text\"  class = \"w3-input w3-margin w3-border\" required value=\"\" size=\"40\" maxlength=\"60\" name=\"funding_eligibility\"/>\r\n"

            + "                <button onclick=\"location.href=\'patient_record\'\" class=\"w3-button w3-round-large w3-green w3-hover-teal w3-margin \"  >Validate</button>\r\n"
            +"                  <button formaction=\"IIS_message\" class=\"w3-button w3-round-large w3-green w3-hover-teal w3-margin \"  >See message</button>\r\n"
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