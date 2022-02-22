package org.immregistries.ehr.servlet;

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
        patient.setAddressLine1(req.getParameter("address_line_1"));
        patient.setAddressLine2(req.getParameter("address_line_2"));


        patient.setBirthFlag(req.getParameter("birth_flag"));
        patient.setBirthOrder(req.getParameter("birth_order"));

        patient.setDeathFlag(req.getParameter("death_flag"));
        patient.setEmail(req.getParameter("email"));
        patient.setEthnicity(req.getParameter("ethnicity"));
        patient.setGuardianFirst(req.getParameter("guardian_first_name"));
        patient.setGuardianLast(req.getParameter("guardian_last_name"));
        patient.setGuardianMiddle(req.getParameter("guardian_middle_name"));
        patient.setGuardianRelationship(req.getParameter("guardian_relationship"));
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
            ServletHelper.doStandardHeader(out, session, req);

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
        Collection<Code> codeListRelationship =codeMap.getCodesForTable(CodesetType.PERSON_RELATIONSHIP);

        out.println("<form method=\"post\" action=\"patient_form\"" +
                "class=\"\"" +
                "style=\"" +
                " display:flex ;" +
                " flex-flow: wrap ;" +
                " justify-content : space-around ;" +
                " gap: 20px 20px ;" +
                "\">");
        EntryRecord.printOpenContainer(out, 75, "row");
        EntryRecord.printSimpleInput(out, patient.getNameFirst(), "first_name", "First name", false, 20);
        EntryRecord.printSimpleInput(out, patient.getNameMiddle(), "middle_name", "Middle name", false, 20);
        EntryRecord.printSimpleInput(out, patient.getNameLast(), "last_name", "Last name", false, 20);

        EntryRecord.printSimpleInput(out, patient.getMotherMaiden(), "mother_maiden_name", "Mother maiden name", false, 20);
        EntryRecord.printCloseContainer(out);

        EntryRecord.printOpenContainer(out, 50, "row");
        EntryRecord.printDateInput(out, patient.getBirthDate(), "birth_date", "Birth date", true);
        EntryRecord.printSimpleInput(out, patient.getBirthOrder(), "birth_order", "Birth order", false, 2);
        EntryRecord.printSelectYesNo(out, patient.getBirthFlag(),"birth_flag", "Birth Flag");
        EntryRecord.printSimpleInput(out, patient.getSex(), "sex", "Sex (F/M)", false, 1);
        EntryRecord.printCloseContainer(out);

        EntryRecord.printOpenContainer(out, 40, "row");
        EntryRecord.printSimpleInput(out, patient.getPhone(), "phone", "Phone number", false, 16);
        EntryRecord.printSimpleInput(out, patient.getEmail(), "email", "E-mail", false, 35);
        EntryRecord.printCloseContainer(out);



        EntryRecord.printOpenContainer(out, 40, "row");
        EntryRecord.printSimpleInput(out, patient.getAddressLine1(), "address_line_1", "Address line 1", false, 25);
        EntryRecord.printSimpleInput(out, patient.getAddressLine2(), "address_line_2", "Address line 2", false, 25);
        EntryRecord.printCloseContainer(out);

        EntryRecord.printOpenContainer(out, 55, "row");
        EntryRecord.printSimpleInput(out, patient.getAddressCity(), "city", "City", false, 20);
        EntryRecord.printSimpleInput(out, patient.getAddressCountyParish(), "county", "County", false, 20);
        EntryRecord.printSimpleInput(out, patient.getAddressCountry(), "country", "Country Code", false, 20);
        EntryRecord.printCloseContainer(out);

        EntryRecord.printOpenContainer(out, 15, "column");
        EntryRecord.printSimpleInput(out, patient.getEthnicity(), "ethnicity", "Ethnicity", false, 15);
        EntryRecord.printSimpleInput(out, patient.getRace(), "race", "Race", false, 15);
        EntryRecord.printCloseContainer(out);

        EntryRecord.printOpenContainer(out, 15, "column");
        EntryRecord.printSelectYesNo(out, patient.getDeathFlag(),"death_flag", "Death Flag");
        EntryRecord.printDateInput(out, patient.getDeathDate(), "death_date", "Death date", false);
        EntryRecord.printCloseContainer(out);

        EntryRecord.printOpenContainer(out, 55, "row");
        out.println("<div style=\"width:100% \">");
        EntryRecord.printSimpleInput(out, patient.getPublicityIndicator(), "publicity_indicator", "Publicity indicator", false, 5);
        EntryRecord.printDateInput(out, patient.getPublicityIndicatorDate(), "publicity_date", "Publicity indicator date", false);
        out.println("</div>");
        out.println("<div style=\"width:100% \">");
        EntryRecord.printSimpleInput(out, patient.getProtectionIndicator(), "protection_indicator", "Protection indicator", false, 5);
        EntryRecord.printDateInput(out, patient.getProtectionIndicatorDate(), "protection_date", "Protection indicator date", false);
        out.println("</div>");
        out.println("<div style=\"width:100% \">");
        EntryRecord.printSimpleInput(out, patient.getRegistryStatusIndicator(), "registry_status_indicator", "Registry status indicator", false, 5);
        EntryRecord.printDateInput(out, patient.getRegistryStatusIndicatorDate(), "registry_status_indicator_date", "Registry status date", false);
        out.println("</div>");
        EntryRecord.printCloseContainer(out);

        EntryRecord.printOpenContainer(out, 100, "row");
        EntryRecord.printSimpleInput(out, patient.getGuardianFirst(), "guardian_first_name", "Guardian first name", false, 30);
        EntryRecord.printSimpleInput(out, patient.getGuardianMiddle(), "guardian_middle_name", "Guardian middle name", false, 30);
        EntryRecord.printSimpleInput(out, patient.getGuardianLast(), "guardian_last_name", "Guardian last name", false, 30);
        EntryRecord.printSelectForm(out, patient.getGuardianRelationship(), codeListRelationship, "guardian_relationship", "Guardian relationship to patient", 150);
        EntryRecord.printCloseContainer(out);

        out.println("<input type=\"hidden\" id=\"paramPatientId\" name=\"paramPatientId\" value=" + req.getParameter("paramPatientId")+ ">"
                + "<button class=\"w3-button w3-round-large w3-green w3-hover-teal w3-margin \"  >Validate</button>"
                + "</form> " + "</div");
    }



}
