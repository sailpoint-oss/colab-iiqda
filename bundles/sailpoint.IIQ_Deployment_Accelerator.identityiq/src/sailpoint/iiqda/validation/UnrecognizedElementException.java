package sailpoint.iiqda.validation;

public class UnrecognizedElementException extends Exception {

  private static final long serialVersionUID = -3967060457869794141L;
  
  private String elementName;

  public UnrecognizedElementException(String elementName) {
    this.elementName=elementName;
  }

  public String getElementName() {
    return elementName;
  }

}
