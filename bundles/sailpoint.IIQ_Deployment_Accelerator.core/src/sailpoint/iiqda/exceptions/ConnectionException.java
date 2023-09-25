package sailpoint.iiqda.exceptions;

public class ConnectionException extends Exception {

  /**
	 * 
	 */
  private static final long serialVersionUID = 6423970915524860815L;
  
	private String msg;
  private Exception originatingException;
  
  public ConnectionException(String msg) {
    this.msg=msg;
  }
  
  public ConnectionException(Exception cause) {
    this.originatingException=cause;
  }
  
  public ConnectionException(String msg, Exception cause) {
    this.msg=msg;
    this.originatingException=cause;    
  }

  public String getMessage() {
    return msg;
  }

  public Exception getCause() {
    return originatingException;
  }
  
  
}
