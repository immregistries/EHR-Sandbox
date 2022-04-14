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
import org.immregistries.ehr.model.Facility;
import org.immregistries.ehr.model.Tenant;
import org.immregistries.ehr.model.User;

public class Tenants extends HttpServlet {

  private static final long serialVersionUID = 1L;
  public static final String PARAM_SHOW = "show";

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    // Fetches selected tenant  and puts it in session object
    HttpSession session = req.getSession();



    doGet(req, resp);
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    HttpSession session = req.getSession(true);
    resp.setContentType("text/html");
    session.setAttribute("patient", null);
    session.setAttribute("facility", null);

    PrintWriter out = new PrintWriter(resp.getOutputStream());
    Session dataSession = PopServlet.getDataSession();

    String tenantId = req.getParameter("tenantId");
    if (tenantId != null) {
      Tenant tenant;
      Facility facility = (Facility) session.getAttribute("facility");
      User user = (User) session.getAttribute("user");
      Query query = dataSession.createQuery("from Tenant where tenantId=? and user_id=?");
      query.setParameter(0, Integer.parseInt(tenantId));
      query.setParameter(1, user.getUserId());
      tenant = (Tenant) query.uniqueResult();
      session.setAttribute("tenant", tenant);
      // reset facility selection
      if (facility != null){
        if (facility.getTenant().getTenantId() != Integer.parseInt(tenantId)){
          session.setAttribute("facility", null);
          session.setAttribute("patient", null);
        }
      }
      resp.sendRedirect("facility_patient_display");
    }

    try {
      {
        ServletHelper.doStandardHeader(out, req, "Tenant selection");
        session.setAttribute("facility", null);
        User user = (User) session.getAttribute("user");
        List<Tenant> tenantList;
        Query query = dataSession.createQuery("from Tenant where user=?");
        query.setParameter(0, user);
        tenantList = query.list();
        String show = req.getParameter(PARAM_SHOW);
        if(req.getParameter("chooseTenant")!=null) {
          out.println("<div class = \" w3-margin-bottom\">"
              + "<label class=\"w3-text-red  w3-margin-bottom\"><b>Choose a tenant</b></label><br/>"
              +"</div>");
              
          }else {
            out.println("<div class = \" w3-margin-bottom\">"
                + "<label hidden class=\"w3-text-red  w3-margin-bottom\"><b>Choose a tenant</b></label><br/>"
                +"</div>");
          }
        out.println( "<div class = \"w3-left\" style=\"width:100%\">" 
            +"  <table class=\" w3-table-all \"style=\"width:45% ;overflow:auto\">"
            + "<thead>"
            + "<tr class=\"w3-green\">"
            + "<th> Tenant</th>"
            + "</thead>"
            + "<tbody>"
            );

        for (Tenant tenantDisplay : tenantList) {
          out.println("<tr>"
                  + "<td class = \"w3-hover-teal\">"
                  + "<a href=\"tenants?tenantId=" + tenantDisplay.getTenantId() + "\" style =\"text-decoration:none \">"
                  + "<div style=\"text-decoration:none;height:100%\">"
                  + tenantDisplay.getNameDisplay()
              + "</div>"
              + "</a>"
              + "</td>"
              +"</tr>");
        }
        out.println("</tbody>"
                + "</table>"
                + "<button onclick=\"location.href='tenant_creation'\"  class=\"w3-button w3-margin w3-round-large w3-green w3-hover-teal w3-left\">Create new tenant</button>"
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

