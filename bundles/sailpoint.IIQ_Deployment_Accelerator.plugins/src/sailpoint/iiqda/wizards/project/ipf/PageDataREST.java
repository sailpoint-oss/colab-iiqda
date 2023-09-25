package sailpoint.iiqda.wizards.project.ipf;

import java.util.ArrayList;
import java.util.List;

import sailpoint.iiqda.wizards.AbstractModelObject;
import sailpoint.iiqda.wizards.project.RESTEndpoint;

public class PageDataREST extends AbstractModelObject {
  
  private boolean hasREST;
  private String baseEndpoint;
  private String baseEndpointRight;
  private String baseClazzName;
  
  private List<RESTEndpoint> endpoints;
  

  public PageDataREST() {
    this.baseEndpoint=null;
    this.baseEndpointRight=null;
    this.endpoints=new ArrayList<RESTEndpoint>();
  }


  public boolean getHasREST() {
    return hasREST;
  }


  public void setHasREST(boolean hasREST) {
    this.hasREST = hasREST;
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

  public String getBaseClazzName() {
    return baseClazzName;
  }


  public void setBaseClazzName(String baseClazzName) {
    this.baseClazzName = baseClazzName;
  }


  public List<RESTEndpoint> getEndpoints() {
    return endpoints;
  }


  public void setEndpoints(List<RESTEndpoint> endpoints) {
    this.endpoints = endpoints;
  }
  
  public void addEndpoint(RESTEndpoint ep) {
    endpoints.add(ep);
    firePropertyChange("endpoints", null, ep);
  }
  
  public void removeEndpoint(String epName) {
    if(epName==null) return;
    
    for (RESTEndpoint ep: endpoints) {
      if ( epName.equals(ep.getEndpointName()) ) {
        removeEndpoint(ep);
        return;
      }
    }
  }
  
  public void removeEndpoint(RESTEndpoint ep) {
    endpoints.remove(ep);
    firePropertyChange("endpoints", null, ep);    
  }


  public RESTEndpoint getEndpointByEndpoint(String sEndpoint) {
    for (RESTEndpoint ep: endpoints) {
      if (ep.getEndpointName().equals(sEndpoint)) {
        return ep;
      }
    }
    return null;
  }
  
  public RESTEndpoint getEndpointByMethod(String sMethod) {
    for (RESTEndpoint ep: endpoints) {
      if (ep.getJavaName().equals(sMethod)) {
        return ep;
      }
    }
    return null;
  }
}
