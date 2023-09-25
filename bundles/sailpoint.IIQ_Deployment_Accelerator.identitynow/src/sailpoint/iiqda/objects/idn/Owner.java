package sailpoint.iiqda.objects.idn;

import java.util.Map;

public class Owner {
  
  private String name;
  private String id;
  
  public Owner() {
  	
  }
  
  public Owner(Map map) {
		if (map!=null) {
			this.name=(String)map.get("name");
			this.id=(String)map.get("id");
		}
	}
	public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  
  
}
