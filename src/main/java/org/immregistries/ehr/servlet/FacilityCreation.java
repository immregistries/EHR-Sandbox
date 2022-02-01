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
import org.immregistries.ehr.model.Silo;
import org.immregistries.ehr.model.Tester;


public class FacilityCreation extends HttpServlet {
  public static final String PARAM_SHOW = "show";

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    HttpSession session = req.getSession(true);
    Session dataSession = PopServlet.getDataSession();

    String name = req.getParameter("facility_name");

    Silo silo = new Silo();
    silo = (Silo) session.getAttribute("silo");
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
    facility.setLocation(req.getParameter("location"));
    facility.setSilo(silo);
    if(parentFacility!=null) {
      //System.out.println("oups");
      facility.setParentFacility(parentFacility);
    }

    Object oldSilo = null;
    Query query = dataSession.createQuery("from Facility where silo_id=? and name_display=?");
    query.setParameter(0, silo.getSiloId());
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
        doHeader(out, session);
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
