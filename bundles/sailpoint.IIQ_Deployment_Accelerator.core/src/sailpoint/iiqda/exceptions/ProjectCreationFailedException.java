package sailpoint.iiqda.exceptions;

public class ProjectCreationFailedException extends Exception {

  private static final long serialVersionUID = 1899526955676081782L;
	private String message;
  private Exception cause;
  
  public ProjectCreationFailedException(String msg) {
    this.message=msg;
  }

  public ProjectCreationFailedException(String msg, Exception cause) {
    this.message=msg;
    this.cause=cause;
  }
  
  @Override
  public String getMessage() {
    return message;
  }

  @Override
  public synchronized Throwable getCause() {
    return cause;
  }
  
  
  
}
