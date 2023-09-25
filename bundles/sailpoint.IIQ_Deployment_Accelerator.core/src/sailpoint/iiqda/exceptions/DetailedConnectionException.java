package sailpoint.iiqda.exceptions;

import java.util.List;

public class DetailedConnectionException extends ConnectionException {

  private static final long serialVersionUID = -1140092762860018283L;
	private List<String> errors;
  
  public DetailedConnectionException(String message, List<String> errors) {
    super(message);
    this.errors=errors;
  }
  
  public List<String> getErrors() {
    return errors;
  }
  
}
