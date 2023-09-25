package sailpoint.iiqda.wizards.project;

public class WidgetDescription {

  private String widgetName;
  private String widgetDescription;
  
  public WidgetDescription(String widgetName, String widgetDescription) {
    this.widgetName = widgetName;
    this.widgetDescription = widgetDescription;
  }

  public String getWidgetName() {
    return widgetName;
  }

  public void setWidgetName(String widgetName) {
    this.widgetName = widgetName;
  }

  public String getWidgetDescription() {
    return widgetDescription;
  }

  public void setWidgetDescription(String widgetDescription) {
    this.widgetDescription = widgetDescription;
  }

}
