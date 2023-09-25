package sailpoint.iiqda.idn;

public class StepUpMethod {

  public enum Type {
    CODE,
    PASSWORD;    
  }
  
  private String label;
  private String description;
  private Type type;
  private String strongAuthType;
  
  public StepUpMethod(String label, String description, String typeString,
      String strongAuthType) throws UnsupportedStepUpMethodException {
    try {
      this.type=Type.valueOf(typeString);
    } catch (IllegalArgumentException e) {
      // Not a supported type of step up. Some (e.g. KBA aren't implemented yet;
      // also this future-proofs us if new methods are added and we haven't implemented yet  
      throw new UnsupportedStepUpMethodException();
    }
    this.label=label;
    this.description=description;
    this.strongAuthType=strongAuthType;
  }

  public String getLabel() {
    return label;
  }

  public String getDescription() {
    return description;
  }

  public Type getType() {
    return type;
  }

  public String getStrongAuthType() {
    return strongAuthType;
  }

}
