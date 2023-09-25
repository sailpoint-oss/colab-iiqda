package sailpoint.iiqda.objects.idn;

import java.util.Map;

public class CorrelationConfig {
  
  public String attributeAssignments;
  public String name;
  public String id;
  
  public CorrelationConfig() {
    System.out.println("CorrelationConfig.CorrelationConfig: ");

  }
  
  public CorrelationConfig(Map map) {
		if (map!=null) {
			this.attributeAssignments=(String)map.get("attributeAssignments");
			this.name=(String)map.get("name");
			this.id=(String)map.get("id");
		}
	}

	public String getAttributeAssignments() {
    System.out.println("CorrelationConfig.getAttributeAssignments: ");

    return attributeAssignments;
  }
  public void setAttributeAssignments(String attributeAssignments) {
    System.out.println("CorrelationConfig.setAttributeAssignments: ");

    this.attributeAssignments = attributeAssignments;
  }
  public String getName() {
    System.out.println("CorrelationConfig.getName: ");

    return name;
  }
  public void setName(String name) {
    System.out.println("CorrelationConfig.setName: ");

    this.name = name;
  }
  public String getId() {
    System.out.println("CorrelationConfig.getId: ");

    return id;
  }
  public void setId(String id) {
    System.out.println("CorrelationConfig.setId: ");

    this.id = id;
  }

  
}
