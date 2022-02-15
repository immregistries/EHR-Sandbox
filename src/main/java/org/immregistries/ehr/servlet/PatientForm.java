package org.immregistries.ehr.servlet;

import com.github.javafaker.Faker;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.immregistries.codebase.client.CodeMap;
import org.immregistries.codebase.client.generated.Code;
import org.immregistries.codebase.client.reference.CodesetType;
import org.immregistries.ehr.model.Facility;
import org.immregistries.ehr.model.Patient;
import org.immregistries.ehr.model.Silo;
import org.immregistries.iis.kernal.model.CodeMapManager;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class PatientForm extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        Boolean creation = false;
        HttpSession session = req.getSession(true);
        Session dataSession = PopServlet.getDataSession();
        Transaction transaction = dataSession.beginTransaction();

        Silo silo = (Silo) session.getAttribute("silo");
        Facility facility = (Facility) session.getAttribute("facility");

        SimpleDateFormat sdf  = new SimpleDateFormat("yyyy-MM-dd");

        Patient patient;
        if (req.getParameter("paramPatientId")!=null && !req.getParameter("paramPatientId").equals("null")) { // Modifying existing patient
            int paramPatientId =  Integer.parseInt(req.getParameter("paramPatientId"));
            patient = (Patient) dataSession.load(new Patient().getClass(),paramPatientId);
        } else{ // creating new patient
            patient = new Patient();
            patient.setSilo(silo);
            patient.setFacility(facility);
            creation = true;
        }

        patient.setNameFirst(req.getParameter("first_name"));
        patient.setNameLast(req.getParameter("last_name"));
        patient.setNameMiddle(req.getParameter("middle_name"));
        patient.setAddressCity(req.getParameter("city"));
        patient.setAddressCountry(req.getParameter("country"));
        patient.setAddressCountyParish(req.getParameter("county"));
        patient.setDeathFlag(req.getParameter("death_flag"));
        patient.setAddressState(req.getParameter("state"));

        patient.setBirthFlag(req.getParameter("birth_flag"));
        patient.setBirthOrder(req.getParameter("birth_order"));

        patient.setDeathFlag(req.getParameter("death_flag"));
        patient.setEmail(req.getParameter("email"));
        patient.setEthnicity(req.getParameter("ethnicity"));
        patient.setGuardianFirst(req.getParameter("guardian_first_name"));
        patient.setGuardianLast(req.getParameter("guardian_last_name"));
        patient.setGuardianMiddle(req.getParameter("guardian_middle_name"));
        patient.setGuardianRelationship(req.getParameter("guardian_relation"));
        patient.setMotherMaiden(req.getParameter("mother_maiden_name"));
        patient.setPhone(req.getParameter("phone"));
        patient.setProtectionIndicator(req.getParameter("protection_indicator"));

        patient.setPublicityIndicator(req.getParameter("publicity_indicator"));

        patient.setRace(req.getParameter("race"));
        patient.setRegistryStatusIndicator(req.getParameter("registry_status_indicator"));

        try {
            patient.setBirthDate(sdf.parse(req.getParameter("DoB")));
            if (!req.getParameter("DoD").equals("")){
                patient.setDeathDate(sdf.parse(req.getParameter("DoD")));
            }
            if (!req.getParameter("protection_date").equals("")){
                patient.setProtectionIndicatorDate(sdf.parse(req.getParameter("protection_date")));
            }
            if (!req.getParameter("publicity_date").equals("")){
                patient.setPublicityIndicatorDate(sdf.parse(req.getParameter("publicity_date")));
            }
            if (!req.getParameter("registry_status_indicator_date").equals("")){
                patient.setRegistryStatusIndicatorDate(sdf.parse(req.getParameter("registry_status_indicator_date")));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        patient.setSex(req.getParameter("sex"));
        Date updatedDate = new Date();
        patient.setUpdatedDate(updatedDate);

        if (!creation) { // Modifying existing patient
            dataSession.update(patient);
        }else {
            patient.setCreatedDate(updatedDate);
            dataSession.save(patient);
        }
        transaction.commit();

        resp.sendRedirect("facility_patient_display");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        Boolean creation = false;
        HttpSession session = req.getSession(true);
        resp.setContentType("text/html");
        PrintWriter out = new PrintWriter(resp.getOutputStream());
        Session dataSession = PopServlet.getDataSession();
        CodeMap codeMap = CodeMapManager.getCodeMap();
        Collection<Code> codeListRelation =codeMap.getCodesForTable(CodesetType.PERSON_RELATIONSHIP);
        try {
            ServletHelper.doStandardHeader(out, session);

            Facility facility = (Facility) session.getAttribute("facility");
            Silo silo = (Silo) session.getAttribute("silo");
            Patient patient = null;
            if(req.getParameter("paramPatientId")!=null && silo!=null) {
                Query query = dataSession.createQuery("from Patient where patient_id=? and silo_id=?");
                query.setParameter(0, Integer.parseInt(req.getParameter("paramPatientId")));
                query.setParameter(1, silo.getSiloId());
                List<Patient> patientList = query.list();
                patient = patientList.get(0);
                session.setAttribute("patient", patient);

                facility = patient.getFacility();
                session.setAttribute("facility", facility);
            } else if (req.getParameter("paramPatientId") != null){
                patient = (Patient) session.getAttribute("patient");
            } else if (facility == null) {
                resp.sendRedirect("facility_patient_display?chooseFacility=1");
            } else{
                patient = new Patient();
                creation = true;
            }

            resp.setContentType("text/html");

            out.println("<div class=\"w3-margin-bottom\"style=\"width:100% height:auto \" >"
                    + "<label class=\"w3-text-green w3-margin-right w3-margin-bottom\"><b>Current tenant : "
                    + silo.getNameDisplay() + "</b></label>");

            out.println( "<label class=\"w3-text-green w3-margin-left w3-margin-bottom\"><b>Current Facility : "
                    + facility.getNameDisplay() + "</b></label>");


            if (!creation){
                out.println("<label class=\"w3-text-green w3-margin-left \"><b>     Current Patient : "
                        + patient.getNameFirst() + "  " + patient.getNameLast() + "</b></label>");
                out.println("</div>");
            }else{
                out.println("</div>");
                out.println("<button onclick=\"location.href='patient_form?testPatient=1'\" class=\"w3-button w3-round-large w3-green w3-hover-teal w3-margin \"  >Fill with test informations</button><br/>");
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(true);
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
            if(!creation){
                testDoB=""+sdf.format(patient.getBirthDate());
                testNameFirst=""+patient.getNameFirst();
                testNameLast=""+patient.getNameLast();
                testMiddleName=""+patient.getNameMiddle();
                testMotherMaidenName=""+patient.getMotherMaiden();
                testSex=""+patient.getSex();
                testRace=""+patient.getRace();
                testAdress=""+patient.getAddressLine1();
                testCity=""+patient.getAddressCity();
                testCountryCode=""+patient.getAddressCountry();
                testState=""+patient.getAddressState();
                testCountyParish=""+patient.getAddressCountyParish();
                testPhone=""+patient.getPhone();
                testEmail=""+patient.getEmail();
                testEthnicity=""+patient.getEthnicity();
                testBirthFlag=""+patient.getBirthFlag();
                testBirthOrder=""+patient.getBirthOrder();
                testDeathFlag=""+patient.getDeathFlag();
                testDeathDate = null;
                if(patient.getDeathDate()!=null && patient.getDeathDate().equals("")) {
                    testDeathDate=""+sdf.format(patient.getDeathDate());
                }
                testPubIndic=""+patient.getPublicityIndicator();
                testPubIndicDate=""+patient.getPublicityIndicatorDate();
                testProtecIndic=""+patient.getProtectionIndicator();
                testProtecIndicDate=""+sdf.format(patient.getProtectionIndicatorDate());
                testRegIndicDate=""+sdf.format(patient.getRegistryStatusIndicatorDate());
                testRegStatus=""+patient.getRegistryStatusIndicator();
                testRegStatusDate=""+patient.getRegistryStatusIndicatorDate();
                testGuardNameFirst=""+patient.getGuardianFirst();
                testGuardNameLast=""+patient.getGuardianLast();
                testGuardMiddleName=""+patient.getGuardianMiddle();
                testGuardRelationship=""+patient.getGuardianRelationship();
            }

            Faker faker = new Faker();
            Date birthDate = new Date();
            int randDay = (int) (Math.random()*30+1);
            int randMonth = (int) (Math.random()*11);
            int randYear = (int) (Math.random()*121+1900);
            if (!creation){
                birthDate.setDate(randDay);
                birthDate.setMonth(randMonth);
                birthDate.setYear(randYear);
            }

            // TEST generation
            if(req.getParameter("testPatient")!=null && creation) {
                testDoB=sdf.format(birthDate);
                testNameFirst=faker.name().firstName();
                testNameLast=faker.name().lastName();
                testMiddleName=faker.name().firstName();
                testMotherMaidenName=faker.name().lastName();
                if(randMonth%2==0) {
                    testSex="F";
                }else {
                    testSex="M";
                }

                long aDay = TimeUnit.DAYS.toMillis(1);
                long now = new Date().getTime();
                Date twoYearsAgo = new Date(now - aDay * 365 * 2);

                Date eightyYearsAgo = new Date(now - aDay * 365 * 80);
                Date fourtyYearsAgo = new Date(now - aDay * 365 * 40);
                Date tenDaysAgo = new Date(now - aDay * 10);
                Date fourYearsAgo = new Date(now - aDay*365*4);

                testDoB = sdf.format(between(eightyYearsAgo, fourtyYearsAgo));

                Random rand = new Random();


                int randomDecision = rand.nextInt(100);
                if(randomDecision<30) {
                    testDeathDate = sdf.format(between(fourYearsAgo,tenDaysAgo ));
                }
                else {
                    testDeathDate = "";
                }


                Date DeathDate = between(fourYearsAgo,tenDaysAgo );
                Date PubIndicDate = between(twoYearsAgo, tenDaysAgo);
                Date ProtecIndicDate = between(twoYearsAgo, tenDaysAgo);
                Date RegIndicDate = between(twoYearsAgo, tenDaysAgo);
                Date RegStatusDate = between(twoYearsAgo, tenDaysAgo);


                testDeathDate = sdf.format(DeathDate);
                testPubIndicDate = sdf.format(PubIndicDate);
                testProtecIndicDate = sdf.format(ProtecIndicDate);
                testRegIndicDate = sdf.format(RegIndicDate);
                testRegStatusDate = sdf.format(RegStatusDate);

                testRace="Asian";
                testAdress=faker.address().buildingNumber()+faker.address().streetName();
                testCity=faker.address().city();
                testCountryCode=faker.address().countryCode();
                testState=faker.address().state();
                testCountyParish="county";
                testPhone=faker.phoneNumber().subscriberNumber(10);
                testEmail=testNameFirst+ randDay +"@gmail.com";
                testEthnicity="Indian";
                testBirthFlag="";
                testBirthOrder="";
                testDeathFlag="";

                testPubIndic="O";
                testProtecIndic="O";
                testRegStatus="O";

                testGuardNameFirst=faker.name().firstName();
                testGuardNameLast=testNameLast;
                testGuardMiddleName=faker.name().firstName();
                testGuardRelationship="BRO";
            }
            out.println("<form method=\"post\" class=\"w3-container\" action=\"patient_form\">\r\n"

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

                    + "<label class=\"w3-text-green\"><b>Date of birth</b></label> "
                    + "<label class=\"w3-text-red w3-margin-right\"><b>*</b></label> "
                    + "                         <input type=\"date\" class = \"w3-input w3-margin w3-border \"  value=\""+testDoB+"\" style=\"width:75% \" name=\"DoB\" />\r\n"

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
                    + "                         <input type=\"date\"  class = \"w3-input w3-margin w3-border\"  value=\""+testDeathDate+"\" style=\"width:75% \" name=\"DoD\"/>\r\n"

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
                    + "                         <input type=\"date\"  class = \"w3-input w3-margin w3-border\"  value=\""+testPubIndicDate+"\" style=\"width:75% \" name=\"publicity_date\"/>\r\n"

                    +"</div>"

                    + "<div style =\"align-items:center\" "

                    + "   <label class=\"w3-text-green\"><b>protection indicator</b></label>"
                    + "                         <input type=\"text\"  class = \"w3-input w3-margin w3-border\"  value=\""+testProtecIndic+"\" style=\"width:75% \"name=\"protection\" />\r\n"
                    +"</div>"
                    +"</div>"
                    +"<div style=\"width:100% \">"
                    + "<div style =\"align-items:center\" "

                    + "    <label class=\"w3-text-green\"><b>protection indicator date</b></label>"
                    + "                         <input type=\"date\"  class = \"w3-input w3-margin w3-border\"  value=\""+testProtecIndicDate+"\" style=\"width:75% \"name=\"protection_date\" />\r\n"

                    +"</div>"

                    + "<div style =\"align-items:center\" "

                    + "    <label class=\"w3-text-green\"><b>Registry indicator date  </b></label>"
                    + "                         <input type=\"date\"  class = \"w3-input w3-margin w3-border\"  value=\""+testRegIndicDate+"\" style=\"width:75% \" name=\"registry_indicator_date\"/>\r\n"

                    +"</div>"

                    + "<div style =\"align-items:center\" "

                    + "    <label class=\"w3-text-green\"><b>registry status indicator</b></label>"
                    + "                         <input type=\"text\"  class = \"w3-input w3-margin w3-border\"  value=\""+testRegStatus+"\" style=\"width:75% \" name=\"registry_status_indicator\"/>\r\n"

                    +"</div>"
                    +"</div>"
                    +"<div style=\"width:100% \">"
                    + "<div style =\"align-items:center\" "

                    + "   <label class=\"w3-text-green\"><b>registry status indicator date</b></label>"
                    + "                         <input type=\"date\"  class = \"w3-input w3-margin w3-border\"  value=\""+testRegStatusDate+"\" style=\"width:75% \" name=\"registry_status_indicator_date\"/>\r\n"

                    +"</div>"
                    +"</div>"
                    +"</div>"

                    + "<div class = \"w3-margin w3-border w3-border-green\" style=\"width:100% ; display:flex \">"
                    + "<div style =\"width: 50% ;align-items:center\" "

                    + "    <label class=\"w3-text-green\"><b>Guardian last name</b></label>"
                    + "                         <input type=\"text\"  class = \"w3-input w3-margin w3-border\"  value=\""+testGuardNameLast+"\" style=\"width:75% \"name=\"guardian_last_name\" />\r\n"

                    +"</div>"

                    + "<div style =\"width: 50% ;align-items:center\" "

                    + "    <label class=\"w3-text-green\"><b>Guardian first name</b></label>"
                    + "                         <input type=\"text\"  class = \"w3-input w3-margin w3-border\"  value=\""+testGuardNameFirst+"\" style=\"width:75% \"name=\"guardian_first_name\" />\r\n"


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
                    +" <input type=\"hidden\" id=\"paramPatientId\" name=\"paramPatientId\" value="+req.getParameter("paramPatientId")+">"


                    + "                <button class=\"w3-button w3-round-large w3-green w3-hover-teal w3-margin \"  >Validate</button>\r\n"
                    + "                </form> " + "</div\r\n");


            ServletHelper.doStandardFooter(out, session);

        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        out.flush();
        out.close();
    }

    public static Date between(Date startInclusive, Date endExclusive) {
        long startMillis = startInclusive.getTime();
        long endMillis = endExclusive.getTime();
        long randomMillisSinceEpoch = ThreadLocalRandom
                .current()
                .nextLong(startMillis, endMillis);

        return new Date(randomMillisSinceEpoch);
    }
}
