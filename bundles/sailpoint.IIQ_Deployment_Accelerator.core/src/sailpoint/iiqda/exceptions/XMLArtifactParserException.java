package sailpoint.iiqda.exceptions;


public class XMLArtifactParserException extends Exception {
	
  private static final long serialVersionUID = 2580063061096264846L;
	private Exception srcException;
	private String msg;

	public XMLArtifactParserException(Exception src) {
	  this.srcException=src;
  }
	
	public XMLArtifactParserException() {
    srcException=null;
  }

  public XMLArtifactParserException(String string) {
    this.msg=string;
  }

  public Exception getRootCause() {
	  return srcException;
	}

  @Override
  public String getMessage() {
    return msg;
  }
  
}
