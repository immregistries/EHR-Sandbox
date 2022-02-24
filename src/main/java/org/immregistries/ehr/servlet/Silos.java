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
import org.immregistries.ehr.model.Silo;
import org.immregistries.ehr.model.Tester;

public class Silos extends HttpServlet {

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

    String siloId = req.getParameter("siloId");
    if (siloId != null) {
      Silo silo;
      Facility facility = (Facility) session.getAttribute("facility");
      Tester tester = (Tester) session.getAttribute("tester");
      Query query = dataSession.createQuery("from Silo where siloId=? and tester_id=?");
      query.setParameter(0, Integer.parseInt(siloId));
      query.setParameter(1, tester.getTesterId());
      silo = (Silo) query.uniqueResult();
      session.setAttribute("silo", silo);
      // reset facility selection
      if (facility != null){
        if (facility.getSilo().getSiloId() != Integer.parseInt(siloId)){
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
        Tester tester = (Tester) session.getAttribute("tester");
        List<Silo> siloList;
        Query query = dataSession.createQuery("from Silo where tester=?");
        query.setParameter(0, tester);
        siloList = query.list();
        String show = req.getParameter(PARAM_SHOW);
        if(req.getParameter("chooseSilo")!=null) {
          out.println("<div class = \" w3-margin-bottom\">"
              + "<label class=\"w3-text-red  w3-margin-bottom\"><b>Choose a tenant</b></label><br/>"
              +"</div>");
              
          }else {
            out.println("<div class = \" w3-margin-bottom\">"
                + "<label hidden class=\"w3-text-red  w3-margin-bottom\"><b>Choose a silo</b></label><br/>"
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

        for (Silo siloDisplay : siloList) {
          out.println("<tr>"
                  + "<td class = \"w3-hover-teal\">"
                  + "<a href=\"silos?siloId=" + siloDisplay.getSiloId() + "\" style =\"text-decoration:none \">"
                  + "<div style=\"text-decoration:none;height:100%\">"
                  + siloDisplay.getNameDisplay()
              + "</div>"
              + "</a>"
              + "</td>"
              +"</tr>");
        }
        out.println("</tbody>"
                + "</table>"
                + "<button onclick=\"location.href='silo_creation'\"  class=\"w3-button w3-margin w3-round-large w3-green w3-hover-teal w3-left\">Create new tenant</button>"
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

