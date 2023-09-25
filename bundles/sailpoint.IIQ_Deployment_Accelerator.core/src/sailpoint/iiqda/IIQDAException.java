package sailpoint.iiqda;

public class IIQDAException extends Exception {

	/**
	 * 
	 */
  private static final long serialVersionUID = 3113484446854129298L;
	private Throwable cause=null;
	private String message=null;
	
	public IIQDAException(String msg) {
		this.message=msg;
	}
	
	public IIQDAException(Throwable t) {
		this.cause=t;
	}
	
	@Override
	public Throwable getCause() {
		return cause;
	}

	@Override
	public String getMessage() {
		return message;
	}

	
	
}
