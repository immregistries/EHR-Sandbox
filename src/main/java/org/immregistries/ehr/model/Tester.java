import java.io.Serializable;
import java.util.Date;

public class Test implements Serializable {

  private static final long serialVersionUID = 1L;

  private int testerId = 0;
  private String loginUsername = "";
  private String loginPassword = "";

  public int getTesterId() {
    return observationReportedId;
  }

  public void setTesterId(int testerId) {
    this.testerId = testerId;
  }

  public String getLoginUsername() {
    return loginUsername;
  }

  public void setLoginUsername(String loginUsername) {
    this.loginUsername = loginUsername;
  }

  public String getLoginPassword() {
    return loginPassword;
  }

  public void setLoginPassword(String loginPassword) {
    this.loginPassword = loginPassword;
  }
}