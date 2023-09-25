package sailpoint.iiqda.wizards.project.ipf;

import java.util.ArrayList;
import java.util.List;

import sailpoint.iiqda.wizards.AbstractModelObject;
import sailpoint.iiqda.wizards.project.WidgetDescription;

public class PageDataWidgets extends AbstractModelObject {
  
  private List<WidgetDescription> widgets;
  private Boolean hasWidgets ;
  

  public PageDataWidgets() {
    this.widgets = new ArrayList<WidgetDescription>();
    this.hasWidgets = false;
  }

  public Boolean getHasWidgets() {
    return hasWidgets;
  }

  public void setHasWidgets(Boolean hasServices) {
    this.hasWidgets  = hasServices;
  }

  public void setServiceDescriptions(List<WidgetDescription> services) {
    this.widgets = services;
  }
  
  public void addService(WidgetDescription sd) {
    widgets.add(sd);
    firePropertyChange("widgetDescriptions", null, sd);
  }
  
  public void removeWidgetByName(String sdName) {
    if(sdName==null) return;
    
    for (WidgetDescription sd: widgets) {
      if ( sdName.equals(sd.getWidgetName()) ) {
        removeWidget(sd);
        return;
      }
    }
  }
  
  public void removeWidget(WidgetDescription sd) {
    widgets.remove(sd);
    firePropertyChange("widgetDescriptions", null, sd);    
  }


  public WidgetDescription getWidgetByName(String sEndpoint) {
    for (WidgetDescription sd: widgets) {
      if (sd.getWidgetName().equals(sEndpoint)) {
        return sd;
      }
    }
    return null;
  }
  
  public List<WidgetDescription> getWidgetDescriptions() {
    return widgets;
  }
  
}
