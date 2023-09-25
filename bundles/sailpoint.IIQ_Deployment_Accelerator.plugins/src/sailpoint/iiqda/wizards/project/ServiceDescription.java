package sailpoint.iiqda.wizards.project;

public class ServiceDescription {

  private String serviceName;
  private String clazzName;
  private int interval;
  private String description;
  
  public ServiceDescription(String serviceName, String clazzName, int interval,
      String description) {
    this.serviceName = serviceName;
    this.clazzName = clazzName;
    this.interval = interval;
    this.description = description;
  }

  public String getServiceName() {
    return serviceName;
  }

  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  public String getClassName() {
    return clazzName;
  }

  public void setClassName(String clazzName) {
    this.clazzName = clazzName;
  }

  public int getInterval() {
    return interval;
  }

  public void setInterval(int interval) {
    this.interval = interval;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  
  
  
}
