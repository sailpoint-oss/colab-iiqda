package sailpoint.iiqda.exceptions;

public class InvalidPreferenceTypeException extends RuntimeException {

  /**
   * 
   */
  private static final long serialVersionUID = 4764429935262499628L;
  
  private String type;
  
  public InvalidPreferenceTypeException(String type) {
    this.type=type;
  }
  
  public String getType() {
    return type;
  }
  
}
