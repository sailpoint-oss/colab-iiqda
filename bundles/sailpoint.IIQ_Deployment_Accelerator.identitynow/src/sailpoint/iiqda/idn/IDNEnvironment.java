package sailpoint.iiqda.idn;

public class IDNEnvironment {

  private String name;
  private String organisation;
  private String apiKey;
  private String apiSecret;
  
  public IDNEnvironment(String name, String org, String key, String secret) {
    this.name=name;
    this.organisation=org;
    this.apiKey=key;
    this.apiSecret=secret;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getOrganisation() {
    return organisation;
  }

  public void setOrganisation(String organisation) {
    this.organisation = organisation;
  }

  public String getApiKey() {
    return apiKey;
  }

  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }

  public String getApiSecret() {
    return apiSecret;
  }

  public void setApiSecret(String apiSecret) {
    this.apiSecret = apiSecret;
  }
  
  public String toString() {
    StringBuilder bldr=new StringBuilder();
    bldr.append(name);
    bldr.append("|");
    bldr.append(organisation);
    bldr.append("|");
    bldr.append(apiKey);
    bldr.append("|");
    bldr.append(apiSecret);
    return bldr.toString();
  }
  
}
