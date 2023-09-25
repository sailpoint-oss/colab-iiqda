package sailpoint.iiqda.wizards.project.ipf;

import java.util.List;

import sailpoint.iiqda.wizards.project.RESTEndpoint;

public class RESTResource {

  private String packageName;
  private String baseClazzName;
  private String baseEndpoint;
  private String baseEndpointRight;
  private List<RESTEndpoint> endpoints;
  private String sampleMessage;

  public RESTResource(String packageName, String baseClazzName,
      String baseEndpoint, String baseEndpointRight, List<RESTEndpoint> endpoints) {
    
    this.packageName=packageName;
    this.baseClazzName=baseClazzName;
    this.baseEndpoint=baseEndpoint;
    this.baseEndpointRight=baseEndpointRight;
    this.endpoints=endpoints;
    
  }

  public String getPackageName() {
    return packageName;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  public String getBaseClazzName() {
    return baseClazzName;
  }

  public void setBaseClazzName(String baseClazzName) {
    this.baseClazzName = baseClazzName;
  }

  public String getBaseEndpoint() {
    return baseEndpoint;
  }
  
  public void setBaseEndpoint(String baseEndpoint) {
    this.baseEndpoint = baseEndpoint;
  }
  
  public String getBaseEndpointRight() {
    return baseEndpointRight;
  }

  public void setBaseEndpointRight(String baseEndpointRight) {
    this.baseEndpointRight = baseEndpointRight;
  }

  public List<RESTEndpoint> getEndpoints() {
    return endpoints;
  }

  public void setEndpoints(List<RESTEndpoint> endpoints) {
    this.endpoints = endpoints;
  }
  
  public String getSampleMessage() {
    return sampleMessage;
  }

  public void setSampleMessage(String widgetDescription) {
    this.sampleMessage=widgetDescription;
  }

  
}
