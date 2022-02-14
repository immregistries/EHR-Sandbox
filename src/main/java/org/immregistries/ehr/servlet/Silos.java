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
import org.immregistries.ehr.model.Silo;
import org.immregistries.ehr.model.Tester;

public class Silos extends HttpServlet {

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
      {
        ServletHelper.doStandardHeader(out, session);
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
            +"  <table class=\" w3-table-all \"style=\"width:45% ;overflow:auto\"> \r\n"
            + "<thead>"
            + "<tr class=\"w3-green\">"
            + "<th> Tenant</th>"
            + "</thead>"
            + "<tbody>"
            );

        for (Silo siloDisplay : siloList) {
          String link = "paramSiloId=" + siloDisplay.getSiloId();
          out.println("<tr>"
              + "<td class = \"w3-hover-teal\">"     
              + "<a href='facility_patient_display?" + link+ "'style = \"text-decoration:none \">\r\n"
              + "<div style=\"text-decoration:none;height:100%\">"  
              + siloDisplay.getNameDisplay()  
              + "</div>"
             + "</a>"              
              + "</td>"
             +"</tr>");
              
        }
        out.println(
             
              "</tbody>"
            + "</table>"
            + "</div>"
            + "  <div class=\"w3-display-right\" style=\"width=15%\">\r\n "
            + "<button onclick=\"location.href='silo_creation'\"  class=\"w3-button w3-round-large w3-green w3-hover-teal\">Create new tenant</button>"
            //+ "		</div>\r\n" 	
            + "</div>\r\n");
        ServletHelper.doStandardFooter(out, session);
      }
    } catch (Exception e) {
      e.printStackTrace(System.err);
    }
    out.flush();
    out.close();
  }

}

