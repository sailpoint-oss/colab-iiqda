package sailpoint.iiqda;

public class SourceReferenceNotFoundException extends Exception {

  private static final long serialVersionUID = -3657183690265791587L;
	private String element;
	private String filename;

	public SourceReferenceNotFoundException(String element, String filename) {
		this.element=element;
		this.filename=filename;
	}

	public String getElement() {
		return element;
	}

	public String getFilename() {
		return filename;
	}
	
	
	
}
