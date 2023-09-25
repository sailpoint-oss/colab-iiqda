package sailpoint.iiqda.wizards.project.idn;

import sailpoint.iiqda.wizards.AbstractModelObject;

public class IDNPageData extends AbstractModelObject {

  private String sOrganisation;
  private String sAdminUser;
  private String sAdminPass;
  private String sAPIKey;
  private String sAPISecret;
  
  public String getOrganisation() {
    return sOrganisation;
  }
  public void setOrganisation(String sOrganisation) {
    this.sOrganisation = sOrganisation;
    firePropertyChange("organisation", null, sOrganisation);
  }
  public String getAdminUser() {
    return sAdminUser;
  }
  public void setAdminUser(String sAdminUser) {
    this.sAdminUser = sAdminUser;
  }
  public String getAdminPass() {
    return sAdminPass;
  }
  public void setAdminPass(String sAdminPass) {
    this.sAdminPass = sAdminPass;
  }
  public String getAPIKey() {
    return sAPIKey;
  }
  public void setAPIKey(String sAPIKey) {
    this.sAPIKey = sAPIKey;
    firePropertyChange("APIKey", null, sAPIKey);
  }
  public String getAPISecret() {
    return sAPISecret;
  }
  public void setAPISecret(String sAPISecret) {
    this.sAPISecret = sAPISecret;
    firePropertyChange("APISecret", null, sAPISecret);
  }
  
  
  
}
