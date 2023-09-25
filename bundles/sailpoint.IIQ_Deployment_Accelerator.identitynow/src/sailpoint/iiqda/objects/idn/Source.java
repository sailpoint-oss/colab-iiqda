package sailpoint.iiqda.objects.idn;

import java.util.Map;

import sailpoint.iiqda.wizards.importresource.idn.IDNObject;

public class Source implements IDNObject {

  private Boolean useForProvisioning;
  private String icon;
  private String description;
  private String supportsEntitlementAggregation;
  private Boolean hasValidAccountProfile;
  private String lastUpdated; // format is 2017-03-07T21:05:40Z;
  private int entitlementsCount;
  private String connector_featuresString;
  private int accountsCount;
  private String id;
  private Owner owner;
  private Long appCount;
  private Boolean sourceConnected;
  private String definitionName;
  private String passwordPolicy;
  private CorrelationConfig correlationConfig;
  private String externalId;
  private Health health;
  private Boolean  useForAccounts;
  private Boolean  isAuthoritative;
  private String iqServiceDownloadUrl;
  private int accessProfilesCount;
  private String applicationTemplate;
  private Boolean useForPasswordManagement;
  private Long version;
  private String passwordPolicyName;
  private Long userCount;
  private String sourceType;
  private String name;
  private String scriptName;
  private Boolean useForAuthentication;
  
  public Source() {
  	
  }
  
  public boolean isUseForProvisioning() {
    return useForProvisioning;
  }
  public void setUseForProvisioning(boolean useForProvisioning) {
    this.useForProvisioning = useForProvisioning;
  }
  public String getIcon() {
    return icon;
  }
  public void setIcon(String icon) {
    this.icon = icon;
  }
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }
  public boolean isSupportsEntitlementAggregation() {
    if (supportsEntitlementAggregation==null) return false;
    return Boolean.valueOf(supportsEntitlementAggregation);
  }
  public void setSupportsEntitlementAggregation(
    String supportsEntitlementAggregation) {
    this.supportsEntitlementAggregation = supportsEntitlementAggregation;
  }
//  public void setSupportsEntitlementAggregation(
//      boolean supportsEntitlementAggregation) {
//    this.supportsEntitlementAggregation = supportsEntitlementAggregation;
//  }
  public boolean isHasValidAccountProfile() {
    return hasValidAccountProfile;
  }
  public void setHasValidAccountProfile(boolean hasValidAccountProfile) {
    this.hasValidAccountProfile = hasValidAccountProfile;
  }
  public String getLastUpdated() {
    return lastUpdated;
  }
  public void setLastUpdated(String lastUpdated) {
    this.lastUpdated = lastUpdated;
  }
  public int getEntitlementsCount() {
    return entitlementsCount;
  }
  public void setEntitlementsCount(int entitlementsCount) {
    this.entitlementsCount = entitlementsCount;
  }
  public String getConnector_featuresString() {
    return connector_featuresString;
  }
  public void setConnector_featuresString(String connector_featuresString) {
    this.connector_featuresString = connector_featuresString;
  }
  public int getAccountsCount() {
    return accountsCount;
  }
  public void setAccountsCount(int accountsCount) {
    this.accountsCount = accountsCount;
  }
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public Owner getOwner() {
    return owner;
  }
  public void setOwner(Owner owner) {
    this.owner = owner;
  }
  public Long getAppCount() {
    return appCount;
  }
  public void setAppCount(Long appCount) {
    this.appCount = appCount;
  }
  public boolean isSourceConnected() {
    return sourceConnected;
  }
  public void setSourceConnected(boolean sourceConnected) {
    this.sourceConnected = sourceConnected;
  }
  public String getDefinitionName() {
    return definitionName;
  }
  public void setDefinitionName(String definitionName) {
    this.definitionName = definitionName;
  }
  public String getPasswordPolicy() {
    return passwordPolicy;
  }
  public void setPasswordPolicy(String passwordPolicy) {
    this.passwordPolicy = passwordPolicy;
  }
  public CorrelationConfig getCorrelationConfig() {
    return correlationConfig;
  }
  public void setCorrelationConfig(CorrelationConfig correlationConfig) {
    this.correlationConfig = correlationConfig;
  }
  public String getExternalId() {
    return externalId;
  }
  public void setExternalId(String externalId) {
    this.externalId = externalId;
  }
  public Health getHealth() {
    return health;
  }
  public void setHealth(Health health) {
    this.health = health;
  }
  public boolean isUseForAccounts() {
    return useForAccounts;
  }
  public void setUseForAccounts(boolean useForAccounts) {
    this.useForAccounts = useForAccounts;
  }
  public boolean isAuthoritative() {
    return isAuthoritative;
  }
  public void setAuthoritative(boolean isAuthoritative) {
    this.isAuthoritative = isAuthoritative;
  }
  public String getIqServiceDownloadUrl() {
    return iqServiceDownloadUrl;
  }
  public void setIqServiceDownloadUrl(String iqServiceDownloadUrl) {
    this.iqServiceDownloadUrl = iqServiceDownloadUrl;
  }
  public int getAccessProfilesCount() {
    return accessProfilesCount;
  }
  public void setAccessProfilesCount(int accessProfilesCount) {
    this.accessProfilesCount = accessProfilesCount;
  }
  public String getApplicationTemplate() {
    return applicationTemplate;
  }
  public void setApplicationTemplate(String applicationTemplate) {
    this.applicationTemplate = applicationTemplate;
  }
  public boolean isUseForPasswordManagement() {
    return useForPasswordManagement;
  }
  public void setUseForPasswordManagement(boolean useForPasswordManagement) {
    this.useForPasswordManagement = useForPasswordManagement;
  }
  public Long getVersion() {
    return version;
  }
  public void setVersion(Long version) {
    this.version = version;
  }
  public String getPasswordPolicyName() {
    return passwordPolicyName;
  }
  public void setPasswordPolicyName(String passwordPolicyName) {
    this.passwordPolicyName = passwordPolicyName;
  }
  public Long getUserCount() {
    return userCount;
  }
  public void setUserCount(Long userCount) {
    this.userCount = userCount;
  }
  public String getSourceType() {
    return sourceType;
  }
  public void setSourceType(String sourceType) {
    this.sourceType = sourceType;
  }
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public String getScriptName() {
    return scriptName;
  }
  public void setScriptName(String scriptName) {
    this.scriptName = scriptName;
  }
  public boolean isUseForAuthentication() {
    return useForAuthentication;
  }
  public void setUseForAuthentication(boolean useForAuthentication) {
    this.useForAuthentication = useForAuthentication;
  }

  public Source(Map<String,Object> map) {
  	this.useForProvisioning=(Boolean)map.get("useForProvisioning");
    this.icon=(String)map.get("icon");
    this.description=(String)map.get("description");
    this.supportsEntitlementAggregation=(String)map.get("supportsEntitlementAggregation");
    this.hasValidAccountProfile=Boolean.valueOf((String)map.get("hasValidAccountProfile"));
    this.lastUpdated=(String)map.get("lastUpdated"); // format is 2017-03-07T21:05:40Z=(String)map.get("");
    if (map.get("entitlementsCount")!=null) {
    	this.entitlementsCount=(Integer)map.get("entitlementsCount");
    }
    this.connector_featuresString=(String)map.get("connector_featuresString");
    if (map.get("accountsCount")!=null) {
    	this.accountsCount=(Integer)map.get("accountsCount");
    }
    this.id=(String)map.get("id");
    this.owner=new Owner((Map)map.get("owner"));
    this.appCount=(Long)map.get("appCount");
    this.sourceConnected=(Boolean)map.get("sourceConnected");
    this.definitionName=(String)map.get("definitionName");
    this.passwordPolicy=(String)map.get("passwordPolicy");
    this.correlationConfig=new CorrelationConfig((Map)map.get("correlationConfig"));
    this.externalId=(String)map.get("externalId");
    this.health=new Health((Map)map.get("health"));
    this.useForAccounts=(Boolean)map.get("useForAccounts");
    if (map.get("isAuthoritative")!=null) {
    	this.isAuthoritative=(Boolean)map.get("isAuthoritative");
    }
    this.iqServiceDownloadUrl=(String)map.get("iqServiceDownloadUrl");
    if (map.get("accessProfilesCount")!=null) {
    	this.accessProfilesCount=(Integer)map.get("accessProfilesCount");
    }
    this.applicationTemplate=(String)map.get("applicationTemplate");
    this.useForPasswordManagement=(Boolean)map.get("useForPasswordManagement");
    this.version=(Long)map.get("version");
    this.passwordPolicyName=(String)map.get("passwordPolicyName");
    this.userCount=(Long)map.get("userCount");
    this.sourceType=(String)map.get("sourceType");
    this.name=(String)map.get("name");
    this.scriptName=(String)map.get("scriptName");
    this.useForAuthentication=(Boolean)map.get("useForAuthentication");
  }
  
}
