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
import org.immregistries.ehr.model.Tester;


/**
 * Servlet implementation class Test_numeromille
 */
public class Test_numeromille extends HttpServlet {
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
    List<Tester> testerList = null;
    try {
      {
        Query query = dataSession.createQuery(
            "from Tester");
        testerList = query.list();
        int count = 0;
        for (Tester tester : testerList) {
          count++;
          if (count > 1) {
            break;
          }
          
          out.println(tester.getLoginUsername());
        }
        doHeader(out, session);
        String show = req.getParameter(PARAM_SHOW);
        out.println("oui c cool");  
        doFooter(out, session);
      }
    } catch (Exception e) {
      e.printStackTrace(System.err);
    }
    out.flush();
    out.close();
  }
  
  public static void doHeader(PrintWriter out, HttpSession session) {
    out.println("oui");
   
  
  }
  
  public static void doFooter(PrintWriter out, HttpSession session) {
    out.println("oui");
  }

}
