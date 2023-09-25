package sailpoint.iiqda.wizards.project.ipf;

import java.util.ArrayList;
import java.util.List;

import sailpoint.iiqda.wizards.AbstractModelObject;
import sailpoint.iiqda.wizards.project.Setting;

public class PageDataFullPage extends AbstractModelObject {

  private boolean enableSettings;
  private boolean enableFull;
  private boolean useAngular;
  
  private String fullPageName;
  private List<Setting> settings;
  
  public PageDataFullPage() {
    settings=new ArrayList<Setting>();
  }
  
	public String getFullPageName() {
		return fullPageName;
	}
	public void setFullPageName(String fullPageName) {
		this.fullPageName = fullPageName;
	}
	public boolean isEnableSettings() {
		return enableSettings;
	}
	public void setEnableSettings(boolean enableSettings) {
		this.enableSettings = enableSettings;
	}
	public boolean isEnableFull() {
		return enableFull;
	}
	public void setEnableFull(boolean enableFull) {
		this.enableFull = enableFull;
	}
	public List<Setting> getSettings() {
	  return settings;
	}
  public boolean isUseAngular() {
    return useAngular;
  }
  public void setUseAngular(boolean useAngular) {
    this.useAngular = useAngular;
  }
	
}
