package sailpoint.iiqda.validation;

public class Variable {

  private String type;
  private String name;
  private Source src;
  
  public enum Source {
    WORKFLOW_VARIABLE,
    FORM_FIELD,
    CONTEXT;
  }

  public Variable(String type, String name, Source src) {
    this.type=type;
    this.name=name;
    this.src=src;
    
  }

  public String getType() {
    return type;
  }

  public String getName() {
    return name;
  }
  
  public Source getSource() {
    return this.src;
  }
  
}
