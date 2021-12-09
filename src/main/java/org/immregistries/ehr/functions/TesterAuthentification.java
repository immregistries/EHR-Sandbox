package org.immregistries.ehr.functions;

public class TesterAuthentification {
  
  public static Tester authenticateOrgAccess(Session dataSession) 
      throws AuthenticationException {
    OrgMaster orgMaster;
    OrgAccess orgAccess = null;
    String authHeader = theRequestDetails.getHeader("Authorization");
    System.err.println();

    // The format of the header must be:
    // Authorization: Basic [base64 of username:password]
    if (authHeader == null || authHeader.startsWith("Basic ") == false) {
      throw new AuthenticationException("Missing or invalid Authorization header");
    }

    String base64 = authHeader.substring("Basic ".length());
    String base64decoded = new String(Base64.decodeBase64(base64));
    String[] parts = base64decoded.split(":");

    String facilityId = theRequestDetails.getTenantId();
    //String facilityId = parts[0];
    String password = parts[1]; //TODO Maybe hash password

    {
      Query query = dataSession.createQuery("from OrgMaster where organizationName = ?");
      query.setParameter(0, facilityId);
      @SuppressWarnings("unchecked")
    List<OrgMaster> orgMasterList = query.list();
      if (orgMasterList.size() == 0) {
        System.out.println("Creation of new access");
        orgMaster = new OrgMaster();
        orgMaster.setOrganizationName(facilityId);
        orgAccess = new OrgAccess();
        orgAccess.setOrg(orgMaster);
        orgAccess.setAccessName(facilityId);
        orgAccess.setAccessKey(password);
        Transaction transaction = dataSession.beginTransaction();
        dataSession.save(orgMaster);
        dataSession.save(orgAccess);
        transaction.commit();
        return orgAccess;
      } else {
        orgMaster = orgMasterList.get(0);
        { // if
          query = dataSession
              .createQuery("from OrgAccess where accessName = ? and accessKey = ? and org = ?");
          query.setParameter(0, facilityId);
          query.setParameter(1, password);
          query.setParameter(2, orgMaster);
          @SuppressWarnings("unchecked")
        List<OrgAccess> orgAccessList = query.list();
          if (orgAccessList.size() != 0) {
            orgAccess = orgAccessList.get(0);
          } else {
            throw new AuthenticationException("password for ID : " + facilityId);
          }
        }
      }

    }
    return orgAccess;
  }
}
