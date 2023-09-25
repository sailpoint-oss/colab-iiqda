package sailpoint.iiqda.wizards.project;

public class Setting {

  private String name;
  private String value;
  private String dataType;
  private String helpText;
  private String label;
  
  public String getLabel() {
    return label;
  }
  public void setLabel(String label) {
    this.label = label;
  }
  public Setting(String name, String label, String value, String datatype, String helptext) {
    this.name=name;
    this.label=label;
    this.value=value;
    this.dataType=datatype;
    this.helpText=helptext;
  }
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public String getValue() {
    return value;
  }
  public void setValue(String value) {
    this.value = value;
  }
  public String getDataType() {
    return dataType;
  }
  public void setDataType(String type) {
    this.dataType = type;
  }
  public String getHelpText() {
    return helpText;
  }
  public void setHelpText(String helpText) {
    this.helpText = helpText;
  }
  
  
  
}
