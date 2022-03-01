package org.immregistries.ehr.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.hibernate.Query;
import org.hibernate.Session;
import org.immregistries.ehr.model.*;
import org.immregistries.ehr.model.Tenant;

/**
 * Servlet implementation class patient_record
 */
public class PatientRecord extends HttpServlet {
  private static final long serialVersionUID = 1L;
  public static final String PARAM_SHOW = "show";

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
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
      
        ServletHelper.doStandardHeader(out, req, "Patient details");
        String show = req.getParameter(PARAM_SHOW);
        Patient patient = (Patient) session.getAttribute("patient");
        Tenant tenant = (Tenant) session.getAttribute("tenant");
        if (session.getAttribute("tenant")==null)  {
            resp.sendRedirect("tenants?chooseTenant=1");
        }

        String patientId = req.getParameter("paramPatientId");
        if (patientId != null) {
            if (patient != null) {
                if (Integer.parseInt(patientId) != patient.getPatientId()){
                    Query query = dataSession.createQuery("from Patient where patient_id=? and tenant_id=?");
                    query.setParameter(0, Integer.parseInt(patientId));
                    query.setParameter(1, tenant.getTenantId());
                    patient = (Patient) query.uniqueResult();
                    session.setAttribute("patient", patient);
                    session.setAttribute("facility", patient.getFacility());
                    resp.sendRedirect("patient_record?paramPatientId=" + patientId);
                }
            } else {
                Query query = dataSession.createQuery("from Patient where patient_id=? and tenant_id=?");
                query.setParameter(0, Integer.parseInt(patientId));
                query.setParameter(1, tenant.getTenantId());
                patient = (Patient) query.uniqueResult();
                session.setAttribute("patient", patient);
                session.setAttribute("facility", patient.getFacility());
                resp.sendRedirect("patient_record?paramPatientId=" + patientId);
            }
        }

        Query query = dataSession.createQuery("from VaccinationEvent where patient=?");
        query.setParameter(0, patient);
        List<VaccinationEvent> entryList = query.list();

        resp.setContentType("text/html");
        out.println( "<div class=\"w3-left\" style=\"width:45%\">"
            + "<table class=\"w3-table-all\">"
                + "<thead>"
                + "<tr class=\"w3-green\">"
                + "<th> Vaccination History</th>"
                + "</thead>"
                + "<tbody>");

        for (VaccinationEvent entryDisplay : entryList) {
          Vaccine vaccineAdmin = entryDisplay.getVaccine();
          String link = "paramEntryId=" + entryDisplay.getVaccinationEventId();
          out.println("<tr>"
              +"<td class = \"w3-hover-teal\">"
              + "<a href='entry_record?" + link+ "' style=\"text-decoration:none\">"
              + "<div style=\"text-decoration:none;height:100%\">"  
              + vaccineAdmin.getVaccineCvxCode() + "   "+vaccineAdmin.getVaccineMvxCode()+ "   "+vaccineAdmin.getVaccineNdcCode()
              + "</div>"
              + "</a>"              
              + "</td>"
              + "</tr>");
          }
        String link = "paramPatientId=" + patient.getPatientId();
        out.println("</tbody></table>"
                + "<button onclick=\"location.href='entry_record?" +link+"'\"  class=\"w3-button w3-margin w3-round-large w3-green w3-hover-teal w3-left\">Create vaccination entry </button>"
                + "</div>");

        out.println("<div class=\"w3-display-right w3-margin\"style=\"width:15%\">"
                + "<button onclick=\"location.href='patient_form?" +link+"'\" style=\"width:100%;height:20%\" class=\"w3-button w3-margin w3-round-large w3-green w3-hover-teal\">Modify patient</button>"
                + "<button onclick=\"location.href='FHIR_messaging?patientOnly=1'\" style=\"width:100%;height:20%\" class=\"w3-button w3-margin w3-round-large w3-green w3-hover-teal\">FHIR Messaging</button>"

                + "</div>");
        ServletHelper.doStandardFooter(out, session);
      
      
    } catch (Exception e) {
      e.printStackTrace(System.err);
    }
    out.flush();
    out.close();
  }

  public static String breadCrumbs(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    
    String html = "";
    
    HttpSession session = req.getSession(true);
    resp.setContentType("text/html");
    PrintWriter out = new PrintWriter(resp.getOutputStream());
    Session dataSession = PopServlet.getDataSession();
    Tester tester = (Tester) session.getAttribute("tester");
    Tenant tenant = new Tenant();
    List<Tenant> tenantList = null;
    String tenantId = req.getParameter("paramTenantId");
    if (tenantId != null) {
      Query query = dataSession.createQuery("from Tenant where tenantId=? and tester_id=?");
      query.setParameter(0, Integer.parseInt(tenantId));
      query.setParameter(1, tester.getTesterId());
      tenantList = query.list();
      tenant = tenantList.get(0);
      session.setAttribute("tenant", tenant);
    } else {
      if (session.getAttribute("tenant")!=null) {
        tenant = (Tenant) session.getAttribute("tenant");
      }
      else {
        resp.sendRedirect("tenants?chooseTenant=1");
      }
      
    }
    List<Facility> facilityList = null;
    Query query = dataSession.createQuery("from Facility where tenant=?");
    query.setParameter(0, tenant);
    facilityList = query.list();
  
  String showFacility = null;
  if (req.getParameter("paramFacilityId") != null) {
    showFacility = req.getParameter("paramFacilityId");
    
    html+="<div class=\"w3-margin-bottom\"style=\"width:100% height:auto \" >"
        + "<label class=\"w3-text-green w3-margin-right w3-margin-bottom\"><b>Current Tenant : "
        + tenant.getNameDisplay() + "</b></label>";
    Facility facility = new Facility();
    if (showFacility != null) {
      
      List<Facility> currentFacility = null;
      query = dataSession.createQuery("from Facility where facilityId=?");
      query.setParameter(0, Integer.parseInt(showFacility));
      currentFacility = query.list();
      facility = currentFacility.get(0);
      session.setAttribute("facility", facility);
      query = dataSession.createQuery("from Patient where facility=?");
      query.setParameter(0, facility);
      
    }
    if (facility != null) {
      html+=
          "<label class=\"w3-text-green w3-margin-left w3-margin-bottom\"><b>Current Facility : "
              + facility.getNameDisplay() + "</b></label>";
    }
  }
  return html;
  }
  
}
