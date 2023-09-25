package sailpoint.iiqda.objects.idn;

import java.util.Map;

public class Health {
  public String hostname;
  public String lastSeen;
  public String org;
  public boolean healthy;
  public String lastChanged;
  public String isAuthoritative; // Why???? Should be boolean; the value is boolean, just in quotes. Weird
  public String externalId;
  public String id;
  public String type;
  public String status;
  public long since;
  
  public Health() {
  	
  }
  
  public Health(Map map) {
		
  	if (map!=null) {
	  	this.hostname=(String)map.get("hostname");
	    this.lastSeen=(String)map.get("lastSeen");
	    this.org=(String)map.get("org");
	    this.healthy=(Boolean)map.get("healthy");
	    this.lastChanged=(String)map.get("lastChanged");
	    this.isAuthoritative=(String)map.get("isAuthoritative"); // Why???? Should be boolean; the value is boolean, just in quotes. Weird
	    this.externalId=(String)map.get("externalId");
	    this.id=(String)map.get("id");
	    this.type=(String)map.get("type");
	    this.status=(String)map.get("status");
	    Object since=map.get("since");
	    if (since!=null) {
	    	if (since instanceof Long) {
	    		this.since=(Long)since;
	    	} else if (since instanceof Integer) {
	    		this.since=(Integer)since;
	    	}
	    }
  	}  	
	}
	public String getHostname() {
    return hostname;
  }
  public void setHostname(String hostname) {
    this.hostname = hostname;
  }
  public String getLastSeen() {
    return lastSeen;
  }
  public void setLastSeen(String lastSeen) {
    this.lastSeen = lastSeen;
  }
  public String getOrg() {
    return org;
  }
  public void setOrg(String org) {
    this.org = org;
  }
  public boolean isHealthy() {
    return healthy;
  }
  public void setHealthy(boolean healthy) {
    this.healthy = healthy;
  }
  public String getLastChanged() {
    return lastChanged;
  }
  public void setLastChanged(String lastChanged) {
    this.lastChanged = lastChanged;
  }
  public String isAuthoritative() {
    return isAuthoritative;
  }
  public void setAuthoritative(String isAuthoritative) {
    this.isAuthoritative = isAuthoritative;
  }
  public String getExternalId() {
    return externalId;
  }
  public void setExternalId(String externalId) {
    this.externalId = externalId;
  }
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }
  public String getStatus() {
    return status;
  }
  public void setStatus(String status) {
    this.status = status;
  }
  public long getSince() {
    return since;
  }
  public void setSince(long since) {
    this.since = since;
  }
  
  
}
