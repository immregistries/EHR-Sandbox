package org.immregistries.ehr.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import org.immregistries.ehr.model.Patient;
import org.immregistries.ehr.model.Silo;
import org.immregistries.ehr.model.Tester;
import org.immregistries.ehr.model.VaccinationEvent;
import org.immregistries.ehr.model.Vaccine;
import org.immregistries.iis.kernal.model.CodeMapManager;
import com.github.javafaker.Faker;
import org.immregistries.ehr.model.Clinician;
import org.immregistries.ehr.model.Facility;

/**
 * Servlet implementation class EntryRecord
 */
public class EntryCreation extends HttpServlet {
  private static final long serialVersionUID = 1L;
  public static final String PARAM_SHOW = "show";

  @SuppressWarnings("UnusedAssignment")
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    Boolean creation = true;
    HttpSession session = req.getSession(true);
    Session dataSession = PopServlet.getDataSession();


    Transaction transaction = dataSession.beginTransaction();
    Facility facility = (Facility) session.getAttribute("facility");
    Patient patient = (Patient) session.getAttribute("patient");
    int paramEntry;

    VaccinationEvent vacc_ev;
    Vaccine vaccine;
    String nameAdmi = req.getParameter("administering_cli");
    String nameOrder = req.getParameter("ordering_cli");
    String nameEnter = req.getParameter("entering_cli");
    Clinician admicli = new Clinician();
    Clinician ordercli = new Clinician();
    Clinician entercli = new Clinician();

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Date updatedDate = new Date();
    Date administeredDate=new Date();
    Date expiredDate=new Date();

    if(req.getParameter("paramEntryId")!=null && !req.getParameter("paramEntryId").equals("null")){
      paramEntry =  Integer.parseInt(req.getParameter("paramEntryId"));
      creation = false;
      vacc_ev = (VaccinationEvent) dataSession.load(new VaccinationEvent().getClass(),paramEntry);
      vaccine = vacc_ev.getVaccine();

      admicli =  (Clinician) dataSession.load(admicli.getClass(), vacc_ev.getAdministeringClinician().getClinicianId());
      ordercli =  (Clinician) dataSession.load(ordercli.getClass(), vacc_ev.getOrderingClinician().getClinicianId());
      entercli =  (Clinician) dataSession.load(entercli.getClass(), vacc_ev.getEnteringClinician().getClinicianId());

    } else {
      creation = true;
      vaccine = new Vaccine();
      vacc_ev = new VaccinationEvent();

      try {
        administeredDate = sdf.parse(req.getParameter("administered_date"));
        expiredDate = sdf.parse(req.getParameter("expiration_date"));
      } catch (ParseException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }


    admicli.setNameLast(nameAdmi.split("").length>0 ? nameAdmi.split(" ")[0]:" ");
    admicli.setNameFirst(nameAdmi.split(" ").length>1 ? nameAdmi.split(" ")[1]: " ");
    admicli.setNameMiddle(nameAdmi.split(" ").length>2 ? nameAdmi.split(" ")[2]:" ");

    ordercli.setNameLast(nameOrder.split(" ").length>0 ? nameOrder.split(" ")[0]:" ");
    ordercli.setNameFirst(nameOrder.split(" ").length>1 ? nameOrder.split(" ")[1]:" ");
    ordercli.setNameMiddle(nameOrder.split(" ").length>2 ? nameOrder.split(" ")[2]:" ");

    entercli.setNameLast(nameEnter.split(" ").length>0 ? nameEnter.split(" ")[0]:" ");
    entercli.setNameFirst(nameEnter.split(" ").length>1 ? nameEnter.split(" ")[1]:" ");
    entercli.setNameMiddle(nameEnter.split(" ").length>2 ? nameEnter.split(" ")[2]:" ");

    vaccine.setActionCode(req.getParameter("action_code"));
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
    vaccine.setRefusalReasonCode(req.getParameter("refusal_reason_code"));    
    vaccine.setUpdatedDate(updatedDate);
    vaccine.setVaccineCvxCode(req.getParameter("vacc_cvx"));
    vaccine.setVaccineMvxCode(req.getParameter("vacc_mvx"));
    vaccine.setVaccineNdcCode(req.getParameter("vacc_ndc"));


    vacc_ev.setAdministeringFacility(facility);
    vacc_ev.setPatient(patient);
    vacc_ev.setEnteringClinician(entercli);
    vacc_ev.setOrderingClinician(ordercli);
    vacc_ev.setAdministeringClinician(admicli);
    vacc_ev.setVaccine(vaccine);
    if(creation) {
      dataSession.save(admicli);
      dataSession.save(ordercli);
      dataSession.save(entercli);
      dataSession.save(vaccine);
      dataSession.save(vacc_ev);
    } else {
      dataSession.update(admicli);
      dataSession.update(ordercli);
      dataSession.update(entercli);
      dataSession.update(vaccine);
      dataSession.update(vacc_ev);
    }
    transaction.commit();

    session.setAttribute("vaccine", vaccine);
    switch(req.getParameter("nextPage")) {
      case "patient_record":
        resp.sendRedirect("patient_record");
        break;
      case "IIS_message":
        resp.sendRedirect("IIS_message");
        break;
      case "FHIR_messaging":
        resp.sendRedirect("FHIR_messaging?paramEntryId=" + vacc_ev.getVaccinationEventId());
        break;
    }
    doGet(req, resp);
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
        ServletHelper.doStandardHeader(out, session);
        Boolean creation = true;


        Patient patient = (Patient) session.getAttribute("patient");
        Facility facility = (Facility) session.getAttribute("facility");
        Silo silo = (Silo) session.getAttribute("silo");

        Vaccine vaccine = null;
        VaccinationEvent vaccination = null;

        Boolean vaccinationPreLoaded = false;

        if(req.getParameter("paramEntryId")!=null && patient != null) {
          creation = false;
          vaccinationPreLoaded = true;
          Query queryVaccination = dataSession.createQuery("from VaccinationEvent where vaccination_event_Id=? and patient_id=?");
          queryVaccination.setParameter(0, Integer.parseInt(req.getParameter("paramEntryId")));
          queryVaccination.setParameter(1, patient.getPatientId());
          vaccination = (VaccinationEvent) queryVaccination.uniqueResult();
          vaccine = vaccination.getVaccine();
        } else {
          creation = true;
          if(req.getParameter("testEntry")!=null) { // Generate random test vaccination
            vaccinationPreLoaded = true;
            vaccination = VaccinationEvent.random(patient,facility);
            vaccine = vaccination.getVaccine();
          }
        }


        String testAdministering = "";
        String testEntering = "";
        String testOrdering = "";
        String testAdministeredDate = "";
        String testVaccId = "";
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
        String fundingEligibility="";
        Code testCodeCvx= null;
        Code testCodeNdc= null;
        Code testCodeMvx= null;
        String testNdc="";
        String testCvx="";
        String testMvx="";

        CodeMap codeMap = CodeMapManager.getCodeMap();
        Collection<Code>codeListCVX=codeMap.getCodesForTable(CodesetType.VACCINATION_CVX_CODE);
        Collection<Code>codeListMVX=codeMap.getCodesForTable(CodesetType.VACCINATION_MANUFACTURER_CODE);
        Collection<Code>codeListNDC=codeMap.getCodesForTable(CodesetType.VACCINATION_NDC_CODE_UNIT_OF_USE);
        Collection<Code>codeListInfSource=codeMap.getCodesForTable(CodesetType.VACCINATION_INFORMATION_SOURCE);


        if (vaccinationPreLoaded){ // Load Vaccination info in form
          session.setAttribute("vaccine", vaccine);
          session.setAttribute("vacc_ev", vaccination);

          testAdministering = ""+vaccination.getAdministeringClinician().getNameFirst()+" "
                  +vaccination.getAdministeringClinician().getNameMiddle()+" "
                  +vaccination.getAdministeringClinician().getNameLast();
          testEntering = ""+vaccination.getEnteringClinician().getNameFirst()+" "
                  +vaccination.getEnteringClinician().getNameMiddle()+" "
                  +vaccination.getEnteringClinician().getNameLast();
          testOrdering = ""+vaccination.getOrderingClinician().getNameFirst()+" "
                  +vaccination.getOrderingClinician().getNameMiddle()+" "
                  +vaccination.getOrderingClinician().getNameLast();
          testAdministeredDate = ""+vaccine.getAdministeredDate();
          testVaccId = ""+vaccine.getVaccineId();

          testAmount=""+vaccine.getAdministeredAmount();
          testManufacturer="";
          testInfSource=""+vaccine.getInformationSource();
          testLot=""+vaccine.getLotnumber();
          testExpDate=""+vaccine.getExpirationDate();
          testCompletion=""+vaccine.getCompletionStatus();
          testActionCode=""+vaccine.getActionCode();
          testRefusal=""+vaccine.getRefusalReasonCode();
          testBodySite=""+vaccine.getBodySite();
          testBodyRoute=""+vaccine.getBodyRoute();
          fundingSource=""+vaccine.getFundingSource();
          fundingEligibility=""+vaccine.getFundingEligibility();

          testCvx=""+vaccine.getVaccineCvxCode();
          testMvx=""+vaccine.getVaccineMvxCode();
          testNdc=""+vaccine.getVaccineNdcCode();
          testCodeCvx= null;
          testCodeMvx= null;
          testCodeNdc= null;
          for(Code code : codeListCVX) {
            if (code.getValue().equals(testCvx)) {
              testCodeCvx=code;
              break;
            }
          }
          for(Code code : codeListMVX) {
            if (code.getValue().equals(testMvx)) {
              testCodeMvx=code;
              break;
            }
          }
          for(Code code : codeListNDC) {
            if (code.getValue().equals(testNdc)) {
              testCodeNdc=code;
              break;
            }
          }
        }
        resp.setContentType("text/html");
        out.println("<div class=\"w3-margin-bottom\"style=\"width:100% height:auto \" >"
                + "<label class=\"w3-text-green w3-margin-right w3-margin-bottom\"><b>Current tenant : "
                + silo.getNameDisplay() + "</b></label>");

        out.println( "<label class=\"w3-text-green w3-margin-left w3-margin-bottom\"><b>Current Facility : "
                + facility.getNameDisplay() + "</b></label>");
        out.println(
                "<label class=\"w3-text-green w3-margin-left \"><b>     Current Patient : "
                        + patient.getNameFirst() + "  " + patient.getNameLast() + "</b></label>"+"</div>"
        );
        out.println("<button onclick=\"location.href='entry_creation?testEntry=1'\" class=\"w3-button w3-round-large w3-green w3-hover-teal w3-margin \"  >Fill with test informations</button><br/>");
        out.println("<form method=\"post\" class=\"w3-container\" action=\"entry_record\">\r\n");


        out.println("<div class = \"w3-margin w3-border w3-border-green\" style=\"width:100% ; display:flex \">"
                + "<div style =\"width: 50% ;align-items:center\" "

                + " <label class=\"w3-text-green\"><b>Administered date</b></label>"
                + "<label class=\"w3-text-red w3-margin-right\"><b>*</b></label> "
                + "                         <input type=\"date\"   class = \" w3-margin w3-border\"  value=\""+testAdministeredDate +"\" style=\"width:75% \"  name=\"administered_date\" />\r\n"
                +"</div>"
                + "<div style =\"width: 50%; align-items:center \" "
                + " <label class=\"w3-text-green\"><b>Administered amount</b></label>"
                + "                         <input type=\"text\"  class = \" w3-margin w3-border\"  value=\""+ testAmount +"\" style=\"width:75% \"  name=\"administered_amount\"/>\r\n"
                +"</div>"

                + "</div>"
                + "<div class = \"w3-margin w3-border w3-border-green\" style=\"width:100% ;display:flex\">"
                + "<div style =\"width: 30% ;align-items:center\" "
                + "    <label class=\"w3-text-green\"><b>Vaccine CVX code </b></label>"
                + "<label class=\"w3-text-red w3-margin-right\"><b>*</b></label> "
                +"  <p class=\"w3-margin\" style=\"width:30% height:5%\">"
                +"                          <SELECT style=\"width : 100%\" name=\"vacc_cvx\" size=\"1\">\r\n");
        if(testCodeCvx==null && creation) {
          out.println("<OPTION value=\"\">Select a vaccine</Option>\r\n");
        }
        else {
          out.println("<OPTION value=\""+testCodeCvx.getValue()+"\">"+testCodeCvx.getLabel()+"</Option>\r\n");
        }
        for(Code code : codeListCVX) {
          out.println("<OPTION value=\""+code.getValue()+"\">"+code.getLabel()+"</Option>\r\n");
        }
        out.println( "</SELECT>\r\n"
                + "</p>"
                + "</div>"
                + "<div style =\"width: 30% ;align-items:center\" "
                + " <label class=\"w3-text-green\"><b>Vaccine NDC code</b></label>"
                + " <input type=\"text\" style=\"width:90%; height:23\" class = \" w3-margin w3-border\"  value=\""+testNdc+"\" size=\"40\" maxlength=\"60\" name=\"vacc_ndc\"/>\r\n"
                + "</div>"
                + "<div style =\"width: 30% ;align-items:center\" "
                + " <label class=\"w3-text-green\"><b>Vaccine MVX code</b></label>"
                //+ "                           <input type=\"text\"  class = \" w3-margin w3-border\"  value=\"\" size=\"40\" maxlength=\"60\" name=\"vacc_mvx\"/>\r\n"
                +"  <p class=\"w3-margin\" style=\"width:40% height:5%\">"
                +"                          <SELECT style =\"width:100%\" name=\"vacc_mvx\" size=\"1\">\r\n");
                if(testCodeMvx==null && creation) {
                  out.println( "<OPTION value=\"\">Select a vaccine</Option>\r\n");
                } else {
                  out.println( "<OPTION value=\""+testCodeMvx.getValue()+"\">"+testCodeMvx.getLabel()+"</Option>\r\n");
                }
                for(Code code : codeListMVX) {
                  out.println("<OPTION value=\""+code.getValue()+"\">"+code.getLabel()+"</Option>\r\n");
                }
        out.println( "</SELECT>\r\n"
                +"  </p>"
                +"</div>"
                + "</div>"
                + "<div class = \"w3-margin w3-border w3-border-green\" style=\"width:100% ;height:auto; display:flex\">"
                + "<div style =\"width: 50%; align-items:center \" "
                + " <label class=\"w3-text-green\"><b>Lot number</b></label>"
                + "                         <input type=\"text\"   style=\"width:75% \" class = \" w3-margin w3-border\"  value=\""+testLot+"\" size=\"40\" maxlength=\"60\" name=\"lot_number\"/>\r\n"
                +"</div>"
                + "<div style =\"width: 50%; align-items:center \" "
                + " <label class=\"w3-text-green\"><b>Body route</b></label>"
                //+ "                           <input type=\"text\"  class = \" w3-margin w3-border\"  value=\"\" size=\"40\" maxlength=\"60\" name=\"body_route\"/>\r\n"
                +"  <p class=\"w3-margin\" style=\"width:40% height:5%\">"
                +"                          <SELECT name=\"body_route\" style=\"width:75%\" size=\"1\">\r\n"
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
                +"</div>"
                + "</div>"

                + "<div class = \"w3-margin w3-border w3-border-green\" style=\"width:100%; height:auto; display:flex\">"
                + "<div style =\"width: 50%; align-items:center \" "
                + "    <label class=\"w3-text-green\"><b>Funding source</b></label>"
                + "                         <input type=\"text\" style=\"width:75% \" class = \" w3-margin w3-border\"  value=\""+fundingSource+"\" size=\"40\" maxlength=\"60\"name=\"funding_source\" />\r\n"
                +"</div>"
                + "<div style =\"width: 50%; align-items:center \" "
                + " <label class=\"w3-text-green\"><b>Funding eligibility</b></label>"
                + "                         <input type=\"text\" style=\"width:75% \" class = \" w3-margin w3-border\"  value=\""+fundingEligibility+"\" size=\"40\" maxlength=\"60\" name=\"funding_eligibility\"/>\r\n"
                +"</div>"

                + "</div>"
                + "<div class = \"w3-margin w3-border w3-border-green\" style=\"width:100%; display:flex\">"
                + "<div style =\"width: 30%; align-items:center \" "
                + " <label class=\"w3-text-green\"><b>Entering clinician</b></label>" 
                + "<label class=\"w3-text-red w3-margin-right\"><b>*</b></label> "
                + "                         <input type=\"text\" style=\"width:75%\" class = \" w3-margin w3-border\"  value=\""+testEntering +"\" size=\"40\" maxlength=\"60\" name=\"entering_cli\" />\r\n"
                +"</div>"
                + "<div style =\"width: 30%; align-items:center \" "
                + " <label class=\"w3-text-green\"><b>Ordering clinician</b></label>"
                + "<label class=\"w3-text-red w3-margin-right\"><b>*</b></label> "
                + "                         <input type=\"text\" style=\"width:75%\" class = \" w3-margin w3-border\"  value=\""+testOrdering +"\" size=\"40\" maxlength=\"60\" name=\"ordering_cli\" />\r\n"
                +"</div>"
                + "<div style =\"width: 30%; align-items:center \" "
                + "<label class=\"w3-text-red w3-margin-right\"><b>*</b></label> "
                +"<label class=\"w3-text-green\"><b>Administering clinician</b></label>"
                + "                         <input type=\"text\" style=\"width: 75%\"class = \" w3-margin w3-border \"  value=\""+testAdministering +"\" size=\"40\" maxlength=\"60\" name=\"administering_cli\" />\r\n"
                +"</div>"

                + "</div>"
                + "<div class = \"w3-margin w3-border w3-border-green\" style=\"width:100% ;display:flex\">"


                + "<div style =\"width: 50%; align-items:center \" "
                + " <label class=\"w3-text-green\"><b>Vaccine ID</b></label>"
                + "                         <input type=\"text\" style=\"width:75%\" class = \" w3-margin w3-border\"  value=\""+testVaccId+"\" size=\"40\" maxlength=\"60\" name=\"vacc_id\" />\r\n"
                +"</div>"

                + "<div style =\"width: 50%; align-items:center \" "
                + " <label class=\"w3-text-green\"><b>Information source</b></label>"
                + "                         <input type=\"text\" style=\"width:75%\" class = \" w3-margin w3-border\"  value=\""+testInfSource+"\" size=\"40\" maxlength=\"60\"  name=\"information_source\"/>\r\n"
                +"</div>"

                + "</div>"
                + "<div class = \"w3-margin w3-border w3-border-green\" style=\"width:100% ;display:flex\">"
                + "<div style =\"width: 50%; align-items:center \" "
                + " <label class=\"w3-text-green\"><b>Expiration_date</b></label>"
                + "                         <input type=\"date\" style=\"width:75%\" class = \" w3-margin w3-border\"  value=\""+testExpDate+"\" size=\"40\" maxlength=\"60\"name=\"expiration_date\" />\r\n"
                +"</div>"
                + "<div style =\"width: 50%; align-items:center \" "
                + " <label class=\"w3-text-green\"><b>Completion status</b></label>"
                //+ "                           <input type=\"text\"  class = \" w3-margin w3-border\"  value=\"\" size=\"40\" maxlength=\"60\" name=\"completion_status\" />\r\n"
                +"  <p class=\"w3-margin\" style=\"width:75%\">"
                +"                          <SELECT style=\"width:75%\" name=\"completion_status\" size=\"1\">\r\n"
                + "                             <OPTION value=\"CP\">Complete</Option>\r\n"
                + "                             <OPTION value=\"RE\">Refused</Option>\r\n"
                + "                             <OPTION value=\"NA\">Not administered</Option>\r\n"
                + "                             <OPTION value=\"PA\">Partially administered </Option>\r\n"
                + "                        </SELECT>\r\n"
                +"  </p>"
                +"</div>"
                + "</div>"
                + "<div class = \"w3-margin w3-border w3-border-green\" style=\"width:100% ;display:flex\">"
                + "<div style =\"width: 50%; align-items:center \" "
                + " <label class=\"w3-text-green\"><b>Action code</b></label>"
                //+ "                           <input type=\"text\"  class = \" w3-margin w3-border\"  value=\"\" size=\"40\" maxlength=\"60\" name=\"action_code\"/>\r\n"
                +"  <p class=\"w3-margin\" style=\"width:75%\">"
                +"                          <SELECT style=\"width:75%\"name=\"action_code\" size=\"1\">\r\n"
                + "                             <OPTION value=\"A\">Add</Option>\r\n"
                + "                             <OPTION value=\"D\">Delete</Option>\r\n"
                + "                             <OPTION value=\"U\">Update</Option>\r\n"
                + "                        </SELECT>\r\n"
                +"  </p>"
                +"</div>"
                + "<div style =\"width: 50%; align-items:center \" "
                + " <label class=\"w3-text-green\"><b>Refusal reason code</b></label>"
                //+ "                           <input type=\"text\"  class = \" w3-margin w3-border\"  value=\"\" size=\"40\" maxlength=\"60\" name=\"refusal_reason_code\"/>\r\n"
                +"  <p class=\"w3-margin\" style=\"width:40% height:5%\">"
                +"                          <SELECT style=\"width:75%\"name=\"refusal_reason_code\" size=\"1\">\r\n"
                + "                             <OPTION value=\"\">none</Option>\r\n"
                + "                             <OPTION value=\"00\">Parental decision</Option>\r\n"
                + "                             <OPTION value=\"01\">Religious exemption</Option>\r\n"
                + "                             <OPTION value=\"02\">Other</Option>\r\n"
                + "                             <OPTION value=\"03\">Patient decision </Option>\r\n"
                + "                        </SELECT>\r\n"
                +"  </p>"
                +"</div>"

                + "</div>"
                + "<div class = \"w3-margin w3-border w3-border-green\" style=\"width:100% ;display:flex\">"

                + " <label class=\"w3-text-green\"><b>Body site</b></label>"
                //+ "                           <input type=\"text\"  class = \" w3-margin w3-border\"  value=\"\" size=\"40\" maxlength=\"60\" name=\"body_site\" />\r\n"
                +"  <p class=\"w3-margin\" style=\"width:75%; float:left\">"
                +"                          <SELECT style=\"width:75%\" name=\"body_site\" size=\"1\">\r\n"
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
                + "</p></div>");

        if (creation) {
          out.println(" <input type=\"hidden\" id=\"paramEntryId\" name=\"paramEntryId\" value="+req.getParameter("paramEntryId")+"></input>");
        }
        out.println("<button type=\"submit\"  name=\"nextPage\" value=\"patient_record\" class=\"w3-button w3-round-large w3-green w3-hover-teal w3-margin \"  >Save EntryRecord</button>\r\n");
        out.println("<button type=\"submit\" name=\"nextPage\" value=\"IIS_message\" class=\"w3-button w3-round-large w3-green w3-hover-teal w3-margin \"  >HL7v2 messaging</button>\r\n");
        out.println("<button type=\"submit\"  name=\"nextPage\" value=\"FHIR_messaging\" class=\"w3-button w3-round-large w3-green w3-hover-teal w3-margin \">FHIR Messaging </button>\r\n");
        out.println("</form></div>");
        ServletHelper.doStandardFooter(out, session);
      }
    } catch (Exception e) {
      e.printStackTrace(System.err);
    }
    out.flush();
    out.close();
  }
}