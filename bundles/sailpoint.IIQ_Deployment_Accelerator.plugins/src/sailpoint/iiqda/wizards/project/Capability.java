package sailpoint.iiqda.wizards.project;

import java.util.List;

public class Capability {

	private String name;
	private List<String> rights;
	
	public Capability(String text, List<String> rights) {
	  this.name=text;
	  this.rights=rights;
  }
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<String> getRights() {
		return rights;
	}
	public void setRights(List<String> rights) {
		this.rights = rights;
	}
  @Override
  public String toString() {
    return name;
  }
	
	
	
}
