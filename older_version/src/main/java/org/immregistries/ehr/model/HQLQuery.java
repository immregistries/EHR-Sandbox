package org.immregistries.ehr.model;

import org.hibernate.Query;
import org.hibernate.Session;
import org.immregistries.ehr.servlet.PopServlet;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HQLQuery {
  Session dataSession;
  
  
  public HQLQuery(HttpServletRequest req, HttpServletResponse resp) {
     this.dataSession= PopServlet.getDataSession();    
  }
  
  
  public Tenant getTenantFromTenantID(String tenantID) {
    Tenant tenant = new Tenant();
    List<Tenant> tenantList = null;
    Query query = dataSession.createQuery("from Tenant where tenantId=?");
    query.setParameter(0, Integer.parseInt(tenantID));
    tenantList = query.list();
    tenant = tenantList.get(0);
    return tenant;
  }
  
  public List<Facility> getFacilityListFromTenantID(String tenantID){
    List<Facility> facilities=null;
    Tenant tenant = getTenantFromTenantID(tenantID);
    Query query = this.dataSession.createQuery("from Facility where tenant=?");
    query.setParameter(0, tenant);
    facilities = query.list();
    return facilities;
  }
  
  public List<Patient> getPatientListFromTenant(Tenant tenant){
    List<Patient> patientList = null;
    Query query = this.dataSession.createQuery("from Patient where tenant=?");
    query.setParameter(0, tenant);
    patientList = query.list();
    return patientList;
  }
  
  public Facility getParentFacilityFromNameDislay(String nameDisplay) {
    Facility parentFacility = new Facility();
    
    if(nameDisplay!=null) {
      Query query = this.dataSession.createQuery("from Facility where nameDisplay=?");
      query.setParameter(0, nameDisplay);
      List<Facility> facilityParentList = query.list();
      if(facilityParentList.size()>0) {
        System.out.println("oups1");
        parentFacility = facilityParentList.get(0);
      }
      
    }
    return parentFacility;
  }
  
  
  public Facility getFacilityListFromFacilityID(String facilityID){
    List<Facility> currentFacility = null;
    Facility facility = new Facility();
    Query query = this.dataSession.createQuery("from Facility where facilityId=?");
    query.setParameter(0, Integer.parseInt(facilityID));
    currentFacility = query.list();
    facility = currentFacility.get(0);
    return facility;
  }
  
  public List<Patient> getPatientListFromFacility(Facility facility){
    List<Patient> patientList = null;
    Query  query = this.dataSession.createQuery("from Patient where facility=?");
    query.setParameter(0, facility);
    patientList = query.list();
    return patientList;
  }
  
  
  
  
  
  
  
}
