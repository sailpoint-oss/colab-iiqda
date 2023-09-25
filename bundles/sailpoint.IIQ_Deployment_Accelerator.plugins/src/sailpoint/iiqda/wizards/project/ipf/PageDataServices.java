package sailpoint.iiqda.wizards.project.ipf;

import java.util.ArrayList;
import java.util.List;

import sailpoint.iiqda.wizards.AbstractModelObject;
import sailpoint.iiqda.wizards.project.ServiceDescription;

public class PageDataServices extends AbstractModelObject {
  
  private List<ServiceDescription> services;
  private Boolean hasServices;
  

  public PageDataServices() {
    this.services=new ArrayList<ServiceDescription>();
    this.hasServices=false;
  }

  public Boolean getHasServices() {
    return hasServices;
  }

  public void setHasServices(Boolean hasServices) {
    this.hasServices = hasServices;
  }

  public void setServiceDescriptions(List<ServiceDescription> services) {
    this.services = services;
  }
  
  public void addService(ServiceDescription sd) {
    services.add(sd);
    firePropertyChange("serviceDescriptions", null, sd);
  }
  
  public void removeServiceByName(String sdName) {
    if(sdName==null) return;
    
    for (ServiceDescription sd: services) {
      if ( sdName.equals(sd.getServiceName()) ) {
        removeService(sd);
        return;
      }
    }
  }
  
  public void removeService(ServiceDescription sd) {
    services.remove(sd);
    firePropertyChange("serviceDescriptions", null, sd);    
  }


  public ServiceDescription getServiceByName(String sEndpoint) {
    for (ServiceDescription sd: services) {
      if (sd.getServiceName().equals(sEndpoint)) {
        return sd;
      }
    }
    return null;
  }
  
  public ServiceDescription getServiceByClass(String sClazzName) {
    for (ServiceDescription sd: services) {
      if (sd.getClassName().equals(sClazzName)) {
        return sd;
      }
    }
    return null;
  }


  public List<ServiceDescription> getServiceDescriptions() {
    return services;
  }
  
}
