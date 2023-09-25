package sailpoint.iiqda.wizards.project.ipf;

import sailpoint.iiqda.wizards.AbstractModelObject;

public class PageOneData extends AbstractModelObject {

  private String descriptiveName;
  private String uniqueName;
  private String version;
  private String minUpgradeable;
  private String minSystemVersion;
  private String pluginRight;

	public String getDescriptiveName() {
		return descriptiveName;
	}
	public void setDescriptiveName(String descriptiveName) {
		this.descriptiveName = descriptiveName;
	}
	public String getUniqueName() {
		return uniqueName;
	}
	public void setUniqueName(String uniqueName) {
		this.uniqueName = uniqueName;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getMinSystemVersion() {
		return minSystemVersion;
	}
	public void setMinSystemVersion(String minSystemVersion) {
		this.minSystemVersion = minSystemVersion;
	}
	public String getPluginRight() {
		return pluginRight;
	}
	public void setPluginRight(String pluginRight) {
		this.pluginRight = pluginRight;
	}
  public String getMinUpgradeable() {
    return minUpgradeable;
  }
  public void setMinUpgradeable(String minUpgradeable) {
    this.minUpgradeable = minUpgradeable;
  }
	
}
