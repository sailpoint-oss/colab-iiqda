package sailpoint.iiqda.wizards.project;

import sailpoint.iiqda.wizards.AbstractModelObject;

public class RESTEndpoint extends AbstractModelObject {
  
  private String method;
  private String endpointName;
  private String javaName;
  private String returnType;
  private String spRight;
  private boolean hasSpecificRight;
  
  public RESTEndpoint(String method, String restName, String javaName,
      String returnType, boolean hasRight, String spRight) {
    this.method=method;
    this.endpointName=restName;
    this.javaName=javaName;
    this.returnType=returnType;
    this.hasSpecificRight=hasRight;
    this.spRight=spRight;
  }
    
  public String getMethod() {
    return method;
  }
  public void setMethod(String method) {
    this.method = method;
  }
  public String getEndpointName() {
    return endpointName;
  }
  public void setEndpointName(String endpointName) {
    this.endpointName = endpointName;
  }
  public String getJavaName() {
    return javaName;
  }
  public void setJavaName(String javaName) {
    this.javaName = javaName;
  }
  public String getReturnType() {
    return returnType;
  }
  public void setReturnType(String returnType) {
    this.returnType = returnType;
  }
  public String getSpRight() {
    return spRight;
  }
  public void setSpRight(String spRight) {
    this.spRight = spRight;
  }
  public boolean hasSpecificRight() {
    return hasSpecificRight;
  }
  public void setHasSpecificRight(boolean right) {
    this.hasSpecificRight=right;
  }
  

}
