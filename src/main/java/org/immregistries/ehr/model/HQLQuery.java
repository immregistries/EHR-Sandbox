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
  
  
  public Silo getSiloFromSiloID(String siloID) {
    Silo silo = new Silo();
    List<Silo> siloList = null;
    Query query = dataSession.createQuery("from Silo where siloId=?");
    query.setParameter(0, Integer.parseInt(siloID));
    siloList = query.list();
    silo = siloList.get(0);    
    return silo;
  }
  
  public List<Facility> getFacilityListFromSiloID(String siloID){
    List<Facility> facilities=null;
    Silo silo = getSiloFromSiloID(siloID);    
    Query query = this.dataSession.createQuery("from Facility where silo=?");
    query.setParameter(0, silo);
    facilities = query.list();
    return facilities;
  }
  
  public List<Patient> getPatientListFromSilo(Silo silo){
    List<Patient> patientList = null;
    Query query = this.dataSession.createQuery("from Patient where silo=?");
    query.setParameter(0, silo);
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
