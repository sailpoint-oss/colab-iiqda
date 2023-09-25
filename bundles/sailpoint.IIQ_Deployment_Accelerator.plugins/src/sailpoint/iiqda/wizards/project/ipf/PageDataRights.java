package sailpoint.iiqda.wizards.project.ipf;

import java.util.ArrayList;
import java.util.List;

import sailpoint.iiqda.wizards.AbstractModelObject;
import sailpoint.iiqda.wizards.project.Capability;

public class PageDataRights extends AbstractModelObject {

	private List<String> spRights;
	
	private List<Capability> capabilities;
	
	public PageDataRights() {
		this.spRights=new ArrayList<String>();
		this.capabilities=new ArrayList<Capability>();
	}
	
	public void setSPRights(List<String> rights) {
		this.spRights=rights;
	}
	
	public void addSPRight(String right) {
		if(!spRights.contains(right)) spRights.add(right);
	}
	
	public void addCapability(Capability cap) {
		capabilities.add(cap);
		firePropertyChange("capabilities", null, capabilities);
	}
	
	public List<String> getSPRights() {
		return spRights;
	}
	
	public List<Capability> getCapabilities() {
		return capabilities;
	}

  public void removeCapabilityByName(String selection) {
    
    for (Capability cap: capabilities) {
      if (cap.getName().equals(selection)) {
        capabilities.remove(cap);
        firePropertyChange("capabilities", null, capabilities);
        return;
      }
    }
    
  }
}
