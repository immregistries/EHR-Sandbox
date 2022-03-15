package org.immregistries.ehr.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.immregistries.ehr.model.Tenant;
import org.immregistries.ehr.model.User;

/**
 * Servlet implementation class Tenant_creation
 */
public class TenantCreation extends HttpServlet {

  public static final String PARAM_SHOW = "show";

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {    
    HttpSession session = req.getSession(true);
    Session dataSession = PopServlet.getDataSession();

    String name = req.getParameter("tenant_name");

    User user = (User) session.getAttribute("user");

    Tenant newTenant = new Tenant();
    newTenant.setNameDisplay(name);
    newTenant.setUser(user);

    Query query = dataSession.createQuery("from Tenant where user=? and name_display=?");
    query.setParameter(0, user);
    query.setParameter(1, name);
    Tenant oldTenant = (Tenant) query.uniqueResult();
    if (oldTenant != null){
      req.setAttribute("duplicate_error", 1);
    } else {
      Transaction transaction = dataSession.beginTransaction();
      dataSession.save(newTenant);
      transaction.commit();
      resp.sendRedirect("tenants");
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
        ServletHelper.doStandardHeader(out, req, "Tenant creation");
                
        if(req.getAttribute("duplicate_error") != null){
          out.println("<label class=\"w3-text-red w3-margin w3-margin-bottom\"><b class=\"w3-margin\">Name already used by the current user</b></label><br/>");
        }

        String show = req.getParameter(PARAM_SHOW);
        out.println("<form method=\"post\" class=\"w3-container\" action=\"tenant_creation\">\r\n"
            + "<label class=\"w3-text-green\"><b>Tenant name</b></label>"
            + "  						<input type=\"text\" class = \"w3-input w3-margin w3-border \" required value=\"\" size=\"40\" maxlength=\"60\" name=\"tenant_name\"/>\r\n"
            + "                <button onclick=\"location.href='tenants'\" class=\"w3-button w3-round-large w3-green w3-hover-teal w3-margin \"  >Validate</button>\r\n"
            + "                </form> " + "            </div>");

        ServletHelper.doStandardFooter(out, session);
      }
    } catch (Exception e) {
      e.printStackTrace(System.err);
    }
    out.flush();
    out.close();
  }

}
