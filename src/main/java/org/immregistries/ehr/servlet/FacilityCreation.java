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
import org.hibernate.Transaction;
import org.immregistries.ehr.model.Facility;
import org.immregistries.ehr.model.Tenant;


public class FacilityCreation extends HttpServlet {
  public static final String PARAM_SHOW = "show";

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    HttpSession session = req.getSession(true);
    Session dataSession = PopServlet.getDataSession();

    String name = req.getParameter("facility_name");

    Tenant tenant = (Tenant) session.getAttribute("silo");
    String nameParent = req.getParameter("parentFacility");
    Facility parentFacility = null;
    if(nameParent!=null) {
      Query query = dataSession.createQuery("from Facility where nameDisplay=?");
      query.setParameter(0, nameParent);
      List<Facility> facilityParentList = query.list();
      if(facilityParentList.size()>0) {
      //System.out.println("oups1");
      parentFacility = facilityParentList.get(0);
      }
      
    }
    Facility facility = new Facility();
    facility.setNameDisplay(name);
    facility.setTenant(tenant);
    if(parentFacility!=null) {
      //System.out.println("oups");
      facility.setParentFacility(parentFacility);
    }

    Object oldSilo;
    Query query = dataSession.createQuery("from Facility where silo_id=? and name_display=?");
    query.setParameter(0, tenant.getTenantId());
    query.setParameter(1, name);
    oldSilo = query.uniqueResult() ;
    if (oldSilo != null){
      req.setAttribute("duplicate_error", 1);
    } else {
      Transaction transaction = dataSession.beginTransaction();
      dataSession.save(facility);
      transaction.commit();
      session.setAttribute("facility", facility);
      resp.sendRedirect("facility_patient_display");
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
        ServletHelper.doStandardHeader(out, req, "Facility creation");
        String show = req.getParameter(PARAM_SHOW);
        if(req.getAttribute("duplicate_error") != null){
          out.println("<label class=\"w3-text-red w3-margin w3-margin-bottom\"><b class=\"w3-margin\">Facility name already used for this silo</b></label><br/>");
        }
        out.println("<form method=\"post\" class=\"w3-container\" action=\"facility_creation\">\r\n"
            + "<label class=\"w3-text-green\"><b>Facility name</b></label>"
            + "                         <input type=\"text\" class = \"w3-input w3-margin w3-border \" required value=\"\"  name=\"facility_name\"/>\r\n"
           
            + "                <button \" class=\"w3-button w3-round-large w3-green w3-hover-teal w3-margin \"  >Validate</button>\r\n"
            + "                </form> " + "            </div>"
                + "</div>");

        ServletHelper.doStandardFooter(out, session);
      }
    } catch (Exception e) {
      e.printStackTrace(System.err);
    }
    out.flush();
    out.close();
  }

}
