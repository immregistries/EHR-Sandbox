package org.immregistries.ehr.servlet;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.immregistries.codebase.client.CodeMap;
import org.immregistries.codebase.client.generated.Code;
import org.immregistries.codebase.client.reference.CodesetType;
import org.immregistries.ehr.model.Facility;
import org.immregistries.ehr.model.Patient;
import org.immregistries.ehr.model.Tenant;
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

public class PatientForm extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        Boolean creation = false;
        HttpSession session = req.getSession(true);
        Session dataSession = PopServlet.getDataSession();
        Transaction transaction = dataSession.beginTransaction();

        Tenant tenant = (Tenant) session.getAttribute("tenant");
        Facility facility = (Facility) session.getAttribute("facility");

        SimpleDateFormat sdf  = new SimpleDateFormat("yyyy-MM-dd");

        Patient patient;
        if (req.getParameter("paramPatientId")!=null && !req.getParameter("paramPatientId").equals("null")) { // Modifying existing patient
            int paramPatientId =  Integer.parseInt(req.getParameter("paramPatientId"));
            patient = (Patient) dataSession.load(Patient.class,paramPatientId);
        } else{ // creating new patient
            patient = new Patient();
            patient.setTenant(tenant);
            patient.setFacility(facility);
            creation = true;
        }

        patient.setNameFirst(req.getParameter("nameFirst"));
        patient.setNameLast(req.getParameter("nameLast"));
        patient.setNameMiddle(req.getParameter("nameMiddle"));
        patient.setAddressCity(req.getParameter("city"));
        patient.setAddressCountry(req.getParameter("country"));
        patient.setAddressCountyParish(req.getParameter("countyParish"));
        patient.setDeathFlag(req.getParameter("deathFlag"));
        patient.setAddressState(req.getParameter("state"));
        patient.setAddressLine1(req.getParameter("addressLine1"));
        patient.setAddressLine2(req.getParameter("addressLine2"));


        patient.setBirthFlag(req.getParameter("birthFlag"));
        patient.setBirthOrder(req.getParameter("birthOrder"));

        patient.setDeathFlag(req.getParameter("deathFlag"));
        patient.setEmail(req.getParameter("email"));
        patient.setEthnicity(req.getParameter("ethnicity"));
        patient.setGuardianFirst(req.getParameter("guardianFirst"));
        patient.setGuardianLast(req.getParameter("guardianLast"));
        patient.setGuardianMiddle(req.getParameter("guardianMiddle"));
        patient.setGuardianRelationship(req.getParameter("guardianRelationship"));
        patient.setMotherMaiden(req.getParameter("motherMaiden"));
        patient.setPhone(req.getParameter("phone"));
        patient.setProtectionIndicator(req.getParameter("protectionIndicator"));

        patient.setPublicityIndicator(req.getParameter("publicityIndicator"));

        patient.setRace(req.getParameter("race"));
        patient.setRegistryStatusIndicator(req.getParameter("registryStatusIndicator"));

        try {
            patient.setBirthDate(sdf.parse(req.getParameter("birthDate")));
            if (!req.getParameter("deathDate").equals("")){
                patient.setDeathDate(sdf.parse(req.getParameter("deathDate")));
            }
            if (!req.getParameter("protectionIndicatorDate").equals("")){
                patient.setProtectionIndicatorDate(sdf.parse(req.getParameter("protectionIndicatorDate")));
            }
            if (!req.getParameter("publicityIndicatorDate").equals("")){
                patient.setPublicityIndicatorDate(sdf.parse(req.getParameter("publicityIndicatorDate")));
            }
            if (!req.getParameter("registryStatusIndicatorDate").equals("")){
                patient.setRegistryStatusIndicatorDate(sdf.parse(req.getParameter("registryStatusIndicatorDate")));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        patient.setSex(req.getParameter("sex"));
        Date updatedDate = new Date();
        patient.setUpdatedDate(updatedDate);

        if (!creation) { // Modifying existing patient
            dataSession.update(patient);
            transaction.commit();
            session.setAttribute("patient", patient);
            resp.sendRedirect("patientRecord?paramPatientId=" + patient.getPatientId());
        }else {
            patient.setCreatedDate(updatedDate);
            dataSession.save(patient);
            transaction.commit();
            resp.sendRedirect("facility_patient_display");

        }

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        Boolean creation = false;
        HttpSession session = req.getSession(true);
        resp.setContentType("text/html");
        PrintWriter out = new PrintWriter(resp.getOutputStream());
        Session dataSession = PopServlet.getDataSession();

        try {
            ServletHelper.doStandardHeader(out, req, "Patient form");

            Facility facility = (Facility) session.getAttribute("facility");
            Tenant tenant = (Tenant) session.getAttribute("tenant");
            Patient patient = new Patient();
            if(req.getParameter("paramPatientId")!=null && tenant !=null) {
                Query query = dataSession.createQuery("from Patient where patient_id=? and tenant_id=?");
                query.setParameter(0, Integer.parseInt(req.getParameter("paramPatientId")));
                query.setParameter(1, tenant.getTenantId());
                patient = (Patient) query.uniqueResult();
                session.setAttribute("patient", patient);
                facility = patient.getFacility();
                session.setAttribute("facility", facility);
            } else if (req.getParameter("paramPatientId") != null){
                patient = (Patient) session.getAttribute("patient");
            } else if (facility == null) {
                resp.sendRedirect("facility_patient_display?chooseFacility=1");
            } else{
                creation = true;
            }

            resp.setContentType("text/html");

            out.println("<div class=\"w3-margin-bottom\"style=\"width:100% height:auto \" >");

            if (creation){
                out.println("<button onclick=\"location.href='patient_form?testPatient=1'\" class=\"w3-button w3-round-large w3-green w3-hover-teal w3-margin \"  >Fill with test informations</button><br/>");
            }


            if(req.getParameter("testPatient")!=null && creation) {
                // TEST generation
                patient = Patient.random(tenant, facility);
            }
            printPatientForm(req, out, patient);
            ServletHelper.doStandardFooter(out, session);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        out.flush();
        out.close();
    }

    private void printPatientForm(HttpServletRequest req, PrintWriter out, Patient patient) {
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
        ServletHelper.printOpenContainer(out, 75, "row");
        ServletHelper.printSimpleInput(out, patient.getNameFirst(), "nameFirst", "First name", false, 20);
        ServletHelper.printSimpleInput(out, patient.getNameMiddle(), "nameMiddle", "Middle name", false, 20);
        ServletHelper.printSimpleInput(out, patient.getNameLast(), "nameLast", "Last name", false, 20);

        ServletHelper.printSimpleInput(out, patient.getMotherMaiden(), "motherMaiden", "Mother maiden name", false, 20);
        ServletHelper.printCloseContainer(out);

        ServletHelper.printOpenContainer(out, 50, "row");
        ServletHelper.printDateInput(out, patient.getBirthDate(), "birthDate", "Birth date", true);
        ServletHelper.printSimpleInput(out, patient.getBirthOrder(), "birthOrder", "Birth order", false, 2);
        ServletHelper.printSelectYesNo(out, patient.getBirthFlag(),"birthFlag", "Birth Flag");
        ServletHelper.printSimpleInput(out, patient.getSex(), "sex", "Sex (F/M)", false, 1);
        ServletHelper.printCloseContainer(out);

        ServletHelper.printOpenContainer(out, 40, "row");
        ServletHelper.printSimpleInput(out, patient.getPhone(), "phone", "Phone number", false, 16);
        ServletHelper.printSimpleInput(out, patient.getEmail(), "email", "E-mail", false, 35);
        ServletHelper.printCloseContainer(out);



        ServletHelper.printOpenContainer(out, 40, "row");
        ServletHelper.printSimpleInput(out, patient.getAddressLine1(), "addressLine1", "Address line 1", false, 25);
        ServletHelper.printSimpleInput(out, patient.getAddressLine2(), "addressLine2", "Address line 2", false, 25);
        ServletHelper.printCloseContainer(out);

        ServletHelper.printOpenContainer(out, 55, "row");
        ServletHelper.printSimpleInput(out, patient.getAddressCity(), "city", "City", false, 20);
        ServletHelper.printSimpleInput(out, patient.getAddressCountyParish(), "countyParish", "County", false, 20);
        ServletHelper.printSimpleInput(out, patient.getAddressCountry(), "country", "Country Code", false, 20);
        ServletHelper.printCloseContainer(out);

        ServletHelper.printOpenContainer(out, 15, "column");
        ServletHelper.printSimpleInput(out, patient.getEthnicity(), "ethnicity", "Ethnicity", false, 15);
        ServletHelper.printSimpleInput(out, patient.getRace(), "race", "Race", false, 15);
        ServletHelper.printCloseContainer(out);

        ServletHelper.printOpenContainer(out, 15, "column");
        ServletHelper.printSelectYesNo(out, patient.getDeathFlag(),"deathFlag", "Death Flag");
        ServletHelper.printDateInput(out, patient.getDeathDate(), "deathDate", "Death date", false);
        ServletHelper.printCloseContainer(out);

        ServletHelper.printOpenContainer(out, 55, "row");
        out.println("<div style=\"width:100% \">");
        ServletHelper.printSimpleInput(out, patient.getPublicityIndicator(), "publicityIndicator", "Publicity indicator", false, 5);
        ServletHelper.printDateInput(out, patient.getPublicityIndicatorDate(), "publicityIndicatorDate", "Publicity indicator date", false);
        out.println("</div>");
        out.println("<div style=\"width:100% \">");
        ServletHelper.printSimpleInput(out, patient.getProtectionIndicator(), "protectionIndicator", "Protection indicator", false, 5);
        ServletHelper.printDateInput(out, patient.getProtectionIndicatorDate(), "protectionIndicatorDate", "Protection indicator date", false);
        out.println("</div>");
        out.println("<div style=\"width:100% \">");
        ServletHelper.printSimpleInput(out, patient.getRegistryStatusIndicator(), "registryStatusIndicator", "Registry status indicator", false, 5);
        ServletHelper.printDateInput(out, patient.getRegistryStatusIndicatorDate(), "registryStatusIndicatorDate", "Registry status date", false);
        out.println("</div>");
        ServletHelper.printCloseContainer(out);

        ServletHelper.printOpenContainer(out, 100, "row");
        ServletHelper.printSimpleInput(out, patient.getGuardianFirst(), "guardianFirst", "Guardian first name", false, 30);
        ServletHelper.printSimpleInput(out, patient.getGuardianMiddle(), "guardianMiddle", "Guardian middle name", false, 30);
        ServletHelper.printSimpleInput(out, patient.getGuardianLast(), "guardianLast", "Guardian last name", false, 30);
        ServletHelper.printSelectForm(out, patient.getGuardianRelationship(), codeListRelationship, "guardianRelationship", "Guardian relationship to patient", 150);
        ServletHelper.printCloseContainer(out);

        out.println("<input type=\"hidden\" id=\"paramPatientId\" name=\"paramPatientId\" value=" + req.getParameter("paramPatientId")+ ">"
                + "<button class=\"w3-button w3-round-large w3-green w3-hover-teal w3-margin \"  >Validate</button>"
                + "</form> " + "</div");
    }



}
