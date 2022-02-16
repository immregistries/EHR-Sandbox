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
            patient.setBirthDate(sdf.parse(req.getParameter("birth_date")));
            if (!req.getParameter("death_date").equals("")){
                patient.setDeathDate(sdf.parse(req.getParameter("death_date")));
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
        Boolean preloaded = false;
        HttpSession session = req.getSession(true);
        resp.setContentType("text/html");
        PrintWriter out = new PrintWriter(resp.getOutputStream());
        Session dataSession = PopServlet.getDataSession();

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
                preloaded = true;
                session.setAttribute("patient", patient);
                facility = patient.getFacility();
                session.setAttribute("facility", facility);
            } else if (req.getParameter("paramPatientId") != null){
                patient = (Patient) session.getAttribute("patient");
                preloaded = true;
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


            if(req.getParameter("testPatient")!=null && creation) {
                // TEST generation
                patient = Patient.random(silo, facility);
                preloaded = true;
            }
            printPatientForm(req, out, patient, preloaded);
            ServletHelper.doStandardFooter(out, session);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        out.flush();
        out.close();
    }

    private void printPatientForm(HttpServletRequest req, PrintWriter out, Patient patient, Boolean preloaded) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(true);
        CodeMap codeMap = CodeMapManager.getCodeMap();
        Collection<Code> codeListRelation =codeMap.getCodesForTable(CodesetType.PERSON_RELATIONSHIP);

        String deathDate="";
        String birthDate="";
        String publicityIndicatorDate="";
        String protectionIndicatorDate="";
        String registryStatusIndicatorDate="";
        if (preloaded){
            birthDate = sdf.format(patient.getBirthDate());
            if(patient.getDeathDate()!=null) {
                deathDate=""+ sdf.format(patient.getDeathDate());
            }
            if(patient.getPublicityIndicatorDate()!=null) {
                publicityIndicatorDate=""+ sdf.format(patient.getPublicityIndicatorDate());
            }
            if(patient.getProtectionIndicatorDate()!=null) {
                protectionIndicatorDate=""+ sdf.format(patient.getProtectionIndicatorDate());
            }
            if(patient.getRegistryStatusIndicatorDate()!=null) {
                registryStatusIndicatorDate=""+ sdf.format(patient.getRegistryStatusIndicatorDate());
            }
        }

        out.println("<form method=\"post\" class=\"w3-container\" action=\"patient_form\">\r\n"

                + "<div class = \"w3-margin w3-border w3-border-green\" style=\"width:100% ; display:flex \">"
                + "<div style =\"width: 50% ;align-items:center\" "

                + "    <label class=\"w3-text-green\"><b>First Name</b></label>"
                + "                         <input type=\"text\"  class = \"w3-input w3-margin w3-border\"  value=\""+ patient.getNameFirst()+"\" style=\"width:75% \" name=\"first_name\" />\r\n"


                +"</div>"
                + "<div style =\"width: 50% ;align-items:center\" "

                + " <label class=\"w3-text-green\"><b>Last name</b></label>"
                + "                         <input type=\"text\"  class = \"w3-input w3-margin w3-border\"  value=\""+ patient.getNameLast()+"\" style=\"width:75% \" name=\"last_name\" />\r\n"

                +"</div>"
                + "<div style =\"width: 50% ;align-items:center\" "

                + "    <label class=\"w3-text-green\"><b>Middle name</b></label>"
                + "                         <input type=\"text\"  class = \"w3-input w3-margin w3-border\"  value=\""+ patient.getNameMiddle()+"\" style=\"width:75% \" name=\"middle_name\" />\r\n"

                +"</div>"

                + "<div style =\"width: 50% ;align-items:center\" "

                + "    <label class=\"w3-text-green\"><b>Mother maiden name</b></label>"
                + "                         <input type=\"text\"  class = \"w3-input w3-margin w3-border\"  value=\""+ patient.getMotherMaiden()+"\" style=\"width:75% \" name=\"mother_maiden_name\" />\r\n"

                +"</div>"
                +"</div>"

                + "<div class = \"w3-margin w3-border w3-border-green\" style=\"width:100% ; display:flex \">"
                + "<div style =\"width: 50% ;align-items:center\" "

                + "<label class=\"w3-text-green\"><b>Date of birth</b></label> "
                + "<label class=\"w3-text-red w3-margin-right\"><b>*</b></label> "
                + "                         <input type=\"date\" class = \"w3-input w3-margin w3-border \"  value=\""+birthDate+"\" style=\"width:75% \" name=\"birth_date\" />\r\n"

                +"</div>"

                + "<div style =\"width: 50% ;align-items:center\" "

                + "    <label class=\"w3-text-green\"><b>Sex (F or M) </b></label>"
                + "                         <input type=\"text\"  class = \"w3-input w3-margin w3-border\"  value=\""+ patient.getSex()+"\" style=\"width:75% \" name=\"sex\"/>\r\n"

                +"</div>"
                +"</div>"

                + "<div class = \"w3-margin w3-border w3-border-green\" style=\"width:100% ; display:flex \">"
                + "<div style =\"align-items:center\" "

                + "    <label class=\"w3-text-green\"><b>Address 1</b></label>"
                + "                         <input type=\"text\"  class = \"w3-input w3-margin w3-border\"  value=\""+ patient.getAddressLine1()+"\" style=\"width:75% \" name=\"address\"/>\r\n"

                +"</div>"
                + "<div style =\"width: 30% ;align-items:center\" "

                + " <label class=\"w3-text-green\"><b>City</b></label>"
                + "                         <input type=\"text\"  class = \"w3-input w3-margin w3-border\"  value=\""+ patient.getAddressCity()+"\" style=\"width:75% \" name=\"city\"/>\r\n"

                +"</div>"
                + "<div style =\"width: 30% ;align-items:center\" "

                + " <label class=\"w3-text-green\"><b>State</b></label>"
                + "                         <input type=\"text\"  class = \"w3-input w3-margin w3-border\"  value=\""+ patient.getAddressState()+"\" style=\"width:75% \" name=\"state\" />\r\n"


                +"</div>"
                + "<div style =\"width: 50% ;align-items:center\" "

                + " <label class=\"w3-text-green\"><b>County/parish</b></label>"
                + "                         <input type=\"text\"  class = \"w3-input w3-margin w3-border\"  value=\""+ patient.getAddressCountyParish()+"\" style=\"width:75% \" name=\"county\"/>\r\n"

                +"</div>"
                + "<div style =\"width: 50% ;align-items:center\" "

                + " <label class=\"w3-text-green\"><b>Country Code</b></label>"
                + "                         <input type=\"text\" class = \"w3-input w3-margin w3-border\"  value=\""+ patient.getAddressCountry()+"\" style=\"width:75% \"   name=\"country\"/>\r\n"

                +"</div>"
                +"</div>"

                + "<div class = \"w3-margin w3-border w3-border-green\" style=\"width:100% ; display:flex \">"
                + "<div style =\"width: 50% ;align-items:center\" "

                + "   <label class=\"w3-text-green\"><b>phone</b></label>"
                + "                         <input type=\"text\"  class = \"w3-input w3-margin w3-border\"  value=\""+ patient.getPhone()+"\" style=\"width:75% \"name=\"phone\" />\r\n"


                +"</div>"
                + "<div style =\"width: 50% ;align-items:center\" "

                + " <label class=\"w3-text-green\"><b>E-mail</b></label>"
                + "                         <input type=\"text\"  class = \"w3-input w3-margin w3-border\"  value=\""+ patient.getEmail()+"\" style=\"width:75% \" name=\"email\"/>\r\n"

                +"</div>"
                +"</div>"
                + "<div class = \"w3-margin w3-border w3-border-green\" style=\"width:100% ; display:flex \">"
                + "<div style =\"width: 50% ;align-items:center\" "

                + "    <label class=\"w3-text-green\"><b>Ethnicity</b></label>"
                + "                         <input type=\"text\"  class = \"w3-input w3-margin w3-border\"  value=\""+ patient.getEthnicity()+"\" style=\"width:75% \" name=\"ethnicity\" />\r\n"


                +"</div>"

                + "<div style =\"width: 50% ;align-items:center\" "

                + " <label class=\"w3-text-green\"><b>Race</b></label>"
                + "                         <input type=\"text\"  class = \"w3-input w3-margin w3-border\"  value=\""+ patient.getRace()+"\" style=\"width:75% \" name=\"race\"/>\r\n"

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
                + "                         <input type=\"text\"  class = \"w3-input w3-margin w3-border\"  value=\""+ patient.getBirthOrder()+"\" style=\"width:75% \" name=\"birth_order\"/>\r\n"

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
                + "                         <input type=\"date\"  class = \"w3-input w3-margin w3-border\"  value=\""+deathDate+"\" style=\"width:75% \" name=\"death_date\"/>\r\n"

                +"</div>"
                +"</div>"

                + "<div class = \"w3-margin w3-border w3-border-green\" style=\"width:100% ; display:flex \">"
                +"<div style=\"width:100% \">"
                + "<div style =\" align-items:center\" "

                + " <label class=\"w3-text-green\"><b>publicity indicator</b></label>"
                + "                         <input type=\"text\"  class = \"w3-input w3-margin w3-border\"  value=\""+ patient.getPublicityIndicator()+"\" style=\"width:75% \"name=\"publicity_indicator\" />\r\n"

                +"</div>"

                + "<div style =\"align-items:center\" "

                + "    <label class=\"w3-text-green\"><b>publicity indicator date</b></label>"
                + "                         <input type=\"date\"  class = \"w3-input w3-margin w3-border\"  value=\""+publicityIndicatorDate+"\" style=\"width:75% \" name=\"publicity_date\"/>\r\n"

                +"</div>"

                + "<div style =\"align-items:center\" "

                + "   <label class=\"w3-text-green\"><b>protection indicator</b></label>"
                + "                         <input type=\"text\"  class = \"w3-input w3-margin w3-border\"  value=\""+ patient.getProtectionIndicator()+"\" style=\"width:75% \"name=\"protection\" />\r\n"
                +"</div>"
                +"</div>"
                +"<div style=\"width:100% \">"
                + "<div style =\"align-items:center\" "

                + "    <label class=\"w3-text-green\"><b>protection indicator date</b></label>"
                + "                         <input type=\"date\"  class = \"w3-input w3-margin w3-border\"  value=\""+ protectionIndicatorDate +"\" style=\"width:75% \"name=\"protection_date\" />\r\n"

                +"</div>"

                + "<div style =\"align-items:center\" "

                + "    <label class=\"w3-text-green\"><b>Registry status indicator</b></label>"
                + "                         <input type=\"text\"  class = \"w3-input w3-margin w3-border\"  value=\""+ patient.getRegistryStatusIndicator()+"\" style=\"width:75% \" name=\"registry_status_indicator\"/>\r\n"

                +"</div>"
                +"</div>"
                +"<div style=\"width:100% \">"
                + "<div style =\"align-items:center\" "

                + "   <label class=\"w3-text-green\"><b>Registry status indicator date</b></label>"
                + "                         <input type=\"date\"  class = \"w3-input w3-margin w3-border\"  value=\""+registryStatusIndicatorDate+"\" style=\"width:75% \" name=\"registry_status_indicator_date\"/>\r\n"

                +"</div>"
                +"</div>"
                +"</div>"

                + "<div class = \"w3-margin w3-border w3-border-green\" style=\"width:100% ; display:flex \">"
                + "<div style =\"width: 50% ;align-items:center\" "

                + "    <label class=\"w3-text-green\"><b>Guardian last name</b></label>"
                + "                         <input type=\"text\"  class = \"w3-input w3-margin w3-border\"  value=\""+ patient.getGuardianLast()+"\" style=\"width:75% \"name=\"guardian_last_name\" />\r\n"

                +"</div>"

                + "<div style =\"width: 50% ;align-items:center\" "

                + "    <label class=\"w3-text-green\"><b>Guardian first name</b></label>"
                + "                         <input type=\"text\"  class = \"w3-input w3-margin w3-border\"  value=\""+ patient.getGuardianFirst()+"\" style=\"width:75% \"name=\"guardian_first_name\" />\r\n"


                +"</div>"

                + "<div style =\"width: 50% ;align-items:center\" "

                + " <label class=\"w3-text-green\"><b>Guardian middle name</b></label>"
                + "                         <input type=\"text\"  class = \"w3-input w3-margin w3-border\"  value=\""+ patient.getGuardianMiddle()+"\" style=\"width:75% \"name=\"guardian_middle_name\" />\r\n"

                +"</div>"

                + "<div style =\"width: 50% ;align-items:center\" "

                + " <label class=\"w3-text-green\"><b>Guardian relationship to patient</b></label>"
                + "                         <input type=\"text\"  class = \"w3-input w3-margin w3-border\"  value=\""+ patient.getGuardianRelationship()+"\" style=\"width:75% \"name=\"guardian_relation\" />\r\n"
                +"  <p class=\"w3-margin\" style=\"width:30% height:5%\">"
                +"                          <SELECT style=\"width : 100%\" name=\"guardian_relation\" size=\"1\">\r\n");
        for(Code code : codeListRelation) {
            out.println("                             <OPTION value=\""+code.getValue()+"\">"+code.getLabel()+"</Option>\r\n");
        }
        out.println( "</SELECT>\r\n"
                + "</p>"
                + "</div>"
                + "</div>"
                + "<input type=\"hidden\" id=\"paramPatientId\" name=\"paramPatientId\" value="+ req.getParameter("paramPatientId")+">"
                + "<button class=\"w3-button w3-round-large w3-green w3-hover-teal w3-margin \"  >Validate</button>\r\n"
                + "</form> " + "</div\r\n");
    }

}
